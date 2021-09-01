import Vue from 'vue';
import axios from 'axios';
import _ from 'lodash';

import UsersList from './users-list';

$(() => new Vue({
  el: document.querySelector('#admin-user-list'),
  data() {
    return {
      isLoading: false,
      userListView: null,
      users: null,
      searchQuery: null,
      sortKey: '',
      reverse: false,
    };
  },
  components: {
    'users-list': UsersList
  },
  computed: {
    shouldRenderEmptyStates() {
      return this.userListView != null && this.userListView.users.length == 0;
    },
    shouldRenderResults() {
      return !this.isLoading && this.userListView != null;
    },
    shouldRenderNoUsersMessage() {
      return !this.isLoading && this.userListView != null && !this.userListView.users.length;
    },
    shouldRenderUserList() {
      return !this.isLoading && this.users != null && this.users.length > 0;
    },
    filteredUsers() {
      var query = this.searchQuery;
      if (query == null || query.trim().length == 0) {
        return this.users;
        //return _.orderBy(this.users, [this.sortKey], ['asc']);
      }
      else {
        return this.users.filter(function (user) {
          return user.name.toLowerCase().indexOf(query.toLowerCase()) >= 0 ||
            user.fullName.toLowerCase().indexOf(query.toLowerCase()) >= 0 ||
            user.email.toLowerCase().indexOf(query.toLowerCase()) >= 0;
        });
      }
    },
  },
  created() {
    this.isLoading = true;
  },
  mounted() {
    const filter = tc.utils.getParameterByName('filter');
    const url = filter == null ? '/admin/users.json' : '/admin/users.json?filter=' + filter;
    setTimeout(()=>{
      axios.get(url)
      .then((response)=>{
        this.isLoading = false;
        this.userListView = response.data;
        this.users = response.data.users;
      })
      .catch((error)=>{
        //TODO error handling
        this.isLoading = false;
      });
    }, 500);
  },
  methods: {
    sortBy(e) {
      var _this = $(e.target);
      var _sortKey = _this.val();
      //this.reverse = this.sortKey == sortKey? !this.reverse : false;
      this.sortKey = _sortKey;
    },
  },
  template: `
  <div>
    <div class="realtime-loading" v-if="isLoading" style="text-align: center">
      <i class="fa fa-spinner fa-spin" aria-hidden="true" />
    </div>
	  <div v-if="shouldRenderResults" class="top-area">
			<div class="prepend-top-default">
	      <form action="/admin/users" accept-charset="UTF-8" method="get">
	        <div class="search-holder">
		        <div class="search-field-holder">
	            <input type="search" id="search_query" placeholder="아이디, 이름, 이메일 등으로 찾아보세요." class="form-control search-text-input js-search-input" spellcheck="false" v-model="searchQuery" autocomplete="off" />
	            <i class="fa fa-search search-icon"></i>
		        </div>
		        <!-- div class="dropdown">
		          <select class="form-control" @change="sortBy">
		            <option value="fullName">이름</option>
		            <option value="">Recent sign in</option>
		            <option>Oldest sign in</option>
		            <option>마지막 추가 순</option>
		            <option>최근 추가 순</option>
		            <option>최근 업데이트 순</option>
		          </select>
		        </div -->
		        <a class="btn btn-new btn-search" href="/admin/users?create">새 사용자 추가</a>
		      </div>
	      </form>
	    </div>
	    <div class="nav-block">
	      <ul class="nav-links wide scrolling-tabs white scrolling-tabs">
	        <li :class="[userListView.filter == null ? 'active' : '']">
            <a href="/admin/users">Active<small class="badge">{{userListView.activeUsersCount}}</small></a>
	        </li>
	        <li :class="[userListView.filter == 'admins' ? 'active' : '']">
            <a href="/admin/users?filter=admins">Admins<small class="badge">{{userListView.adminsCount}}</small></a>
	        </li>
	        <li :class="[userListView.filter == 'blocked' ? 'active' : '']">
            <a href="/admin/users?filter=blocked">Blocked<small class="badge">{{userListView.blockedUsersCount}}</small></a>
	        </li>
	      </ul>
	    </div>
		</div>
	  <ul class="flex-list content-list">
		  <li v-if="shouldRenderEmptyStates">
				<div class="nothing-here-block">사용자가 없습니다.</div>
		  </li>
		  <template v-if="shouldRenderUserList" v-for="user in filteredUsers">
	      <li is="users-list" :user="user" class="flex-row"></li>
	    </template>
		</ul>
	</div>
  `
}));