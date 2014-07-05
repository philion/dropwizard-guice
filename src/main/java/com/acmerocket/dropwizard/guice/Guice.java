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

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acmerocket.guice.modules.Modules;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * @author philion
 *
 */
@Deprecated
public final class Guice {
    private static final Logger LOG = LoggerFactory.getLogger(Guice.class);

	private Set<String> moduleNames = Sets.newHashSet();
	private Set<Module> modules = Sets.newHashSet();
	private Set<String> paths = Sets.newHashSet();
	private Stage stage = Stage.PRODUCTION;

	public static Guice builder() {
		return new Guice();
	}
	
	private Guice() {}
	
	public Guice modules(Module... modules) {
		// check null
		Collections.addAll(this.modules, modules);
		return this;
	}
	
	public Guice modules(String... moduleNames) {
		// check null
		Collections.addAll(this.moduleNames, moduleNames);
		return this;
	}
	
	public Guice module(Module module) {
		// check null
		this.modules.add(module);
		return this;
	}
	
	public Guice module(String moduleName) {
		// check null
		this.moduleNames.add(moduleName);
		return this;
	}
	
	public Guice path(String path) {
		// check null
		this.paths.add(path);
		return this;
	}

	public Guice paths(String... paths) {
		// check null
		Collections.addAll(this.paths, paths);
		return this;
	}

	public Guice stage(Stage stage) {
		this.stage = stage;
		return this;
	}
	
	public Guice config(GuiceConfiguration config) {
		// FIXME preconditions: not-null, not set
		
		if (config != null) {
			this.modules(config.getModules());
			this.path(config.getScanPackage());
			this.stage(config.getStage());
		}
		
		return this;
	}
	
	public Injector build() {		
		// FIXME Error checking and validation
		
		// scan for modules and add to modules
		Iterables.addAll(this.modules, this.scanForModules());
		
		LOG.debug("Creating injector with: {}", this.modules);
		
		// create the injector
		return com.google.inject.Guice.createInjector(this.stage, this.modules); // FIXME double-check!
	}
	
	private Iterable<? extends Module> scanForModules() {
		if (this.paths.isEmpty() && this.moduleNames.isEmpty()) {
			return Collections.emptySet();
		}
		else if (!this.paths.isEmpty() && !this.moduleNames.isEmpty()) {
			Iterable<? extends Module> modules = 
					Modules.Builder.packages(this.paths).build(this.moduleNames);
			//LOG.info("### Found modules: {}", modules);
			return modules;
		}
		else {
			// FIXME: Better error
			throw new RuntimeException("Invalid state: Cannot load modules without paths");
		}
	}
	
//	public static void main(String[] args) {
//		String configPath = "src/test/resources/test.yml";
//		Stage stage = Stage.DEVELOPMENT; // optional, move to config
//		String scanPath = "com.acmerocket.dropwizard.guice";
//		
//		Configuration config = null; //GuiceConfiguration.load(configPath);
//		
//		//String[] modules = config.getModules();
//		//LOG.info("### From config: {}", modules);
//		
//		Injector injector = Guice.builder()
//				//.modules(config.getModules())
//				.module(new ConfigurationModule(config))
//				.path(scanPath)
//				.stage(stage)
//				.build();
//		
//		for (Map.Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet()) {
//			LOG.info(entry.getKey() + " => " + entry.getValue());
//		}
//		
//		Configuration generic = injector.getInstance(Configuration.class);
//		LOG.info("Generic config, type={}: {}", generic.getClass(), generic);
//		
//		GuiceConfiguration guice = injector.getInstance(GuiceConfiguration.class);
//		LOG.info("Guice config, type={}: {}", guice.getClass(), guice);
//
//	}
}
