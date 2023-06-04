#Stage 1
# initialize build and set base image for first stage
FROM maven:3.9.2-eclipse-temurin-17 as stage1
# set working directory
WORKDIR /opt/financial
# copy files
COPY . .
# compile the source code and package it in a jar file
RUN mvn clean install -Dmaven.test.skip=true
#Stage 2
# set base image for second stage
FROM eclipse-temurin:17.0.7_7-jre
# set deployment directory
WORKDIR /opt/financial
# copy over the built artifact from the maven image
COPY --from=stage1 /opt/financial/launcher/target/*.jar /opt/financial/launcher.jar
# configure start of application
ENTRYPOINT ["java", "-jar", "launcher.jar"]