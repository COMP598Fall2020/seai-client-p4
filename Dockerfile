# dockerfile to containerize the model infrastructure
FROM gradle:latest
COPY . .
ENTRYPOINT [ "./gradlew", "run"]