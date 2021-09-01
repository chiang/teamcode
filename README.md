## 팀코드 소개

팀코드는 Gitlab 을 보면서 **Gitlab 같은 제품이 Subversion 을 지원하면 좋겠다라는 생각에서 출발한 제품**입니다. 2015년도에 첫 제품을 출시했으며 2017년도에 **Teamcode** 상표 출원과 함께 기능을 고도화했습니다. Gitlab 을 벤치마킹한 만큼 Gitlab 의 소스 코드 (Ruby) 를 분석하고 이를 자바 기반으로 제작하는 과정을 거쳤습니다. UI 도 많이 Concept 을 가지고 왔습니다.

Gitlab 스타일과 Container 기반 배포 기능와 같은 고도화는 약 3개월의 개발 기간이 소요되었습니다. 여러 고객사에 납품하기는 하였으나 Subversion 이 점점 퇴출되고 있고 Gitlab 과 같이 훌륭한 제품과 경쟁하는 것보다는 지금까지 제가 개발한 경험을 공유하는 것이 좋겠다는 생각에 이렇게 Github 에 공개합니다. 이 소스 코드를 기반으로 다양한 토론이나 스터디 등을 통해서 제가 미처 해결 못했거나 더 나은 방법을 같이 공유하는 기회를 가지면 좋겠습니다.

## 주요 기술 요소

### Frontend
1. Thymeleaf
2. Vuejs
3. Webpack
4. Gulp
5. Sass


### Backend
1. Spring Boot
2. JPA
3. RESTful w/ hateoas
4. Spring Statemachine


### Etc.
1. PostgreSQL
2. Lombok
3. Docker
4. Gradle
5. Winrm (w/ Python)
6. Flyway

## 관련 Repositories

1. https://github.com/chiang/teamcode-runner => CI 빌드를 처리하는 도구입니다. Gitlab CI Runner 와 같습니다. 다만 Golang -> Java 입니다.


## 주의 사항
1. Teamcode 는 출원된 상표입니다. 상업적 목적으로 사용이 불가능합니다.
2. 제품 소스 코드를 가지고 활용해서 제품을 개발하실 수 있으나 본 Repository 를 그대로 빌드에서 판매, 유통할 수 없습니다.

