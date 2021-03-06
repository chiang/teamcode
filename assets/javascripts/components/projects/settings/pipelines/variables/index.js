import Vue from 'vue';
import axios from 'axios';
import toastr from 'toastr';

if (document.querySelector('#pipeline-settings-variables')) {
	const VARIABLE_NAME_PATTERN = /^[a-zA-Z_$][a-zA-Z_$0-9]*$/;
	$(() => new Vue({
	  el: document.querySelector('#vue-pipeline-settings-variables'),
	  data() {
	    const vueData = document.querySelector('#pipeline-settings-variables').dataset;

	    return {
	      endpoint: vueData.endpoint,
	      baseEndpoint: vueData.baseEndpoint,
	      addEndpoint: vueData.addEndpoint,
	      variableName: '',
	      variableValue: '',
	      secured: false,
	      beforeEditCacheName: null,
	      beforeEditCacheValue: null,
	      editedVariable: null,
	      isLoading: false,
	      updating: false,
	      variables: null,
	    };
	  },
	  components: {
	    //'users-list': UsersList
	  },
	  computed: {
			shouldRenderEmptyStates() {
	      return !this.isLoading && this.variables != null && this.variables.length == 0;
	    },
	  },
	  created() {
	    this.isLoading = true;
	  },
	  mounted() {
			axios.get(this.endpoint)
	    .then((response)=>{
	      this.isLoading = false;
	      this.variables = response.data;
	    })
	    .catch((error)=>{
	      //TODO error handling
	      this.isLoading = false;
	    });
	  },
	  methods: {
			addVariable() {
				this.disableForm();
				$('#input-pipeline-variable-name, #input-pipeline-variable-value').removeClass('invalid');
				setTimeout(()=>{
					if (this.variableName.trim().length > 0 && this.variableValue.trim().length > 0) {
	          var newArr = this.variables.filter((variable) => {
	            return variable.name === this.variableName;
	          });
	          if (newArr.length > 0) {
	            $('#input-pipeline-variable-name').addClass('invalid');
	            this.enableForm();
	            return;
	          }

	          if (!VARIABLE_NAME_PATTERN.test(this.variableName)) {
	            $('#input-pipeline-variable-name').addClass('invalid');
	            this.enableForm();
	            return;
	          }
	          if (this.variableName.trim().length < 2 || this.variableName.trim().length > 20) {
	            $('#input-pipeline-variable-name').addClass('invalid');
	            this.enableForm();
	            return;
	          }

						var params = new URLSearchParams();
	          params.append('name', this.variableName.trim().toUpperCase());
	          params.append('value', this.variableValue.trim());
	          params.append('secured', this.secured);
						axios.post(this.addEndpoint, params)
	          .then((response)=>{
	            const serverVariable = response.data;
	            if (this.secured)
	              this.variables.push({id: serverVariable.id, name: this.variableName.toUpperCase(), value: '??????????????????', secured: this.secured});
	            else
	              this.variables.push({id: serverVariable.id, name: this.variableName.toUpperCase(), value: this.variableValue, secured: this.secured});

	            this.variableName = '';
	            this.variableValue = '';
	            this.secured = false;

	            $('#input-pipeline-variable-name').removeClass('invalid');
	            $('#input-pipeline-variable-value').removeClass('invalid');

	            this.enableForm();
	          })
	          .catch((error)=>{
	            toastr.error(error.response.data.message, '?????? ?????? ??????');

	            this.enableForm();
	            $('#input-pipeline-variable-name').addClass('invalid');
	            $('#input-pipeline-variable-name').focus();
	          });
	        }
	        else {
	          var focused = false;
	          if (this.variableName.trim().length == 0) {
	            $('#input-pipeline-variable-name').addClass('invalid').focus();
	            this.variableName = '';
	            focused = true;
	          }
	          else {
	            $('#input-pipeline-variable-name').removeClass('invalid');
	          }
	          if (this.variableValue.trim().length == 0) {
	            $('#input-pipeline-variable-value').addClass('invalid');
	            this.variableValue = '';
	            if (!focused)
	              $('#input-pipeline-variable-value').focus();
	          }
	          else {
	            $('#input-pipeline-variable-value').removeClass('invalid');
	          }

	          this.enableForm();
	        }
				}, 100);
			},
			removeVariable(index,variable, event) {
				let _btn = $(event.target);
				if (!_btn.hasClass('btn'))
					_btn = _btn.closest('td').find('.btn-remove');
				if (!confirm(_btn.data('confirm'))) {
					return;
				}

				var params = new URLSearchParams();
	      params.append('_method', 'delete');
	      toastr.options.hideDuration = 1;
	      toastr.options.timeOut = 5000;
	      toastr.options.closeOnHover = false;
	      //toastr.options.hideMethod = 'slideUp';
	      axios.post(this.baseEndpoint + '/' + variable.id, params)
	      .then((response)=>{
	        this.variables.splice(index, 1);
	        let message = `?????? ?????? '${variable.name}' ??? ??????????????????. ?????? ?????????????????? ??? ????????? ??????????????? ?????? ??? ?????? ????????? ?????????.`;
	        toastr.info(message, '????????? ??????????????????.');
	      })
	      .catch((error)=>{
	        //TODO error handling
	      });
			},
			editVariable(variable, event) {
				if (this.updating)
					return;

				if (this.editedVariable != null) {
					//?????? ????????? ?????? Edit ????????? ??????????????? ?????? Variable ?????? ????????? ???????????? ??????.
					this.editedVariable.name = this.beforeEditCacheName;
					this.editedVariable.value = this.beforeEditCacheValue;
				}

	      this.beforeEditCacheName = variable.name;
	      this.beforeEditCacheValue = variable.value;
	      if (variable.secured)
	        variable.value = '';
	      this.editedVariable = variable;

	      var _target = $(event.target);
	      if (_target.get(0).tagName != 'td') {
	        _target = _target.closest('td');
	      }
				setTimeout(()=>{_target.find('input').focus()}, 100)
	    },
	    updateVariable(variable, event) {
	      this.disableForm();
	      const control = $(event.target).closest('tr').find('.variable-name');
	      control.removeClass('invalid');

	      const vn = variable.name.trim().toUpperCase();
	      const vv = variable.value.trim();
	      if (vn == this.beforeEditCacheName && vv == this.beforeEditCacheValue) {
	        this.enableForm();
	        this.editedVariable = null;
	        this.beforeEditCacheName = null;
	        this.beforeEditCacheValue = null;
	        return;
	      }


	      if (!VARIABLE_NAME_PATTERN.test(vn)) {
	        control.addClass('invalid');
	        return;
	      }

				this.toggleUpdateSpinner(event, true);
	      setTimeout(()=>{
	        var params = new URLSearchParams();
	        params.append('name', variable.name.trim().toUpperCase());
	        params.append('value', variable.value.trim());
	        axios.post(this.baseEndpoint + '/' + variable.id, params)
	        .then((response)=>{
	          this.enableForm();

	          variable.name = variable.name.trim().toUpperCase();
	          if (variable.secured)
	            variable.value = this.beforeEditCacheValue;

	          this.editedVariable = null;
	          this.beforeEditCacheName = null;
	          this.beforeEditCacheValue = null;
	          this.toggleUpdateSpinner(event, false);
	        })
	        .catch((error)=>{
	          //TODO error handling
	          this.enableForm();
	          this.editedVariable = null;
	          this.beforeEditCacheName = null;
	          this.beforeEditCacheValue = null;
	          this.toggleUpdateSpinner(event, false);
	        });
	      }, 1000);
	    },
	    cancelEdit: function (variable) {
	      this.editedVariable = null;
	      variable.name = this.beforeEditCacheName;
	      variable.value = this.beforeEditCacheValue;
	      this.beforeEditCacheName = null;
	      this.beforeEditCacheValue = null;
	      //console.log('cancel: ' + this.beforeEditCacheName + ', ' + this.beforeEditCacheValue);
	    },
	    validateVariable(variable, event) {
	      var _target = $(event.target);
	      if (_target.hasClass('variable-name')) {
	        const vn = variable.name.trim();
	        if (vn.length == 0) {
	          variable.name = this.beforeEditCacheName;
	        }
	      }
	      else if (_target.hasClass('variable-value')) {
					const vn = variable.value.trim();
	        if (vn.length == 0) {
	          variable.value = this.beforeEditCacheValue;
	        }
	      }
	    },
	    disableForm() {
	      $('#btn-add-pipeline-variable').addClass('disabled').prop('disabled', true);
	      $('#input-pipeline-variable-name, #input-pipeline-variable-value, #cb-pipeline-variable-secret').prop('disabled', true);
	    },
	    enableForm() {
	      $('#btn-add-pipeline-variable').removeClass('disabled').prop('disabled', false);
	      $('#input-pipeline-variable-name, #input-pipeline-variable-value, #cb-pipeline-variable-secret').prop('disabled', false);
	    },
	    toggleUpdateSpinner(event, isRender) {
	      this.updating = isRender;
	      const _tr = $(event.target).closest('tr');
	      if (isRender) {
	        _tr.find('.fa-spinner').show();
	        _tr.find('.btn').hide();
	      }
	      else {
	        _tr.find('.fa-spinner').hide();
	        _tr.find('.btn').show();
	      }
	    },
	    moreHelp(event) {
	      $(event.target).hide();
	      $('.help-block-more').show();
	    },
	  },
	  template: `
	  <div class="row">
	    <div class="col-md-12 project-feature">
	      <label class="label-light">??????????????? ?????? ??????</label>
	      <p class="help-block">?????? ??? ?????? ??? Runner ??? ???????????? ???????????????. ?????? ???????????? ????????? ???????????? ????????? ?????????.</p>
	      <a href="#!" class="btn-more" @click="moreHelp($event)">?????? ??? ??????</a>
	      <p class="help-block help-block-more">
	        ?????? ????????? ????????? ?????? ????????? _ (Underscore) ??? ????????? ??? ????????? ???????????? ???????????? ???????????????. ?????? ?????? ????????? ?????? 2??? ?????? ?????? 20??? ????????? ???????????????.
	        ????????? ????????? ??????????????? ?????? ??? ??? ????????? ????????? Secured ??? ???????????? ???????????????. <strong>Secured</strong> ??? ???????????? ????????? ???????????????,
	         ?????? ?????? ?????? ????????? ???????????? ????????????. ??????, ????????? <code>echo $VARIABLE</code> ??? ?????? ???????????? ?????? ????????? ???????????????.
	      </p>
	    </div>
	    <div class="col-md-12 prepend-top-20">
	      <table class="table variables">
	        <thead>
	          <tr class="variable-form">
	            <th><input id="input-pipeline-variable-name" v-model="variableName" type="text" class="form-control" placeholder="??????" autocomplete="off" spellcheck="false" maxlength="20"/></th>
	            <th><input id="input-pipeline-variable-value" v-model="variableValue" type="text" class="form-control" placeholder="???" autocomplete="off" spellcheck="false" maxlength="500"/></th>
	            <th class="text-right"><input id="cb-pipeline-variable-secret" type="checkbox" v-model="secured"/><label for="cb-pipeline-variable-secret">Secured</label><button id="btn-add-pipeline-variable" href="#!" type="text" class="btn" @click="addVariable">??????</button></th>
	          </tr>
	        </thead>
	        <tbody v-if="shouldRenderEmptyStates">
	          <tr>
	            <td colspan="3" class="text-center">?????? ???????????? ???????????????.</td>
	          </tr>
	        </tbody>
	        <tbody v-if="!shouldRenderEmptyStates" :class="{ updating: updating}">
	          <tr v-for="(variable, index) in variables" :class="{ editing: variable == editedVariable }">
	            <td>
	              <span class="variable-label" @click="editVariable(variable, $event)">{{variable.name}}</span>
	              <input class="edit form-control variable-name" type="text" placeholder="????????? ???????????????"
	                     v-model="variable.name"
	                     :disabled="updating ? true : false"
	                     @blur="validateVariable(variable, $event)"
	                     @keyup.esc="cancelEdit(variable)" />
	               <i class="fa fa-pencil" aria-hidden="true" @click="editVariable(variable, $event)"></i>
	            </td>
	            <td>
	              <span v-if="variable.secured == 'false'" class="variable-label" @click="editVariable(variable, $event)">{{variable.value}}</span>
	              <span v-if="variable.secured" class="variable-label" @click="editVariable(variable, $event)">......</span>
	              <input class="edit form-control variable-value" type="text" placeholder="?????? ???????????????"
	                     v-model="variable.value"
	                     :disabled="updating ? true : false"
	                     @blur="validateVariable(variable, $event)"
	                     @keyup.esc="cancelEdit(variable)" />
	              <i class="fa fa-pencil" aria-hidden="true" @click="editVariable(variable, $event)"></i>
	            </td>
	            <td class="text-right">
	              <a href="#!" class="btn btn-sm btn-save" @click="updateVariable(variable, $event)">??????</a>
	              <a href="#!" class="btn btn-sm" @click="cancelEdit(variable)">??????</a>
	              <a href="#!" class="btn btn-remove" v-on:click="removeVariable(index,variable, $event)" data-confirm="??? ?????? ????????? ?????????????????????????"><i class="fa fa-trash-o" aria-hidden="true"></i></a>
	              <i class="fa fa-spinner fa-spin" aria-hidden="true" />
	            </td>
	          </tr>
	        </tbody>
	      </table>
	    </div>
	  </div>
	  `
	}));
}