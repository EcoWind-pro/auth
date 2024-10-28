FROM gradle:8.5 AS build
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:17-jdk-slim
COPY --from=build /home/gradle/src/build/libs/ /app/
ENTRYPOINT ["java","-jar","/app/auth-1.0.jar"]