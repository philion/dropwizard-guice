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

import java.util.Arrays;

import com.google.inject.Stage;

/**
 * @author philion
 *
 */
public class GuiceConfiguration {
	
	private String[] modules;
	private Stage stage;
	private String scanPackage;

	/**
	 * @return the modules
	 */
	public String[] getModules() {
		return modules;
	}

	/**
	 * @param modules the modules to set
	 */
	public void setModules(String[] modules) {
		this.modules = modules;
	}
	
	/**
	 * @return the stage
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * @param stage the stage to set
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	/**
	 * @return the scanPackage
	 */
	public String getScanPackage() {
		return scanPackage;
	}

	/**
	 * @param scanPackage the scanPackage to set
	 */
	public void setScanPackage(String scanPackage) {
		this.scanPackage = scanPackage;
	}

	@Override
	public String toString() {
		return "GuiceConfiguration [modules=" + Arrays.toString(modules)
				+ ", stage=" + stage + ", scanPackage=" + scanPackage + "]";
	}
}
