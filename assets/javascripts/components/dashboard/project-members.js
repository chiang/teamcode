import '../../libs/initial.js';

/**
 * Pipelines Table Component.
 *
 * Given an array of objects, renders a table.
 */
export default {
  props: {
    members: {
      type: Array,
      required: true,
      default: () => ([]),
    },
  },
  computed: {
    projectMembers() {
	    var html = [];
	    var count = 2;
      const marginRight = 22;

	    if (this.members.length == 0) {
	      for (var i = 0; i < 3; i++) {
		      html.push('<span ');
	        html.push(' style="right: '); html.push(marginRight * count); html.push('px; z-index:'); html.push(count); html.push('"');
	        html.push('></span>');
	        count--;
        }
	    }
	    else {
	      var max = this.members.length == 3 ? 3 : 2;
	      for(var i = 0; i < this.members.length && i < max; i++) {
	        var member = this.members[i];
	        if (member.avatarFileName != null) {
	          html.push('<img ');
	          html.push('src="'); html.push('/avatar/users/'); html.push(member.name); html.push('/'); html.push(member.avatarFileName);
	          html.push('" style="right: '); html.push(marginRight * count); html.push('px; z-index:'); html.push(count); html.push('"');
	          html.push('/>');
	        }
	        else {
	          html.push('<img class="profile" ');
            html.push('data-name="'); html.push(member.fullName); html.push('" data-user-name="'); html.push(member.name);
            html.push('" style="right: '); html.push(marginRight * count); html.push('px; z-index:'); html.push(count); html.push('"');
            html.push('/>');
	        }

	        count--;
	      }

	      if (this.members.length < 3) {
	        for (var i = 0; i < (3 - this.members.length); i++) {
	          html.push('<span ');
	          html.push(' style="right: '); html.push(marginRight * count); html.push('px; z-index:'); html.push(count); html.push('"');
	          count--;
	          html.push('></span>');
	        }
	      }
	      else if (this.members.length > 3) {
	        html.push('<span ');
          html.push(' style="right: '); html.push(marginRight * count); html.push('px; z-index:'); html.push(count); html.push('"');
          html.push('>+'); html.push(this.members.length - 2);
          html.push('</span>');
          count--;
	      }
	    }

	    return html.join('');
    }
  },
  mounted() {
    $('.project-row .members img.profile').initial();
  },
  updated() {
    $('.project-row .members img.profile').initial();
  },
  template: `
    <span class="members" v-html="projectMembers"></span>
  `,
};