

controller:
	./gradlew :docker-controller:distDocker
	docker tag de.schub.docker_controller/docker-controller:1.0-SNAPSHOT 10.123.0.1:5000/docker-controller:1.0-SNAPSHOT
	docker push 10.123.0.1:5000/docker-controller:1.0-SNAPSHOT
