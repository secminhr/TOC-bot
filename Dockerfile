FROM openjdk:11
EXPOSE 80:80
RUN apt-get update && apt-get install -y graphviz
RUN mkdir /app
COPY /build/libs/fat.jar /app/ktor-docker-sample.jar
ENTRYPOINT ["java","-jar","/app/ktor-docker-sample.jar"]
