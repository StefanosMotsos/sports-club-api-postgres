FROM amazoncorretto:21
WORKDIR /app
COPY build/libs/sportspostgres.jar ./app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]