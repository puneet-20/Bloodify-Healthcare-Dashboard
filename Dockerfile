# Step 1: Use Maven to build the application
FROM maven:3.8.4-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use Java to run the application
FROM openjdk:17-jdk-slim
COPY --from=build /target/bloodify.jar bloodify.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","bloodify.jar"]