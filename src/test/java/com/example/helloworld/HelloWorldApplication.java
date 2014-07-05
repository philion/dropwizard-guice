package com.example.helloworld;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.acmerocket.dropwizard.guice.GuiceBundle;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
    	
    	if (args == null || args.length == 0) {
    		args = new String[] { "server", "src/test/resources/helloworld.yml" };
    	}
    	
        new HelloWorldApplication().run(args);
    }

	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
		GuiceBundle<HelloWorldConfiguration> guiceBundle = new GuiceBundle<HelloWorldConfiguration>(this);
		bootstrap.addBundle(guiceBundle);
	}

	@Override
	public void run(HelloWorldConfiguration configuration, Environment environment) {
		
	}
}
