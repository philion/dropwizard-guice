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

import com.google.inject.AbstractModule;

/**
 * @author philion
 *
 */
public class ConfigurationModule extends AbstractModule {
	private final Configuration config;
	
	public ConfigurationModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	public void configure() {
		this.bind(Configuration.class).toInstance(this.config);
	}
}
