import axios from 'axios';

if(document.querySelector('#vue_issue_label_list')) {
	$(() => new Vue({
	  el: document.querySelector('#vue_issue_label_list'),
	  data() {
	    const vueData = document.querySelector('#vue_issue_label_list').dataset;

	    return {
	      endpoint: vueData.endpoint,
	      labels: null,
	      isLoading: false,
	    };
	  },
	  created() {
      this.isLoading = true;
    },
	  mounted() {
      axios.get(this.endpoint + '.json')
      .then((response)=>{
        this.isLoading = false;
        this.labels = response.data;
      })
      .catch((error)=>{
        //TODO error handling
        this.isLoading = false;
      });
    },
	  computed: {
	    starredLabels() {
        return !this.shouldRenderEmptyState ? this.labels.filter((label)=>{return label.starred;}) : [];
      },
	    otherLabels() {
	      return !this.shouldRenderEmptyState ? this.labels.filter((label)=>{return !label.starred;}) : [];
	    },
	    createDefaultLabelsUrl() {
	      return this.endpoint + '?createDefault';
	    },
	    shouldRenderEmptyState() {
	      return this.labels == null || this.labels.length == 0;
	    },
	    shouldRenderStarredLabels() {
        return !this.shouldRenderEmptyState && this.starredLabels.length > 0;
      },
	    shouldRenderOtherLabels() {
	      return !this.shouldRenderEmptyState && this.otherLabels.length > 0;
	    },
	  },
	  methods: {
	    star(label) {
	      label.starred = true;
	    },
	    unstar(label) {
	    	label.starred = false;
	    },
	  },
	  template: `
	  <div class="labels">
	    <div v-if="shouldRenderEmptyState">
	      <a :href="createDefaultLabelsUrl">기본 레이블 만들기</a>
	    </div>
	    <div v-if="shouldRenderStarredLabels" class="prioritized-labels">
        <h5 class="">Prioritized Labels</h5>
        <ul class="content-list list-unstyled">
          <li v-for="label in starredLabels">
            <span class="label-row">
              <div class="js-toggle-priority toggle-priority" data-dom-id="project_label_1258204" data-type="ProjectLabel" data-url="/baramboy/jandi-connector/labels/1258204/remove_priority">
                <button class="remove-priority btn has-tooltip" data-placement="top" title="Remove priority" @click="unstar(label)"><i aria-hidden="true" class="fa fa-star"></i></button>
              </div>
              <span class="label-name">
                <a href="#!">
                  <span class="label color-label" :style="{backgroundColor: label.color}">{{label.title}}</span>
                </a>
              </span>
            </span>
            <div class="pull-right hidden-xs hidden-sm hidden-md">
              <a class="btn btn-transparent btn-action" href="/baramboy/jandi-connector/issues?label_name%5B%5D=discussion">view open issues</a>
              <div class="label-subscription inline">
                <button class="js-subscribe-button label-subscribe-button btn btn-default" data-status="unsubscribed" data-url="/baramboy/jandi-connector/labels/1258205/toggle_subscription" type="button">
                  <span>Subscribe</span>
                  <i aria-hidden="true" class="fa fa-spinner fa-spin label-subscribe-button-loading"></i>
                </button>
              </div>
              <a title="" class="btn btn-transparent btn-action" data-toggle="tooltip" href="/baramboy/jandi-connector/labels/1258205/edit" data-original-title="Edit"><span class="sr-only">Edit</span>
                <i aria-hidden="true" class="fa fa-pencil-square-o"></i>
              </a><a title="" class="btn btn-transparent btn-action remove-row" data-confirm="Remove this label? Are you sure?" data-toggle="tooltip" rel="nofollow" data-method="delete" href="/baramboy/jandi-connector/labels/1258205" data-original-title="Delete"><span class="sr-only">Delete</span>
              <i aria-hidden="true" class="fa fa-trash-o"></i>
            </a></div>
          </li>
        </ul>
      </div>
      <div v-if="shouldRenderOtherLabels" class="other-labels">
        <h5 class="">Other Labels</h5>
        <ul class="content-list list-unstyled">
          <li v-for="label in otherLabels">
            <span class="label-row">
              <div class="js-toggle-priority toggle-priority" data-dom-id="project_label_1258204" data-type="ProjectLabel" data-url="/baramboy/jandi-connector/labels/1258204/remove_priority">
                <button class="add-priority btn has-tooltip" data-placement="top" title="" data-original-title="Prioritize" @click="star(label)"><i aria-hidden="true" class="fa fa-star-o"></i></button>
              </div>
              <span class="label-name">
                <a href="#!">
                  <span class="label color-label" :style="{backgroundColor: label.color}">{{label.title}}</span>
                </a>
              </span>
            </span>
            <div class="pull-right hidden-xs hidden-sm hidden-md">
              <a class="btn btn-transparent btn-action" href="/baramboy/jandi-connector/issues?label_name%5B%5D=discussion">view open issues</a>
              <div class="label-subscription inline">
                <button class="js-subscribe-button label-subscribe-button btn btn-default" data-status="unsubscribed" data-url="/baramboy/jandi-connector/labels/1258205/toggle_subscription" type="button">
                  <span>Subscribe</span>
                  <i aria-hidden="true" class="fa fa-spinner fa-spin label-subscribe-button-loading"></i>
                </button>
              </div>
              <a title="" class="btn btn-transparent btn-action" data-toggle="tooltip" href="/baramboy/jandi-connector/labels/1258205/edit" data-original-title="Edit"><span class="sr-only">Edit</span>
                <i aria-hidden="true" class="fa fa-pencil-square-o"></i>
              </a><a title="" class="btn btn-transparent btn-action remove-row" data-confirm="Remove this label? Are you sure?" data-toggle="tooltip" rel="nofollow" data-method="delete" href="/baramboy/jandi-connector/labels/1258205" data-original-title="Delete"><span class="sr-only">Delete</span>
              <i aria-hidden="true" class="fa fa-trash-o"></i>
            </a></div>
          </li>
        </ul>
      </div>
    </div>
	  `,
	}));
}