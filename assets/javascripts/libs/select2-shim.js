(function() {
  /* globals define, jQuery */

  function generateModule(name, values) {
    define(name, [], function() {
      'use strict';

      return values;
    });
  }

  function camelCase(str) {
    return str.charAt(0).toUpperCase() + str.substr(1);
  }

  var shims = {
    'select2': ['default:core', 'defaults', 'dropdown', 'options', 'results', 'utils'],
    'select2/data': ['ajax', 'array', 'base', 'maximumInputLength', 'maximumSelectionLength', 'minimumInputLength', 'select', 'tags', 'tokenizer'],
    'select2/dropdown': ['attachBody', 'attachContainer', 'closeOnSelect', 'hidePlaceholder', 'infiniteScroll', 'minimumResultsForSearch', 'search', 'selectOnClose'],
    'select2/selection': ['allowClear', 'base', 'multiple', 'placeholder', 'search', 'single']
  };

  for (var moduleName in shims) {
    var values = {};

    shims[moduleName].forEach(function(name) {
      var exportName = name;
      if (name.indexOf('default:') === 0) {
        name = name.slice('default:'.length);
        exportName = name;

        // export as default
        values['default'] = jQuery.fn.select2.amd.require(moduleName + '/' + name);
      }

      // export CamelCased version
      exportName = camelCase(exportName);
      values[exportName] = jQuery.fn.select2.amd.require(moduleName + '/' + name);
    });

    generateModule(moduleName, values);
  }
})();