package wisps.ml

import java.nio.file.{Files, Paths}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

object CollaborativeFilteringModel {

  val csv = "com.databricks.spark.csv"
  val csvOptions = Map("delimiter" -> "|", "header" -> "true", "inferSchema" -> "true")

  val listSeparator = ";"

  //inputs
  val dir = "data/"
  val business = dir + "yelp_academic_dataset_business.json"
  val review = dir + "yelp_academic_dataset_review.json"
  val user = dir + "yelp_academic_dataset_user.json"

  //outputs
  val outputDir = "models/"
  val modelFileName = outputDir + "simpleCfModel"
  val usersFileName = outputDir + "users"
  val businessesFileName = outputDir + "businesses"
  val perBusiness = outputDir + "perBusiness"
  val perUser = outputDir + "perUser"
  val perCategory = outputDir + "perCategory"

  case class CFParams(rank: Int, lambda: Double, numIter: Int)

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("iWisps-CF")
    //Here point to your spark cluster
      .setMaster("spark://your-spark-master:7077")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val scotBusinessDf = sqlContext.read.format("json").load(business)
    //To limit to Edinburgh uncomment below
        .where("city = 'Edinburgh'")
    //To limit to Scotland uncomment below
//      .where("longitude < 0").where("longitude > -8")
//      .where("latitude > 54").where("latitude < 61")

    val reviewDf = sqlContext.read.format("json").load(review)
    val userDf = sqlContext.read.format("json").load(user)

    val scotReviewDf = scotBusinessDf.select("business_id")
      .join(reviewDf.select("business_id", "user_id", "stars"), Seq("business_id"))

    val scotUserWithIdDf = getUserWithIdDf(sqlContext, scotReviewDf, userDf)
    val scotBusinessWithIdDf = getBusinessWithIdDf(sqlContext, scotReviewDf, scotBusinessDf)

    bestBusinessesPerCategory(sqlContext, scotBusinessWithIdDf)

    if(!Files.exists(Paths.get(modelFileName))){
      val ratings = scotReviewDf.join(scotUserWithIdDf.select("numeric_user_id", "user_id"), Seq("user_id"))
        .join(scotBusinessWithIdDf.select("numeric_business_id", "business_id"), Seq("business_id"))
        .select("numeric_user_id", "numeric_business_id", "stars").rdd.map(r =>
        Rating(r.getInt(0), r.getInt(1), r.getLong(2).toDouble)).cache()

      //choose the best model
      //val bestParams = train(ratings)
      val bestParams = CFParams(12, 0.1, 20)

      //train on all data with the best model
      buildBestModel(ratings, bestParams).save(sc, modelFileName)

      //export to be used outside of Spark
      export(sc, modelFileName)
    }
  }

  def bestBusinessesPerCategory(sqlContext: SQLContext, businessDf: DataFrame) = {
    if(!Files.exists(Paths.get(perCategory))){
      val sorted = businessDf.select("numeric_business_id", "stars", "categories").rdd.flatMap(r =>
        r.getString(2).split(listSeparator).map(c => (c, (r.getInt(0), r.getDouble(1)))))
        .groupByKey()
        .mapValues(v => v.toList.sortWith((x,y) => x._2 > y._2).map(x => x._1).mkString(","))
        .zipWithIndex.map(x => x._2 + "|" + x._1._1 + "|" + x._1._2)

      sorted.coalesce(200).saveAsTextFile(perCategory)
    }
  }

  def getBusinessWithIdDf(sqlContext: SQLContext, scotReviewDf: DataFrame, scotBusinessDf: DataFrame): DataFrame = {
    if(Files.exists(Paths.get(businessesFileName))){
      sqlContext.read.format(csv).options(csvOptions).load(businessesFileName)
    } else {
      val scotBusinessWithId = scotReviewDf.select("business_id").distinct.rdd.zipWithIndex.map(x =>
        (x._2.toInt, x._1.getString(0)))
      val scotBusinessWithIdDf = sqlContext.createDataFrame(scotBusinessWithId.map(x => Row.fromTuple(x)), StructType(
        Seq(StructField("numeric_business_id", IntegerType, false), StructField("business_id", StringType, false))))
        .join(scotBusinessDf.where("open = true"), Seq("business_id"))
        //choose fields of interest
        .select("numeric_business_id", "business_id", "hours", "categories", "name", "latitude", "longitude", "stars", "full_address")

      val removeLineBreakers = udf((s: String) => {
        s.replaceAll("\n", ", ")
      })

      val turnToString = udf((seq: Seq[String]) => {
        seq.mkString(listSeparator)
      })

      val cleaned = scotBusinessWithIdDf.withColumn("address", removeLineBreakers(scotBusinessWithIdDf("full_address")))
        .drop("full_address").withColumnRenamed("address", "full_address")
        .withColumn("clean_categories", turnToString(scotBusinessWithIdDf("categories")))
        .drop("categories").withColumnRenamed("clean_categories", "categories")

      cleaned.write.format(csv).options(csvOptions).save(businessesFileName)
      //cleaned
      sqlContext.read.format(csv).options(csvOptions).load(businessesFileName)
    }
  }

  def getUserWithIdDf(sqlContext: SQLContext, scotReviewDf: DataFrame, userDf: DataFrame): DataFrame = {
    if(Files.exists(Paths.get(usersFileName))){
      sqlContext.read.format(csv).options(csvOptions).load(usersFileName)
    } else {
      val scotUserWithId = scotReviewDf.select("user_id").distinct.rdd.zipWithIndex.map(x => (x._2.toInt, x._1.getString(0)))
      val scotUserWithIdDf = sqlContext.createDataFrame(scotUserWithId.map(x => Row.fromTuple(x)), StructType(
        Seq(StructField("numeric_user_id", IntegerType, false), StructField("user_id", StringType, false))))
        .join(userDf, Seq("user_id"))
      //choose fields of interest
        .select("numeric_user_id","user_id", "name")

      scotUserWithIdDf.write.format(csv).options(csvOptions).save(usersFileName)
      scotUserWithIdDf
    }
  }

  def export(sc: SparkContext, modelFileName: String): Unit = {
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val modelCf = MatrixFactorizationModel.load(sc, modelFileName)

    val users = modelCf.userFeatures.map(x => x._1)
    val businesses = modelCf.productFeatures.map(x => x._1)

    //only keep the best recommendations - with more than 4.5 stars
    val allOptionsDf = modelCf.predict(users.cartesian(businesses)).filter(r => r.rating > 4.5).toDF.cache

    if(!Files.exists(Paths.get(perBusiness))){
      val perBusinessRDD = allOptionsDf.rdd.map(r => (r.getInt(1), r.getInt(0))).groupByKey()
        .map(x => x._1 + "|" + x._2.mkString(",")).coalesce(200).saveAsTextFile(perBusiness)
    }

    if(!Files.exists(Paths.get(perUser))){
      val perUserRDD = allOptionsDf.rdd.map(r => (r.getInt(0), r.getInt(1))).groupByKey()
        .map(x => x._1 + "|" + x._2.mkString(",")).coalesce(200).saveAsTextFile(perUser)
    }
  }

  def buildBestModel(ratings: RDD[Rating], params: CFParams): MatrixFactorizationModel = {
    val blocks = -1
    val seed = 42L

    ALS.train(ratings, params.rank, params.numIter, params.lambda, blocks, seed)
  }

  //The best model was trained with rank = 12 and lambda = 0.1, and numIter = 20, and its RMSE on the test set is 1.0992.
  //good score as for netflix prize it was: 0.8985
  def train(ratings: RDD[Rating]): CFParams = {

    val splits = ratings.randomSplit(Array(0.6,0.2,0.2), 42L)
    val training = splits(0).cache
    val validation = splits(1).cache
    val test = splits(2).cache
    val numValidation = validation.count
    val numTest = test.count

    val ranks = List(8, 10, 12)
    val lambdas = List(0.1, 1.0, 10.0)
    val numIters = List(5, 10, 20)
    var bestModel: Option[MatrixFactorizationModel] = None
    var bestValidationRmse = Double.MaxValue
    var bestRank = 0
    var bestLambda = -1.0
    var bestNumIter = -1
    val blocks = -1
    val seed = 42L

    for (rank <- ranks; lambda <- lambdas; numIter <- numIters) {
      val model = ALS.train(training, rank, numIter, lambda, blocks, seed)
      val validationRmse = computeRmse(model, validation, numValidation)
      println("RMSE (validation) = " + validationRmse + " for the model trained with rank = "
        + rank + ", lambda = " + lambda + ", and numIter = " + numIter + ".")
      if (validationRmse < bestValidationRmse) {
        bestModel = Some(model)
        bestValidationRmse = validationRmse
        bestRank = rank
        bestLambda = lambda
        bestNumIter = numIter
      }
    }

    val testRmse = computeRmse(bestModel.get, test, numTest)

    println("The best model was trained with rank = " + bestRank + " and lambda = " + bestLambda
      + ", and numIter = " + bestNumIter + ", and its RMSE on the test set is " + testRmse + ".")

    CFParams(bestRank, bestLambda, bestNumIter)
  }

  def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating], n: Long) = {
    val predictions: RDD[Rating] = model.predict(data.map(x => (x.user, x.product)))
    val predictionsAndRatings = predictions.map(x => ((x.user, x.product), x.rating))
      .join(data.map(x => ((x.user, x.product), x.rating)))
      .values
    math.sqrt(predictionsAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).reduce(_ + _) / n)
  }
}

