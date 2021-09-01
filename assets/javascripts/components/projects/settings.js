import axios from 'axios';

$(function(){
    $('.edit-project .btn-save').click(function(){
        var _this = $(this);
        var _form = _this.closest('form');
        if(!_this.hasClass('disabled')) {
            _form.submit();
        }
    });
    $('.js-choose-project-avatar-button input[type="file"]').change(function(){
        var _this = $(this);
        var _form = _this.closest('form');
        var _formGroup = _this.closest('.form-group');
        var _labelFileName = _formGroup.find('.js-avatar-filename');
        var file = this.files[0];
        _labelFileName.text(file.name);
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
        }
    });
    $('.js-confirm-danger-input').keyup(function(){
        var _this = $(this);
        var _modal = _this.closest('.modal');
        var _matchChar = _modal.find('.js-confirm-danger-match').text();
        if (_this.val() == _matchChar) {
            if($('#modal-confirm-danger .btn-danger').hasClass('disabled')) {
                $('#modal-confirm-danger .btn-danger').removeClass('disabled').prop('disabled', false);
            }
        }
        else {
            if(!$('#modal-confirm-danger .btn-danger').hasClass('disabled')) {
                $('#modal-confirm-danger .btn-danger').addClass('disabled').prop('disabled', true);
            }
        }
    });
    $('#modal-confirm-danger .btn-danger').click(function(){
        $('#form-remove-project').submit();
    });
    $('.btn-remove').click(function(){
        var _this = $(this);
        $('.js-confirm-danger-input').val('');
        //$('#modal-confirm-danger .text-danger').text(_this.data('confirm-danger-message'));
        if(!$('#modal-confirm-danger .btn-danger').hasClass('disabled')) {
            $('#modal-confirm-danger .btn-danger').addClass('disabled').prop('disabled', true);
        }

        $('#modal-confirm-danger').modal('show');
        return false;
    });

  $('#toggle-pipeline').change(()=>{
    //TODO 실패 시 메시지 출력 및 원래대로 돌리기
    const action = $('#form-project-pipeline-settings').attr('action');
    const enabled = $('#toggle-pipeline').is(':checked') ? 'true' : 'false';
    var params = new URLSearchParams();
    params.append('_method', "patch");
    params.append('enabled', enabled);

    axios.post(action, params)
    .then((response)=>{
      if (enabled == 'true') {
        $('#pipeline-config-path-settings').fadeIn(200);
      }
      else {
        console.log('--> hide...');
        $('#pipeline-config-path-settings').fadeOut(200);
      }
    })
    .catch((error)=>{
      //TODO error handling
    });
  });

  $('#btn-update-pipeline-config-path').click(function(){
    var _path = $('#input-pipeline-config-path').val();
    var _form = $('#form-update-pipeline-config-path');
    _form.find('input[name="pipelineConfigPath"]').val(_path);
    _form.submit();
  });
});
