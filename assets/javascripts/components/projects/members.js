/**
 * Project Member Settings
 *
 */

import localTimeAgo from '../../utils/datetime-utils';

$(function(){
    var _projectPath = $('body').data('project-path');
    localTimeAgo('.js-timeago');
    $('.js-data-notmembers-ajax').select2({
        multiple: true,
        "language": {
           "noResults": function(){
               return "이 프로젝트에 추가할 멤버가 없습니다.";
           }
        },
        ajax: {
            url: '/projects/' + _projectPath + '/admin/members?not',
            dataType: 'json',
            type: "GET",
            quietMillis: 50,
            data: function (params) {
              return {
                query: params.term
              };
            },
            processResults: function (data) {
                return {
                    results: $.map(data, function(item) {
                        return {
                            text: item.fullName,
                            id: item.id
                        }
                    })
                };
            }
        }
    });
    $("#project_settings_members a[data-method='delete']").click(function(){
		var _this = $(this);
		var _project = _this.data('project-name');
		var _name = _this.data('member-name');
		if(confirm("정말로 프로젝트 '" + _project + "' 에서 멤버 '" + _name + "' 를 제외하시겠습니까?")) {
			$('#form-delete-member').attr('action', _this.attr('href')).submit();

			return false;
		}
		else {
			return false;
		}
	});
	$('.js-edit-member-form li a').click(function(){
	    var _form = $(this).closest('form');
	    var _roleName = $(this).data('role-name');
	    var _inputRole = _form.find('input[name="role"]');
	    _inputRole.val(_roleName);

	    _form.submit();
	});
});