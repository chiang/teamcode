import PipelinesTableRowComponent from './pipelines-table-row';
/**
 * Pipelines Table Component.
 *
 * Given an array of objects, renders a table.
 */
export default {
  props: {
    pipelines: {
      type: Array,
      required: true,
      default: () => ([]),
    },
    service: {
      type: Object,
      required: true,
    },
  },
  components: {
    'pipelines-table-row-component': PipelinesTableRowComponent,
  },
  template: `
    <table class="table ci-table">
      <thead>
        <tr>
          <th class="js-pipeline-status pipeline-status">상태</th>
          <th class="pipeline-info">파이프라인</th>
          <th class="js-pipeline-commit pipeline-commit">Commit</th>
          <th class="js-pipeline-stages pipeline-stages">Stages</th>
          <th class="js-pipeline-date pipeline-date"></th>
          <th class="js-pipeline-actions pipeline-actions"></th>
        </tr>
      </thead>
      <tbody>
        <template v-for="model in pipelines"
          v-bind:model="model">
          <tr is="pipelines-table-row-component"
            :pipeline="model"
            :service="service"></tr>
        </template>
      </tbody>
    </table>
  `,
};