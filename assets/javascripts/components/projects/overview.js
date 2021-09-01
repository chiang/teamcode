import axios from 'axios';

$(function(){
	$('.photo-pile-slot img').popover({
    trigger: 'hover',
    placement: 'bottom',
    html: true,
    //delay: {'hide': 100000},
    content: function(){
      let src = $(this).attr('src');
      let dataset = $(this).data();
      let organization = typeof dataset.organization !== 'undefined' ? dataset.organization : '';
      let template = `<div class="popover-card">
        <h4>${dataset.fullName}</h4>
        <div class="profile-desc">${dataset.userName}</div>
        <div class="profile-desc">${dataset.email}</div>
        <div class="profile-desc">${organization}</div>
        <img src="${src}"/>
      </div>
      `;

      return template;
    }
  });

  $('#form-project-link .btn-save').click(function(){
    const inputTitle = document.querySelector('#input-project-link-title');
    const inputLink = document.querySelector('#input-project-link');
    if (inputTitle.value.trim().length == 0) {
      var _input= $(inputTitle);
      //if (!_input.hasClass('has-error')) {
        //_input.closest('.form-group').addClass('has-error');
        _input.focus();
        return false;
      //}
    }
    if (inputLink.value.trim().length == 0) {
      var _input= $(inputLink);
      //if (!_input.hasClass('has-error')) {
        //_input.closest('.form-group').addClass('has-error');
        _input.focus();
        return false;
      //}
    }

    $('#form-project-link').submit();
    return false;
  });

	$('#form-project-link').submit(function(e){
		const inputTitle = document.querySelector('#input-project-link-title');
		const inputLink = document.querySelector('#input-project-link');

		const data = document.querySelector('#form-project-link').dataset;
		var params = new URLSearchParams();
    params.append('title', inputTitle.value);
    params.append('link', inputLink.value);
		axios.post(data.action, params)
		.then((response)=>{
			location.href = data.origin;
		})
		.catch((error)=>{
			//TODO error handling
		});

		return false;
	});

	$('.repo-links .links a>i').click(function(e){
		e.stopPropagation();
    e.preventDefault();
		const _this = $(this);
		const deleteLink = _this.data('delete-url');
		const deleteMessage = _this.data('delete-message');
		if (confirm(deleteMessage)) {
			var params = new URLSearchParams();
	    params.append('_method', 'delete');
	    axios.post(deleteLink, params)
	    .then((response)=>{
	      location.href = _this.data('origin');
	    })
	    .catch((error)=>{
	      //TODO error handling
	    });
    }
	});
});