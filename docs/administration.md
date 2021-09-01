
## Runner

Runner 는 CI Pipeline 을 처리하는 Java 기반 애플리케이션입니다. Runner 는 TeamCode 서버가 실행되는 서버와 동일한 
곳에서 실행이 됩니다. Runner 는 Java 8 이상에서 실행할 수 있습니다.

Runner 는 다음과 같이 실행합니다.

```
$ cd $TEAMCODE_HOME/bin
$ ./start-runner.sh
```

### Configuration
Runner 실행 시 필요한 환경 설정은 아래 파일에서 찾을 수 있습니다.

```
$TEAMCODE_HOME/config/runner.yml
```

아래 표는 각 설정에 대한 설명입니다.

```
runner:
  concurrent: 
```

