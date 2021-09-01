/**
 * TeamCode Custom Form
 *
 */
window.tc = window.tc || {};

function TcForm(form) {
  this.form = form;
  this.textarea = this.form.find('textarea.js-tfm-input');
  // Before we start, we should clean up any previous data for this form
  this.destroy();
  // Setup the form
  this.setupForm();
  this.form.data('tc-form', this);
}
TcForm.prototype.destroy = function() {
  // Clean form listeners
  this.clearEventListeners();
  return this.form.data('tc-form', null);
};
TcForm.prototype.setupForm = function() {
  var isNewForm;
  isNewForm = this.form.is(':not(.tfm-form)');
  this.form.removeClass('js-new-note-form');
  if (isNewForm) {
    this.form.find('.div-dropzone').remove();
    this.form.addClass('tfm-form');
    // remove notify commit author checkbox for non-commit notes
    tc.utils.disableButtonIfEmptyField(this.form.find('.js-note-text'), this.form.find('.js-comment-button, .js-note-new-discussion'));

    //tc.GfmAutoComplete.setup(this.form.find('.js-tfm-input'));
    //new DropzoneInput(this.form);
    autosize(this.textarea);
    // form and textarea event listeners
    this.addEventListeners();
  }
  tc.text.init(this.form);
  // hide discard button
  this.form.find('.js-note-discard').hide();
  this.form.show();
  if (this.isAutosizeable) this.setupAutosize();
};
TcForm.prototype.setupAutosize = function () {
  this.textarea.off('autosize:resized')
    .on('autosize:resized', this.setHeightData.bind(this));

  this.textarea.off('mouseup.autosize')
    .on('mouseup.autosize', this.destroyAutosize.bind(this));

  setTimeout(() => {
    autosize(this.textarea);
    this.textarea.css('resize', 'vertical');
  }, 0);
};

TcForm.prototype.setHeightData = function () {
  this.textarea.data('height', this.textarea.outerHeight());
};

TcForm.prototype.destroyAutosize = function () {
  const outerHeight = this.textarea.outerHeight();

  if (this.textarea.data('height') === outerHeight) return;

  autosize.destroy(this.textarea);

  this.textarea.data('height', outerHeight);
  this.textarea.outerHeight(outerHeight);
  this.textarea.css('max-height', window.outerHeight);
};

TcForm.prototype.clearEventListeners = function() {
  this.textarea.off('focus');
  this.textarea.off('blur');
  return tc.text.removeListeners(this.form);
};

TcForm.prototype.addEventListeners = function() {
  this.textarea.on('focus', function() {
    return $(this).closest('.md-area').addClass('is-focused');
  });
  return this.textarea.on('blur', function() {
    return $(this).closest('.md-area').removeClass('is-focused');
  });
};

window.tc.TcForm = TcForm;