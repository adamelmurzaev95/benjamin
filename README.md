# Benjamin
Benjamin - это система управления проектами в режиме онлайн, позволяющая командам эффективно решать рабочие задачи. 
Разработка ведется с использованием следующих технологий: Kotlin, Spring Boot, Spring Security, Spring Data JPA, Spring Web, TestContainers, Spring Mockk, MockServer,  Keycloak, Oauth2, PostgreSQL, Flyway. 

На что следует обратить внимание: 
- надстройка над Hibernate для совместимости Kotlin enum c PostgreSQL enum https://github.com/adamelmurzaev95/benjamin/blob/master/src/main/kotlin/benjamin/projects/tasks/api/TaskStatusToPostgreEnum.kt
- вынос авторизации в Keycloak с использованием протокола Oauth2 и JWT токенов
- взаимодействие с Keycloak сервером с помощью Web Client, который внутри себя держит токен в актуальном состоянии https://github.com/adamelmurzaev95/benjamin/blob/user-service/src/main/kotlin/benjamin/common/BenjaminConfig.kt
- настройка CI Github actions, проверка что проходят тесты
- использования Ktlint (плагин помогающий обеспечивать единый code style на проекте)
- поднятие контейнеров с помощью docker compose (postgres, keycloak)
- использования testcontainers для интеграционных тестов
- реализация логики для приватного доступа пользователей к проектам
- настройка Oauth2 в Spring Security
- Отправка событий в топик Kafka, из которого читает mail-service, и отправляет приглашения на почту
