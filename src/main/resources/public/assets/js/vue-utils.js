Vue.filter('byteFormat', function (value, unit) {
	var thresh = 1024;
	if (unit == 'KB')
		value = value * 1024;
	if(Math.abs(value) < thresh) {
			return value + ' B';
	}
	var units = ['KB','MB','GB','TB','PB','EB','ZB','YB'];
	var u = -1;
	do {
			value /= thresh;
			++u;
	} while(Math.abs(value) >= thresh && u < units.length - 1);
	return value.toFixed(1)+' '+units[u];
});
/**
 * YYYY-MM-DD HH:mm:ss "2016-08-18 09:28:20"
 *
 */
Vue.filter('moment', function () {
	var args = Array.prototype.slice.call(arguments);
	return moment(args[0]).format('YYYY-MM-DD HH:mm:ss');
});
Vue.filter('stringify', function (value) {
	return JSON.stringify(value);
});
Vue.directive('attachment-progress', {
	inserted: function(el) {
		el.style.visibility = 'visible';
		el.style.width = '0%';
	},
	update: function(el, binding) {
		if (binding.value >= 100) {
			el.style.width = '100%';
			setTimeout(function(){
				el.style.visibility = 'hidden';
				$(el).closest('.upload-item').attr('aria-busy', false);
			}, 1500);
		}
		else {
			el.style.width = binding.value + '%';
		}
	}
});
