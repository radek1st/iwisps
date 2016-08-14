package models

import play.api.libs.json.Json

object RecommendModel {
  case class Business(numericId: Int, id: String, name: String, lat: Double, lon: Double, stars: Double, address: String)
  case class User(numericId: Int, id: String, name: String)

  implicit val businessFormat = Json.format[Business]
  implicit val userFormat = Json.format[User]
}
