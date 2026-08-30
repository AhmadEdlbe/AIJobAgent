FROM gradle:8.7-jdk17 as builder
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts ./
COPY src src
RUN gradle bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
