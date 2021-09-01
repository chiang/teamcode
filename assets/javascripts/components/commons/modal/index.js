
export default {
  template:
  `
  <transition name="modal">
    <div class="tc-modal-mask" v-show="show" @click.self="$emit('close')">
      <div class="tc-modal-container">
        <button type="button" class="tc-modal-close" @click="$emit('close')"><div class="off-screen">Close</div></button>
        <div class="tc-modal-header">
            <slot name="header"></slot>
        </div>
        <div class="tc-modal-body">
          <slot name="body">
          </slot>
          <div class="tc-modal-options">
            <slot name="footer"></slot>
          </div>
        </div>
      </div>
    </div>
  </transition>
  `,
  props: ['show'],
  data () {
      return {
          editor: Object,
          beforeContent: ''
      }
  },
  methods: {

  }
}