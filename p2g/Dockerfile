FROM eclipse-temurin:21-jre
# 8080 only: every REST endpoint here is Spring MVC, and all five Camel routes
# are direct: ones, so camel.server-port (5000) never gets bound.
EXPOSE 8080

COPY p2g/build/libs/app.jar app.jar
CMD ["java", "-jar", "app.jar"]
