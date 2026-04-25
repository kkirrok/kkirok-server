FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
