From eclipse-temurin:17-jdk

WORKDIR /app

COPY target/eventbook-0.0.1-SNAPSHOT.jar eventbook.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "eventbook.jar"]