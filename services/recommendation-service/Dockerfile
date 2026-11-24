# syntax=docker/dockerfile:1
# Contexte de build : racine du dépôt (contrat OpenAPI, référentiel et POM parent).

FROM eclipse-temurin:25.0.5.1_2-jdk-noble AS build
# unzip : sans lui, le wrapper Maven télécharge le .tar.gz, dont la somme ne correspond pas au .zip épinglé.
RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY contracts/ contracts/
COPY services/recommendation-service/pom.xml services/recommendation-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -pl services/recommendation-service -am dependency:go-offline
COPY services/recommendation-service/src/ services/recommendation-service/src/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -pl services/recommendation-service -am package -DskipTests \
    && cp services/recommendation-service/target/recommendation-service-*.jar /workspace/app.jar

FROM eclipse-temurin:25.0.5.1_2-jre-noble
RUN groupadd --system --gid 10001 medhead \
    && useradd --system --uid 10001 --gid medhead --no-create-home --shell /usr/sbin/nologin medhead
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
# Le référentiel est aussi embarqué dans le jar ; ce fichier sert de valeur par défaut surchargeable.
COPY contracts/reference/specialties.json /app/reference/specialties.json
ENV MEDHEAD_RECOMMENDATION_SERVICE_SPECIALTIES_FILE=file:/app/reference/specialties.json \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
USER 10001:10001
EXPOSE 8080 8081
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=6 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8081 && printf 'GET /actuator/health/readiness HTTP/1.0\\r\\nHost: localhost\\r\\n\\r\\n' >&3 && grep -q '\"status\":\"UP\"' <&3"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
