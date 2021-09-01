
## Docker Image

```
wget
xvfb
curl
git: 1.9.1
java: 1.8u66
maven: 3.0.5
node: 4.2.1
npm: 2.14.7
nvm: 0.29.0
python: 2.7.6
gcc: 4.8.4
```

## Pipelines

```
ci/
 ├-- artifactsFile
		 ├-- 2017/
		     ├-- 05/
		         ├-- {JOB_ID}%256/
                 ├-- {JOB_ID}/artifactsFile.zip
 ├-- pipelines
 		 ├-- 2016/
 		     ├-- 07/
 		         ├-- {PIPELINE_ID}%256/
 		             ├-- {PIPELINE_ID}
 		 ├-- 2017/
 		     ├-- 01/
 		         ├-- {JOB_ID}/artifactsFile.zip
 ├-- logs
     ├-- 201702/
     ├-- 201703/
      	 ├-- 01/
      		   ├-- {JOB_ID}/artifactsFile.zip
  		         
```

빌드의 경우 year/month/mod256({PIPELINE_ID})/{PIPELINE_ID} 형태로 관리합니다. 이 디렉터리에는 Checkout 한 파일들이
저장됩니다. 


### Build Directory 

Build Directory 는 하나의 Pipeline 이 실행될 때마다 하나 씩 생성이 됩니다. 파이프라인 하나는 동시에 실행이 될 수 없기 때문에 
중복 실행 이슈는 없습니다. 다만 Pipeline 개수만큼 생기므로 관리가 절실합니다.

완료된 Pipeline 에 대해서는 주기적으로 삭제해 주는 작업이 필요합니다. 또한, 이미 실행된 파이프라인에 대한 디렉터리가 이미 있는 경우 삭제하는 
작업도 필요합니다.



### Log Directory

Gitlab 의 경우 Bulk 로 로그를 지운다던지 주기적으로 지운다던지 하는 기능은 없습니다. 다만 Job 하나의 로그를 화면이나 API 를 통해서 
삭제하는 기능은 제공합니다.



## Artifacts

Artifacts 는 ``$TC_HOME/data/ci/artifactsFile`` 밑에서 관리합니다. 해당 파일은 Runner 가 빌드를 끝낸 후 파이프라인 설정에 
Artifacts 설정이 있다면 결과물을 복사하는 작업을 수행합니다. 

```

artifactsFile/ 
 ├-- 201702/
 ├-- 201703/
     ├-- 01/
         ├-- {JOB_ID}/artifactsFile.zip
         ├-- {JOB_ID}/artifactsFile.zip
         ├-- {JOB_ID}/artifactsFile.zip
         ├-- {JOB_ID}/artifactsFile.zip
     ├-- 02/
     ├-- 03/
 ├-- 201704/

```

Artifacts 는 Job 마다 생성이 되므로 위처럼 JOB_ID 별로 디렉터리를 만들고 파일을 보관합니다. Job 이 하루에 수 천개 이상 있을 리가 없으므로 이 구성은 적절합니다.

나중에 너무 파일이 많아지는 경우 mod(256) 시스템을 사용하는 것도 방법입니다.

날짜별로 만드는 이유는 나중에 혹시 모를 저장 공간 관리를 위해서 특정 월, 일을 제거하기 편하게 하기 위해서입니다.

### Artifacts Metadata 에 대해서

간단히 요약하면, Artifacts Metadata 는 Archive 된 Artifacts 의 정보입니다. 접근 URL Tree 구조 내용 등을 포함하는 것으로 예상합니다. 

https://gitlab.com/gitlab-org/gitlab-ce/issues/3426

### Artifacts 제거 

기본적으로 Artifact 는 영구적으로 저장됩니다. 

## Deployment

Job 에서 만든 Artifact 를 이용해서 배포를 하는 방식을 살펴봅시다. Job 에서 만든 Artifact 는 다른 Job 이 실행 시 Artifact 를 복사해서 
현재 Job 의 Working Directory 에 옮깁니다. 이렇게 옮긴 파일을 그대로 Job 에서 사용할 수 있습니다.



## Controlling Pipelines and Jobs

### Manual Job

실행 가능한 상태는 아래와 같습니다.

1. Running Stage 내의 Manual Job
2. Failed Stage 내의 Manual Job
3. Canceled Stage 내의 Manual Job
4. Success Stage 내의 Manual Job

### Retry a Pipeline

파이프라인이 Failed 혹은 Canceled 상태이면 Retry 할 수 있습니다.

1. Pipeline 을 Retry 하면 성공한 다음 Stage 내의 모든 실패한, 취소된 Job 들을 모두 Pending 으로 처리합니다. Manual Job 은 그냥 둡니다.
2. 다음 Stage 는 모두 Created 로 변경합니다.

### Retry a Job

1. 완료가 된 상태에서 특정 Job 만 Retry 하면 해당 것만 처리가 됩니다.
2. 오류가 난 상태에서 Retry 를 하면 다시 전체 상태 및 Stage 상태가 Pending -> Running 으로 바꾼다 (그러니까 정상 상태로 간다는 것). 근데 그 다음 Stage 로 넘어가는가? (아니지)
 
### Canceling a Job

Job 을 취소하는 경우 상태 관리는 다음과 같습니다.

1. 한 Job 을 취소하면 전체 Pipeline 상태는 Canceled 가 됩니다.
2. 한 Stage 내에 두 개 이상 Job 이 있을 때 한 Job 을 Cancel 하는 경우 한 Job 은 Canceled 상태가 되며 해당 Stage 는 계속 실행 상태가 됩니다. 
3. 또한, 성공적으로 완료되면 전체 상태는 Canceled 상태가 되며 이후 다른 모든 Stage 는 Skipped 상태가 됩니다.
4. 만약 Skip 처리한 Job 이 Manual Job 이라면 실행 버튼을 살려 줍니다 (Gitlab 에서. 근데 이렇게 하면 이전 Stage 가 완료가 되어야 하는 조건을 가진 Manual Job 이라면 문제가 생길텐데????)


Runner 에서는 network/patch_response.go 파일에서 IsAborted 메소드에서 취소 처리를 합니다. 원격 상태가 Canceled 이거나 Failed 일 때 (Failed 상태는 빌드할 때 나오는 것이지만 CI 서버 측에서도 있을 수 있겠습니다) 취소하는 작업을 합니다.

CI Server 에서는 ``lib/api/runner.rb`` 파일에서 ``patch '/:id/trace' do`` Route 에서 Trace 정보를 업데이트하면서 동시에 서버의 상태를 반환함으로써 따로 Cancel 여부를 확인할 필요가 없어 처리합니다. Server 의 응답과 Header 정보를 Runner 에서 ``TracePatchResponse`` 에 할당하고 전달합니다. 

중요한 것은 Runner 에서 어떻게 Job 을 취소하느냐? 인데, 빠른 취소를 위해서는 로그를 보내는 쪽에서 서버 측의 상태를 체크해서 현재 빌드 작업을 취소하는 방식이 좋습니다 (현재 그렇게 되어 있음).

## Deployment

만약, 이전 Job 에서 만든 Artifacts 를 배포하려고 할 때 Deploy Job 에서 소스 파일을 Checkout 하는 것은 불필요한 작업입니다. 이 경우 아래 변수를 설정해서 Skip 처리할 수 있습니다.

```
deploy-war:
	stage: deploy
	variables:
	  SVN_STRATEGY: none
```

## TODO

### Bitbucket

1. https://blog.revolucija.hr/building-testing-and-deploying-django-app-with-bitbucket-pipelines-32868e12a472

### MS Build

1. http://mixedcode.com/Article/Index?aidx=1072
2. https://msdn.microsoft.com/ko-kr/library/dd394698(v=vs.100).aspx