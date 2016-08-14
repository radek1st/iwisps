#iWisps

Fast, Scalable and Portable Local Business Recommender Microservice.

<img src="https://cloud.githubusercontent.com/assets/246085/17652269/f3b8fd8c-626f-11e6-8a98-6c5acd9b7e6a.png">

##Inspiration

Top tech companies like Apple (https://en.wikipedia.org/wiki/IBeacon) and
Google (https://en.wikipedia.org/wiki/Eddystone_(Google))
are investing in beacon devices. This project, demonstrates how *beacon* concept
can be used, together with the knowledge discovered
in the Yelp dataset, to build a recommendation web service which
empowers local businesses and personalises customer experience.

##What it does

It helps local businesses to identify potential nearby customers that would rate their service highly
and, by employing targeted messaging, increase the sales:

    “Hi Aaron, visit us today and when you buy 3 wines, you’ll get one for free - Appellation Wines.”

Additionally, it allows users to get personalised and context aware recommendations for any service category they may require, like Thai food:

    “You will definitely like ‘Spirit of Thai’ restaurant which is just round the corner from you.”

Finally, it provides a lookup for best rated businesses in each category:

    “For best French food in Edinburgh, visit ‘Martin Wishart’.”

## How I built it
1. Used IBM Data Science Experience for data analysis, model building...
2. Built Dockerised Play! stateless web service exposing REST interface
3. Tested portability by running both in IBM Containers Service and Docker Cloud
4. Tested performance and stability by running Gatling tests
5. Used Swagger to allow for interactive exploration and to document the REST API

## Challenges I ran into
I'm happy to say that this was a rather pain free project.

## Accomplishments that I'm proud of
Being able to complete this complex undertaking in a short time frame.

## What I learned
I realised that I'm much more effective at using APIs (Swagger) than building websites :)

## What's next for iWisps
Develop a smartphone app allowing users to take advantage of the iWisps service and act as a beacon.
Also create a business service that integrates with iWisps and can receive signals from
the smartphone app when in a certain proximity and is capable of sending notifications to the app.
