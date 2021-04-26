FROM openjdk:8
ADD target/gym-master-catalogue-0.0.1-SNAPSHOT.jar catalogue.jar
EXPOSE 5104
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "catalogue.jar"]
