package com.acmerocket.dropwizard.guice;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;

public class GuiceContextOld<T extends Configuration> {

	//private final Injector injector;
	//private final T config;
	private Class<T> configClass; //FIXME Generics.getTypeParameter(bootstrap, Configuration.class), 

//	public GuiceContextOld(Iterable<Module> modules, String basePackage, String pathToConfig) throws ConfigurationException, IOException {
//
//		OLDGuiceBundle.Builder<T> builder = null; // GuiceBundle.<T>builder();
//
//		// add modules
//		for (Module module : modules) {
//			builder.addModule(module);
//		}
//
//		// setup autoconfig
//		if (basePackage != null) {
//			builder.enableAutoConfig(basePackage);
//		}
//
//		this.configClass = Generics.getTypeParameter(getClass(), Configuration.class);
//		builder.setConfigClass(this.configClass);
//
//		OLDGuiceBundle<T> bundle = builder.build();
//
//		Bootstrap<T> bootstrap = this.buildBootstrap();
//		this.config = this.buildConfiguration(bootstrap, pathToConfig);
//		Environment environment = this.buildEnvironment(bootstrap);
//
//		bundle.initialize(bootstrap);
//		bundle.run(this.config, environment);
//
//		this.injector = bundle.getInjector();
//	}

	private Bootstrap<T> buildBootstrap() {
		Application<T> app = new Application<T>() {
			public String getName() { return "GuiceTestApp"; }
			public void initialize(Bootstrap<T> bootstrap) {}
			public void run(T configuration, Environment environment) {}
		};
		return new Bootstrap<T>(app);
	}

	private T buildConfiguration(Bootstrap<T> bootstrap, String path) throws ConfigurationException, IOException {
		ConfigurationFactory<T> configurationFactory = bootstrap.getConfigurationFactoryFactory().create(
				this.configClass,
				bootstrap.getValidatorFactory().getValidator(), 
				bootstrap.getObjectMapper(), 
				"dw");
		return configurationFactory.build(bootstrap.getConfigurationSourceProvider(), path);
	}

	private Environment buildEnvironment(Bootstrap<T> bootstrap) {
		return new Environment(bootstrap.getApplication().getName(),
				bootstrap.getObjectMapper(),
				bootstrap.getValidatorFactory().getValidator(),
				bootstrap.getMetricRegistry(),
				bootstrap.getClassLoader());
	}

}
