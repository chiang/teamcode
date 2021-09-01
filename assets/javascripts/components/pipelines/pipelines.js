import Vue from 'vue';
import Visibility from 'visibilityjs';

import PipelinesService from './services/pipelines-service';
import eventHub from './event-hub';
import EmptyState from './empty-state';
import PipelinesTableComponent from './pipelines-table';
import NavigationTabs from './navigation-tabs';
import TablePaginationComponent from '../table-pagination';
import Poll from '../../utils/poll';


export default {
  props: {
    store: {
      type: Object,
      required: true,
    },
  },
  components: {
    'pagination': TablePaginationComponent,
    'navigation-tabs': NavigationTabs,
    'pipelines-table-component': PipelinesTableComponent,
    'empty-state': EmptyState,
  },
  data() {
    const pipelinesData = document.querySelector('#pipelines-list-vue').dataset;
    return {
      endpoint: pipelinesData.endpoint,
      cssClass: pipelinesData.cssClass,
      helpPagePath: pipelinesData.helpPagePath,
      newPipelinePath: pipelinesData.newPipelinePath,
      canCreatePipeline: pipelinesData.canCreatePipeline,
      allPath: pipelinesData.allPath,
      pendingPath: pipelinesData.pendingPath,
      runningPath: pipelinesData.runningPath,
      finishedPath: pipelinesData.finishedPath,
      branchesPath: pipelinesData.branchesPath,
      tagsPath: pipelinesData.tagsPath,
      hasCi: pipelinesData.hasCi,
      ciLintPath: pipelinesData.ciLintPath,
      state: this.store.state,
      apiScope: 'all',
      pagenum: 1,
      isLoading: false,
      hasError: false,
      isMakingRequest: false,
    };
  },
  computed: {
    canCreatePipelineParsed() {
      //return gl.utils.convertPermissionToBoolean(this.canCreatePipeline);
    },
    scope() {
      const scope = tc.utils.getParameterByName('scope');
      return scope === null ? 'all' : scope;
    },
    shouldRenderErrorState() {
      return this.hasError && !this.isLoading;
    },
    /**
    * The empty state should only be rendered when the request is made to fetch all pipelines
    * and none is returned.
    *
    * @return {Boolean}
    */
    shouldRenderEmptyState() {
      return !this.isLoading &&
        !this.hasError &&
        !this.state.pipelines.length &&
        (this.scope === 'all' || this.scope === null);
    },
    /**
     * When a specific scope does not have pipelines we render a message.
     *
     * @return {Boolean}
     */
    shouldRenderNoPipelinesMessage() {
      return !this.isLoading &&
        !this.hasError &&
        !this.state.pipelines.length &&
        this.scope !== 'all' &&
        this.scope !== null;
    },
    shouldRenderTable() {
      return !this.hasError &&
        !this.isLoading && this.state.pipelines.length;
    },
    /**
    * Pagination should only be rendered when there is more than one page.
    *
    * @return {Boolean}
    */
    shouldRenderPagination() {
      return !this.isLoading &&
        this.state.pipelines.length &&
        this.state.pageInfo.total > this.state.pageInfo.perPage;
    },
    hasCiEnabled() {
      return this.hasCi !== undefined;
    },
    paths() {
      return {
        allPath: this.allPath,
        pendingPath: this.pendingPath,
        finishedPath: this.finishedPath,
        runningPath: this.runningPath,
        branchesPath: this.branchesPath,
        tagsPath: this.tagsPath,
      };
    },
    pageParameter() {
      return tc.utils.getParameterByName('page') || this.pagenum;
    },
    scopeParameter() {
      return tc.utils.getParameterByName('scope') || this.apiScope;
    },
  },
  created() {
    this.service = new PipelinesService(this.endpoint);
    const poll = new Poll({
      resource: this.service,
      method: 'getPipelines',
      data: { page: this.pageParameter, scope: this.scopeParameter },
      successCallback: this.successCallback,
      errorCallback: this.errorCallback,
      notificationCallback: this.setIsMakingRequest,
    });
    if (!Visibility.hidden()) {
      this.isLoading = true;
      poll.makeRequest();
    }
    Visibility.change(() => {
      if (!Visibility.hidden()) {
        poll.restart();
      } else {
        poll.stop();
      }
    });
    eventHub.$on('refreshPipelines', this.fetchPipelines);
  },
  beforeUpdate() {
    if (this.state.pipelines.length &&
        this.$children &&
        !this.isMakingRequest &&
        !this.isLoading) {
      //this.store.startTimeAgoLoops.call(this, Vue);
    }
  },
  beforeDestroyed() {
    eventHub.$off('refreshPipelines');
  },
  methods: {
    /**
     * Will change the page number and update the URL.
     *
     * @param  {Number} pageNumber desired page to go to.
     */
    change(pageNumber) {
      const param = tc.utils.setParamInURL('page', pageNumber);
      tc.utils.visitUrl(param);
      return param;
    },
    fetchPipelines() {
      if (!this.isMakingRequest) {
        this.isLoading = true;
        this.service.getPipelines({ scope: this.scopeParameter, page: this.pageParameter })
          .then(response => this.successCallback(response))
          .catch(() => this.errorCallback());
      }
    },
    successCallback(resp) {
      const response = {
        headers: resp.headers,
        body: resp.data,
      };
      this.store.storeCount(response.body.count);
      this.store.storePipelines(response.body.pipelines);
      this.store.storePagination(response.headers);
      this.isLoading = false;
    },
    errorCallback() {
      this.hasError = true;
      this.isLoading = false;
    },
    setIsMakingRequest(isMakingRequest) {
      this.isMakingRequest = isMakingRequest;
    },
  },
  template: `
    <div :class="cssClass">
      <div class="top-area scrolling-tabs-container inner-page-scroll-tabs" v-if="!isLoading && !shouldRenderEmptyState">
        <div class="fade-left">
          <i class="fa fa-angle-left" aria-hidden="true"></i>
        </div>
        <div class="fade-right">
          <i class="fa fa-angle-right" aria-hidden="true"></i>
        </div>
        <navigation-tabs
          :scope="scope"
          :count="state.count"
          :paths="paths" />
        <navigation-controls
          :new-pipeline-path="newPipelinePath"
          :has-ci-enabled="hasCiEnabled"
          :help-page-path="helpPagePath"
          :ciLintPath="ciLintPath"
          :can-create-pipeline="canCreatePipelineParsed " />
      </div>
      <div class="content-list pipelines">
        <div
          class="realtime-loading" v-if="isLoading">
          <i class="fa fa-spinner fa-spin" aria-hidden="true" />
        </div>
        <empty-state v-if="shouldRenderEmptyState" :help-page-path="helpPagePath" />
        <error-state v-if="shouldRenderErrorState" />
        <div
          class="blank-state blank-state-no-icon"
          v-if="shouldRenderNoPipelinesMessage">
          <h2 class="blank-state-title js-blank-state-title">파이프라인 정보가 없습니다.</h2>
        </div>
        <div
          class="table-holder"
          v-if="shouldRenderTable">
          <pipelines-table-component
            :pipelines="state.pipelines"
            :service="service"/>
        </div>

        <pagination
          v-if="shouldRenderPagination"
          :pagenum="pagenum"
          :change="change"
          :count="state.count.all"
          :pageInfo="state.pageInfo"/>

      </div>
    </div>
  `,
};