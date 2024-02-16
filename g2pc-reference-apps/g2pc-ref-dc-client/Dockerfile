FROM openjdk:17-jdk-alpine
COPY target/*.war dc-client-0.1.war
ENTRYPOINT ["java","-jar","/dc-client-0.1.war"]