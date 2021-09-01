-- 아래 내용들은 모두 예전 문서입니다.
## 각종 가이드, 안내 (마지막 개발하던 내용들)
### TODOs

1. Log 디렉터리를 다른 파일 시스템에 마운트해서 처리할 수 있도록 해 주자. 보통 그렇게 관리하니까.
2. 계정을 모두 teamcode 로 실행되도록. 현재는 모두 root 로 설정이 됨. 아마도 docker 때문 같음.
3. application.yml 파일도 설치 세팅하도록
4. Runner 설치하도록...

### 개발 환경 세팅

#### PC
Mac 인 경우 아래 명령어로 최신 버전 Subversion Client (1.9.x) 를 설치합니다. 설치 시간이 다소 소요됩니다 (약 30분 이내).
```
brew install subversion --with-java
```
설치 여부를 확인하려면 아래와 같이 해 보세요.
```
brew info subversion
```
만약 이전 버전을 삭제하고 싶다면 아래와 같이 실행하면 됩니다.
```
brew remove subversion (or svn)
```
Brew 로 설치한 경우 Library Path 가 정확히 Fix 되지 않으므로 아래와 같이 실행 스크립트에 Path 를 설정합니다.
```
-Djava.library.path=/usr/local/Cellar/subversion/1.9.5_2/lib
```


#### 웹

```bash
sudo npm install -g vue-cli
sudo npm install -g vue-template-compiler
sudo npm install svg-inline-loader

sudo npm install --save-dev webpack babel-core babel-loader
sudo npm install --save vue
sudo npm install --save-dev vue-loader
//replace to vue-loader : sudo npm install --save-dev raw-loader
sudo npm install -g vue-cli (for vue-loader)
sudo npm install --save jquery
sudo npm install --save visibilityjs
sudo npm install --save axios
sudo npm install --save toastr
sudo npm install --save vuejs-datepicker``````
```

##### Webpack

Node Version 을 확인합니다.

```
node --version
```

우리는 8 버전 대를 사용해야 하므로 현재 설치되어 있는지 확인합니다.

```
nvm ls
```

>혹시 다른 사이트에서 rules나 use 대신 loaders를 쓰고, options 대신 query를 쓰는 곳이 있다면, 웹팩1에 대한 강좌입니다. 웹팩2에서 바뀌었습니다. (물론 하위호환을 위해 여전히 지원하긴 합니다)

혹시 아래와 같은 오류가 발생한다면?

-bash: webpack: command not found

아래와 같이 실행해서 webpack 을 Global 하게 설정합니다.

```
sudo npm i webpack -g
```

만약, 처음 이 저장소를 가져와서 구성하는 경우에는 아래 명령어로 Module 들을 설치해 줘야 합니다.

```
sudo npm install
```

위 명령어를 실행하면 ``package.json`` 에서 정보를 읽어와서 자동으로 설치를 합니다.

빌드는

webpack

항상 자동으로 하고 싶다면?

webpack -w or webpack --watch

Vue 를 화면에서 로딩하는데 아래와 같이 오류가 발생하면?

[Vue warn]: You are using the runtime-only build of Vue where the template compiler is not available. Either pre-compile the templates into render functions, or use the compiler-included build. (found in <Root>)

 webpack.config.js 에 아래 항목 추가해야 Template 을 처리할 수 있음.

```
 resolve: {
   alias: {
     'vue$': 'vue/dist/vue.esm.js' // 'vue/dist/vue.common.js' for webpack 1
   },
 },
 ```



### Application Build

1. gradle -x test build
2. ./build-docker-image.sh 로 Docker 이미지 생성; tag 를 계속 동일한 것으로 사용하면, 이전 빌드의 Repository 와 Tag 는 <none> 으로 표기되고 새로 생성이 됩니다.

### Docker Build

Docker 이미지를 빌드하는 방법은 아래와 같습니다.

1. build.gradle 파일에서 ``buildDocker`` Task 의 버전 정보를 하나 올려줍니다.
2. ``./gradlew clean -x test build buildDocker`` 명령어로 빌드합니다.
3. docker push teamcode/teamcode:tag 와 같이 Docker Repository 에 Push 합니다.

docker login teamcode

Push 한 후 아래 주소에서 결과를 확인할 수 있습니다. 아이디는 chiang@rightstack.net 입니다.

```
https://hub.docker.com/r/teamcode/teamcode
```

Push 한 후 개발 서버에서 테스트를 해 봅니다. 개발 서버에 접속 후 해당 이미지를 Pull 합니다.

```
docker pull teamcode/teamcode:0.67
```
이미지를 잘 받았다면 이전 프로세스를 종료하고 다시 실행합니다.
```
$ docker stop [container-id]
$ docker rm [container-id]
```

docker run -d --name tc -h tc --volumes-from tc-home -e TZ="Asia/Seoul" -e XMS=2048m -e XMX=2048m --network tcnet teamcode/teamcode:0.67

그리고 아래와 같이 실행합니다.

```
docker run -d --name tc ec66c7083572

```


TODO 위 방식의 개선점으로 tagVersion 을 Application Version 으로 대치하게끔 구성하는 것입니다. 자동화를 위해서 말이죠.


### Theme Build


#### 초기 설정

```
$ sudo npm install --save-dev gulp-concat


```



최초에는 Bower 를 사용합니다.

```
sudo bower install emojione --allow-root
```


아래 명령어로 ``css`` 를 빌드합니다.

```
$ gulp
```

결과물은 ``assets/public`` 밑에 생성됩니다. Gulp 가 설치되어 있지 않다면 아래와 같이 설치합니다.

```
$ sudo npm install gulp-sass --save-dev
```


### Emoji

Emoji 는 Emojione 을 사용합니다. Emoji 를 기본적으로 지원하는 웹 표준은 없습니다. 이를 처리하기 위해서는
Emoji 를 Font Icon 화 하거나, CSS Sprite, SVG 를 이용하는 방법밖에 없습니다. :wave: 와 같은 것을
Shortname 이라고 하는데, 이 Shortname 은 Emojione 에서 제공하는 Javascript Converter 를 가지고서
처리를 하면 됩니다. Server Side 에서 처리하는 것도 가능하겠지만, 일단은 Javascript 로 합니다.

만약, Emoji 로 작성한 Cotnents 가 있다면 어찌하냐고요? 그냥 Shortname 만 보이겠죠.

아래를 참고하세요.

https://demos.emojione.com/sprites-png.html

세팅은 아래와 같이 합니다. Webpack 과 통합하기 위해서 먼저 Node JS 방식으로 설치합니다.

```javascript
sudo npm install emojione
```

다음으로 아래와 같이 ``assets/javascripts/components/commons/emoji`` 디렉터리를 생성하고 ``index.js`` 파일을 생성합니다.

그리고 아래와 같이 코드를 작성합니다.
```javascript
import emojione from 'emojione';

emojione.imageType = 'png';

$('tc-emoji').each(function() {
    var _this = $(this);
    var _shortName = _this.data('name');
    // use .shortnameToImage if only converting shortnames (for slightly better performance)
    //var converted = emojione.shortnameToUnicode(_shortName);
    var converted = emojione.shortnameToImage(_shortName);
    _this.html(converted);
});
```
위 코드는 ``tc-emoji`` 라는 태그를 만나면 모두 emoji 로 변환하는 코드입니다.

## Setup 방법

아래와 같이 순서대로 Docker Image 를 설치합니다.

1. teamcode/subversion
2. teamcode/teamcode

이를 실행하기 위한 docker-compose 파일은 아래와 같습니다.


# Features

1. Repository Management - with DB, Subversion
2. User Management - with DB, Subversion
3. Commit Management - with DB Subversion
4.


# Validation

## Project Name
1. Path can contain only letters, digits, '_', '-' and '.'. Cannot start with '-', end in '.git' or end in '.atom'
2. 이름이 대소문자를 가리지는 않지만, 동일한 이름을 사용할 수는 없습니다. 즉, ChiangPero == chiangpero 는 같은 것입니다.


# Integrations

## Turbolink

https://github.com/turbolinks/turbolinks

# Architecture

## Content Detection

Repository 내부에 있는 Contents 에 대해서 Mimetype 을 인식하려고 할 때 Subversion 에 있는 svn:mime-type 을 사용하기에는 조금 기능이
부족하다. png 파일을 커밋해서 보았더니 octet-stream 으로 나오더라.



## Issue

Issue 는 Github 나 Gitlab 에서 관리하는 매우 심플한, 하지만 유연한 방식으로 구성합니다. 예를 들어 Issue Status 는 딱 두 개의 유형만
다룹니다. OPENED, CLOSED 입니다. 식별은 아이디로 하는데 Gitlab 에서는 iid 라는 필드로 관리합니다.

### Workflow


# Archiecture

## Entity

updatedAt, updatedBy 가 null 이면 만들기는 했지만 수정한 적이 없는 Entity 로 보면 된다.

### Content

title, titleHtml 과 같이 관리하는 경우는 어떤 경우인가? (Gitlab like)


# Release Plan

## Version 3.1

### 사용자
1. 자기가 프로젝트를 조회한 후 가입 신청을 할 수 있게 하기 (이 기능은 관리자에서 제어할 수 있도록)


## Version 3.2
### 관리자
1. 사용자 Impersonate 기능 (Gitlab 과 같은 기능)
2. Monitoring / Requests Profiles: Static 파일 같은 것 분리. 100 개 이상 출력 가능하도록. JSON 포맷.


# Subversion

## Repository Format

db/format 파일의 버전 번호과 7이면 1.9 부터 지원하는 것으로 1.8 Client 에서 file:/// 로 접근 못함.

접근 가능한 버전은 1 ~ 6 사이가 되어야 함.