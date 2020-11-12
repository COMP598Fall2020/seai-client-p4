killall -9 java
./gradlew --stop 
nohup ./gradlew run > recommendations.out 2> recommendations.err < /dev/null &