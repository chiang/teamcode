import axios from 'axios';
import Vue from 'vue';
import JobStatus from './job-status';

$(()=>{
	if ($('.build-page').length > 0) {
		const traceLink = $('.build-page').data('trace-link');
		axios.get(traceLink)
	  .then((response)=>{
	    if (!response.data.complete) {
	      $('#build-trace .build-loader-animation').show();
	    }
	    else {
	      $('#build-trace .build-loader-animation').hide();
	      $('#build-trace .js-build-output').html(response.data.html);
	    }
	    //location.href = _this.data('origin');
	  })
	  .catch((error)=>{
	    //TODO error handling
	  });
	}
});

$(() => {
	new Vue({
		el: document.querySelector('.job-status-view'),
    components: {
      'job-status': JobStatus,
    },
  });
});