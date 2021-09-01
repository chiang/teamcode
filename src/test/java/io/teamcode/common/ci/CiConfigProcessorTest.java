package io.teamcode.common.ci;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.teamcode.common.ci.config.CiConfig;
import io.teamcode.common.ci.config.entry.JobEntry;
import io.teamcode.common.ci.config.entry.StageEntry;
import io.teamcode.common.ci.config.entry.StagesEntry;
import io.teamcode.domain.entity.ci.ArtifactsWhen;
import io.teamcode.domain.entity.ci.JobWhen;
import io.teamcode.web.api.model.ci.Variable;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class CiConfigProcessorTest {

    private Object Map;

    @Test
    public void variables() {
        String ymlString ="variables:\n" +
                "  DATABASE_URL: \"postgres://postgres@postgres/my_database\"\n" +
                "  DATABASE_USER: lala";

        CiConfigProcessor processor = new CiConfigProcessor();
        processor.process(ymlString);

        CiConfig ciConfig = processor.getCiConfig();
        List<Variable> variables = ciConfig.getVariables();
        Assert.assertEquals(2, variables.size());
        Assert.assertEquals("DATABASE_URL", variables.get(0).getName());
        Assert.assertEquals("DATABASE_USER", variables.get(1).getName());
        Assert.assertEquals("lala", variables.get(1).getValue());
    }

    @Test
    public void jobVariables() {
        String yamlString =
                "image: java8\n" +
                        "before-script: \n" +
                        "  - echo hellow\n" +
                        "job1:\n" +
                        "  variables:\n" +
                        "    SVN_STRATEGY: none\n" +
                        "    CI_VAL: yes";

        CiConfigProcessor processor = new CiConfigProcessor();
        processor.process(yamlString);

        CiConfig ciConfig = processor.getCiConfig();
        JobEntry jobEntry = ciConfig.getJobEntries().get(0);
        List<Variable> variables = jobEntry.getVariables();
        Assert.assertEquals("none", variables.stream().filter(v -> v.getName().equals("SVN_STRATEGY")).findFirst().get().getValue());
        Assert.assertEquals("true", variables.stream().filter(v -> v.getName().equals("CI_VAL")).findFirst().get().getValue());
    }

    @Test
    public void validateHasNoJobs() {
        String yamlString = "before-script: \n"
                + "  - echo hellow";

        CiConfigProcessor processor = new CiConfigProcessor();
        processor.process(yamlString);

        Assert.assertTrue(processor.getCiConfig().hasErrors());
    }

    @Test
    public void getJobs() {
        String yamlString =
                "before-script: \n" +
                        "  - echo hellow\n" +
                        "job1:\n" +
                        "  stage: deploy\n" +
                        "  script:\n" +
                        "    - echo hello\n" +
                        "    - echo world\n" +
                        "  when: manual\n" +
                        "  artifactsFile:\n" +
                        "    paths:\n" +
                        "      - abc/ccc.dll\n" +
                        "      - target/test.war\n" +
                        "    when: on_failure";


        CiConfigProcessor processor = new CiConfigProcessor();
        processor.process(yamlString);

        CiConfig ciConfig = processor.getCiConfig();

        Assert.assertEquals(1, ciConfig.getJobEntries().size());
        Assert.assertEquals(2, ciConfig.getJobEntries().get(0).getScript().getCommands().size());

        Assert.assertEquals(JobWhen.MANUAL, ciConfig.getJobEntries().get(0).getWhen());

        Assert.assertEquals(2, ciConfig.getJobEntries().get(0).getArtifactsEntry().getPaths().size());
        Assert.assertEquals(ArtifactsWhen.ON_FAILURE, ciConfig.getJobEntries().get(0).getArtifactsEntry().getWhen());
    }

    @Test
    public void getJobsAttributes() {
        String yamlString =
                "image: java8\n" +
                "before-script: \n" +
                "  - echo hellow\n" +
                "job1:\n" +
                "  stage: deploy\n" +
                "  script:\n" +
                "    - echo hello";

        CiConfigProcessor processor = new CiConfigProcessor();
        processor.process(yamlString);

        //List<JobAttributes> jobAttributesList = processor.getJobsAttributes();

        CiConfig ciConfig = processor.getCiConfig();

        Assert.assertEquals("java8", ciConfig.getImage());
        //Assert.assertEquals(1, jobAttributesList.size());
        //Assert.assertEquals("job1", jobAttributesList.get(0).getName());
        //Assert.assertEquals(JobWhen.ON_SUCCESS, jobAttributesList.get(0).getJobWhen());
    }

    @Test
    public void beforeScripts() {
        String yamlString = "before-script: \n"
                + "  - echo hello\n"
                + "  - echo world\n";

        CiConfigProcessor processor = new CiConfigProcessor();
        //CiYaml ciYaml = processor.getCiYaml(yamlString);
        //Assert.assertEquals(2, ciYaml.getBeforeScript().size());
        //Assert.assertEquals("echo hello", ciYaml.getBeforeScript().get(0));
        //Assert.assertEquals("echo world", ciYaml.getBeforeScript().get(1));
    }

    @Test
    public void unmarshall() throws JsonProcessingException {
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("stage", "qa");

        StagesEntry stagesEntry = Mockito.mock(StagesEntry.class);
        Mockito.when(stagesEntry.getStageEntries()).thenReturn(Arrays.asList(new StageEntry("build")));

        JobEntry jobEntry = new JobEntry(stagesEntry, "test-job", jobMap);
        System.out.println(jobEntry.toJson());
    }

    @Test
    public void emptyList() throws JsonProcessingException {
        String yamlString =
                "image: java8\n" +
                        "before-script: \n" +
                        "  - echo hellow\n" +
                        "job1:\n" +
                        "  stage: deploy\n" +
                        "  dependencies: \n" +
                        "  script:\n" +
                        "    - echo hello";

        CiConfigProcessor processor = new CiConfigProcessor();
        processor.process(yamlString);

        List<JobEntry> jobEntries = processor.getCiConfig().getJobEntries();
        System.out.println(jobEntries.get(0).toJson());
    }
}
