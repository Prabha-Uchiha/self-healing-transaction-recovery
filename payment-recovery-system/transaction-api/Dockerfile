FROM eclipse-temurin:21
RUN mkdir /opt/app
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]