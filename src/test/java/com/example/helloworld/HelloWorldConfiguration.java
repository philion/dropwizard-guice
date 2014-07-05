
package com.example.helloworld;

import io.dropwizard.Configuration;

import javax.inject.Singleton;
import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acmerocket.dropwizard.guice.GuiceConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

@Singleton
public class HelloWorldConfiguration extends Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldConfiguration.class);

	@NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";
    
	@JsonProperty @Valid
	private GuiceConfiguration guice;
	
	public HelloWorldConfiguration() {
		LOG.debug("Constructing, hash={}", hashStr());
	}

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
	
	private String hashStr() {
		return Integer.toString(System.identityHashCode(this), 36);
	}

	@Override
	public String toString() {
		return "<" + hashStr() + "> [template=" + template
				+ ", defaultName=" + defaultName + ", guice=" + guice + "]";
	}
}
