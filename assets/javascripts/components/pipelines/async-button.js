//import '~/flash';
import eventHub from './event-hub';

export default {
  props: {
    endpoint: {
      type: String,
      required: true,
    },

    service: {
      type: Object,
      required: true,
    },

    title: {
      type: String,
      required: true,
    },

    icon: {
      type: String,
      required: true,
    },

    cssClass: {
      type: String,
      required: true,
    },

    confirmActionMessage: {
      type: String,
      required: false,
    },
  },

  data() {
    return {
      isLoading: false,
    };
  },

  computed: {
    iconClass() {
      return `fa fa-${this.icon}`;
    },

    buttonClass() {
      //return `btn has-tooltip ${this.cssClass}`;
      return `btn ${this.cssClass}`;
    },
  },

  methods: {
    onClick() {
      if (this.confirmActionMessage && confirm(this.confirmActionMessage)) {
        this.makeRequest();
      } else if (!this.confirmActionMessage) {
        this.makeRequest();
      }
    },

    makeRequest() {
      this.isLoading = true;

      this.service.postAction(this.endpoint)
        .then(() => {
          this.isLoading = false;
          eventHub.$emit('refreshPipelines');
        })
        .catch(() => {
          this.isLoading = false;
          new Flash('An error occured while making the request.');
        });
    },
  },
  template: `
    <button
      type="button"
      @click="onClick"
      :class="buttonClass"
      :title="title"
      :aria-label="title"
      data-container="body"
      data-placement="top"
      :disabled="isLoading"
    >
      <i :class="iconClass" aria-hidden="true"></i>
      <i class="fa fa-spinner fa-spin" aria-hidden="true" v-if="isLoading"></i>
    </button>
  `,
};

