FROM amazoncorretto:23-alpine-jdk
COPY /build/libs/tech-0.0.1-SNAPSHOT.jar main.jar
EXPOSE 443
ENTRYPOINT ["java", "-jar","/main.jar"]