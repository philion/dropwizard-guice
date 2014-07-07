Dropwizard-Guice
================

[![Build Status](https://travis-ci.org/philion/dropwizard-guice.svg)](https://travis-ci.org/philion/dropwizard-guice)

A simple DropWizard extension for integrating Guice via a bundle. It optionally uses classpath 
scanning courtesy of the Reflections project to discover resources and more to install into 
the dropwizard environment upon service start.

### Usage

```xml
    <dependencies>
        <dependency>
            <groupId>com.acmerocket.dropwizard</groupId>
            <artifactId>dropwizard-guice</artifactId>
            <version>0.7.1</version>
        </dependency>
    </dependencies>
```

Simply install a new instance of the bundle during your service initialization
```java
public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

	public static void main(String[] args) throws Exception {
		new HelloWorldApplication().run(args);
	}

	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {

		GuiceBundle<HelloWorldConfiguration> guiceBundle = GuiceBundle.<HelloWorldConfiguration>newBuilder()
				.addModule(new HelloWorldModule())
				.setConfigClass(HelloWorldConfiguration.class)
				.build();

		bootstrap.addBundle(guiceBundle);
	}

    @Override
    public String getName() {
        return "hello-world";
    }

	@Override
	public void run(HelloWorldConfiguration helloWorldConfiguration, Environment environment) throws Exception {
        environment.jersey().register(HelloWorldResource.class);
        environment.healthChecks().register("Template", TemplateHealthCheck.class);
	}
}
```

Lastly, you can enable auto configuration via package scanning.
```java
public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

	public static void main(String[] args) throws Exception {
		new HelloWorldApplication().run(args);
	}

	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {

		GuiceBundle<HelloWorldConfiguration> guiceBundle = GuiceBundle.<HelloWorldConfiguration>newBuilder()
				.addModule(new HelloWorldModule())
				.enableAutoConfig(getClass().getPackage().getName())
				.setConfigClass(HelloWorldConfiguration.class)
				.build();

		bootstrap.addBundle(guiceBundle);
	}

    @Override
    public String getName() {
        return "hello-world";
    }

	@Override
	public void run(HelloWorldConfiguration helloWorldConfiguration, Environment environment) throws Exception {
        // now you don't need to add resources, tasks, healthchecks or providers
        // you must have your health checks inherit from InjectableHealthCheck in order for them to be injected
	}
}
```
If you are having trouble accessing your Configuration or Environment inside a Guice Module, you could try using a provider.

```java
public class HelloWorldModule extends AbstractModule {

    @Override
    protected void configure() {
        // anything you'd like to configure
    }

    @Provides
    public SomePool providesSomethingThatNeedsConfiguration(HelloWorldConfiguration configuration) {
        return new SomePool(configuration.getPoolName());
    }

    @Provides
    public SomeManager providesSomenthingThatNeedsEnvironment(Environment env) {
        return new SomeManager(env.getSomethingFromHere()));
    }
}
```

Please fork [an example project](https://github.com/eliast/dropwizard-guice-example) if you'd like to get going right away. 

Enjoy!
