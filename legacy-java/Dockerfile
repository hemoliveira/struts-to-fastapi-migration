# Build stage
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Run stage
FROM tomcat:7-jre8
COPY --from=build /app/target/orderco-legacy.war /usr/local/tomcat/webapps/orderco.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
