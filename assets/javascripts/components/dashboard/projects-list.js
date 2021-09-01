import ProjectMembers from './project-members';

/**
 * Pipelines Table Component.
 *
 * Given an array of objects, renders a table.
 */
export default {
  props: {
    project: {
      type: Object,
      required: true,
    },
  },
  computed: {
    shouldRenderProjectDescription() {
      return this.project.description != null && this.project.description.trim().length > 0;
    },
    shouldRenderProgrammingLanguage() {
      return this.project.programmingLanguage != null;
    },
    shouldRenderLastCommit() {
      return false;
    },
    projectAvatar() {
      if (this.project.avatarFileName != null) {
        return `<img class="avatar project-avatar" src="/avatar/projects/${this.project.path}/${this.project.avatarFileName}"/>`;
      }
      else {
        return `<img class="avatar project-avatar" src="/avatar/langs/${this.project.programmingLanguageIcon}.svg"/>`;
      }
    },
    projectLink() {
      return `/projects/${this.project.path}`;
    },
  },
  components: {
    'project-members': ProjectMembers,
  },
  template: `
    <li class="project-row">
      <div class="controls">
        <span is="project-members" :members="project.members">
        </span>
      </div>
      <div class="title">
        <a :href="projectLink">
          <div class="dash-project-avatar">
            <div class="avatar-container project-avatar-container s40" v-html="projectAvatar">
            </div>
          </div>
          <span class="project-full-name"><span class="project-name filter-title">{{project.name}}</span></span>
        </a>
      </div>
      <div v-if="shouldRenderProjectDescription">
         <p dir="auto">{{project.description}}</p>
      </div>
      <div class="project-annotations">
      </div>
    </li>
  `,
};