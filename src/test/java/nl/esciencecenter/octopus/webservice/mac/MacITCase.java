package nl.esciencecenter.octopus.webservice.mac;

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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.webservice.JobLauncherService;
import nl.esciencecenter.octopus.webservice.mac.MacCredential;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * MAC Access Authentication integration test.
 *
 * Requires: Url to test, config
 * file with valid mac credentials and web server with MAC Access
 * Authentication.
 *
 * @author verhoes
 *
 */
public class MacITCase {

    @Test
    public void test() throws URISyntaxException, ClientProtocolException,
            IOException {
        // TODO read url to test from somewhere instead of hardcoding
        URI url = new URI(
                "http://localhost/magma/status/8a566a5f-565e-4861-8018-128adec07bbe.json");
        String state = "STOPPED";
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(state));

        // TODO read mac_id, mac_key, scope from config file or somewhere else
        String mac_id = "eyJzYWx0IjogImIwN2JlZiIsICJleHBpcmVzIjogMTM4NzEwNjg2NC4wMzMwMTMsICJ1c2VyaWQiOiAiam9ibWFuYWdlciJ9MAagqaCQxnD-pCxCKaowmXz0rUU=";
        String mac_key = "msr3vIHozDgO1thMYzh89pG8qg0=";
        URI scope = new URI("http://localhost");
        ImmutableList<MacCredential> macs = ImmutableList.of(new MacCredential(
                mac_id, mac_key, scope));
        HttpClient httpClient = new DefaultHttpClient();
        httpClient = JobLauncherService.macifyHttpClient(
                (AbstractHttpClient) httpClient, macs);

        HttpResponse response = httpClient.execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
    }

}
