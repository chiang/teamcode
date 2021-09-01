# Java CI

## Gradle

TeamCode 에서는 Gradle Daemon 을 Turn Off 합니다. Gradle 에서도 CI 환경에서는 Gradle Daemon 을 사용하지 말 것을 권장합니다. TeamCode 에서는
자동으로 Gradle Daemon 을 사용하지 않도록 설정합니다.


### Cache Dependencies

Gradle 만 실행하는 경우 Dependencies 는 모두 아래 디렉터리에 저장됩니다. 

```
~/.gradle/caches/modules-2/files-2.1
```

Docker Container 내에서는 ``root`` 계정을 사용하므로 아래와 같을 것입니다.

```
/root/.gradle/caches/modules-2/files-2.1
```

## Maven

maven 환경이 설정되었다면, 다음의 명령어를 실행할 수 있을 것이다. 만약 -Dmaven 버전을 명시하지 않으면, 실행 시점에 maven 최신 버전이 mvnw 설정되며, 이후 maven이 설치되어 있지 않은 개발자는 mvnw만으로도 maven이 설치되면서 빌드를 할 수 있다. 이렇게 실행하면 위에 설명한 mvnw, mvnw.cmd와 .mvn 디렉토리가 생성되는 것을 확인할 수 있다.

```
mvn -N io.takari:maven:wrapper -Dmaven=3.3.3
```

커스텀한 설정을 위해서 ``settings.xml`` 를 사용하고 싶다면 아래와 같이 설정하시기 바랍니다.

```
mvn -q -B -T4 clean deploy --settings settings.xml
```

```
[ERROR] bootstrap class path not set in conjunction with -source 1.6
/var/opt/teamcode/data/ci/pipelines/2017/08/1/1/4/src/main/java/com/cts/wingsapp/dao/ContentsDAO.java:[12,22] error: unmappable character for encoding ASCII
[ERROR] /var/opt/teamcode/data/ci/pipelines/2017/08/1/1/4/src/main/java/com/cts/wingsapp/dao/ContentsDAO.java:[12,23] error: unmappable character for encoding ASCII
```

위와 같은 오류가 발생하면 아래와 같이 합니다.

```
<plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>1.6</source>
                <target>1.6</target>
                <encoding>UTF-8</encoding>
            </configuration>
        </plugin>
```