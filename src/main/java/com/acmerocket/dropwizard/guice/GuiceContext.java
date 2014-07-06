package com.acmerocket.dropwizard.guice;

import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.valuehandling.OptionalValidatedValueUnwrapper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import com.acmerocket.guice.modules.Modules;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;

@SuppressWarnings("rawtypes")
public class GuiceContext {
	static {
		// Too much?
		LoggingFactory.bootstrap(Level.INFO);
	}
	private static final Logger LOG = LoggerFactory.getLogger(GuiceContext.class);

	// Cache for stored GuiceContext
	private static final Map<String,GuiceContext> CACHE = Maps.newHashMap();

	private final Injector injector;

	public static Builder builder() {
		return new Builder();
	}

	static class Builder {
		private final Set<String> moduleNames = Sets.newHashSet();
		private final Set<Module> modules = Sets.newHashSet();
		private final Set<String> paths = Sets.newHashSet();
		private Stage stage = Stage.PRODUCTION;

		public Builder modules(Module... modules) {
			// check null
			Collections.addAll(this.modules, modules);
			return this;
		}

		public Builder modules(String... moduleNames) {
			// check null
			Collections.addAll(this.moduleNames, moduleNames);
			return this;
		}

		public Builder module(Module module) {
			// check null
			this.modules.add(module);
			return this;
		}

		public Builder module(String moduleName) {
			// check null
			this.moduleNames.add(moduleName);
			return this;
		}

		public Builder path(String path) {
			// check null
			this.paths.add(path);
			return this;
		}

		public Builder paths(String... paths) {
			// check null
			Collections.addAll(this.paths, paths);
			return this;
		}

		public Builder stage(Stage stage) {
			this.stage = stage;
			return this;
		}

		public Builder config(GuiceConfiguration config) {
			// FIXME preconditions: not-null, not set

			if (config != null) {
				this.modules(config.getModules());
				this.path(config.getScanPackage());
				this.stage(config.getStage());
			}

			return this;
		}

		public GuiceContext build() {		
			return new GuiceContext(this);
		}
	}

	@SuppressWarnings("unchecked")
	public static GuiceContext build(Configuration configuration, Environment environment, GuiceContainer container) {
		GuiceConfiguration guiceConfig = getGuiceConfig(configuration);
		return builder().config(guiceConfig)
				.module(new ConfigurationModule(configuration))
				.module(new EnvironmentModule(environment))
				.module(new JerseyContainerModule(container))
				.build();
	}

	@SuppressWarnings("unchecked")
	public static GuiceContext build(Configuration configuration, Environment environment) {
		GuiceConfiguration guiceConfig = getGuiceConfig(configuration);
		return builder().config(guiceConfig)
				.module(new ConfigurationModule(configuration))
				.module(new EnvironmentModule(environment))
				.build();
	}

	// FIXME: DRY with above?
	@SuppressWarnings("unchecked")
	public static GuiceContext build(Configuration configuration) {
		GuiceConfiguration guiceConfig = getGuiceConfig(configuration);
		return builder().config(guiceConfig)
				.module(new ConfigurationModule(configuration))
				.build();
	}

	public static GuiceContext build(File configFile, Class<? extends Configuration> clazz) {
		return build(configFile.getAbsolutePath(), clazz);
	}
	
	public static GuiceContext build(String configFile, Class<? extends Configuration> clazz) {
		return build(loadConfig(configFile, clazz));
	}

	public static GuiceContext get(String configFile, Class<? extends Configuration> clazz) {
		GuiceContext context = CACHE.get(configFile);
		if (context == null) {
			context = build(configFile, clazz);
			CACHE.put(configFile, context);
		}
		return context;
	}

	public static GuiceConfiguration getGuiceConfig(Configuration config) {
		for (Method method : config.getClass().getDeclaredMethods()) {
			if (GuiceConfiguration.class.equals(method.getReturnType())) {
				try {
					return (GuiceConfiguration) method.invoke(config);
				} 
				catch (Exception e) {
					LOG.error("Error getting config from {}: {}", method.getName(), e.toString());
				}
			}
		}
		return null;
	}

	public static <T extends Configuration> T loadConfig(String path, Class<T> clazz) {
		try {
			long start = System.currentTimeMillis();
			
			// FIXME: This should all be part of dropwizard?
			ConfigurationFactoryFactory<T> configurationFactoryFactory = new DefaultConfigurationFactoryFactory<T>();
			ConfigurationSourceProvider provider = new FileConfigurationSourceProvider();
			Validator validator = Validation.byProvider(HibernateValidator.class).configure().addValidatedValueHandler(new OptionalValidatedValueUnwrapper()).buildValidatorFactory().getValidator();
			ObjectMapper objectMapper = Jackson.newObjectMapper();

			ConfigurationFactory<T> configurationFactory = configurationFactoryFactory.create(clazz, validator, objectMapper, "dw");
			T config = configurationFactory.build(provider, path);
			
			// init logging
			LOG.info("Initializing logging from configuration: {}", path);
	        config.getLoggingFactory().configure(new MetricRegistry(), "guice-context");

			LOG.info("Loaded config in {}ms: {}", (System.currentTimeMillis() - start), path);
			
			return config;
		} 
		catch (Exception e) {
			throw new RuntimeException("Error loading config: " + path, e);
		}
	}
	
	private GuiceContext(Builder builder) {
		long start = System.currentTimeMillis();
		
		// In builder, or here?
		Iterables.addAll(builder.modules, this.scanForModules(builder));
		this.injector = Guice.createInjector(builder.stage, builder.modules);
		LOG.info("Created injector with: {}", Iterables.transform(builder.modules, simpleNames));
		if (LOG.isTraceEnabled()) {
			this.logInjector();
		}
		LOG.info("Loaded GuiceContext in {}ms: {}", (System.currentTimeMillis() - start), this);
	}

	private Iterable<? extends Module> scanForModules(Builder builder) {
		if (builder.paths.isEmpty() && builder.moduleNames.isEmpty()) {
			return Collections.emptySet();
		}
		else if (!builder.paths.isEmpty()) {
			LOG.debug("Scanning for modules in {}", builder.paths);
			Iterable<? extends Module> modules = Modules.Builder.packages(builder.paths).build(builder.moduleNames);
			LOG.info("Found modules: {}", Iterables.transform(modules, simpleNames));
			return modules;
		}
		else {
			throw new RuntimeException("Invalid state: Cannot load modules without paths");
		}
	}

	public Injector injector() {
		return this.injector;
	}

	public <C> C get(Class<C> type) {
		return this.injector.getInstance(type);
	}
	
	public String toString() {
		//return this.injector.toString();
		Map<Key<?>, Binding<?>> bindings = this.injector().getBindings();
		Set<String> keys = Sets.newHashSet(Iterables.transform(bindings.keySet(), keyNames));
		return "GuiceContext[" + bindings.size() + "] " + keys;
	}

	private static final Function<Object,String> simpleNames = new Function<Object,String>() {
		public String apply(Object input) {
			return input.getClass().getSimpleName();
		}
	};
	
	private static final Function<Key<?>,String> keyNames = new Function<Key<?>,String>() {
		public String apply(Key<?> input) {
			return input.getTypeLiteral().getRawType().getSimpleName();
		}
	};

	protected void logInjector() {
		StringBuilder builder = new StringBuilder("\n---- Available Bindings ----\n");
		for (Map.Entry<Key<?>, Binding<?>> binding : this.injector.getAllBindings().entrySet()) {
			builder.append(binding.getKey())
				.append(" ==> ")
				.append(binding.getValue())
				.append("\n");
		}
		LOG.info(builder.toString());
	}
}
