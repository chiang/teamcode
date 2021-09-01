
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
                <p class="help-block">커밋 시 내용을 수신 받을 이메일 주소를 입력해 주세요. 여러 주소를 입력할 때는 Comma 구분으로 입력해 주세요.</p>
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
      return this.settings.properties.recipients;
    }
  },
  components: {
    'integration-config': IntegrationConfig,
  },
}