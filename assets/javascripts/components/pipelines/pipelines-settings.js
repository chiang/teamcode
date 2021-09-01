import Vue from 'vue';
import axios from 'axios';
import select2 from '../commons/select2.js';
import template from './pipelines-settings-template';

$(() => new Vue({
  el: document.querySelector('#vue-pipeline-configuration'),
  data() {
    let configData;
    if (document.querySelector('#vue-pipeline-configuration')) {
      configData = document.querySelector('#vue-pipeline-configuration').dataset;
    }
    else {
      configData = {};
    }

    return {
templates: {
java:
`
image: 1.1
build: stage;
`,
javascript:
`# Gradle Wrapper 를 기본으로 사용합니다.
 # Gradle Wrapper 가 없다면 생성 후 커밋해 주세요.
 build:
   script:
     - npm install
     - npm test
`,
gradle:
`# Gradle Wrapper 를 기본으로 사용합니다.
# Gradle Wrapper 가 없다면 생성 후 커밋해 주세요.
build:
  script:
    - './gradlew build'
`,
dotnetCore:
`# .NET Core 애플리케이션을 빌드하기 위한 설정입니다.
# Only use spaces to indent your .yml configuration.
# -----
# You can use any Docker image from Docker Hub, or your own container registry, as your build environment.
image: microsoft/dotnet:onbuild
pipelines:
  default:
    - step:
        script: # Modify the commands below to build your repository.
          - export PROJECT_NAME=yourProjectName
          - export TEST_NAME=yourTestName
          - dotnet restore
          - dotnet build $PROJECT_NAME
          - dotnet test $TEST_NAME
`,
php:
`image: php
build: app
`,
python:
`image: python
build: app
`,
other:
`
build:
  script:
    - echo hello universe!
`
},
      languageTemplate: '',
      yaml: '',
      selectedLanguage: 'java',
      select2Selected: null,//Dummy
      commitUrl: configData.commitUrl,
      commitMessage: '새 Pipeline 설정 파일 추가',
    }
  },
  components: {
    'pipelines-template': template,
    'select2': select2,
  },
  computed: {
    languages() {
      return [
        { id: 'dotnetCore', text: '.NET Core' },
        { id: 'php', text: 'PHP' },
        { id: 'other', text: '기타' }
      ]
    }
  },
  created() {
    this.languageTemplate = this.templates.java;
  },
  methods: {
    chooseTemplate(language) {
      this.languageTemplate = this.templates[language];
      this.selectedLanguage = language;

      var isSelect2Selected = false;
      var vm = this;
      this.languages.forEach(function(ele, index){
        if (ele.id == vm.selectedLanguage) {
          isSelect2Selected = true;
          return;
        }
      });
      if (!isSelect2Selected)
        this.select2Selected = null;

      //console.log('---> isSelect2Selected: ' + isSelect2Selected + ', select2Selected: ' + this.select2Selected);
    },
    templateChanged(content) {
      this.yaml = content;
    },
    commit() {
      var _this = this;
      var params = new URLSearchParams();
      params.append('yaml', _this.yaml);
      params.append('message', this.commitMessage);
      axios.post(this.commitUrl, params)
      .then(function (response) {
        //console.log(response);
      })
      .catch(function (error) {
        console.log(error);
      });
    },
  }
}));