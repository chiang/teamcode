
import IntegrationConfig from './integration-config';

export default {
  template: `
    <integration-config>
      <div slot="header">
        <label class="label-light">{{settings.title}}</label>
        <span class="help-block">{{settings.description}}</span>
      </div>
      <div slot="body">
        <div class="text-muted">준비 중입니다.</div>
      </div>
    </integration-config>
  `,
  props: ['settings', 'list-url'],
  components: {
    'integration-config': IntegrationConfig,
  },
}