FROM maven:3.8.6-eclipse-temurin-11 as maven
WORKDIR /app
COPY ./ ./
RUN mvn package -DskipTests

FROM maven:3.8.6-eclipse-temurin-11
ENV VERSION=1.3-SNAPSHOT
RUN apk --no-cache add git
WORKDIR /app
COPY --from=maven /app/custom-war-packager-cli/target/custom-war-packager-cli-*-jar-with-dependencies.jar /app/custom-war-packager-cli.jar
ENTRYPOINT ["java", "-jar", "/app/custom-war-packager-cli.jar"]  
