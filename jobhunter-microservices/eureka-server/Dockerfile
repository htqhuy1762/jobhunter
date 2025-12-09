FROM eclipse-temurin:21-jre-alpine

# Install curl for healthcheck
RUN apk add --no-cache curl

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]

