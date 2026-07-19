# Estágio 1: Build da Aplicação com Eclipse Temurin JDK
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Estágio 2: Imagem Final de Execução com Eclipse Temurin JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Roda como usuário não-root
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar
USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]