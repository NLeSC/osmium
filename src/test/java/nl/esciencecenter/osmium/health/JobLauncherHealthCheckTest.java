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
package nl.esciencecenter.osmium.health;


import static org.junit.Assert.*;
import nl.esciencecenter.osmium.health.JobLauncherHealthCheck;

import org.junit.Test;

import com.codahale.metrics.health.HealthCheck.Result;

public class JobLauncherHealthCheckTest {

    @Test
    public void testCheck() throws Exception {
        JobLauncherHealthCheck hc = new JobLauncherHealthCheck();
        assertEquals(Result.healthy(), hc.check());
    }

}
