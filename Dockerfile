FROM eclipse-temurin:21-jre
WORKDIR /work/
COPY target/quarkus-app/ /work/
EXPOSE 8082
CMD ["java", "-jar", "quarkus-run.jar"]