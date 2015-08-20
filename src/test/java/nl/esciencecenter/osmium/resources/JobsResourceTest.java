/*
 * #%L
 * Osmium
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
package nl.esciencecenter.osmium.resources;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import nl.esciencecenter.osmium.api.JobSubmitRequest;
import nl.esciencecenter.osmium.api.SandboxedJob;
import nl.esciencecenter.osmium.callback.CallbackClient;
import nl.esciencecenter.osmium.job.XenonManager;
import nl.esciencecenter.osmium.resources.JobsResource;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.Test;

public class JobsResourceTest {

    @Test
    public void testSubmitJob() throws Exception {
        JobSubmitRequest request = mock(JobSubmitRequest.class);
        XenonManager manager = mock(XenonManager.class);
        SandboxedJob job = mock(SandboxedJob.class);
        when(job.getIdentifier()).thenReturn("1234");
        CallbackClient callbackClient = new CallbackClient(HttpClients.createDefault(), new BasicHttpContext());
        when(manager.submitJob(request, callbackClient)).thenReturn(job);
        UriInfo uriInfo = mock(UriInfo.class);
        UriBuilder builder = UriBuilder.fromUri("http://localhost/job/");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
        JobsResource resource = new JobsResource(manager, callbackClient, uriInfo);

        Response response = resource.submitJob(request);

        assertEquals(201, response.getStatus());
        URI expected = new URI("http://localhost/job/1234");
        assertEquals(expected, response.getMetadata().getFirst("Location"));
    }

    @Test
    public void getJobs() throws URISyntaxException {
        // mock manager so it returns a list of jobs
        XenonManager manager = mock(XenonManager.class);
        SandboxedJob job = mock(SandboxedJob.class);
        when(job.getIdentifier()).thenReturn("1234");
        Collection<SandboxedJob> jobs = new LinkedList<SandboxedJob>();
        jobs.add(job);
        when(manager.getJobs()).thenReturn(jobs);
        CallbackClient callbackClient = new CallbackClient(HttpClients.createDefault(), new BasicHttpContext());
        UriInfo uriInfo = mock(UriInfo.class);
        UriBuilder builder = UriBuilder.fromUri("http://localhost/job/");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
        JobsResource resource = new JobsResource(manager, callbackClient, uriInfo);

        URI[] response = resource.getJobs();

        URI[] expected = { new URI("http://localhost/job/1234") };
        assertThat(response, is(expected));
    }

    @Test
    public void getJobs_2Jobs() throws URISyntaxException {
        // mock manager so it returns a list of jobs
        XenonManager manager = mock(XenonManager.class);
        Collection<SandboxedJob> jobs = new LinkedList<SandboxedJob>();
        SandboxedJob job = mock(SandboxedJob.class);
        when(job.getIdentifier()).thenReturn("1234");
        jobs.add(job);
        SandboxedJob job2 = mock(SandboxedJob.class);
        when(job2.getIdentifier()).thenReturn("4567");
        jobs.add(job2);
        when(manager.getJobs()).thenReturn(jobs);
        CallbackClient callbackClient = new CallbackClient(HttpClients.createDefault(), new BasicHttpContext());
        UriInfo uriInfo = mock(UriInfo.class);
        UriBuilder builder = UriBuilder.fromUri("http://localhost/job/");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
        JobsResource resource = new JobsResource(manager, callbackClient, uriInfo);

        URI[] response = resource.getJobs();

        URI[] expected =
                { new URI("http://localhost/job/1234"),
                        new URI("http://localhost/job/4567") };
        assertThat(response, is(expected));
    }

}
