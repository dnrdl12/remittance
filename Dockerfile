# Java 17 (Spring Boot 3.x 권장)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# pom에서 버전관리 포기 못해서 0.0.1 수정해가며 사용
COPY target/remittance-0.0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
