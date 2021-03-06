/* eslint-disable func-names, space-before-function-paren, wrap-iife, no-var, no-param-reassign, no-cond-assign, comma-dangle, no-unused-expressions, prefer-template, max-len */
/* global timeago */
/* global dateFormat */

//window.timeago = require('vendor/timeago');
//window.dateFormat = require('vendor/date.format');

import '../libs/bootstrap-3.3.7.min.js';
import timeago from '../libs/timeago';
import dateFormat from '../libs/date.format';

(function() {
  (function(w) {
    var base;
    var timeagoInstance;

    if (w.tc == null) {
      w.tc = {};
    }
    if ((base = w.tc).utils == null) {
      base.utils = {};
    }
    w.tc.utils.days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    w.tc.utils.formatDate = function(datetime) {
      return dateFormat(datetime, 'mmm d, yyyy h:MMtt Z');
    };

    w.tc.utils.getDayName = function(date) {
      return this.days[date.getDay()];
    };

    w.tc.utils.localTimeAgo = function($timeagoEls, setTimeago = true) {
      $timeagoEls.each((i, el) => {
        el.setAttribute('title', tc.utils.formatDate(el.getAttribute('datetime')));

        if (setTimeago) {
          // Recreate with custom template
          $(el).tooltip({
            template: '<div class="tooltip local-timeago" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
          });
        }

        el.classList.add('js-timeago-render');
      });

      tc.utils.renderTimeago($timeagoEls);
    };

    w.tc.utils.getTimeago = function() {
      var locale;

      if (!timeagoInstance) {
        /*locale = function(number, index) {
          return [
            ['less than a minute ago', 'a while'],
            ['less than a minute ago', 'in %s seconds'],
            ['about a minute ago', 'in 1 minute'],
            ['%s minutes ago', 'in %s minutes'],
            ['about an hour ago', 'in 1 hour'],
            ['about %s hours ago', 'in %s hours'],
            ['a day ago', 'in 1 day'],
            ['%s days ago', 'in %s days'],
            ['a week ago', 'in 1 week'],
            ['%s weeks ago', 'in %s weeks'],
            ['a month ago', 'in 1 month'],
            ['%s months ago', 'in %s months'],
            ['a year ago', 'in 1 year'],
            ['%s years ago', 'in %s years']
          ][index];
        };*/
        /*locale = function(number, index) {
          return [
            ['less than a minute ago', 'a while'],
            ['less than a minute ago', 'in %s seconds'],
            ['about a minute ago', 'in 1 minute'],
            ['%s minutes ago', '??? %s??? ???'],
            ['about an hour ago', 'in 1 hour'],
            ['about %s hours ago', 'in %s hours'],
            ['a day ago', 'in 1 day'],
            ['%s days ago', 'in %s days'],
            ['a week ago', 'in 1 week'],
            ['%s weeks ago', 'in %s weeks'],
            ['a month ago', 'in 1 month'],
            ['%s months ago', 'in %s months'],
            ['a year ago', 'in 1 year'],
            ['%s years ago', 'in %s years']
          ][index];
        };*/
        locale = function(number, index, total_sec) {
          // number: the timeago / timein number;
          // index: the index of array below;
          // total_sec: total seconds between date to be formatted and today's date;
          return [
            ['?????? ???', 'right now'],
            ['%s??? ???', 'in %s seconds'],
            ['1??? ???', 'in 1 minute'],
            ['%s??? ???', 'in %s minutes'],
            ['1?????? ???', 'in 1 hour'],
            ['%s?????? ???', 'in %s hours'],
            ['1??? ???', 'in 1 day'],
            ['%s??? ???', 'in %s days'],
            ['1??? ???', 'in 1 week'],
            ['%s??? ???', 'in %s weeks'],
            ['1??? ???', 'in 1 month'],
            ['%s??? ???', 'in %s months'],
            ['1??? ???', 'in 1 year'],
            ['%s??? ???', 'in %s years']
          ][index];
        };

        //timeago.register('tc_en', locale);
        timeago.register('tc_kr', locale);
        timeagoInstance = timeago();
      }

      return timeagoInstance;
    };

    w.tc.utils.timeFor = function(time, suffix, expiredLabel) {
      var timefor;
      if (!time) {
        return '';
      }
      suffix || (suffix = 'remaining');
      expiredLabel || (expiredLabel = 'Past due');
      timefor = tc.utils.getTimeago().format(time, 'tc_kr').replace('in', '');
      if (timefor.indexOf('ago') > -1) {
        timefor = expiredLabel;
      } else {
        timefor = timefor.trim() + ' ' + suffix;
      }
      return timefor;
    };

    w.tc.utils.cachedTimeagoElements = [];
    w.tc.utils.renderTimeago = function($els) {
      if (!$els && !w.tc.utils.cachedTimeagoElements.length) {
        w.tc.utils.cachedTimeagoElements = [].slice.call(document.querySelectorAll('.js-timeago-render'));
      } else if ($els) {
        w.tc.utils.cachedTimeagoElements = w.tc.utils.cachedTimeagoElements.concat($els.toArray());
      }

      w.tc.utils.cachedTimeagoElements.forEach(tc.utils.updateTimeagoText);
    };

    w.tc.utils.updateTimeagoText = function(el) {
      const timeago = tc.utils.getTimeago();
      const formattedDate = timeago.format(el.getAttribute('datetime'));

      if (el.textContent !== formattedDate) {
        el.textContent = formattedDate;
      }
    };

    w.tc.utils.initTimeagoTimeout = function() {
      tc.utils.renderTimeago();

      tc.utils.timeagoTimeout = setTimeout(tc.utils.initTimeagoTimeout, 1000);
    };

    w.tc.utils.getDayDifference = function(a, b) {
      var millisecondsPerDay = 1000 * 60 * 60 * 24;
      var date1 = Date.UTC(a.getFullYear(), a.getMonth(), a.getDate());
      var date2 = Date.UTC(b.getFullYear(), b.getMonth(), b.getDate());

      return Math.floor((date2 - date1) / millisecondsPerDay);
    };
  })(window);
}).call(window);


let localTimeAgo = function(selector) {
  $(selector).each(function(i, e){
    $(e).tooltip({
        template: '<div class="tooltip local-timeago" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
    });
    $(e).timeago('update', $(e).attr('datetime'));
  });
}
// Korean
$.timeago.settings.strings = {
  prefixAgo: null,
  prefixFromNow: null,
  suffixAgo: "???",
  suffixFromNow: "???",
  seconds: "1???",
  minute: "??? 1???",
  minutes: "%d???",
  hour: "??? 1??????",
  hours: "??? %d??????",
  day: "??????",
  days: "%d???",
  month: "??? 1??????",
  months: "%d??????",
  year: "??? 1???",
  years: "%d???",
  wordSeparator: " ",
  numbers: []
};
export default localTimeAgo;
