import AsyncButtonComponent from './async-button';
import PipelinesStatusComponent from './pipeline-status';
import PipelinesUrlComponent from './pipeline-url';
import CommitComponent from './pipeline-commit';
import PipelinesStageComponent from './pipeline-stage';
import PipelinesTimeagoComponent from './pipeline-time-ago';

/**
 * Pipeline table row.
 *
 * Given the received object renders a table row in the pipelines' table.
 */
export default {
  props: {
    pipeline: {
      type: Object,
      required: true,
    },
    service: {
      type: Object,
      required: true,
    },
  },
  components: {
    'async-button-component': AsyncButtonComponent,
    'td-pipeline-status': PipelinesStatusComponent,
    'td-pipeline-url': PipelinesUrlComponent,
    'td-time-ago': PipelinesTimeagoComponent,
    'commit-component': CommitComponent,
    'dropdown-stage': PipelinesStageComponent,
  },
  computed: {
    /**
     * If provided, returns the commit tag.
     * Needed to render the commit component column.
     *
     * This field needs a lot of verification, because of different possible cases:
     *
     * 1. person who is an author of a commit might be a GitLab user
     * 2. if person who is an author of a commit is a GitLab user he/she can have a GitLab avatar
     * 3. If GitLab user does not have avatar he/she might have a Gravatar
     * 4. If committer is not a GitLab User he/she can have a Gravatar
     * 5. We do not have consistent API object in this case
     * 6. We should improve API and the code
     *
     * @returns {Object|Undefined}
     */
    commitAuthor() {

      return this.pipeline.commit.author;
    },
    commitRevision() {
      return this.pipeline.commit.revision;
    },
    /**
     * If provided, returns the commit url.
     * Needed to render the commit component column.
     *
     * @returns {String|Undefined}
     */
    commitUrl() {
      if (this.pipeline.commit &&
        this.pipeline.commit.commitPath) {
        return this.pipeline.commit.commitPath;
      }
      return undefined;
    },
    /**
     * If provided, returns the commit title.
     * Needed to render the commit component column.
     *
     * @returns {String|Undefined}
     */
    commitTitle() {
      if (this.pipeline.commit &&
        this.pipeline.commit.message) {
        return this.pipeline.commit.message;
      }
      return undefined;
    },
  },
  template: `
    <tr>
      <td-pipeline-status :pipeline="pipeline"/>
      <td-pipeline-url :pipeline="pipeline"></td-pipeline-url>
      <td>
        <commit-component
          :revision="commitRevision"
          :commit-url="commitUrl"
          :title="commitTitle"
          :author="commitAuthor"/>
      </td>
      <td class="stage-cell">
        <div class="stage-container dropdown js-mini-pipeline-graph" v-if="pipeline.details.stages.length > 0"
          v-for="stage in pipeline.details.stages">
          <dropdown-stage :stage="stage" :pipeline-id="pipeline.id"/>
        </div>
      </td>
      <td-time-ago :pipeline="pipeline"/>
      <td class="pipeline-actions">
        <div class="pull-right btn-group">
          <pipelines-actions-component
            v-if="pipeline.details.manualActions.length"
            :actions="pipeline.details.manualActions"
            :service="service" />

          <pipelines-artifacts-component
            v-if="pipeline.details.artifacts.length"
            :artifacts="pipeline.details.artifacts" />

          <async-button-component
            v-if="pipeline.flags.retryable"
            :service="service"
            :endpoint="pipeline.retryPath"
            css-class="js-pipelines-retry-button btn-default btn-retry"
            title="Retry"
            icon="repeat" />

          <async-button-component
            v-if="pipeline.flags.cancelable"
            :service="service"
            :endpoint="pipeline.cancelPath"
            css-class="js-pipelines-cancel-button btn-remove"
            title="Cancel"
            icon="remove"
            confirm-action-message="Are you sure you want to cancel this pipeline?" />
        </div>
      </td>
    </tr>
  `,
};