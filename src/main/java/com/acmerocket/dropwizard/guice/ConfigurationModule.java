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

import io.dropwizard.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;

/**
 * @author philion
 *
 */
public class ConfigurationModule<T extends Configuration> extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationModule.class);

	private final T config;
	private final Class<T> configClazz;
	
	@SuppressWarnings("unchecked")
	public ConfigurationModule(T config) {
		this.config = config;
		this.configClazz = (Class<T>) config.getClass();
	}
	
	@Override
	public void configure() {
		LOG.debug("Binding {}", this.config);
		this.bind(Configuration.class).toInstance(this.config);
		this.bind(this.configClazz).toInstance(this.config);
	}
}
