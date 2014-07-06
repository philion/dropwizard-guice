/**
 * Copyright 2014 Acme Rocket Company [acmerocket.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acmerocket.dropwizard.guice;

import io.dropwizard.Application;
import io.dropwizard.Configuration;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Injector;

/**
 * A JUnit rule for starting and stopping your application at the start and end of a test class.
 * <p/>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 *
 * @param <C> the configuration type
 */
public class GuiceRule implements TestRule {

	private final GuiceContext context;
	
    public GuiceRule(String configPath, Class<? extends Configuration> configClass) {
    	this.context = GuiceContext.get(configPath, configClass);
    }
    
    public GuiceRule(GuiceContext context) {
    	this.context = context;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
    	return base;
    }

    public GuiceContext context() {
    	return this.context;
    }
    
    public Injector injector() {
    	return this.context().injector();
    }
    
	public <T> T get(Class<T> type) {
		return this.injector().getInstance(type);
	}
}
