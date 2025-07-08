FROM gradle:jdk21-ubi AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/gl-exercise-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "gl-exercise-0.0.1-SNAPSHOT.jar"]