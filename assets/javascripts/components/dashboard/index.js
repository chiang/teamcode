import Vue from 'vue';
import axios from 'axios';

import ProjectsList from './projects-list';

$(() => new Vue({
  el: document.querySelector('#my-projects'),
  data() {
    return {
      searchQuery: '',
      projects: [],
      isLoading: false,
    };
  },
  components: {
    'projects-list': ProjectsList
  },
  computed: {
    hasProjects() {
      return this.projects != null && this.projects.length > 0;
    },
    shouldRenderEmptyState() {
      return !this.isLoading &&
        !this.projects.length;
    },
    filteredProjects() {
      var query = this.searchQuery;
      if (query == null || query.trim().length == 0) {
        return this.projects;
      }
      else {
        return this.projects.filter(function (project) {
          return project.name.toLowerCase().indexOf(query.toLowerCase())>=0;
          //return Object.keys(row).some(function (key) {
          //  return String(row[key]).toLowerCase().indexOf(filterKey) > -1
          //})
        });
      }
      //return this.projects.filter(function(project){project.name.toLowerCase().indexOf(query.toLowerCase())>=0;});
    },
  },
  created() {
    //FIXME 이렇게 안 해주면 모든 페이지에서 이 코드가 호출이 된다. 해당 Element 가 없으면 실행이 안 될 줄 알았는데 그게 아니다.
    if ($('#my-projects').length > 0) {
	    this.isLoading = true;
	    setTimeout(()=>{
	      axios.get('/projects.json')
	      .then((response)=>{
	        //location.href = data.origin;
	        this.isLoading = false;
	        this.projects = response.data;
	      })
	      .catch((error)=>{
	        //TODO error handling
	        this.isLoading = false;
	      });
	    }, 100);
	  }
  },
  template: `
    <div>
      <input class="form-control" autocomplete="off" spellcheck="false" placeholder="프로젝트 이름으로 검색해 보세요." v-model="searchQuery"/>
      <div v-if="isLoading">
      </div>
      <div v-if="hasProjects" class="projects-list-holder prepend-top-30">
	      <ul class="projects-list content-list">
	        <template v-for="project in filteredProjects" v-bind:model="project">
	          <li is='projects-list' :project="project" class="project-row"></li>
	        </template>
	      </ul>
	    </div>
      <div v-if="shouldRenderEmptyState" class="nothing-here-block">No projects found</div>
    </div>
  `,
}));
