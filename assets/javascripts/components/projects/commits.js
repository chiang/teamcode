import Vue from 'vue';
import axios from 'axios';
import queryString from 'query-string';
import localTimeAgo from '../../utils/datetime-utils';
import dateFormat from '../../libs/date.format';
import '../../libs/initial.js';

const CommitRow = {
  template: `
    <tr class="media-content commits-row">
      <td>
        <a href="#!">
          <img v-if="shouldRenderInitialIcon" :data-name="commit.user.fullName" class="avatar s20 profile"/>
          <img v-else :alt="commit.author" class="avatar s20 hidden-xs has-tooltip" data-container="body" :src="imageUrl"/>
        </a>
        <a class="commit-author-link has-tooltip" :title="commit.user != null ? commit.user.email : '시스템에 없는 사용자'" href="#!">{{commit.author}}</a>
      </td>
      <td>
        <a :href="commitUrl" class="commit-short-id">{{revision}}</a>
      </td>
      <td>
        <span v-html="message"></span>
      </td>
      <td>
        <time class="js-timeago" :datetime="createdAt" :title="createdAtTooltip" data-toggle="tooltip" data-placement="top" data-container="body"></time>
      </td>
      <td class="text-center">
        <a class="btn btn-secondary" title="해당 리비전에 대한 소스 목록 보기">B</a>
      </td>
    </tr>
  `,
  props: ['groupPath', 'projectPath', 'commit', 'issueLinkEnabled', 'issueLinkUrl', 'issueRegexp'],
  computed: {
    revision() {
      return `r${this.commit.revision}`;
    },
    commitUrl() {
      return `/${this.groupPath}/${this.projectPath}/commits/${this.commit.revision}`;
    },
    shouldRenderInitialIcon() {
      return this.commit.user != null && this.commit.user.avatarFileName == null;
    },
    imageUrl() {
      if (this.commit.user == null) {
        return `/avatar/users/${this.commit.author}/${this.commit.author}`;
      }
      else if (this.commit.user != null && this.commit.user.avatarFileName != null) {
        return `/avatar/users/${this.commit.user.name}/${this.commit.user.avatarFileName}`;
      }
      else {
        return '';
      }
    },
    createdAt() {
      //yyyy-MM-dd''T''HH:mm:ssZ ISO 8601
      return new Date(this.commit.createdAt).toISOString();
      //return dateFormat(this.commit.createdAt, 'mmm d, yyyy h:MMtt Z');
    },
    createdAtTooltip() {
      return dateFormat(this.commit.createdAt, 'yyyy년 mm월 dd일 HH시 MM분');
    },
    message() {
      if (this.issueLinkEnabled && this.issueLinkUrl) {

        var pattern = new RegExp(this.issueRegexp);
        var match = pattern.exec(this.commit.message);

        if (match !== null) {
          var resolvedLink = this.issueLinkUrl.replace(':id', match[1]);
          return this.commit.message.replace(pattern, `<a href="${resolvedLink}" target="_blank">${match[0]}</a>`);
        } else {
          return this.commit.message;
        }
      }
      else {
        return this.commit.message;
      }
    },
  },
  mounted() {
    this.$nextTick(function() {
      $('img.profile').initial();
      localTimeAgo('.js-timeago');
    });
  },
  watch: {
    'commit': function(val, oldVal) {
      //$('img.profile').initial();
      this.$nextTick(function() {
        //$('img.profile').initial();
        //localTimeAgo('.js-timeago');
      });
    }
  },
}

const vueCommitsId = '#vue-commits';
if(document.querySelector(vueCommitsId)) {
  $(() => new Vue({
    el: document.querySelector(vueCommitsId),
    components: {
      'commit-row': CommitRow,
    },
    data() {
      const vmDataSet = document.querySelector(vueCommitsId).dataset;

      return {
        groupPath: vmDataSet.groupPath,
        projectPath: vmDataSet.projectPath,
        commitsResponse: {
          commits: {}
        },
        currentOffset: -1,
        loading: false,
      }
    },
    computed: {
      fetchUrl() {
        return `/${this.groupPath}/${this.projectPath}/commits.json?offset=${this.currentOffset}`;
      },
    },
    created() {
      const parsed = queryString.parse(location.hash);
      console.log(parsed);
      if (parsed.offset) {
        this.currentOffset = parsed.offset;
      }
      this.fetch(this.currentOffset);
    },
    methods: {
      fetch(offset) {
        this.loading = true;
        var vm = this;
        vm.currentOffset = offset;

        setTimeout(()=>{
          axios.get(vm.fetchUrl)
          .then((response)=>{
            vm.commitsResponse = response.data;
            vm.loading = false;
            var params = {'offset': offset};
            console.log(params);
            location.hash = queryString.stringify(params);
            //console.log(queryString.stringify(params));
          })
          .catch((error)=>{
            //TODO error handling
            vm.loading = false;
          });
        }, 500)
      },
    },
    template: `
      <div>
        <div v-if="loading">
          <div class="loading"><i class="fa fa-spinner fa-spin"></i></div>
        </div>
        <div v-else>
          <table class="table">
            <thead>
              <tr>
                <th style="width: 140px;">커밋한 사람</th>
                <th style="width: 110px;">커밋</th>
                <th>커밋 메시지</th>
                <th style="width: 110px;">날짜</th>
                <th class="text-center" style="width: 70px;">&nbsp;</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="commit in commitsResponse.commits.content"
                  :group-path="groupPath"
                  :project-path="projectPath"
                  :commit="commit"
                  :issue-link-enabled="commitsResponse.issueLinkEnabled"
                  :issue-link-url="commitsResponse.issueLinkUrl"
                  :issue-regexp="commitsResponse.regexp"
                  is="commit-row"></tr>
            </tbody>
          </table>
          <div class="gl-pagination" pagenum="1" count="44357" style="border-top: none;">
            <ul class="pagination clearfix">
              <li class="prev" :class="commitsResponse.commits.first ? 'disabled' : ''">
                <a v-if="commitsResponse.commits.first" href="#!">이전</a>
                <a v-else href="#!" @click="fetch(commitsResponse.prevOffset)">이전</a>
              </li>
              <li class="next" :class="commitsResponse.commits.last ? 'disabled' : ''">
                <a v-if="commitsResponse.commits.last" href="#!">다음</a>
                <a v-else href="#!" @click="fetch(commitsResponse.offset)">다음</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    `,
  }));
}
