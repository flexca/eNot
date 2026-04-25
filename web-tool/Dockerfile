FROM eclipse-temurin:17-jre

WORKDIR /app

COPY backend/target/enot-web-tool-backend-*.jar /app/enot-web-tool.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/enot-web-tool.jar"]
