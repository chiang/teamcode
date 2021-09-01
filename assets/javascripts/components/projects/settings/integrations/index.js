import Vue from 'vue';
import axios from 'axios';

import IntegrationCustomIssueTracker from './integration-custom-issue-tracker';
import IntegrationEmailsOnCommit from './integration-emails-on-commit';
import IntegrationPipelineEmails from './integration-pipeline-emails';

const vueCustomIssueTrackerConfigId = '#vue-integration-service-config';
if(document.querySelector(vueCustomIssueTrackerConfigId)) {
  $(() => new Vue({
    el: document.querySelector(vueCustomIssueTrackerConfigId),
    components: {
      'integration-custom-issue-tracker': IntegrationCustomIssueTracker,
      'integration-emails-on-commit': IntegrationEmailsOnCommit,
      'integration-pipeline-emails': IntegrationPipelineEmails,
    },
    data() {
      const vmDataSet = document.querySelector(vueCustomIssueTrackerConfigId).dataset;

      return {
        groupPath: vmDataSet.groupPath,
        projectPath: vmDataSet.projectPath,
        serviceName: vmDataSet.serviceName,
        settings: {
          properties: {}
        },
      }
    },
    computed: {
      listUrl() {
        return `/${this.groupPath}/${this.projectPath}/admin/settings/integrations`;
      },
      fetchUrl() {
        return `/${this.groupPath}/${this.projectPath}/admin/settings/integrations/${this.serviceName}.json`;
      }
    },
    created() {
      console.log('service name: ' + this.serviceName);
      var vm = this;
      axios.get(this.fetchUrl)
      .then((response)=>{
        //location.href = data.origin;
        vm.settings = response.data.settings;
      })
      .catch((error)=>{
        console.log(error);
        //TODO error handling
        //this.isLoading = false;
      });
    },
    template: `
      <div>
        <integration-custom-issue-tracker v-if="serviceName === 'custom_issue_tracker'" :settings="settings" :list-url="listUrl"></integration-custom-issue-tracker>
        <integration-emails-on-commit v-if="serviceName === 'emails_on_commit'" :settings="settings" :list-url="listUrl"></integration-emails-on-commit>
        <integration-bugzilla v-if="serviceName === 'bugzilla'" :settings="settings" :list-url="listUrl"></integration-bugzilla>
        <integration-redmine v-if="serviceName === 'redmine'" :settings="settings" :list-url="listUrl"></integration-redmine>
        <integration-pipeline-emails v-if="serviceName === 'pipeline_emails'" :settings="settings" :list-url="listUrl"></integration-pipeline-emails>
      </div>
    `,
  }));
}

