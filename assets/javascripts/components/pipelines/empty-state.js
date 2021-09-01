export default {
  props: {
    helpPagePath: {
      type: String,
      required: true,
    },
  },

  template: `
      <div class="row empty-state">
        <div class="col-xs-12">
          <!-- div class="svg-content" v-html="pipelinesEmptyStateSVG" / -->
        </div>

        <div class="col-xs-12 text-center">
          <div class="text-content">
            <div><img src="/assets/images/t-bot.png" height="59" style="margin-right: 20px;"/>아직 파이프라인을 실행한 적이 없습니다.</div>
            <div style="background-color: #F0F2F5; border-radius: 4px; padding: 24px 40px; line-height: 24px;">
              소스 코드를 커밋하면 파이프라인이 바로 실행됩니다.
            </div>
            <!-- div class="prepend-top-20">
              <a href="#!">파이프라인 파일 확인하러 가기 →</a>
            </div -->
          </div>
        </div>
      </div>
  `,
};
