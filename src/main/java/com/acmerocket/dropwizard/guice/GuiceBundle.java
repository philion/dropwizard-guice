package com.acmerocket.dropwizard.guice;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T> {
    private static final Logger LOG = LoggerFactory.getLogger(GuiceBundle.class);

    private final AutoConfig autoConfig;
    private GuiceContext context;
    private final GuiceContainer container = new GuiceContainer();

    public GuiceBundle() {
    	this.autoConfig = null;
    }
    
    public GuiceBundle(String... basePackages) {
        Preconditions.checkNotNull(basePackages.length > 0);
        this.autoConfig = new AutoConfig(basePackages);
        LOG.debug("Initialized AutoConfig with {}", basePackages);
    }
    
    /**
     * Sugar: Use the Application package as the base scan root.
     * @param baseObj
     */
    public GuiceBundle(Application<T> baseObj) {
    	this(baseObj.getClass().getPackage().getName());
    }
    
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    	// no-op, unless load bundles
    	// TODO: Load bundles into bootstrap
    }

	@Override
    public void run(final T configuration, final Environment environment) {    	
    	this.context = GuiceContext.build(configuration, environment, this.container);
    	LOG.info("Initialized GuiceContext: {}", context);
       	
        this.container.setResourceConfig(environment.jersey().getResourceConfig());
        environment.jersey().replace(new Function<ResourceConfig, ServletContainer>() {
            @Nullable
            @Override
            public ServletContainer apply(ResourceConfig resourceConfig) {
                return container;
            }
        });
        environment.servlets().addFilter("Guice Filter", GuiceFilter.class)
                .addMappingForUrlPatterns(null, false, environment.getApplicationContext().getContextPath() + "*");

        if (this.autoConfig != null) {
            this.autoConfig.run(environment, this.injector());
        }
    }
    
    public Injector injector() {
    	return this.context().injector();
    }
    
    public GuiceContext context() {
    	return this.context;
    }
}
