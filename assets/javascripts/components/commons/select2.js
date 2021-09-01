
export default {
  props: {
    options: {
      type: Array,
      required: true,
    },
    minimumResultsForSearch: {
      type: Number,
      default: 1
    },
    selectPlaceholder: {
      type: String,
      required: false
    },
    value: {
      type: Object,
      required: true,
    },
  },
  template: `
    <select>
      <slot></slot>
    </select>
  `,
  mounted: function () {
    var vm = this
    $(this.$el)
      // init select2
      .select2({ data: this.options, minimumResultsForSearch: this.minimumResultsForSearch, placeholder: this.selectPlaceholder })
      .val(this.value)
      .trigger('change')
      // emit event on change.
      .on('change', function () {
        vm.$emit('select2-changed', this.value)
      })
  },
  watch: {
    value: function (value) {
      // update value
      console.log('updating...');
      $(this.$el).val(value).trigger('change');
    },
    options: function (options) {
      // update options
      $(this.$el).empty().select2({ data: options, minimumResultsForSearch: this.minimumResultsForSearch, placeholder: this.selectPlaceholder })
    }
  },
  destroyed: function () {
    $(this.$el).off().select2('destroy')
  }
}