FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
RUN chmod +x ./gradlew

COPY src src
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre AS base-runtime
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

##  개발용
FROM base-runtime AS dev
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m"
ENTRYPOINT ["java","-jar","/app/app.jar"]

## 운영용
FROM base-runtime AS prod
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m"
ENTRYPOINT ["java","-jar","/app/app.jar"]