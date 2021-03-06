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
package nl.esciencecenter.osmium.api;


import static com.yammer.dropwizard.testing.JsonHelpers.asJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.InvalidCopyOptionsException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.util.Sandbox;
import nl.esciencecenter.osmium.api.JobSubmitRequest;
import nl.esciencecenter.osmium.api.SandboxedJob;

import org.apache.http.Consts;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SandboxedJobTest {
    JobSubmitRequest request;
    Sandbox sandbox;
    Job ojob;
    HttpClient httpClient;
    JobStatus status;
    int pollIterations;
    SandboxedJob job;

    @Before
    public void setUp() throws URISyntaxException {
        sandbox = mock(Sandbox.class);
        ojob = new JobImplementation(mock(Scheduler.class), "1234", mock(JobDescription.class), false, false);
        request = new JobSubmitRequest();
        request.status_callback_url = new URI("http://localhost/job/status");
        httpClient = mock(HttpClient.class);
        Map<String, String> info = new HashMap<String, String>();
        info.put("status", "STOPPED");
        status = new JobStatusImplementation(ojob, "DONE", 0, null, false, true, info);
        pollIterations = 10;
        job = new SandboxedJob(sandbox, ojob, request, httpClient, status, pollIterations);
    }

    @Test
    public void testSandboxedJob_Default() {
        SandboxedJob sjob = new SandboxedJob(sandbox, ojob, request, httpClient);

        assertThat(sjob.getStatus()).isEqualTo(null);
        assertThat(sjob.getPollIterations()).isEqualTo(0);
    }

    @Test
    public void getRequest() {
        assertThat(job.getRequest()).isEqualTo(request);
    }

    @Test
    public void testGetSandbox() {
        assertThat(job.getSandbox()).isEqualTo(sandbox);
    }

    @Test
    public void testGetJob() {
        assertThat(job.getJob()).isEqualTo(ojob);
    }

    @Test
    public void testGetHttpClient() {
        assertThat(job.getHttpClient()).isEqualTo(httpClient);
    }

    @Test
    public void testGetStatus() {
        assertThat(job.getStatus()).isEqualTo(status);
    }

    @Test
    public void testGetPollIterations() {
        assertThat(job.getPollIterations()).isEqualTo(10);
    }

    @Test
    public void testSetPollIterations() {
        job.setPollIterations(25);

        assertThat(job.getPollIterations()).isEqualTo(25);
    }

    @Test
    public void testIncrPollIterations() {
        job.incrPollIterations();

        assertThat(job.getPollIterations()).isEqualTo(11);
    }

    @Test
    public void testSetStatus_ChangedWithCallback_HttpClientExecute() throws UnsupportedEncodingException,
            ClientProtocolException, IOException, URISyntaxException {
        JobStatus rstatus = new JobStatusImplementation(ojob, "RUNNING", null, null, true, false, null);
        pollIterations = 10;
        job = new SandboxedJob(sandbox, ojob, request, httpClient, rstatus, pollIterations);

        job.setStatus(this.status);

        assertThat(job.getStatus()).isEqualTo(this.status);
        ArgumentCaptor<HttpPut> argument = ArgumentCaptor.forClass(HttpPut.class);
        verify(httpClient).execute(argument.capture());
        HttpPut callback_request = argument.getValue();
        assertThat(callback_request.getURI()).isEqualTo(new URI("http://localhost/job/status"));
        assertThat(callback_request.getEntity().getContentType().getValue()).isEqualTo("application/json; charset=UTF-8");
        String body = EntityUtils.toString(callback_request.getEntity(), Consts.UTF_8);
        assertThat(body).isEqualTo(jsonFixture("fixtures/status.done.json"));
    }

    @Test
    public void testSetStatus_ChangedWithoutCallback_NoHttpClientExecute() throws UnsupportedEncodingException,
            ClientProtocolException, IOException {
        request.status_callback_url = null;
        JobStatus rstatus = new JobStatusImplementation(ojob, "RUNNING", null, null, true, false, null);
        pollIterations = 10;
        job = new SandboxedJob(sandbox, ojob, request, httpClient, rstatus, pollIterations);

        job.setStatus(this.status);
        verifyNoMoreInteractions(httpClient);
    }

    @Test
    public void testSetStatus_UnChangedWithCallback_NoHttpClientExecute() throws UnsupportedEncodingException,
            ClientProtocolException, IOException {
        pollIterations = 10;
        job = new SandboxedJob(sandbox, ojob, request, httpClient, status, pollIterations);

        job.setStatus(this.status);

        assertThat(job.getStatus()).isEqualTo(this.status);
        verifyNoMoreInteractions(httpClient);
    }

    @Test
    public void testCleanSandbox() throws URISyntaxException, UnsupportedOperationException, XenonException {
        job.cleanSandbox();

        verify(sandbox).delete();
    }

    @Test
    public void testDownloadSandbox() throws URISyntaxException, UnsupportedOperationException, InvalidCopyOptionsException, XenonException {
        job.downloadSandbox();

        verify(sandbox).download(CopyOption.REPLACE);
    }

    @Test
    public void getIdentifier() {
        String id = job.getIdentifier();

        String expected = "1234";
        assertThat(id).isEqualTo(expected);
    }

    @Test
    public void serializesToJSON() throws IOException {
        JobSubmitRequest request2 = JobSubmitRequestTest.sampleRequest();
        job = new SandboxedJob(sandbox, ojob, request2, httpClient, status, pollIterations);

        assertThat(asJson(job), is(equalTo(jsonFixture("fixtures/job.json"))));
    }
}
