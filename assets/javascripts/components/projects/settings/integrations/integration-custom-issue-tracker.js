
import IntegrationConfig from './integration-config';

export default {
  template: `
    <integration-config>
      <div slot="header">
        <div class="row">
          <div class="col-md-10">
            <label class="label-light">{{settings.title}}</label>
            <span class="help-block">{{settings.description}}</span>
          </div>
          <div class="col-md-2">
            <div class="onoffswitch">
                <input type="checkbox" name="active" class="onoffswitch-checkbox" id="myonoffswitch" :checked="settings.active" value="true">
                <label class="onoffswitch-label" for="myonoffswitch">
                    <span class="onoffswitch-inner"></span>
                    <span class="onoffswitch-switch"></span>
                </label>
            </div>
          </div>
        </div>
      </div>
      <div slot="body">
        <div class="row">
          <div class="col-md-12">
            <div class="form-group row">
              <label class="col-sm-2 text-right" for="issue-tracker-title">이름</label>
              <div class="col-sm-10">
                <input required="required" class="form-control" id="issue-tracker-title" type="text" name="title" v-model="settings.title" autocomplete="off" spellcheck="false"/>
              </div>
            </div>
            <div class="form-group row">
              <label class="col-sm-2 text-right" for="project-desc">이슈 트랙커 설명</label>
              <div class="col-sm-10">
                <input required="required" class="form-control" id="project-desc" type="text" name="description" v-model="settings.description" autocomplete="off" spellcheck="false"/>
              </div>
            </div>
            <div class="form-group row">
              <label class="required col-sm-2 text-right" for="issue-id-regexp">정규식</label>
              <div class="col-sm-10">
                <input required="required" class="form-control" id="issue-id-regexp" type="text" name="regexp" autocomplete="off" spellcheck="false" :value="settings.properties.regexp"/>
                <p class="help-block">커밋 메시지에서 이슈 아이디를 추출할 정규식을 입력합니다. 기본 값인 <strong>#(\\d+)</strong> 은 #319 와 같은 아이디 값을 추출합니다.</p>
              </div>
            </div>
            <div class="form-group row">
              <label class="required col-sm-2 text-right" for="project-issue-url">이슈 주소</label>
              <div class="col-sm-10">
                <input required="required" class="form-control" id="project-issue-url" type="text" name="url" autocomplete="off" spellcheck="false" :value="settings.properties.url"/>
                <p class="help-block">이슈 주소를 입력하면 정규식으로 추출한 이슈 아이디 값을 이슈 주소에 연결하여 해당 이슈로 바로 이동할 수 있습니다.</p>
              </div>
            </div>
            <div class="form-group row">
              <label class="col-sm-2 text-right" for="custom-issue-show-link">링크 노출하기</label>
              <div class="col-sm-10">
                <input class="" id="custom-issue-show-link" type="checkbox" name="linkEnabled" value="true" v-model="settings.properties.linkEnabled"/>
                <p class="help-block">체크하면, 커밋 정보를 보여줄 때 해당 이슈로 이동할 수 있는 링크도 함께 표시합니다.</p>
              </div>
            </div>
          </div>
          <div class="col-md-12 text-right">
            <a :href="listUrl" class="btn btn-secondary">취소</a>
            <button type="submit" name="commit" value="Save changes" class="btn btn-save">설정을 저장합니다.</button>
          </div>
        </div>
      </div>
    </integration-config>
  `,
  props: ['settings', 'list-url'],
  components: {
    'integration-config': IntegrationConfig,
  },
}