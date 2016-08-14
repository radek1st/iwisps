package services

import models.RecommendModel.{Business, User}

import scala.io.{Codec, Source}

object RecommendService {

  implicit val defaultEncoding = Codec.UTF8

  val pipe = "\\|"

  def loadPerBusinessModel(): Map[Int, Set[Int]] = {
    Source.fromFile("models/perBusiness.tsv").getLines()
      .map(line => line.split(pipe))
      .map(s =>(s(0).toInt, s(1).split(",").map(x => x.toInt).toSet)).toMap
  }

  def loadPerUserModel(): Map[Int, Set[Int]] = {
    Source.fromFile("models/perUser.tsv").getLines()
      .map(line => line.split(pipe))
      .map(s =>(s(0).toInt, s(1).split(",").map(x => x.toInt).toSet)).toMap
  }

  def loadPerCategoryModel(): Map[Int, List[Int]] = {
    Source.fromFile("models/perCategory.tsv").getLines()
      .map(line => line.split(pipe))
      .map(s =>(s(0).toInt, s(2).split(",").map(x => x.toInt).toList)).toMap
  }

  def loadBusinesses(): Map[Int, Business] = {
    Source.fromFile("models/businesses.tsv").getLines()
      .map(line => line.split(pipe))
      .map(s =>(s(0).toInt, Business(s(0).toInt, s(1), s(3), s(4).toDouble, s(5).toDouble, s(6).toDouble, s(7)))).toMap
  }

  def loadUsers(): Map[Int, User] = {
    Source.fromFile("models/users.tsv").getLines()
      .map(line => line.split(pipe))
      .map(s => (s(0).toInt, User(s(0).toInt, s(1), s(2)))).toMap
  }

  val perBusinessModel = loadPerBusinessModel()
  val perUserModel = loadPerUserModel()
  val perCategoryModel = loadPerCategoryModel()
  val allBusinesses = loadBusinesses()
  val allUsers = loadUsers()


  def recommendUsers(business: Int, users: Set[Int]): Option[Set[User]] = {
    if(perBusinessModel.contains(business)) {
      Some(perBusinessModel(business).filter(user => users.contains(user)).map(u => allUsers(u)))
    } else {
      None
    }
  }

  def recommendBusinesses(user: Int, category: Int, top: Int): Option[List[Business]] = {
    if(perUserModel.contains(user) && perCategoryModel.contains(category)){
      Some(perCategoryModel(category).filter(business => perUserModel(user).contains(business))
        .take(top).map(b => allBusinesses(b)))
    } else {
      None
    }
  }

  def recommendBestInCategory(category: Int, top: Int): Option[List[Business]] = {
    if(perCategoryModel.contains(category)){
      Some(perCategoryModel(category).take(top).map(b => allBusinesses(b)))
    } else {
      None
    }
  }
}
