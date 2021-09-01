

## 기본

http://localhost:8080/health

위와 같이 호출하면 단순하게 status 만 나옵니다. 세부적인 내용을 알려면 아래와 같이 설정합니다.

```yaml
management:
  security:
    enabled: false
```

### 특정 사용자만 볼수 있게 하기.

먼저 아래를 참고하세요.

https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-monitoring.html

요약하면, 사용자 Role 에 ACTUATOR 가 있으면 됩니다. 이렇게 설정한 다음 application.yml 에서 ``management.security.enabled=true`` 로 설정하거나 
설정을 삭제하세요.

**근데 잘 안됨. 뭔가 커스텀하게 작업을 해서 그런 모양임.**