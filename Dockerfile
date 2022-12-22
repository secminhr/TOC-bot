FROM openjdk:11
EXPOSE 80:80
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/fat.jar /app/ktor-docker-sample.jar
ENTRYPOINT ["java","-jar","/app/ktor-docker-sample.jar"]
