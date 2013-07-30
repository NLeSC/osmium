package nl.esciencecenter.octopus.webservice.job;

/*
 * #%L
 * Octopus Job Webservice
 * %%
 * Copyright (C) 2013 Nederlands eScience Center
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import nl.esciencecenter.octopus.webservice.job.OctopusConfiguration;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class OctopusConfigurationTest {
    @Test
    public void testGATConfigurationBrokerPrefs() throws URISyntaxException {
        URI scheduler = new URI("local:///");
        String queue = null;
        URI sandbox = new URI("file:///tmp");
        ImmutableMap<String, String> prefs = ImmutableMap.of(
                "octopus.adaptors.local.queue.multi.maxConcurrentJobs", "1");
        PollConfiguration pollConf = new PollConfiguration();
        OctopusConfiguration config = new OctopusConfiguration(scheduler, queue, sandbox, prefs, pollConf);

        assertThat(config.getScheduler()).isEqualTo(scheduler);
        assertThat(config.getQueue()).isEqualTo(queue);
        assertThat(config.getSandboxRoot()).isEqualTo(sandbox);
        assertThat(config.getPreferences()).isEqualTo(prefs);
    }

    @Test
    public void testGetPreferences() {
        OctopusConfiguration g = new OctopusConfiguration();
        ImmutableMap<String, String> expected = ImmutableMap.of();
        assertThat(g.getPreferences()).isEqualTo(expected);
    }

    @Test
    public void testSetPreferences() {
        OctopusConfiguration g = new OctopusConfiguration();
        ImmutableMap<String, String> prefs = ImmutableMap.of("mykey", "myval");
        g.setPreferences(prefs);
        assertThat(g.getPreferences()).isEqualTo(prefs);
    }

    @Test
    public void deserializesFromJson() throws IOException, URISyntaxException {
        OctopusConfiguration actual = fromJson(jsonFixture("fixtures/octopus.json"),
                OctopusConfiguration.class);

        assertThat(actual.getScheduler()).isEqualTo(new URI("local:///"));
        assertThat(actual.getQueue()).isEqualTo("multi");
        assertThat(actual.getSandboxRoot()).isEqualTo(new URI("file:///tmp/sandboxes"));
        ImmutableMap<String, String> prefs = ImmutableMap.of(
                "octopus.adaptors.local.queue.multi.maxConcurrentJobs", "4");
        assertThat(actual.getPreferences()).isEqualTo(prefs);
        PollConfiguration expected_poll = new PollConfiguration(500, 3600000, 43200000);
        assertThat(actual.getPollConfiguration()).isEqualTo(expected_poll);
    }

    @Test
    public void validateFromJson() throws IOException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        OctopusConfiguration actual = fromJson(jsonFixture("fixtures/octopus.json"),
                OctopusConfiguration.class);

        Set<ConstraintViolation<OctopusConfiguration>> constraintViolations = validator.validateProperty(actual, "scheduler");

        assertThat(constraintViolations.size()).isEqualTo(0);
    }
}
