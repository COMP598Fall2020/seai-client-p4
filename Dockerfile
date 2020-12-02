FROM java:8-jdk-alpine

COPY build/distributions/seai-client-template-1.0-SNAPSHOT.tar /usr/app/
WORKDIR /usr/app
EXPOSE 8082
ENTRYPOINT ["java","-jar", "build/distributions/seai-client-template-1.0-SNAPSHOT.tar"]


# docker build ... //builds image
# docker run ... //starts the container
# docker push jazlynhellman/se4ai-client-p4 //push the image to the dockerhub repo
