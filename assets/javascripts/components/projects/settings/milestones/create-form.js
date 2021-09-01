import DatePicker from 'vuejs-datepicker';
import MarkdownArea from '../../../markdown/markdown-area';

if(document.querySelector('#vue-milestone-settings')) {
	$(() => new Vue({
	  el: document.querySelector('#vue-milestone-settings'),
	  data() {
	    const vueData = document.querySelector('#vue-milestone-settings').dataset;

	    return {
	      endpoint: vueData.endpoint,
	    };
	  },
	  components: {
	    'date-picker': DatePicker,
	    'markdown-area': MarkdownArea,
	  },
	  mounted() {
	    new tc.TcForm($('.milestone-form'));
	  },
	  template: `
	  <form class="tc-note-form milestone-form prepend-top-20" id="form-milestone" :action="endpoint" enctype="multipart/form-data" accept-charset="UTF-8" method="post">
	    <input type="hidden" name="name" id="user_username_hidden"/>
	    <div class="form-group">
	      <label class="required" for="milestone_title">이름</label>
	      <input class="form-control" type="text" name="title" id="milestone_title" autocomplete="off" spellcheck="false"/>
	    </div>
	    <div class="form-group">
        <label class="" for="milestone_description">설명</label>
        <markdown-area/>
	    </div>
	    <div class="form-group">
	      <label class="" for="milestone-start-date">시작일자</label>
	      <date-picker id="milestone-start-date" name="startDateStr" format="yyyy-MM-dd" input-class="form-control fcl-sm" monday-first="true" language="ko" calendar-button="true" calendar-button-icon="fa fa-calendar"></date-picker>
	    </div>
	    <div class="form-group">
	      <label class="" for="milestone-due-date">완료일자</label>
	      <date-picker id="milestone-due-date" name="dueDateStr" format="yyyy-MM-dd" input-class="form-control fcl-sm" monday-first="true" language="ko" calendar-button="true" calendar-button-icon="fa fa-calendar"></date-picker>
	    </div>
	    <div class="form-actions" style="margin-bottom: 25px;">
	      <input type="submit" name="commit" value="마일스톤 저장" class="btn btn-create"/>
	      <a class="btn btn-cancel" th:href="@{/admin/users}" th:text="#{btn.cancel}">Cancel</a>
	    </div>
	  </form>
	  `,
	}));
}