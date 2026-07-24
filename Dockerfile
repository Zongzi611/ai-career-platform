# ============================================================
# QKC 大学生职业规划智能体 — 多阶段 Docker 构建
# ============================================================

# ---- Stage 1: Maven 编译 ----
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build
COPY pom.xml .
# 首次下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B -q || true

COPY src ./src
RUN mvn clean package -DskipTests -B -q

# ---- Stage 2: JRE 运行 ----
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S qkc && adduser -S qkc -G qkc

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

RUN chown -R qkc:qkc /app
USER qkc

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", "/app/app.jar"]
