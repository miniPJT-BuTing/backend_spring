FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app/dev

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/dev/app.jar"]
