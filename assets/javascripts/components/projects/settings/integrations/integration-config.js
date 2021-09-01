


export default {
  template: `
    <div>
      <div class="integration-config-header">
        <slot name="header"></slot>
      </div>
      <div class="integration-config-body">
        <slot name="body"></slot>
      </div>
    </div>
  `,
  props: ['settings'],
  components: {
  },
}