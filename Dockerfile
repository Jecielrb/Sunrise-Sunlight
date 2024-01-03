# Use an official OpenJDK runtime as a base image
FROM openjdk:17-oracle

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/umbrella-codechallenge-jeciel.jar /app/

# Expose the port that your application will run on
EXPOSE 8080

# Specify the command to run on container startup
CMD ["java", "-jar", "umbrella-codechallenge-jeciel.jar"]
