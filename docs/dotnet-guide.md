# .NET Guide

이 가이드는 .NET 애플리케이션을 배포하는 방법을 가이드합니다. 여러분의 프로젝트 구조는 다소 복잡할 수도 있습니다. 그래서 전체 과정을 이해하는데 어려움이 있을 수 있습니다.

그래서 새로운, 그리고 간단한 구조의 애플리케이션을 만들어서 배포하는 작업을 해 보시는 것이 좋습니다. 이 과정은 총 10분????? 이 걸립니다.

## 시작하기

아래를 참고했습니다.

http://www.bsidesoft.com/?p=3915



먼저 .NET Core 를 설치합니다. 아래 사이트의 안내를 받으세요.

```
https://www.microsoft.com/net/core#macos
```

다음으로 프로젝트 디렉터리를 만듭니다.

```
mkdir dotnet-web-sample
``

그리고 아래와 같이 프로젝트를 생성해 보세요.

```
dotnet new mvc
```

위 명령어를 실행 후 디렉터리에서 확인해 보면 관련 파일들이 쭈루록. 다음으로, 관련 Dependency 를 구성합니다.

```
dotnet restore
```

이제 서버를 실행하면 바로 웹 애플리케이션을 확인할 수 있습니다. 정말 쉽습니다.

```
dotnet run
```

기본 포트는 5000 번입니다. http://localhost:5000 번으로 확인할 수 있습니다.

이 모든 것은 MacOS 에서도 가능합니다.

## 배포하기

애플리케이션을 만들었으면 배포를 해야 합니다. 먼저 ``publish`` 를 통해서 배포할 애플리케이션을 만듭니다. 이 과정은 아래 링크를 참조하세요.

https://docs.microsoft.com/ko-kr/dotnet/core/tools/dotnet-publish?tabs=netcore2x

```
dotnet publish
```