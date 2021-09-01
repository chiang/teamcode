/* eslint-disable func-names, space-before-function-paren, wrap-iife, no-var, no-unused-expressions, no-param-reassign, no-else-return, quotes, object-shorthand, comma-dangle, camelcase, one-var, vars-on-top, one-var-declaration-per-line, no-return-assign, consistent-return, max-len, prefer-template */
(function() {
  (function(w) {
    var base;
    const faviconEl = document.getElementById('favicon');
    const originalFavicon = faviconEl ? faviconEl.getAttribute('href') : null;
    w.tc || (w.tc = {});
    (base = w.tc).utils || (base.utils = {});
    w.tc.utils.isInGroupsPage = function() {
      return tc.utils.getPagePath() === 'groups';
    };
    w.tc.utils.isInProjectPage = function() {
      return tc.utils.getPagePath() === 'projects';
    };
    w.tc.utils.getProjectSlug = function() {
      if (this.isInProjectPage()) {
        return $('body').data('project');
      } else {
        return null;
      }
    };
    w.tc.utils.getGroupSlug = function() {
      if (this.isInGroupsPage()) {
        return $('body').data('group');
      } else {
        return null;
      }
    };

    w.tc.utils.ajaxGet = function(url) {
      return $.ajax({
        type: "GET",
        url: url,
        dataType: "script"
      });
    };

    w.tc.utils.extractLast = function(term) {
      return this.split(term).pop();
    };

    w.tc.utils.rstrip = function rstrip(val) {
      if (val) {
        return val.replace(/\s+$/, '');
      } else {
        return val;
      }
    };

    tc.utils.updateTooltipTitle = function($tooltipEl, newTitle) {
      return $tooltipEl.attr('title', newTitle).tooltip('fixTitle');
    };

    w.tc.utils.disableButtonIfEmptyField = function(field_selector, button_selector, event_name) {
      event_name = event_name || 'input';
      var closest_submit, field, that;
      that = this;
      field = $(field_selector);
      closest_submit = field.closest('form').find(button_selector);
      if (this.rstrip(field.val()) === "") {
        closest_submit.disable();
      }
      return field.on(event_name, function() {
        if (that.rstrip($(this).val()) === "") {
          return closest_submit.disable();
        } else {
          return closest_submit.enable();
        }
      });
    };

    // automatically adjust scroll position for hash urls taking the height of the navbar into account
    // https://github.com/twitter/bootstrap/issues/1768
    w.tc.utils.handleLocationHash = function() {
      var hash = w.tc.utils.getLocationHash();
      if (!hash) return;

      // This is required to handle non-unicode characters in hash
      hash = decodeURIComponent(hash);

      // scroll to user-generated markdown anchor if we cannot find a match
      if (document.getElementById(hash) === null) {
        var target = document.getElementById('user-content-' + hash);
        if (target && target.scrollIntoView) {
          target.scrollIntoView(true);
        }
      } else {
        // only adjust for fixedTabs when not targeting user-generated content
        var fixedTabs = document.querySelector('.js-tabs-affix');
        if (fixedTabs) {
          window.scrollBy(0, -fixedTabs.offsetHeight);
        }
      }
    };

    // Check if element scrolled into viewport from above or below
    // Courtesy http://stackoverflow.com/a/7557433/414749
    w.tc.utils.isInViewport = function(el) {
      var rect = el.getBoundingClientRect();

      return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= window.innerHeight &&
        rect.right <= window.innerWidth
      );
    };

    tc.utils.getPagePath = function(index) {
      index = index || 0;
      return $('body').data('page').split(':')[index];
    };

    tc.utils.parseUrl = function (url) {
      var parser = document.createElement('a');
      parser.href = url;
      return parser;
    };

    tc.utils.parseUrlPathname = function (url) {
      var parsedUrl = tc.utils.parseUrl(url);
      // parsedUrl.pathname will return an absolute path for Firefox and a relative path for IE11
      // We have to make sure we always have an absolute path.
      return parsedUrl.pathname.charAt(0) === '/' ? parsedUrl.pathname : '/' + parsedUrl.pathname;
    };

    tc.utils.getUrlParamsArray = function () {
      // We can trust that each param has one & since values containing & will be encoded
      // Remove the first character of search as it is always ?
      return window.location.search.slice(1).split('&');
    };

    tc.utils.isMetaKey = function(e) {
      return e.metaKey || e.ctrlKey || e.altKey || e.shiftKey;
    };

    tc.utils.isMetaClick = function(e) {
      // Identify following special clicks
      // 1) Cmd + Click on Mac (e.metaKey)
      // 2) Ctrl + Click on PC (e.ctrlKey)
      // 3) Middle-click or Mouse Wheel Click (e.which is 2)
      return e.metaKey || e.ctrlKey || e.which === 2;
    };

    tc.utils.scrollToElement = function($el) {
      var top = $el.offset().top;
      gl.mrTabsHeight = gl.mrTabsHeight || $('.merge-request-tabs').height();

      return $('body, html').animate({
        scrollTop: top - (gl.mrTabsHeight)
      }, 200);
    };

    /**
      this will take in the `name` of the param you want to parse in the url
      if the name does not exist this function will return `null`
      otherwise it will return the value of the param key provided
    */
    w.tc.utils.getParameterByName = (name) => {
      const url = window.location.href;
      name = name.replace(/[[\]]/g, '\\$&');
      const regex = new RegExp(`[?&]${name}(=([^&#]*)|&|#|$)`);
      const results = regex.exec(url);
      if (!results) return null;
      if (!results[2]) return '';
      return decodeURIComponent(results[2].replace(/\+/g, ' '));
    };

    w.tc.utils.getSelectedFragment = () => {
      const selection = window.getSelection();
      if (selection.rangeCount === 0) return null;
      const documentFragment = selection.getRangeAt(0).cloneContents();
      if (documentFragment.textContent.length === 0) return null;

      return documentFragment;
    };

    w.tc.utils.insertText = (target, text) => {
      // Firefox doesn't support `document.execCommand('insertText', false, text)` on textareas

      const selectionStart = target.selectionStart;
      const selectionEnd = target.selectionEnd;
      const value = target.value;

      const textBefore = value.substring(0, selectionStart);
      const textAfter = value.substring(selectionEnd, value.length);
      const newText = textBefore + text + textAfter;

      target.value = newText;
      target.selectionStart = target.selectionEnd = selectionStart + text.length;

      // Trigger autosave
      $(target).trigger('input');

      // Trigger autosize
      var event = document.createEvent('Event');
      event.initEvent('autosize:update', true, false);
      target.dispatchEvent(event);
    };

    w.tc.utils.nodeMatchesSelector = (node, selector) => {
      const matches = Element.prototype.matches ||
        Element.prototype.matchesSelector ||
        Element.prototype.mozMatchesSelector ||
        Element.prototype.msMatchesSelector ||
        Element.prototype.oMatchesSelector ||
        Element.prototype.webkitMatchesSelector;

      if (matches) {
        return matches.call(node, selector);
      }

      // IE11 doesn't support `node.matches(selector)`

      let parentNode = node.parentNode;
      if (!parentNode) {
        parentNode = document.createElement('div');
        node = node.cloneNode(true);
        parentNode.appendChild(node);
      }

      const matchingNodes = parentNode.querySelectorAll(selector);
      return Array.prototype.indexOf.call(matchingNodes, node) !== -1;
    };

    /**
      this will take in the headers from an API response and normalize them
      this way we don't run into production issues when nginx gives us lowercased header keys
    */
    w.tc.utils.normalizeHeaders = (headers) => {
      const upperCaseHeaders = {};

      Object.keys(headers).forEach((e) => {
        upperCaseHeaders[e.toUpperCase()] = headers[e];
      });

      return upperCaseHeaders;
    };

    /**
      this will take in the getAllResponseHeaders result and normalize them
      this way we don't run into production issues when nginx gives us lowercased header keys
    */
    w.tc.utils.normalizeCRLFHeaders = (headers) => {
      const headersObject = {};
      const headersArray = headers.split('\n');

      headersArray.forEach((header) => {
        const keyValue = header.split(': ');
        headersObject[keyValue[0]] = keyValue[1];
      });

      return w.tc.utils.normalizeHeaders(headersObject);
    };

    /**
     * Parses pagination object string values into numbers.
     *
     * @param {Object} paginationInformation
     * @returns {Object}
     */
    w.tc.utils.parseIntPagination = paginationInformation => ({
      perPage: parseInt(paginationInformation['X-PER-PAGE'], 10),
      page: parseInt(paginationInformation['X-PAGE'], 10),
      total: parseInt(paginationInformation['X-TOTAL'], 10),
      totalPages: parseInt(paginationInformation['X-TOTAL-PAGES'], 10),
      nextPage: parseInt(paginationInformation['X-NEXT-PAGE'], 10),
      previousPage: parseInt(paginationInformation['X-PREV-PAGE'], 10),
    });

    /**
     * Updates the search parameter of a URL given the parameter and value provided.
     *
     * If no search params are present we'll add it.
     * If param for page is already present, we'll update it
     * If there are params but not for the given one, we'll add it at the end.
     * Returns the new search parameters.
     *
     * @param {String} param
     * @param {Number|String|Undefined|Null} value
     * @return {String}
     */
    w.tc.utils.setParamInURL = (param, value) => {
      let search;
      const locationSearch = window.location.search;

      if (locationSearch.length) {
        const parameters = locationSearch.substring(1, locationSearch.length)
          .split('&')
          .reduce((acc, element) => {
            const val = element.split('=');
            acc[val[0]] = decodeURIComponent(val[1]);
            return acc;
          }, {});

        parameters[param] = value;

        const toString = Object.keys(parameters)
          .map(val => `${val}=${encodeURIComponent(parameters[val])}`)
          .join('&');

        search = `?${toString}`;
      } else {
        search = `?${param}=${value}`;
      }

      return search;
    };

    /**
     * Converts permission provided as strings to booleans.
     *
     * @param  {String} string
     * @returns {Boolean}
     */
    w.tc.utils.convertPermissionToBoolean = permission => permission === 'true';

    /**
     * Back Off exponential algorithm
     * backOff :: (Function<next, stop>, Number) -> Promise<Any, Error>
     *
     * @param {Function<next, stop>} fn function to be called
     * @param {Number} timeout
     * @return {Promise<Any, Error>}
     * @example
     * ```
     *  backOff(function (next, stop) {
     *    // Let's perform this function repeatedly for 60s or for the timeout provided.
     *
     *    ourFunction()
     *      .then(function (result) {
     *        // continue if result is not what we need
     *        next();
     *
     *        // when result is what we need let's stop with the repetions and jump out of the cycle
     *        stop(result);
     *      })
     *      .catch(function (error) {
     *        // if there is an error, we need to stop this with an error.
     *        stop(error);
     *      })
     *  }, 60000)
     *  .then(function (result) {})
     *  .catch(function (error) {
     *    // deal with errors passed to stop()
     *  })
     * ```
     */
    w.tc.utils.backOff = (fn, timeout = 60000) => {
      const maxInterval = 32000;
      let nextInterval = 2000;

      const startTime = Date.now();

      return new Promise((resolve, reject) => {
        const stop = arg => ((arg instanceof Error) ? reject(arg) : resolve(arg));

        const next = () => {
          if (Date.now() - startTime < timeout) {
            setTimeout(fn.bind(null, next, stop), nextInterval);
            nextInterval = Math.min(nextInterval + nextInterval, maxInterval);
          } else {
            reject(new Error('BACKOFF_TIMEOUT'));
          }
        };

        fn(next, stop);
      });
    };

    w.tc.utils.setFavicon = (iconName) => {
      if (faviconEl && iconName) {
        faviconEl.setAttribute('href', `/assets/${iconName}.ico`);
      }
    };

    w.tc.utils.resetFavicon = () => {
      if (faviconEl) {
        faviconEl.setAttribute('href', originalFavicon);
      }
    };

    w.tc.utils.setCiStatusFavicon = (pageUrl) => {
      $.ajax({
        url: pageUrl,
        dataType: 'json',
        success: function(data) {
          if (data && data.icon) {
            tc.utils.setFavicon(`ci_favicons/${data.icon}`);
          } else {
            tc.utils.resetFavicon();
          }
        },
        error: function() {
          tc.utils.resetFavicon();
        }
      });
    };
  })(window);
}).call(window);
