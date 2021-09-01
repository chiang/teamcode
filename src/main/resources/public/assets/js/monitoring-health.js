$(function(){
	var healthVue = new Vue({
    el: '#content',
    data: {
      health: null,
    },
    computed: {
    	loaded: function(){
    		return this.health !=  null;
    	}
    },
    mounted: function() {
    	var _this = this;
    	$.get('/health', function (data) {
    		_this.health = data;
      });
    }
  });
});