FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/spring-websocket-demo-0.0.1-SNAPSHOT.jar spring-websocket-demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","spring-websocket-demo.jar"]