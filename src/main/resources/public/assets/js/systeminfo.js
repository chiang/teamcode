$(function(){
	console.log(2);
	var metricsVue = new Vue({
    el: '#content',
    data: {
      metrics: null,
      health: null
    },
    computed: {
    	memusage: function(){
    		return (parseInt(this.metrics.mem) - parseInt(this.metrics['mem.free'])) * 1024;
    	},
    	loaded: function(){
    		return this.metrics !=  null && this.health != null;
    	}
    },
    mounted: function() {
    	console.log(1);
    	var _this = this;
    	$.getJSON('/metrics', function (data) {
    		_this.metrics = data;
    		$.getJSON('/health', function (data) {
    			_this.health = data;
    		});
      });
    }
  });
});