//https://github.com/thlorenz/brace

import AceEditorComponent from '../ace-editor';

export default {
  props: {
    template: {
      type: String,
      required: true,
    },

    count: {
      type: Object,
      required: true,
    },

    paths: {
      type: Object,
      required: true,
    },
  },
  created() {
  },
  components: {
    'code-editor': AceEditorComponent,
  },
  mounted() {
  },
  methods: {
    templateChanged(content) {
      this.$emit('template-changed', content)
    }
  },
  template: `
    <div class="pipeline-template">
      <div class="pipeline-template-header">teamcode-pipelines.yml</div>
      <code-editor editor-id="editorA" :content="template" v-on:change-content="templateChanged"></code-editor>
    </div>
  `,
};
