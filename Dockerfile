# --- Estágio 1: Build (Compilação) ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia apenas arquivos de dependência primeiro (Cache Layering)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Baixa as dependências (se nada mudou, o Docker usa o cache aqui)
RUN ./gradlew dependencies --no-daemon || return 0

# Copia o código fonte e compila
COPY src src
RUN ./gradlew bootJar --no-daemon

# --- Estágio 2: Runtime (Execução) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria um usuário não-root por segurança (Best Practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o JAR gerado no estágio anterior
COPY --from=build /app/build/libs/*.jar app.jar

# Expõe a porta
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]