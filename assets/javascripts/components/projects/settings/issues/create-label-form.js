import DatePicker from 'vuejs-datepicker';
import MarkdownArea from '../../../markdown/markdown-area';

if(document.querySelector('#vue-issue-label-form')) {
	$(() => new Vue({
	  el: document.querySelector('#vue-issue-label-form'),
	  data() {
	    const vueData = document.querySelector('#vue-issue-label-form').dataset;

	    return {
	      endpoint: vueData.endpoint,
	      colors: [
	        '#0033CC',
	        '#428BCA',
	        '#44AD8E',
	        '#A8D695',
	        '#5CB85C',
	        '#69D100',
	        '#004E00',
	        '#34495E',
	        '#7F8C8D',
	        '#A295D6',
	        '#5843AD',
	        '#8E44AD',
	        '#FFECDB',
	        '#AD4363',
	        '#D10069',
	        '#CC0033',
	        '#FF0000',
	        '#D9534F',
	        '#D1D100',
	        '#F0AD4E',
	        '#AD8D43'
	      ],
	      labelTitle: null,
	      labelColorCode: '#428BCA',
	    };
	  },
	  mounted() {
	    //new tc.TcForm($('.milestone-form'));
	  },
	  computed: {
	    shouldEnableCreateButton() {
	      return this.labelTitle != null && this.labelTitle.trim().length > 0 && this.labelColorCode && this.validColorCode;
	    },
	    validColorCode() {
	      return this.labelColorCode != null && /^#([0-9a-f]{3}|[0-9a-f]{6})$/i.test(this.labelColorCode.trim());
	    }
	  },
	  methods: {
	    changeColor(event) {
	      let _target = $(event.target);
	      this.labelColorCode = _target.data('color');
	    },
	    updatePreview(event) {

	    },
	  },
	  template: `
	  <form class="issue-label-form prepend-top-20" id="form-milestone" :action="endpoint" enctype="multipart/form-data" accept-charset="UTF-8" method="post">
	    <input type="hidden" name="name" id="user_username_hidden"/>
	    <div class="form-group">
	      <label class="required" for="label-title">이름</label>
	      <input class="form-control fcl-md" type="text" name="title" id="label-title" v-model="labelTitle" autocomplete="off" spellcheck="false" maxlength="20"/>
	    </div>
	    <div class="form-group">
        <label class="" for="label-description">설명</label>
        <input class="form-control" type="text" name="description" id="label-description" autocomplete="off" spellcheck="false" maxlength="500"/>
	    </div>
	    <div class="form-group">
	      <label class="label-color required" for="milestone-start-date">배경색</label>
	      <div class="input-group">
          <div class="input-group-addon label-color-preview" :style="{backgroundColor: labelColorCode}">&nbsp;</div>
          <input class="form-control fcl-md" type="text" v-model="labelColorCode" name="color" id="label-color"/>
        </div>
        <div class="help-block prepend-top-10">색상 코드를 입력하거나 아래 색상 중 하나를 사용해 보세요.</div>
	      <div class="suggest-colors">
	        <a href="#!" v-for="color in colors" :style="{backgroundColor: color}" :data-color="color" @click="changeColor">&nbsp;</a>
	      </div>
	    </div>
	    <div class="form-actions" style="margin-bottom: 25px;">
	      <input type="submit" name="commit" value="레이블 만들기" class="btn btn-create" :disabled="!shouldEnableCreateButton"/>
	      <a class="btn btn-cancel" :href="endpoint">취소</a>
	    </div>
	  </form>
	  `,
	}));
}