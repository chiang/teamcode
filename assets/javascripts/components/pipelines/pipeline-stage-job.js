/* global Flash */
import axios from 'axios';
import canceledSvg from 'icons/_icon_status_canceled.svg';
import createdSvg from 'icons/_icon_status_created.svg';
import failedSvg from 'icons/_icon_status_failed.svg';
import manualSvg from 'icons/_icon_status_manual.svg';
import pendingSvg from 'icons/_icon_status_pending.svg';
import runningSvg from 'icons/_icon_status_running.svg';
import skippedSvg from 'icons/_icon_status_skipped.svg';
import successSvg from 'icons/_icon_status_success.svg';
import warningSvg from 'icons/_icon_status_warning.svg';
import playSvg from 'icons/_icon_action_play.svg';
import cancelSvg from 'icons/_icon_action_cancel.svg';
import retrySvg from 'icons/_icon_action_retry.svg';

const svgsDictionary = {
  icon_status_canceled: canceledSvg,
  icon_status_created: createdSvg,
  icon_status_failed: failedSvg,
  icon_status_manual: manualSvg,
  icon_status_pending: pendingSvg,
  icon_status_running: runningSvg,
  icon_status_skipped: skippedSvg,
  icon_status_success: successSvg,
  icon_status_warning: warningSvg,
  icon_action_play: playSvg,
  icon_action_retry: retrySvg,
  icon_action_cancel: cancelSvg,
};

export default {

  props: {
    job: {
      type: Object,
      required: true,
    },
  },
  computed: {
    jobButtonClass() {
      return `ci-status-icon ci-status-icon-${this.job.status.group}`;
    },
    icon() {
      return svgsDictionary[this.job.status.icon];
    },
    actionIcon() {
      if (this.job.status.hasAction)
        return svgsDictionary[this.job.status.jobAction.icon];
      else
        return '';
    },
  },
  methods: {
    executeJobAction(e) {
      axios.post(this.job.actionPath)
	      .then((response) => {
	          location.reload();
	      });
    }
  },
  template: `
    <li>
    <a class="mini-pipeline-graph-dropdown-item"
       data-toggle="tooltip"
       data-title="chiang - manual play action"
       :href="job.path"
       data-original-title="" title="">
      <span :class="jobButtonClass" v-html="icon"></span>
      <span class="ci-build-text">{{job.name}}</span>
    </a>
    <a v-if="job.status.hasAction" class="ci-action-icon-wrapper js-ci-action-icon"
       data-toggle="tooltip"
       data-title=""
       href="#!"
       @click="executeJobAction"
       rel="nofollow"
       data-method="post" v-html="actionIcon">
    </a>
    </li>
  `,
};
