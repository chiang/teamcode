import Vue from 'vue';

//libraries
import './libs/select2';
import './libs/initial.js';
import './libs/jquery-timeago';

//commons
import localTimeAgo from './utils/datetime-utils';
import './utils';
import './components/commons/emoji';
import './components/commons/content-placeholder';

// behaviors
import './behaviors';

import './components/form';

//pages
import './components/admin/users';
import './components/dashboard';
import './components/projects';
import './components/projects/settings/issues';
import './components/projects/settings/pipelines/variables';
import './components/projects/settings/milestones';

import Pipelines from './components/pipelines';
import PipelinesSettings from './components/pipelines/pipelines-settings.js';
import './components/jobs';



//import $ from 'jquery';
import DotDotDot from './libs/jquery-dotdotdot';

//everything else...
import './components/markdown';
import toastr from 'toastr';

//초기화
toastr.options.hideDuration = 12;
toastr.options.timeOut = 5000;
toastr.options.closeOnHover = false;

document.addEventListener('beforeunload', function () {
  // Unbind scroll events
  $(document).off('scroll');
  // Close any open tooltips
  $('.has-tooltip, [data-toggle="tooltip"]').tooltip('destroy');
});

$(function(){
  var $body = $('body');

  // Initialize tooltips
  $.fn.tooltip.Constructor.DEFAULTS.trigger = 'hover';
  $body.tooltip({
    selector: '.has-tooltip',
    placement: function (tip, el) {
      return $(el).data('placement') || 'bottom';
    }
  });

  $('.profile').initial();


  $('[data-toggle="tooltip"]').tooltip({
      template: '<div class="tooltip local-timeago" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
  });


  localTimeAgo('.js-timeago');
  $('#search').focusin(function(){
      $(this).closest('.search-form').addClass('search-active');
  });
  $('#search').focusout(function(){
      $(this).closest('.search-form').removeClass('search-active');
  });
  $('.alert-wrapper').click(function(){
      $(this).fadeOut( "slow" );
  });
  $('.js-data-languages').select2();
  $('.js-data-pipeline-templates').select2({
    minimumResultsForSearch: -1
  });
    /*$('a[data-method="delete"]').click(function(){
        var _this = $(this);
        var _li = _this.closest('li');
        var _href = _this.attr('href');
        var _confirm = _this.data('confirm');
        if (confirm(_confirm)) {
            $.ajax({
                type:'POST',
                url: _href,
                data: {
                    '_method': 'delete'
                },
                success: function(){
                    _li.fadeOut('slow');
                }
            });
        }

        return false;
    });*/

//TODO move to page?
    $('.edit-user .btn-save').click(function(){
            var _this = $(this);
            var _form = _this.closest('form');
            if(!_this.hasClass('disabled')) {
                _form.submit();
            }
        });
});
$(function(){
	var vueAttachment = new Vue({
  	el: '.project-attachments',
  	data: {
  		attachments: []
  	},
  	methods: {
  		deleteAttachment: function(event){
  			var _btn = $(event.target);
  			var _url = _btn.data('upload-remove');
  			$.ajax({
  				type:'POST',
  				url: _url,
  				data: {
  					delete: ''
  				},
  			});

  			//If the ajax request is success or failed, force remove element (Server file will deleted?)
  			_btn.closest('.upload-item').remove();
  		}
  	}
  });

  $('.upload-dropzone input[type="file"]').change(function(){
  	var _this = $(this);
  	var _btn = _this.closest('.upload-dropzone').find('.btn');
  	var _uploading = _this.closest('.upload-dropzone').find('.uploading');
  	_uploading.addClass('ing');
  	_btn.addClass('disabled');

  	var createUrl = _this.data('create-url');
  	var listUrl = _this.data('list-url');
		var file = this.files[0];
		console.log('name: ' + file.name + ', size: ' + file.size + ', type: ' + file.type);
		var form = new FormData;
		form.append("contentType", file.type);
		form.append("attachment", file);

		var at = {};
		at.contentType = file.type;
		at.fileId = file.name + '|' + file.size;
		at.name = file.name;
		at.size = file.size;
		at.serverId = -1;
		at.deleteUrl = null;
		at.progress = 0;

		setTimeout(function(){
			$.ajax({
				type:'POST',
				url: createUrl,
				data: form,
				xhr: function() {
					var myXhr = $.ajaxSettings.xhr();
					if(myXhr.upload){
						//myXhr.upload.addEventListener('progress', progress, false);
					}
					return myXhr;
				},
				cache:false,
				contentType: false,
				processData: false,
				success:function(data){
					location.href = listUrl;
				},
				error: function(data){
					console.log(data);
				}
			});
		}, 700);
	});

	$('.js-choose-avatar-button input[type="file"]').change(function(){
			var _this = $(this);
			var _form = _this.closest('form');
			var _formGroup = _this.closest('.form-group');
			var _img = _formGroup.find('img');
			var _labelFileName = _formGroup.find('.js-avatar-filename');
			var file = this.files[0];
			_labelFileName.text(file.name);
			console.log('file size: ' + file.size);
			var _max = 1024 * 200;//200KB
			if (file.size > _max) {
					_form.find('.btn-save').addClass('disabled');
					_formGroup.find('.label-size-exceeded').show();
					_formGroup.find('.help-block').hide();
			}
			else {
					_form.find('.btn-save').removeClass('disabled');
					_formGroup.find('.label-size-exceeded').hide();
					_formGroup.find('.help-block').show();

					var reader = new FileReader();
					reader.onload = function (e) {
							_img.attr('src', e.target.result);
					}
					reader.readAsDataURL(file);
			}
	});

	$(".download-files a[data-method='delete']").click(function(){
    var _this = $(this);
    if(confirm("정말로 이 파일을 삭제하시겠습니까?")) {
      $('#form-delete-attachment').attr('action', _this.attr('href')).submit();

      return false;
    }
    else {
      return false;
    }
  });
});
