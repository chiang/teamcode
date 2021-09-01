import * as ace from 'brace';
import 'brace/mode/yaml';
import 'brace/theme/monokai';
import 'brace/theme/github';

export default {
  template: '<div :id="editorId" class="code-editor" style="height: 330px"></div>',
  props: ['editorId', 'content', 'lang', 'theme'],
  data () {
      return {
          editor: Object,
          beforeContent: ''
      }
  },
  watch: {
      'content' (value) {
          if (this.beforeContent !== value) {
              this.editor.setValue(value, 1)
          }
      }
  },
  mounted () {
    const lang = this.lang || 'yaml'
    const theme = this.theme || 'github'
    //const theme = this.theme || 'monokai'

    this.editor = ace.edit(this.editorId)
    //this.editor.config.set("modePath", "Scripts/Ace");
    this.editor.setValue(this.content, 1)

    // mode-xxx.js or theme-xxx.jsがある場合のみ有効
    this.editor.getSession().setMode(`ace/mode/${lang}`)
    this.editor.session.setOptions({
      tabSize: 2,
      useSoftTabs: true
    });

    //this.editor.getSession().setMode(`ace/mode/sql`)
    this.editor.setTheme(`ace/theme/${theme}`)

    this.editor.on('change', () => {
        this.beforeContent = this.editor.getValue()
      this.$emit('change-content', this.editor.getValue())
    })
    //$('#' + this.editorId).height($('.top-window').outerHeight() + 'px');
    //this.editor.resize()
  }
}