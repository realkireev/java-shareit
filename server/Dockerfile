FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar app.jar
CMD sleep 10
ENTRYPOINT ["java","-jar","/app.jar"]