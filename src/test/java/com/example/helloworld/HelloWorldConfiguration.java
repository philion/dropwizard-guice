
package com.example.helloworld;

import io.dropwizard.Configuration;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.acmerocket.dropwizard.guice.GuiceConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HelloWorldConfiguration extends Configuration {
	@NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";
    
	@JsonProperty @Valid
	private GuiceConfiguration guice;

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

	/**
	 * @return the guice
	 */
	public GuiceConfiguration getGuice() {
		return guice;
	}
}
