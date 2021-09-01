

## Mac 에서 설치 (개발 환경)

### Docker 설치

먼저 Docker 를 설치합니다.

### 네트워크 구성

Container 간에 통신을 위한 네트워크를 구성합니다 (docker network create tcnet).

### 데이터베이스 설치

기본 데이터베이스는 PostgreSQL 입니다. 다른 것을 지원할 계획은 아직 없습니다. 

먼저 ``docker pull postgres`` 으로 이미지를 가져옵니다.

다음으로 아래 명령어로 데이터를 저장할 Volume Container 를 생성합니다.

```
docker create -v ~/my-temp/teamcode-home/data/postgresql/data:/var/lib/postgresql/data --name tc-data busybox /bin/true
```

다음으로 PostgreSQL 데이터베이스 파일을 저장할 디렉터리를 만듭니다.
 
```
mkdir -p ~/my-temp/teamcode-home/data/postgresql/data
```

이제 아래와 같이 Container 를 실행해 봅니다. Port 를 노출시켜 외부에서도 확인할 수 있게 구성합니다.

```
docker run -d --name tc-db -h tc-db -p 5432:5432 -e POSTGRES_PASSWORD=******** --volumes-from tc-data -e TZ="Asia/Seoul" --network tcnet postgres
```

SQLDeveloper 같은 도구로 접속할 때는 postgres / tcsa 로 로그인합니다.

다음으로 기본 데이터베이스를 설치합니다.

접속은 아래와 같이 합니다.

```
$ docker exec -it tc-db /bin/bash
psql -h localhost -p 5432  -U postgres --password

```

그리고 아래와 같이 데이터베이스와 사용자를 생성합니다.

```
CREATE USER teamcode WITH PASSWORD 'tcdb';
CREATE DATABASE teamcode WITH owner=teamcode template=template0 encoding='UTF8';
GRANT ALL PRIVILEGES ON DATABASE teamcode to teamcode;
```

다시 ``teamcode`` 로 로그인 합니다. 그리고 아래와 같이 Schema 에 권한을 부여합니다. FIXME 잘 안됨. 필요 하나?

```
GRANT ALL ON SCHEMA public to teamcode;
```


### Apache Subversion, HTTPD 구성

이제 Subversion 서비스와 HTTPD Reverse Proxy 등을 위한 Container 를 구성해 보겠습니다. 먼저 아래와 같이 Docker Image 를 다운로드합니다.

```
docker pull teamcode/subversion:latest
```

다음으로 필요한 Volume Container 들과 HTTP Container 를 만듭니다. 

>여기서 중요! Host Name 은 설치해서 서비스하는 곳 이름을 적어 줍니다. 이 이름은 application.yml 상의 external-url 과 동일해야 합니다.

```
docker create -v ~/my-temp/teamcode-home:/var/opt/teamcode --name tc-home busybox /bin/true
docker create -v ~/my-temp/teamcode-home/data/repositories:/var/opt/subversion/repositories --name tc-repo busybox /bin/true
docker create -v ~/my-temp/teamcode-home/config/httpd:/etc/subversion/httpd --name tc-httpd-config busybox /bin/true
docker create -v ~/my-temp/teamcode-home/data/httpd/htdocs:/var/opt/subversion/httpd/htdocs --name tc-htdocs busybox /bin/true
docker create -v ~/my-temp/teamcode-home/logs/httpd:/var/log/httpd --name tc-httpd-logs busybox /bin/true
docker run -d --name tc-httpd -h tc-httpd -p 80:80 --volumes-from tc-httpd-config --volumes-from tc-httpd-config --volumes-from tc-repo --volumes-from tc-httpd-logs -e TZ="Asia/Seoul" --entrypoint /opt/subversion/assets/entrypoint.sh --network tcnet teamcode/subversion:latest
```

``tc-home`` 은 TeamCode Home 을 모두 바라보는 Volume Container 입니다. runner 에서도 사용합니다. TeamCode 서버는 모든 데이터에 접근이 가능해야 하므로 이렇게 해야 합니다.



아래는 비상 교육 서비스에서 구성한 예입니다. Host Name 을 실제 서비스하는 URL 인 svn.visang.com 으로 구성했습니다.

```
docker run -d --name tc-httpd -h svn.visang.com -p 80:80 --volumes-from tc-httpd-config --volumes-from tc-httpd-config --volumes-from tc-repo --volumes-from tc-httpd-logs -e TZ="Asia/Seoul" --entrypoint /opt/subversion/assets/entrypoint.sh --network tcnet teamcode/subversion:latest
```

이제 아래와 같이 URL 로 접속해 봅니다. 기본적으로 Twitter Bootstrap Project 를 예제 저장소로 제공하고 있습니다.

```
http://localhost/repos/bootstrap/
```

### TeamCode 서버 띄우기

서버를 띄우기 전에 먼저 Subversion Library 를 구성해야 합니다. 리눅스 등에서는 자동으로 링크가 되지만 개발 PC 에서는 임의로 설정해야 합니다.

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
-Djava.library.path=/usr/local/Cellar/subversion/1.9.7_1/lib
```


이제 마지막으로 아래와 같이 서버를 구동합니다.

```
./start.sh
```

```
http://localhost:8080
```

admin / h!5ive
lala / land

## 설치


## 순서

아래는 빠른 이해를 돕기 위한 전체 설치 순서입니다. CentOS 7 기준입니다.

### 필요 패키지 설치

CentOS 를 기본으로 설치한 경우 netstat 명령 등을 사용할 수 없습니다. 모니터링을 위해서 아래 도구를 설치합니다.

```
yum install net-tools
```

다음으로 JDK 를 설치합니다. 가급적 Oracle JDK 1.8 최신 버전을 다운로드 받아서 설치합니다. 설치는 $TEAMCODE_HOME/runtimes 밑에 합니다. 그리고 JAVA_HOME 과 PATH 를 설정합니다.

다음으로 Subversion Client 를 설치합니다. 이 클라이언트는 테스트 및 Runner 에서 사용합니다.

#### Step 1: Setup Yum Repository

Firstly we need to configure yum repository in our system. Create a new repo file /etc/yum.repos.d/wandisco-svn.repo and add following content as per your operating system version.

[WandiscoSVN]
name=Wandisco SVN Repo
baseurl=http://opensource.wandisco.com/centos/$releasever/svn-1.9/RPMS/$basearch/
enabled=1
gpgcheck=0

#### Step 2: Install Subversion 1.9

Before installing latest package remove existing subversion packages from system to remove conflict.

```
# yum remove subversion*
```

Now install latest available Subversion package using yum command line package manager utility.

```
# yum clean all
# yum install subversion
```

#### Step 3: Verify Subversion Version

At this stage you have successfully install Subversion client on your system. Lets use following command to verify version of svn client.

```
# svn --version
svn, version 1.9.5 (r1770682)
```


### 계정 추가

teamcode 라는 계정을 추가합니다. 그리고 UID 를 알와 같이 변경합니다.

```
sudo usermod --uid 578 teamcode
sudo groupmod --gid 578 teamcode
```

만약 아래와 같이 메시지가 나오면 현재 User 로 실행되고 있는 Process 가 있다는 의미이므로 찾아서 종료합니다.

```
usermod: user teamcode is currently used by process 15037
```



### Sudoer 설정

teamcode 계정은 root Permission 계정을 많이 사용하므로 아래와 같이 sudoer 를 설정합니다. sudoer 파일은 권한 설정을 변경해 주어야 합니다.

1. root로 사용자 전환 (su -)
2. /etc/sudoers의 파일 permission 변경; chmod u+w /etc/sudoers
3. /etc/sudoers에 일반 사용자 등록; teamcode ALL=(ALL) ALL
4. /etc/sudoers 퍼미션 원복
5. /etc/sudoers는 440 퍼미션이어야 함
6. chmod u-w /etc/sudoers


### Docker 설치 

아래 링크를 참고해서 설치합니다.

https://docs.docker.com/engine/installation/linux/docker-ce/centos/#install-using-the-repository

설치가 끝난 후에는 아래와 같이 현재 계정 (우리는 teamcode) 에 적절히 권한을 줍니다. 아래와 같이 권한을 주지 않으면 docker 명령어를 실행할 때마다 sudo 를 입력해 줘야 합니다.

```
sudo usermod -a -G docker $USER
```

### Teamcode Container 구성

먼저 아래와 같이 전용 Network 를 구성합니다. Network 를 구성하면 해당 Network 를 사용하는 Container 간에 통신 시 Host Name 으로 통신할 수 있어 편리합니다.

```
docker network create tcnet
```

Network 가 있는지 확인하려면 아래와 같이 명령어를 실행합니다.

```
docker network ls
```

#### 데이터베이스 구성

이제 PostgreSQL Container 를 설치합니다. 먼저 아래와 같이 이미지를 다운로드 받습니다.

```
docker pull postgres
```

다음으로 PostgreSQL 데이터베이스 파일이 저장될 곳을 아래와 같이 Volume Container 로 만듭니다. ``/home/teamcode`` 는 기본 설치 디렉터리입니다.

```
docker create -v /home/teamcode/data/postgresql/data:/var/lib/postgresql/data --name tc-data busybox /bin/true
```

이제 PostgreSQL 데이터베이스 파일을 저장할 디렉터리를 만듭니다.
 
```
mkdir -p /home/teamcode/data/postgresql/data
```

이제 아래와 같이 Container 를 실행해 봅니다.

```
docker run -d --name tc-db -h tc-db -e POSTGRES_PASSWORD=******** --volumes-from tc-data -e TZ="Asia/Seoul" --network tcnet postgres
```

#### Apache Subversion, HTTPD 구성

이제 Subversion 서비스와 HTTPD Reverse Proxy 등을 위한 Container 를 구성해 보겠습니다. 먼저 아래와 같이 Docker Image 를 다운로드합니다.

```
docker pull teamcode/subversion:latest
```

다음으로 필요한 Volume Container 들과 HTTP Container 를 만듭니다. 여기서 중요! Host Name 은 설치해서 서비스하는 곳 이름을 적어 줍니다. 이 이름은 application.yml 상의 external-url 과 동일해야 합니다.

```
docker create -v /home/teamcode/data/repositories:/var/opt/subversion/repositories --name tc-repo busybox /bin/true
docker create -v /home/teamcode/config/httpd:/etc/subversion/httpd --name tc-httpd-config busybox /bin/true
docker create -v /home/teamcode/data/httpd/htdocs:/var/opt/subversion/httpd/htdocs --name tc-htdocs busybox /bin/true
docker create -v /home/teamcode/logs/httpd:/var/log/httpd --name tc-httpd-logs busybox /bin/true
docker run -d --name tc-httpd -h tc-httpd -p 80:80 --volumes-from tc-httpd-config --volumes-from tc-httpd-config --volumes-from tc-repo --volumes-from tc-httpd-logs -e TZ="Asia/Seoul" --entrypoint /opt/subversion/assets/entrypoint.sh --network tcnet teamcode/subversion:latest
```

docker run -d --name tc-httpd -h svn.visang.com -p 80:80 --volumes-from tc-httpd-config --volumes-from tc-httpd-config --volumes-from tc-repo --volumes-from tc-httpd-logs -e TZ="Asia/Seoul" --entrypoint /opt/subversion/assets/entrypoint.sh --network tcnet teamcode/subversion:latest

이제 아래와 같이 URL 로 접속해 봅니다. 기본적으로 Twitter Bootstrap Project 를 예제 저장소로 제공하고 있습니다.

```
http://localhost/repos/bootstrap/
```

접속에 성공하면 성공!

#### TeamCode Server 설치

먼저 아래와 같이 TeamCode Home 을 모두 바라보는 Volume Container 를 만듭니다. TeamCode 서버는 모든 데이터에 접근이 가능해야 하므로 이렇게 해야 합니다.

```
docker create -v /home/teamcode:/var/opt/teamcode --name tc-home busybox /bin/true
```

그리고 아래와 같이 TeamCode Image 를 다운로드 받습니다. TeamCode Docker Image 는 우리 회사 Private Repository 에 있으므로 먼저 ``docker login`` 로 로그인해 둬야 합니다. 아이디는 ``teamcode``, 패스워드는 기본입니다.

```
docker pull teamcode/teamcode:0.66
```

다음으로 ``$TEAMCODE_HOME/config/application.yml`` 파일을 수정합니다.
 
Home Directory 등을 설정합니다.

다음으로 DataSource 를 수정합니다.

```
driverClassName: org.postgresql.Driver
url: jdbc:postgresql://tcdb/teamcode
username: 'teamcode'
password: '********'
```

그리고 JPA 데이터베이스 유형을 수정합니다. Hibernate 자동 업데이트는 사용하지 않습니다 (Flyway 사용). 

```
spring.jpa.database=postgresql
spring.jpa.hibernate.ddl-auto: none
```

서버를 구동한 다음에 아래 테이블을 조회해 봅니다. Flyway 확인.

```
schema_version
```

그리고 마지막으로 아래와 같이 실행합니다 (메모리가 부족한 경우 512m 정도로 설정하세요!!).

```
docker run -d --name tc -h tc --volumes-from tc-home -e TZ="Asia/Seoul" -e XMS=2048m -e XMX=2048m --network tcnet teamcode/teamcode:0.67
```

다음으로 workers.properites 파일에서 host 를 tomcat 에서 tc 로 변경해야 합니다. 배포한 Subversion Image 에서는 기본적으로 tomcat 으로 설정되어 있습니다.

```
worker.list=tomcat-worker

worker.tomcat-worker.type=ajp13
worker.tomcat-worker.host=tc
worker.tomcat-worker.port=8009
```


#### Runner 설치

먼저 teamcode-default-image 를 설치합니다. 이 이미지는 Private Repository 나 Public Repository 에 두지 않으므로 직접 Dockerfile 을 이용해서 해당 서버에서 빌드합니다.

Runner Jar 파일을 해당 서버에 업로드합니다.

그리고 application.yml 파일을 구성합니다.

#### 기타

각종 관리용 Shell Script 를 만들면 편리합니다. 먼저, 아래는 Container 이름만 넣으면 바로 해당 Container 에 진입할 수 있는 Shell 입니다.

```
docker exec -it $1 /bin/bash
```

파일명을 ``console.sh`` 로 만들고 아래와 같이 실행하면 PostgreSQL 에 진입할 수 있습니다.

```
console.sh tc-db
```


### 서버 환경 설정

1. python 설치

### Subversion Docker Container 설치


>만약 0.0.6 버전 미만인 경우 실행할 때마다 usermod -u 578 apache 명령어를 실행해야 합니다. id 를 변경한 후에는 프로세스 혹은 Docker 를 재실행해야 합니다 (프로세스를 중지하면 Docker 가 중지되므로 당연히 docker 를 재기동해야 합니다).
그 0.0.6 버전 이상인 경우에는 자동으로 uid 를 설정합니다.  



### 설정

1. /etc/hosts 파일에 svn.visang.com 설정 
2. application.yml 의 external-url 설정 
workers.properites 에 host 지정 (tc container host 이름. 링크 걸려고...)


## 애플리케이션 빌드

```
gradle clean -x test build buildDocker
```

성공적으로 빌드가 되었다면 이제 Docker Hub 에 등록합니다.

```
docker push teamcode/teamcode:tag
```

설치하려고 하는 곳에서 ``docker pull`` 로 이미지를 다운로드합니다.

```
docker pull teamcode/teamcode:tag
```

이제 실행합니다. 실행 전에 먼저 PostgreSQL 컨테이너가 실행되어 있어야 합니다 (아래는 개발 서버 예입니다).

```
docker run -d --name teamcode -h teamcode -v /home/teamcode/config:/var/opt/teamcode/config --link tcpostgres:tcpostgres teamcode/teamcode:0.56
docker run -d --name teamcode -h teamcode -v /home/ec2-user/teamcode:/var/opt/teamcode:rw --link tcpostgres:db teamcode/teamcode:0.21
```

잘 실행이 안 되어서 내부 구조를 살짝? 보고 싶다면 아래와 같이 합니다. 종료하면 자동으로 컨네이너가 삭제됩니다.

```
docker run -it --rm --entrypoint /bin/bash teamcode/teamcode:tag
```

## Database

아래와 같이 설치합니다.

```
docker pull postgres
```

단독으로 실행할 때는 아래와 같이 합니다 (``--rm`` 옵션은 빼고 ``-d`` 옵션을 추가합니다).
 
```
docker run -d --name tcpostgres -h tcpostgres -e POSTGRES_PASSWORD=******** -p 5437:5432 -v /home/teamcode/data/postgresql/data:/var/lib/postgresql/data postgres
```

접속은 아래와 같이 합니다.

```
$ docker exec -it tc-db /bin/bash
psql -h localhost -p 5432  -U postgres --password

```

```
CREATE USER teamcode WITH PASSWORD '********';
CREATE DATABASE teamcode WITH owner=teamcode template=template0 encoding='UTF8';
GRANT ALL PRIVILEGES ON DATABASE teamcode to teamcode;
```

psql -h localhost -p 5432  -U teamcode --password

## 실행 순서

실행은 아래와 같은 순서로 합니다.

1. DB Container
2. Teamcode Container (or HTTPD)
3. HTTPD Container (or Teamcode)


### General

먼저 네트워크와 데이터 볼륨 생성. 별도의 볼륨 컨테이너를 생성한 후에 사용해야 UID 충돌에 따른 오류가 발생하지 않는다. 그리고 subversion 에서는 Apache HTTDP 서버가 Reverse Proxy 역할도 하기 때문에 
Teamcode Container 를 먼저 실행 후 Subversion 을 실행하는 것이 좋습니다 (최초에 한번만).

```
docker network create tcnet
docker create -v /home/ec2-user/teamcode/data/postgresql/data:/var/lib/postgresql/data --name tcdata busybox /bin/true
docker create -v /home/ec2-user/teamcode/data/repositories:/var/opt/subversion/repositories --name tcrepo busybox /bin/true

docker create -v /home/ec2-user/teamcode:/var/opt/teamcode --name tchome busybox /bin/true

docker create -v /home/teamcode:/var/opt/teamcode --name tccidata busybox /bin/true
```
위와 같이 실행한 후에 docker ps -a 로 Volume Container 존재 여부 확인 가능.

TODO ``tccidata`` 는 임시임. 나중에 변경 필요.

어떤 Container 에 어떤 경로가 매핑되어 있는지 확인하려면 아려 명령어를 실행합니다. 마지막 Parameter 는 Container ID 입니다.

```
docker inspect -f '{{ .Mounts }}' 1963abcee730
```

1. docker pull teamcode/teamcode:0.56

1. docker run -d --name tcdb -h tcpostgres -e POSTGRES_PASSWORD=******** --volumes-from tcdata -e TZ="Asia/Seoul" --network tcnet postgres
2. docker run -d --name teamcode -h teamcode -v /home/teamcode:/var/opt/teamcode:rw -e TZ="Asia/Seoul" --network tcnet teamcode/teamcode:0.58
3. docker run -d --name subversion -h svn.visang.com -p 80:80 -v /home/teamcode/config/httpd:/etc/subversion/httpd -v /home/teamcode/data:/var/opt/subversion -v /home/teamcode/logs/httpd:/var/log/httpd -e TZ="Asia/Seoul" --entrypoint /opt/subversion/assets/entrypoint.sh --network tcnet teamcode/subversion:0.0.2

### on AWS

docker network create tcnet

```
docker run -d --name tcdb -h tcpostgres -e POSTGRES_PASSWORD=******** --volumes-from tcdata -e TZ="Asia/Seoul" --network tcnet postgres
docker run -d --name teamcode -h teamcode -v /home/ec2-user/teamcode:/var/opt/teamcode:rw -e TZ="Asia/Seoul" -e XMS=512m -e XMX=512m --network tcnet teamcode/teamcode:0.59
docker run -d --name subversion -h subversion -p 9494:80 --volumes-from tcrepo -v /home/ec2-user/teamcode/config/httpd:/etc/subversion/httpd -v /home/ec2-user/teamcode/logs/httpd:/var/log/httpd -e TZ="Asia/Seoul" --entrypoint /opt/subversion/assets/entrypoint.sh --network tcnet teamcode/subversion:0.0.2
```

URL 은 아래와 같음.

```
http://support.rightstack.net:9494/
```

### 로그 확인
만약 실행 시 무슨 오류가 발생했다면?
```
docker logs [container-id]
```

뒤에 ``-f`` 옵션을 붙이면 ``tail`` 과 같이 동작합니다.

Docker Daemon (혹은 Engine) Log 보는 법

```
sudo journalctl -fu docker.service
```


### Database 확인

```
psql -U teamcode
```




docker run -d --name 
 -h tcpostgres -e POSTGRES_PASSWORD=******** --volumes-from tcdata -e TZ="Asia/Seoul" --network tcnet postgres
