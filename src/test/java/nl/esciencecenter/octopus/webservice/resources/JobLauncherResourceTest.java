package nl.esciencecenter.octopus.webservice.resources;

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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.webservice.api.JobStatusResponse;
import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.JobSubmitResponse;
import nl.esciencecenter.octopus.webservice.job.OctopusManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

public class JobLauncherResourceTest {

    @Test
    public void testSubmitJob() throws Exception {
        JobSubmitRequest request = mock(JobSubmitRequest.class);
        OctopusManager manager = mock(OctopusManager.class);
        Job job = mock(Job.class);
        HttpClient httpClient = new DefaultHttpClient();
        when(manager.submitJob(request, httpClient)).thenReturn(job);
        when(job.getIdentifier()).thenReturn("1234");

        JobLauncherResource resource = new JobLauncherResource(manager, httpClient);

        JobSubmitResponse response = resource.submitJob(request);

        assertEquals(response.jobid, "1234");
    }

    @Test
    public void testStateJob() throws OctopusIOException, OctopusException {
        OctopusManager manager = mock(OctopusManager.class);
        JobStatusResponse status = mock(JobStatusResponse.class);
        HttpClient httpClient = new DefaultHttpClient();
        when(manager.stateOfJob("1234")).thenReturn(status);

        JobLauncherResource resource = new JobLauncherResource(manager, httpClient);

        JobStatusResponse response = resource.stateOfJob("1234");

        assertThat(response).isEqualTo(status);
    }

    @Test
    public void cancelJob() throws OctopusIOException, OctopusException {
        String request = "1234";
        OctopusManager manager = mock(OctopusManager.class);
        HttpClient httpClient = new DefaultHttpClient();

        JobLauncherResource resource = new JobLauncherResource(manager, httpClient);

        resource.cancelJob(request);

        verify(manager).cancelJob("1234");
    }

}
