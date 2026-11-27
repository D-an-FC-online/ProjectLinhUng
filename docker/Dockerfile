# Build image ứng dụng (backend Spring Boot + giao diện frontend đóng gói chung).
# Chạy từ thư mục gốc dự án:  docker build -f docker/Dockerfile -t damsen-guide:latest .

# ---------- Giai đoạn 1: build bằng Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY backend/pom.xml backend/pom.xml
RUN mvn -q -f backend/pom.xml dependency:go-offline
COPY backend backend
COPY frontend frontend
RUN mvn -q -f backend/pom.xml -DskipTests package

# ---------- Giai đoạn 2: chạy ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 damsen && mkdir -p /data/media && chown damsen /data/media
COPY --from=build /src/backend/target/damsen-guide.jar app.jar
USER damsen
ENV MEDIA_DIR=/data/media \
    SPRING_WEB_RESOURCES_STATIC_LOCATIONS=classpath:/static/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
