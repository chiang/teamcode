$(function(){
	Vue.directive('logfile', {
		bind: function (el, binding, vnode) {
			console.log(binding.value);
			var lines = binding.value.split('\n');
			var html = [];
      for(var i = 0;i < lines.length;i++){
     		html.push('<li><p>');
     		html.push(lines[i]);
     		html.push('</p></li>')
      }
      console.log(html.join(''));
			el.innerHTML = html.join('');
		}
	});

	var logsVue = new Vue({
    el: '#content',
    data: {
      logs: null,
    },
    computed: {
    	loaded: function(){
    		return this.logs !=  null;
    	}
    },
    mounted: function() {
    	var _this = this;
    	$.get('/logfile', function (data) {
    		_this.logs = data;
      });
    }
  });
  $('.log-bottom').click(function(e) {
		var visible_log;
		e.preventDefault();
		visible_log = $(".file-content:visible");
		return visible_log.animate({
			scrollTop: visible_log.find('ol').height()
		}, "fast");
	});
});