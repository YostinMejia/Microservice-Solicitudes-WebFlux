FROM gradle:8.10.2-jdk21-alpine AS build

WORKDIR /app

COPY . .

# Compilar solo el módulo app-service, ignorando tests y validateStructure
RUN gradle :app-service:clean :app-service:bootJar -x test -x validateStructure --no-daemon

FROM eclipse-temurin:21-jdk-alpine AS production

WORKDIR /app

COPY --from=build /app/applications/app-service/build/libs/*.jar app.jar

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
