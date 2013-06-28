package nl.esciencecenter.octopus.webservice.job;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.octopus.webservice.api.JobSubmitRequest;
import nl.esciencecenter.octopus.webservice.api.SandboxedJob;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

public class PendingCancelITCase {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private OctopusManager manager;

    private HttpClient httpClient = new DefaultHttpClient();

    @Test
    public void test() throws Exception {
        URI scheduler = new URI("local:///");
        PollConfiguration pollConfiguration = new PollConfiguration();
        ImmutableMap<String, Object> preferences =
                ImmutableMap.of("octopus.adaptors.local.queue.multi.maxConcurrentJobs", (Object) 1);
        URI sandboxRoot = testFolder.newFolder("sandboxes").toURI();
        String queue = "multi";
        OctopusConfiguration configuration =
                new OctopusConfiguration(scheduler, queue, sandboxRoot, preferences, pollConfiguration);

        manager = new OctopusManager(configuration);

        String jobdir = testFolder.newFolder("job1").getPath();
        List<String> arguments = new ArrayList<String>();
        arguments.add("6000");
        JobSubmitRequest submit =
                new JobSubmitRequest(jobdir, "/bin/sleep", arguments, new ArrayList<String>(), new ArrayList<String>(),
                        "stderr.txt", "stdout.txt", null);

        // when 1 job is submmited -> test passes,
        // when 2 jobs are submitted and second cancelled -> test fails.
        manager.submitJob(submit, httpClient);
        SandboxedJob job = manager.submitJob(submit, httpClient);

        manager.start();
        Thread.sleep(5000); // allow poller to update status

        manager.cancelJob(job.getIdentifier());

        SandboxedJob job_out = manager.getJob(job.getIdentifier());

        assertEquals(job_out.getStatus().getState(), "KILLED");

        manager.stop();
    }
}
