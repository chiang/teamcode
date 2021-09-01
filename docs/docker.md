# Docker 다루기 

TeamCode 는 Docker 를 이용해서 빌드를 하고 있습니다. 이때 여러 필수 도구들이 사전에 설치된 기본 이미지를 사용합니다. 이미지에는 Oracle JDK 가 
포함되어 있는데 이를 Public 하게 배포하는 것은 라이선스 정책이 명확하지 않아서 하지 않고 그때 그때 빌드에서 시스템에 구성합니다 (하지만 미리 만들어 두는 것도 나중을 위해서 좋을 것 같습니다).

TODO AWS S3 같은 곳에 넣어 두기

이미지 이름은 아래와 같습니다. 
```
teamcode/default-image:latest
```

## Container ID

Build Container ID 는 아래와 같이 만들어 집니다.

```
runner-a4f9043d-project-2-concurrent-1-build
```

중간은 Runner 의 Short Token 이며 프로젝트마다 순차적으로 번호가 부여됩니다.

## Docker Container 와 Host 간의 관계

Docker Container 내에서 빌드를 할 때는 아래 항목들이 CI Server 혹은 Host Machine 과 공유를 합니다. 중요한 Concept 은 Docker Container 가 직접 Host 파일 시스템에 접근하지 못하게 하는 것입니다.

만에 하나 모를 실수로 작성한 Script 나 의도적으로 작성한 Script 등은 시스템을 망가뜨릴 수 있습니다.

아래는 Docker Container 가 아니라 Runner 에서 공유하는 대상입니다. 이 대상들은 어차피 User Script 로 접근할 수 없는 작업이기 때문에 보안 상 문제가 없습니다.

1. Repository Root
2. Artifacts

 