FROM maven:3.9.4-amazoncorretto-17 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine
COPY --from=build /app/target/api-1.0.0-jar-with-dependencies.jar /app/api.jar
COPY --from=build /app/data/ /app/data/

RUN apk update && apk add gcompat opencv-dev && rm -rf /var/cache/apk/*
EXPOSE 8080
WORKDIR /app
ENTRYPOINT ["java", "-jar", "api.jar"]