FROM eclipse-temurin:21-jre
EXPOSE 8080

# Copy the boot jar by name, not `*.jar`: `gradlew build` also produces a
# -plain.jar, which has no main class and sorts first in the glob.
COPY crm/build/libs/app.jar app.jar
CMD ["java", "-jar", "app.jar"]
