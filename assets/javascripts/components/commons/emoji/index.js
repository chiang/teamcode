import emojione from 'emojione';
//var emojione = require('./libs/emojione.min.js');


emojione.imageType = 'svg';

$('tc-emoji').each(function() {
    var _this = $(this);
    var _shortName = _this.data('name');
    // use .shortnameToImage if only converting shortnames (for slightly better performance)
    //var converted = emojione.shortnameToUnicode(_shortName);
    var converted = emojione.shortnameToImage(_shortName);
    _this.html(converted);
});