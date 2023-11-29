FROM maven:3-amazoncorretto-21 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
COPY --from=build /app/target/api-1.0.0-jar-with-dependencies.jar /app/api.jar
COPY --from=build /app/data/ /app/data/

RUN apk update && apk add gcompat opencv-dev && rm -rf /var/cache/apk/*
RUN apk add --update ttf-dejavu && rm -rf /var/cache/apk/*
EXPOSE 8080
WORKDIR /app
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseG1GC", "-XX:G1NewSizePercent=20", "-XX:G1ReservePercent=20", "-XX:MaxGCPauseMillis=50", "-XX:G1HeapRegionSize=32M", "-jar", "api.jar"]