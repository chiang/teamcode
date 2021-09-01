
## artifactsFile

**artifactsFile** 는 Job 이 완료된 후에 Job 이 생성한 파일들 혹은 디렉터리 들을 보관하기 위한 설정입니다. 대상 파일들이나 디렉터리들은 
프로젝트 작업 디렉터리 내에 있는 것만 가능합니다. 특정 Job 이 생성한 Artifacts 를 다른 Job 에서 사용하려면 dependencies 를 설정합니다. 

아래는 간단한 예시입니다.

```yaml
artifactsFile:
  paths:
    - binaries/
    - .config
```

위 설정은 ``binaries`` 디렉터리 내의 모든 파일과 ``.config`` 파일을 Artifacts 로 저장합니다. 위 파일들은 ``artifacts.zip`` 으로 저장됩니다.

### artifactsFile:name

``name`` 속성은 Artifacts 아키이브의 이름을 지정합니다. 이 설정을 이용해서 자동으로 생성되는 Artifacts 아카이브 이름을 원하는 이름으로 
변경할 수 있습니다. The ``artifacts:name`` variable can make use of any of the predefined variables. The 
default name is ``artifacts``, which becomes ``artifacts.zip`` when downloaded.

#### Example Configurations

To create an archive whit a name of the current job:

```yaml
job:
  artifacts:
    name: "$CI_JOB_NAME"
```


### artifacts:when

...

### artifactsFile:expire_in

## dependencies

이 속성은 다른 Job 에서 사용할 ``artifacts`` 를 정의합니다. 

이전 Stage 에서 생성된 모든 ``artifacts`` 는 현재 Job 에 모두 전달됩니다. 현재 Job 에서는 모두


## Deployment

일반적으로 배포는 이전 혹은 현재 Job 에서 생성된 Artifacts 를 사용합니다. 아래와 같은 설정을 살펴봅시다.

```yaml
webapp:
  stage: build
  script:
    - mvn package -U
    
  artifacts:
    paths: 
      - target/*.war
      
deploy-prod:
  stage: deploy
  script:
    - fabric 
  when: manual
```

``-U`` 는 ``--update-snapshots`` 를 의미합니다. 위의 스크립트는 빌드를 한 후에 모든 WAR 파일을 Artifact 로 등록합니다. 
 

### Case Study

#### 멀티 프로젝트인 경우 빌드 및 배포 설정하는 방법

한 프로젝트에 다시 여러 프로젝트가 있는 경우, 빌드하는 대상이 다르고 배포하는 서버도 다릅니다. 이러한 경우를 ...
