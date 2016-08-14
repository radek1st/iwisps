package controllers

import javax.inject._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.mvc._
import services.RecommendService
import play.api.libs.json._
import models.RecommendModel.businessFormat
import models.RecommendModel.userFormat
import play.api.cache.Cached

import scala.concurrent.Future

@Singleton
class RecommendController @Inject() (cached: Cached) extends Controller {

  implicit val cl = getClass.getClassLoader

  private lazy val generator = SwaggerSpecGenerator("controllers", "models")

  def specs = cached("swaggerDef") {
    Action.async { _ =>
      Future.fromTry(generator.generate()).map(Ok(_))
    }
  }

  val allowOriginHeader = ("Access-Control-Allow-Origin", "*")

  def recommendUsers(business: Int) = Action {
    request => {
      if(request.body.asJson.isDefined) {
        val usersList = request.body.asJson.get.asInstanceOf[JsArray].as[Set[Int]]
        val result = RecommendService.recommendUsers(business, usersList)
        if(result.isDefined) {
          Ok(Json.toJson(result.get)).withHeaders(allowOriginHeader)
        } else {
          BadRequest(s"Invalid parameter: 'business' id not found").withHeaders(allowOriginHeader)
        }
      } else {
        BadRequest(s"Invalid body parameter: users list cannot be empty").withHeaders(allowOriginHeader)
      }
    }
  }

  def recommendBusinesses(user: Int, category: Int, top: Option[Int]) = Action {
    val topBest = top.getOrElse(5)
    if(topBest < 1) {
      BadRequest(s"Invalid parameters: 'top' should be greater than zero").withHeaders(allowOriginHeader)
    } else {
      val result = RecommendService.recommendBusinesses(user, category, topBest)
      if(result.isDefined) {
        Ok(Json.toJson(result.get)).withHeaders(allowOriginHeader)
      } else {
        BadRequest(s"Invalid parameters: 'user' or 'category' ids not found").withHeaders(allowOriginHeader)
      }
    }
  }

  def recommendBestInCategory(category: Int, top: Option[Int]) = Action {
    val topBest = top.getOrElse(5)
    if(topBest < 1) {
      BadRequest(s"Invalid parameters: 'top' should be greater than zero").withHeaders(allowOriginHeader)
    } else {
      val result = RecommendService.recommendBestInCategory(category, topBest)
      if(result.isDefined) {
        Ok(Json.toJson(result.get)).withHeaders(allowOriginHeader)
      } else {
        BadRequest(s"Invalid parameters: 'category' id not found").withHeaders(allowOriginHeader)
      }
    }
  }

}

