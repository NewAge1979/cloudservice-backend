FROM mirror.gcr.io/library/openjdk:17-jdk-alpine

LABEL authors="khrapatiy"

EXPOSE 8080

ADD build/libs/backend-1.0.0.jar cloudservice-backend.jar

ENTRYPOINT ["java", "-jar", "cloudservice-backend.jar"]