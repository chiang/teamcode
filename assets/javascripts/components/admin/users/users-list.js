
/**
 * Pipelines Table Component.
 *
 * Given an array of objects, renders a table.
 */
export default {
  props: {
    user: {
      type: Object,
      required: true,
    },
  },
  computed: {
		shouldShowAvatar() {
			return this.user.avatarFileName != null;
		},
		avatarPath() {
			return `/avatar/users/${this.user.name}/${this.user.avatarFileName}`;
		},
		detailPath() {
			return `/admin/users/${this.user.name}`;
		},
		editPath() {
			return `/admin/users/${this.user.name}?edit`;
		},
		blockPath() {
			return `/admin/users/${this.user.name}/block`;
		},
		unblockPath() {
      return `/admin/users/${this.user.name}/unblock`;
    },
  },
  mounted() {
    $(this.$el).find('.user-avatar .avatar.profile').initial();
  },
  updated() {
    $(this.$el).find('.user-avatar .avatar.profile').initial();
  },
  methods: {
    toggleBlock(e) {
      var _this = $(e.target);
      if(confirm(_this.data('confirm'))) {
        $('#form-block-unblock-user').attr('action', _this.data('action')).submit();

        return false;
      }
      else {
        return false;
      }
    },
  },
  template: `
    <li class="flex-row">
      <div class="user-avatar">
        <img v-if="shouldShowAvatar" class="avatar" :src="avatarPath"/>
        <img v-else class="avatar profile" :data-name="user.fullName"/>
      </div>
      <div class="row-main-content">
        <div class="user-name row-title str-truncated-100">
          <a :href="detailPath">{{user.fullName}}</a>
        </div>
        <span v-if="user.admin" class="label label-success">관리자</span>
        <span v-if="user.me" class="label label-success prepend-left-5">나</span>
        <div class="row-second-line str-truncated-100">
          <a :href="'mailto:' + user.email">{{user.email}}</a>
        </div>
      </div>
      <div class="controls">
        <a class="btn" :href="editPath">수정하기</a>
        <div v-if="!user.admin" class="dropdown inline">
          <a class="dropdown-new btn btn-default" data-toggle="dropdown" href="#" id="project-settings-button">
            <i class="fa fa-cog"></i>
            <i class="fa fa-caret-down"></i>
          </a>
          <ul class="dropdown-menu dropdown-menu-align-right">
            <li>
              <a v-if="user.state == 'ACTIVE'" href="#!" data-confirm="사용자가 접속 차단됩니다. 정말 차단하시겠습니까?" rel="nofollow" @click="toggleBlock" :data-action="blockPath">사용자 차단하기</a>
              <a v-if="user.state == 'BLOCKED'" href="#!" data-confirm="차단된 사용자를 차단 해제하시겠습니까?" rel="nofollow" @click="toggleBlock" :data-action="unblockPath">차단 해제하기</a>
            </li>
          </ul>
        </div>
      </div>
  </li>
  `,
};