package wisps

import io.gatling.core.Predef._
import io.gatling.core.feeder.Record
import io.gatling.http.Predef._

import scala.util.Random
import scala.concurrent.duration._
import scala.io.{Codec, Source}

class IwispsSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://iwisps.com")
    .acceptHeader("application/json")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val categoryFeeder = new Feeder[String] {
    val randomEngine = new Random
    val maxCategoryId = 356

    override def hasNext: Boolean = true

    override def next(): Map[String, String] = {
      val category = randomEngine.nextInt(maxCategoryId) + 1

      Map("category" -> category.toString)
    }
  }

  val userCategoryFeeder = new Feeder[String] {
    implicit val defaultEncoding = Codec.UTF8
    val pipe = "\\|"
    val randomEngine = new Random

    val users = Source.fromFile("data/perUser.tsv").getLines()
      .map(line => line.split(pipe))
      .map(s =>s(0)).toList

    val maxCategoryId = 356

    override def hasNext: Boolean = true

    override def next(): Map[String, String] = {
      val user = users(randomEngine.nextInt(users.size))
      val category = randomEngine.nextInt(maxCategoryId) + 1

      Map("user" -> user,
        "category" -> category.toString)
    }
  }

  val businessUsersFeeder = new Feeder[String] {
    implicit val defaultEncoding = Codec.UTF8
    val pipe = "\\|"
    val randomEngine = new Random

    val users = Source.fromFile("data/perUser.tsv").getLines()
      .map(line => line.split(pipe))
      .map(s =>s(0)).toList

    val businesses = Source.fromFile("data/perBusiness.tsv").getLines()
      .map(line => line.split(pipe))
      .map(s =>s(0)).toList

    override def hasNext: Boolean = true

    override def next(): Map[String, String] = {
      val user = users(randomEngine.nextInt(users.size))
      val business = businesses(randomEngine.nextInt(businesses.size))

      Map("business" -> business,
        "user" -> s"[${user}]")
    }
  }

  val businessScenario = scenario("RecommendUsers").feed(businessUsersFeeder).repeat(1) {
    exec(http("request")
      .post("""/api/businesses/${business}""")
      .body(StringBody("""${user}""")).asJSON
      .check(status.is(200))
    )
  }

  val userScenario = scenario("RecommendBusinesses").feed(userCategoryFeeder).repeat(1) {
    exec(http("request")
      .get("""/api/users/${user}/categories/${category}?top=5""")
      .check(status.is(200))
    )
  }

  val categoryScenario = scenario("TopPerCategory").feed(categoryFeeder).repeat(1) {
    exec(http("request")
      .get("""/api/categories/${category}?top=5""")
      .check(status.is(200))
    )
  }

  setUp(
    businessScenario.inject(rampUsers(10000) over (800 seconds)),
    userScenario.inject(rampUsers(10000) over (800 seconds)),
    categoryScenario.inject(rampUsers(10000) over (800 seconds))
  ).protocols(httpConf)

}
