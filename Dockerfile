FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :server:impl:buildFatJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG SNAG_ENV=dev
COPY --from=build /app/server/impl/build/libs/*-all.jar app.jar
COPY --from=build /app/config/common-release.properties config/common.properties
COPY --from=build /app/config/backend-${SNAG_ENV}.env config/backend.env
COPY --from=build /app/config/docker-entrypoint.sh docker-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["./docker-entrypoint.sh"]
