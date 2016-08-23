#iWisps-Service
This is the Web Service part of the project.

##Building Instructions

As for prerequisites, copy the resulting models built with Spark into `models` folder.
Additionally, you must have Docker environment installed.
I'm using [Docker for Mac](https://docs.docker.com/engine/installation/mac/#/docker-for-mac).

To build and package the Play! web service just run:

    sbt docker:publishLocal

You can then push the resulting Docker image to the cloud provider of your choice, simple as that.

DockerHub:

    docker push radek1st/iwisps:1.0.0


IBM Bluemix Containers Service Repository:

    docker tag radek1st/iwisps:1.0.0 registry.eu-gb.bluemix.net/iwisps/iwisps:1.0.0
    docker push registry.eu-gb.bluemix.net/iwisps/iwisps:1.0.0

