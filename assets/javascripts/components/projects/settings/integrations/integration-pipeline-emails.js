
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
              <label class="col-sm-2 text-right" for="issue-tracker-title">이메일 주소</label>
              <div class="col-sm-10">
                <textarea name="recipients" class="form-control" rows="5" placeholder="" :value="recipients"></textarea>
                <p class="help-block">파이프라인 상태를 수신 받을 이메일 주소를 입력해 주세요. 여러 주소를 입력할 때는 Comma 구분으로 입력해 주세요.</p>
              </div>
            </div>
            <div class="form-group row">
              <label class="col-sm-2 text-right" for="custom-issue-show-link">파이프라인이 깨졌을 때만 알려주기</label>
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
  computed: {
    recipients() {
      //return this.settings.properties.recipients;
      return [];
    }
  },
  components: {
    'integration-config': IntegrationConfig,
  },
}