killall -9 java
./gradlew --stop 

# Run gradlew in background
nohup ./gradlew run > recommendations.out 2> recommendations.err < /dev/null &

# Call gradle task to connect to Kafka Server and appends to the old data for retraining.
# This grabs the 'next' 5000 data points from server.
# This data is synthetic, but it represents the next 5000 points in the real-time 
# continuous data stream as it would appear in real life.
./gradlew integrationTask

# git push