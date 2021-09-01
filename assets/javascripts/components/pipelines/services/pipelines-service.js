/* eslint-disable class-methods-use-this */
import Vue from 'vue';
import axios from 'axios';

export default class PipelinesService {

  /**
  * Commits and merge request endpoints need to be requested with `.json`.
  *
  * The url provided to request the pipelines in the new merge request
  * page already has `.json`.
  *
  * @param  {String} root
  */
  constructor(root) {
    //let endpoint;

    if (root.indexOf('.json') === -1) {
      this.endpoint = `${root}.json`;
    } else {
      this.endpoint = root;
    }
  }

  getPipelines(data = {}) {
    //const { scope, page } = data;
    var params = new URLSearchParams();
    params.append('scope', data.scope);
    params.append('page', data.page);

    return axios.get(this.endpoint, {params: { scope: data.scope, page: data.page }});

    //return this.pipelines.get({ scope, page });
  }

  /**
   * Post request for all pipelines actions.
   * Endpoint content type needs to be:
   * `Content-Type:application/x-www-form-urlencoded`
   *
   * @param  {String} endpoint
   * @return {Promise}
   */
  postAction(endpoint) {
    return axios.post(endpoint, {});
    //return Vue.http.post(endpoint, {}, { emulateJSON: true });
  }
}
