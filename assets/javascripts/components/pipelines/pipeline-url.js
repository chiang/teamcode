export default {
  props: [
    'pipeline',
  ],
  computed: {
    user() {
      return !!this.pipeline.user;
    },
  },
  template: `
    <td>
      <a
        :href="pipeline.path"
        class="js-pipeline-url-link">
        <span class="pipeline-id">#{{pipeline.id}}</span>
      </a>
      <span
        v-if="pipeline.flags.yamlErrors"
        class="js-pipeline-url-yaml label label-danger has-tooltip"
        :title="pipeline.yamlErrors"
        :data-original-title="pipeline.yamlErrors">
        설정 오류
      </span>
      <span
        v-if="pipeline.flags.stuck"
        class="js-pipeline-url-stuck label label-warning">
        stuck
      </span>
    </td>
  `,
};
