# Step 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/bloodify.jar bloodify.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","bloodify.jar"]