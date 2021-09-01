//import commitIconSvg from 'icons/_icon_commit.svg';

export default {
  props: {
    author: {
      type: String,
      required: false,
      default: '',
    },
  },

  computed: {

  },
  methods: {
    preview() {

    },
  },
  template: `
    <div class="md-area">
      <div class="md-header">
        <ul class="nav-links clearfix">
          <li class="active"><a class="js-md-write-button" href="#md-write-holder" tabindex="-1">Write</a></li>
          <li class=""><a class="js-md-preview-button" href="#md-preview-holder" tabindex="-1" @click="preview">Preview</a></li>
          <li class="pull-right">
            <div class="toolbar-group">
              <button type="button" class="toolbar-btn js-md has-tooltip hidden-xs" tabindex="-1" data-md-tag="**" data-container="body" title="" aria-label="Add bold text" data-original-title="Add bold text"><i aria-hidden="true" class="fa fa-bold fa-fw"></i></button>
              <button type="button" class="toolbar-btn js-md has-tooltip hidden-xs" tabindex="-1" data-md-tag="*" data-container="body" title="" aria-label="Add italic text" data-original-title="Add italic text"><i aria-hidden="true" class="fa fa-italic fa-fw"></i></button>
              <button type="button" class="toolbar-btn js-md has-tooltip hidden-xs" tabindex="-1" data-md-tag="> " data-md-prepend="true" data-container="body" title="" aria-label="Insert a quote" data-original-title="Insert a quote"><i aria-hidden="true" class="fa fa-quote-right fa-fw"></i></button>
              <button type="button" class="toolbar-btn js-md has-tooltip hidden-xs" tabindex="-1" data-md-tag="\`" data-md-block="\`\`\`" data-container="body" title="Insert code" aria-label="Insert code"><i aria-hidden="true" class="fa fa-code fa-fw"></i></button>
              <button type="button" class="toolbar-btn js-md has-tooltip hidden-xs" tabindex="-1" data-md-tag="* " data-md-prepend="true" data-container="body" title="" aria-label="Add a bullet list" data-original-title="Add a bullet list"><i aria-hidden="true" class="fa fa-list-ul fa-fw"></i></button>
              <button type="button" class="toolbar-btn js-md has-tooltip hidden-xs" tabindex="-1" data-md-tag="1. " data-md-prepend="true" data-container="body" title="" aria-label="Add a numbered list" data-original-title="Add a numbered list"><i aria-hidden="true" class="fa fa-list-ol fa-fw"></i></button>
              <button type="button" class="toolbar-btn js-md has-tooltip hidden-xs" tabindex="-1" data-md-tag="* [ ] " data-md-prepend="true" data-container="body" title="" aria-label="Add a task list" data-original-title="Add a task list"><i aria-hidden="true" class="fa fa-check-square-o fa-fw"></i></button>
            </div>
            <div class="toolbar-group">
              <button aria="{:label=>&quot;Go full screen&quot;}" class="toolbar-btn js-zen-enter has-tooltip" data-container="body" tabindex="-1" title="" type="button" data-original-title="Go full screen">
                <i aria-hidden="true" class="fa fa-arrows-alt fa-fw"></i>
              </button>
            </div>
          </li>
        </ul>
      </div>
      <div class="md-write-holder">
        <div class="zen-backdrop div-dropzone-wrapper">
          <textarea required="required" autocomplete="off" class="note-textarea markdown-area form-control js-tfm-input" name="description" id="milestone_description"></textarea>
        </div>
        <div class="comment-toolbar clearfix">
          <div class="toolbar-text"><a target="_blank" tabindex="-1" href="/help/user/markdown">Markdown</a> is supported</div>
          <button class="toolbar-button markdown-selector" tabindex="-1" type="button"><i aria-hidden="true" class="fa fa-file-image-o toolbar-button-icon"></i> Attach a file</button>
        </div>
      </div>
      <div class="md md-preview-holder js-md-preview hide md-preview" data-url="/baramboy/jandi-connector/preview_markdown">
      </div>
    </div>
  `,
};
