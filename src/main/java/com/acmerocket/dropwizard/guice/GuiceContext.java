package com.acmerocket.dropwizard.guice;

import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.valuehandling.OptionalValidatedValueUnwrapper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acmerocket.guice.modules.Modules;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;


public class GuiceContext {
	private static final Logger LOG = LoggerFactory.getLogger(GuiceContext.class);

	private final Injector injector;
	
	public static Builder builder() {
		return new Builder();
	}
	
	private static class Builder {
		private final Set<String> moduleNames = Sets.newHashSet();
		private final Set<Module> modules = Sets.newHashSet();
		private final Set<String> paths = Sets.newHashSet();
		private Stage stage = Stage.PRODUCTION;
		
		@SuppressWarnings("unused")
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

		@SuppressWarnings("unused")
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

		@SuppressWarnings("unused")
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
			// FIXME Error checking and validation

			return new GuiceContext(this);

			// scan for modules and add to modules
			//Iterables.addAll(this.modules, this.scanForModules());

			// create the injector
			//return com.google.inject.Guice.createInjector(this.stage, this.modules); // FIXME double-check!
		}
	}
	
	public static GuiceContext build(Configuration configuration, Environment environment, GuiceContainer container) {
    	GuiceConfiguration guiceConfig = getGuiceConfig(configuration);
    	return builder().config(guiceConfig)
    			.module(new ConfigurationModule(configuration))
    			.module(new EnvironmentModule(environment))
    			.module(new JerseyContainerModule(container))
    			.build();
	}
	
	public static GuiceContext build(Configuration configuration, Environment environment) {
    	GuiceConfiguration guiceConfig = getGuiceConfig(configuration);
    	return builder().config(guiceConfig)
    			.module(new ConfigurationModule(configuration))
    			.module(new EnvironmentModule(environment))
    			.build();
	}
	
	// FIXME: DRY with above?
	public static GuiceContext build(Configuration configuration) {
    	GuiceConfiguration guiceConfig = getGuiceConfig(configuration);
    	return builder().config(guiceConfig)
    			.module(new ConfigurationModule(configuration))
    			.build();
	}
	
	public static GuiceContext build(File configFile, Class<? extends Configuration> clazz) {
		return build(loadConfig(configFile.getAbsolutePath(), clazz));
	}
	
    // build with ???
	
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
        	// FIXME: This should all be part of dropwizard?
        	ConfigurationFactoryFactory<T> configurationFactoryFactory = new DefaultConfigurationFactoryFactory<T>();
        	ConfigurationSourceProvider provider = new FileConfigurationSourceProvider();
        	Validator validator = Validation.byProvider(HibernateValidator.class).configure().addValidatedValueHandler(new OptionalValidatedValueUnwrapper()).buildValidatorFactory().getValidator();
        	ObjectMapper objectMapper = Jackson.newObjectMapper();
        	
            ConfigurationFactory<T> configurationFactory = configurationFactoryFactory.create(clazz, validator, objectMapper, "dw");
            
            return configurationFactory.build(provider, path);
        } 
        catch (Exception e) {
            throw new RuntimeException("Error loading config: " + path, e);
        }
    }
    
	private GuiceContext(Builder builder) {
		// In builder, or here?
		Iterables.addAll(builder.modules, this.scanForModules(builder));
		this.injector = Guice.createInjector(builder.stage, builder.modules);
	}
	
	private Iterable<? extends Module> scanForModules(Builder builder) {
		if (builder.paths.isEmpty() && builder.moduleNames.isEmpty()) {
			return Collections.emptySet();
		}
		else if (!builder.paths.isEmpty()) {
			Iterable<? extends Module> modules = 
					Modules.Builder.packages(builder.paths).build(builder.moduleNames);
			LOG.info("### Found modules: {}", modules);
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
		return this.injector().getInstance(type);
	}
}
