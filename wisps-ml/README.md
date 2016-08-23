#iWisps-ML
This is the Machine Learning part of the project.

##Model Building Instructions
Use the IBM Data Science experience with the notebook provided and import the **Yelp** dataset into the Bluemix Object Storage. 
Or, copy the uncompressed **Yelp** dataset into the `data` folder or update the `inputs`
 in [CollaborativeFilteringModel](src/main/scala/wisps/ml/CollaborativeFilteringModel.scala)
file. Also change `setMaster` so it points to your Spark cluster and `outputs` to where
 you want the resulting models stored.
In either case, execute the code and make the models available for the second part
of the project.

##Performance Simulation Instructions

Update [iWispsSimulation](src/test/scala/wisps/IwispsSimulation.scala)
to point to your running instance of the iWisps service and invoke:

    sbt gatling:test

Gatling generates test results as HTML page by default.

