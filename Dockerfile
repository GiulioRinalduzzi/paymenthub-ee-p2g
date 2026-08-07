FROM eclipse-temurin:21-jre
# 5000 = Camel REST routes, 8080 = Spring Boot / actuator
EXPOSE 5000 8080

COPY build/libs/app.jar app.jar
CMD ["java", "-jar", "app.jar"]
