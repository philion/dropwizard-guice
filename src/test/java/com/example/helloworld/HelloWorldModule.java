package com.example.helloworld;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acmerocket.guice.modules.Modules;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

@Modules
public class HelloWorldModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldModule.class);

	@Override
	protected void configure() {
		//LOG.debug("Configuring (no-op)");
	}

	@Provides @Named("template")
	public String provideTemplate(HelloWorldConfiguration configuration) {
		LOG.debug("Providing tempate={}, from {}", configuration.getTemplate(), configuration);
		return configuration.getTemplate();
	}

	@Provides @Named("defaultName")
	public String provideDefaultName(HelloWorldConfiguration configuration) {
		LOG.debug("Providing defaultName: {}", configuration.getDefaultName());
		return configuration.getDefaultName();
	}
}