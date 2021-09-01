package io.teamcode.common.ci;

import io.teamcode.common.Strings;
import io.teamcode.common.ci.config.CiConfig;
import io.teamcode.common.ci.config.entry.JobEntry;
import io.teamcode.common.ci.config.entry.ValidationError;
import io.teamcode.domain.entity.ci.CiYaml;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.beans.IntrospectionException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by chiang on 2017. 4. 8..
 */
public class CiConfigProcessor {

    private String ymlString;

    private Map<String, Object> ymlData;

    private CiConfig ciConfig;

    public void process(String ymlString) {
        this.ymlString = ymlString;

        Yaml yaml = new Yaml();
        ymlData = (Map<String, Object>)yaml.load(ymlString);
        this.ciConfig = new CiConfig(this.ymlData);
        this.ciConfig.validate();
    }

    public CiConfig getCiConfig() {
        return this.ciConfig;
    }

    private void parseBeforeScripts() {
    }

    /*public CiYaml getCiYaml(String yamlString) {

        return parse(yamlString);
    }*/

    public CiConfig resolveConfig(String ymlString) {

        Constructor constructor = new Constructor(CiYaml.class);
        constructor.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<? extends Object> type, String name) throws IntrospectionException {
                if ( name.indexOf('-') > -1 ) {
                    name = Strings.toCamelCase(name, '-');
                }
                return super.getProperty(type, name);
            }
        });

        Yaml yaml = new Yaml(constructor);
        Map<String, Object> map = (Map<String, Object>)yaml.load(ymlString);

        return new CiConfig(map);
    }

    /*public void validate(String yamlString) {
        parse(yamlString);
    }

    private CiYaml parse(String yamlString) {
        Constructor constructor = new Constructor(CiYaml.class);
        constructor.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<? extends Object> type, String name) throws IntrospectionException {
                if ( name.indexOf('-') > -1 ) {
                    name = Strings.toCamelCase(name, '-');
                }
                return super.getProperty(type, name);
            }
        });

        Yaml yaml = new Yaml(constructor);

        return yaml.loadAs(yamlString, CiYaml.class);
    }*/

    public List<JobAttributes> getJobsAttributes() throws ValidationError {
        List<JobEntry> jobEntries = ciConfig.getJobEntries();

        List<JobAttributes> jobAttributesList = new LinkedList<>();
        JobAttributes jobAttributes;
        for (JobEntry jobEntry : jobEntries) {
            jobAttributes = new JobAttributes();
            jobAttributes.setName(jobEntry.getName());
            jobAttributes.setStage(jobEntry.getStageEntry());
            jobAttributes.setJobWhen(jobEntry.getWhen());

            jobAttributesList.add(jobAttributes);
        }

        return jobAttributesList;

        /*
        job = @jobEntries[name.to_sym] || {}
      {
        stage_idx: @stages.index(job[:stage]),
        stage: job[:stage],
        commands: job[:commands],
        tag_list: job[:tags] || [],
        name: job[:name].to_s,
        allow_failure: job[:ignore],
        when: job[:when] || 'on_success',
        environment: job[:environment_name],
        coverage_regex: job[:coverage],
        yaml_variables: yaml_variables(name),
        options: {
          image: job[:image],
          services: job[:services],
          artifactsFile: job[:artifactsFile],
          cache: job[:cache],
          dependencies: job[:dependencies],
          after_script: job[:after_script],
          environment: job[:environment],
        }.compact
      }
         */

    }

}
