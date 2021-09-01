$(function(){
	Vue.directive('response-status', {
    bind: function (el, binding, vnode) {
      var s = JSON.stringify
      el.style.fontWeight = 'bold';
      switch(binding.value) {
      	case '200':
      		el.style.color = '#6BB536';
      		break;
      	case '404':
      		el.style.color = '#E1BB0F';
      		break;
      	case '500':
					el.style.color = '#B50000';
					break;
      }
    }
  })

	var traceVue = new Vue({
    el: '#content',
    data: {
      traces: null
    },
    computed: {
    	present: function(){
    		return this.traces != null && this.traces.length > 0;
    	}
    },
    mounted: function() {
    	var _this = this;
    	$.getJSON('/trace', function (data) {
    		_this.traces = data;
      });
    },
    methods: {
    	toggle: function(event){
    		var _btn = $(event.target);
    		var _rawEl = _btn.closest('li').find('.raw');
    		if (_rawEl.is(':visible')) {
    			_rawEl.hide();
    		}
    		else {
    			_rawEl.show();
    		}
    	}
    }
  });
});