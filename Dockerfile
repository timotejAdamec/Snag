FROM eclipse-temurin:21-jdk AS build
RUN apt-get update && apt-get install -y --no-install-recommends git && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . .
RUN ./gradlew :server:impl:buildFatJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/server/impl/build/libs/*-all.jar app.jar
COPY --from=build /app/config/common-release.properties config/common.properties
COPY --from=build /app/config/backend-release.env config/backend.env
COPY --from=build /app/config/docker-entrypoint.sh docker-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["./docker-entrypoint.sh"]
