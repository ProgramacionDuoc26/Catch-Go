spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/catchgo_db
    username: catchgo_admin
    password: admin_password_123
  jpa:
    hibernate:
      ddl-auto: update # Para el MVP y desarrollo local
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379