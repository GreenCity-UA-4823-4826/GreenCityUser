FROM eclipse-temurin:21-jre as runner
WORKDIR runner
COPY **/target/app.jar runner/
CMD java -jar runner/app.jar
