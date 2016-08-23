#iWisps
This project has been entered into IBM Spark competition and it's official page is on: http://devpost.com/software/iwisps

For Devpost-like summary of the project please refer [here](DEVPOST.md).

The service currently runs in IBM Bluemix Containers Service on [http://iwisps.com](http://iwisps.com/#/routes).

Under [wisps-ml](wisps-ml) you can find the code used to build and export the models with
Spark from the Yelp Dataset.

Summary IBM Data Science Experience notebook can be found in Bluemix
[here](https://apsportal.ibm.com/analytics/notebooks/6431ff1e-ca96-4137-b694-75c3b5b39c09/view?access_token=7b090981f11d7266526b11aba296e68563c0ecd01287a9c3b587940e42c7bb29) or as [pdf](wisps-ml/iWisp-public-ibm-notebook.pdf).

The actual code for the microservice built with
Play! and Docker is located in [wisps-service](wisps-service).

Gatling test used to evaluate the performance
and stability of the microservice can be found
[here](wisps-ml/src/test/scala/wisps/IwispsSimulation.scala) and the
results are published on: http://iwisps.com/simulation/index.html.

