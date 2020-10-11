# Client Template for SEAI Project

## Instructions

1. Make sure you are connected to the [CS VPN](https://www.cs.mcgill.ca/docs/remote/dynamic/) (e.g. `ssh -D 9000 [username]@ubuntu.cs.mcgill.ca`).
2. Check to see that you can access the Movie API service, [`http://fall2020-comp598.cs.mcgill.ca:8080`](http://fall2020-comp598.cs.mcgill.ca:8080). (You should see "Movie API Service".)
3. Try out some REST requests and replies (`/user/[USER_ID]`, `/movie/[MOVIE_ID]`)
4. Fork and clone this template. This fork will reside on your team's shared repository.
5. Clone your fork onto your local development environment.
6. Log onto your team's VM with the team credentials (i.e. `ssh team[TEAM_NUMBER]@fall2020-comp598-[TEAM_NUMBER].cs.mcgill.ca`). This requires you to have first sent us your public key.
7. Clone your fork onto the remote development environment.
8. Run `./gradlew run` from inside the project directory.
9. Check that your service is running by visiting [http://fall2020-comp598-[TEAM_NUMBER].cs.mcgill.ca:8082/recommend/[USER_ID]](http://fall2020-comp598-[TEAM_NUMBER].cs.mcgill.ca:8082/recommend/[USER_ID]). This should return a comma-separated list of movie recommendations.
10. Check the Kafka log to see that the replies are being received.
11. Open the parent project using your favorite IDE.
12. Update the code in [Main.kt](/src/main/kotlin/Main.kt).
13. Deploy the changes to your VM, and repeat steps 6-11.