package model

import es.upm.etsisi.cf4j.data.DataModel
import es.upm.etsisi.cf4j.data.DataSet
import es.upm.etsisi.cf4j.data.RandomSplitDataSet
import es.upm.etsisi.cf4j.qualityMeasure.QualityMeasure
import es.upm.etsisi.cf4j.qualityMeasure.prediction.RMSE
import es.upm.etsisi.cf4j.recommender.Recommender
import es.upm.etsisi.cf4j.recommender.matrixFactorization.PMF
import krangl.DataFrame
import krangl.asDataFrame
import krangl.fromResultSet
import krangl.readCSV
import java.io.*
import java.util.*
import java.sql.*
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.F1
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Precision
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Recall

class CFmodel()
{
    val NUM_ITERS : Int = 100
    val RANDOM_SEED : Long = 43
    val DATA_FILE_NAME : String = "./data/ratings.csv"
    val DATA_SAVE_PATH : String = "./saved/datamodel.txt"
    val MODEL_SAVE_PATH : String = "./saved/model.txt"
    val TRAIN_PERCENT : Double = 0.7
    val TEST_PERCENT : Double = 0.3
    val REGULATION : Double = 0.250
    val NUM_FACTORS : Int = 5
    val LR : Double = 0.01

    lateinit var dataModel : DataModel
    protected lateinit var dataSet : RandomSplitDataSet
    lateinit var recommender : Recommender
    //protected lateinit var movies : MutableMap<String, String>
    protected lateinit var movies : DataFrame

    init { }

    fun initDataSet() {
        // laod new dataset from csv
        dataSet = RandomSplitDataSet(DATA_FILE_NAME, TRAIN_PERCENT, TEST_PERCENT, ",")
        dataModel = DataModel(dataSet)
        // save data model
        saveDataModel()

        //readFile("data/movies.csv", ",")
        movies = DataFrame.readCSV("data/movies_all.csv")
    }

    fun saveDataModel() {
        dataModel.save(DATA_SAVE_PATH)
    }

    fun loadDataModel() : Boolean {
        val file = File(DATA_SAVE_PATH)
        val fileExists = file.exists()
        if (fileExists) {
            dataModel = DataModel.load(DATA_SAVE_PATH)
            return true
        }
        return false
    }

    fun trainWithParam(numFactors: Int, numIter: Int, regulation: Double) : Double {
        val pmf = PMF(dataModel, numFactors, numIter, regulation, 43)
        pmf.fit()
        val rmse : QualityMeasure=  RMSE(pmf)
        val rmseScore : Double = rmse.getScore()
        System.out.println("Finish training")
        recommender = pmf
        return rmseScore
    }


    fun train() {
        val pmf = PMF(dataModel, NUM_FACTORS, NUM_ITERS, REGULATION, LR, RANDOM_SEED);
        pmf.fit();

        postEval(pmf)

        recommender = pmf
    }

    fun postEval(recommender:Recommender) {
        // TODO: add evaluation here, then store to the database
        val url = "jdbc:postgresql://localhost:5432/se4ai_t4?user=postgres&password=team_jelly"
        try {
            val conn: Connection = DriverManager.getConnection(url);

            val rmse : QualityMeasure=  RMSE(recommender)
            val rmseScore : Double = rmse.getScore()

            val precision = Precision(recommender, 5, 4.0)
            val precisionScore: Double = precision.getScore()

            val recall = Recall(recommender, 5, 4.0)
            val recallScore = recall.score

            val f1 = F1(recommender, 5, 4.0)
            val f1Score = f1.score

            // insert data
            val stmt = conn.prepareStatement(
                    """
                        INSERT INTO public.performance (rmse, precision, recall, f1, time) 
                        VALUES (?,?,?,?,?)
                    """
            )
            stmt.setFloat(1, rmseScore.toFloat())
            stmt.setFloat(2, precisionScore.toFloat())
            stmt.setFloat(3, recallScore.toFloat())
            stmt.setFloat(4, f1Score.toFloat())
            val timestamp : Timestamp = Timestamp(System.currentTimeMillis())
            stmt.setTimestamp(5, timestamp)

            try {
                val result = stmt.executeUpdate()
            }
            catch (e:SQLException) {
                println("failed to insert performance data, " + e.message)
            }

            stmt.close()
            conn.close()
        }
        catch (e: SQLException) {
            println(e.message)
        }


    }

    /**
     * Predict function used to output a list of recommended movies for the user
     * @userId : active user id to give recommendations
     * @num : number of recommendations to give
     * @exclude_rate: weather or not to include movies that have been rated by the user before
     *
     * @return a list of Triple that consists of movieId, movieName, and predicted ratings
     */
    fun predict(userId: String?, num: Int = 20, exclude_rated: Boolean = false) : List<Triple<Any?, Any?, Any?>>
    {
        // find user id in dataModel
        var id : Int = dataModel.findUserIndex(userId)
        if (id == -1) 
            id = dataModel.findTestUserIndex(userId)
        //if (id == -1) {
            //println("Cannot predict because userId not found")
            //val list : MutableList<Triple<Any?, Any?, Any?>> = mutableListOf()
            //for (i in 1..num) {
            //    list.add(Triple(userId, (0..movies.nrow).random(), null))
            //}
            //return Collections.unmodifiableList(list)
        //}
        while (id == -1) {
            id = dataModel.findUserIndex((0..182799).random().toString())
        }
        // pass in id and calculate score with every movie
        val items = dataModel.getItems()
        val testItems = dataModel.getTestItems()
        val allItems = (items + testItems).toList()
        
        if (exclude_rated) {
            // remove rated item from allItems
        }

        val recommend_candidates : MutableList<Candidate> = mutableListOf()
        //println(movies.filterByRow(it["id"] == "9004"))
        for (item in allItems) {
            val score : Double = recommender.predict(id, item.getItemIndex())
            recommend_candidates.add(
                Candidate(
                    userId,
                    item.getId(),
                    movies.row(item.getId().toInt() - 1)["id_long"] as String?,
                    score
                )
            )
        }    

        // convert to df and sort
        val recommend_candidates_df : DataFrame = recommend_candidates.asDataFrame().sortedByDescending("rating")

        val recommend_list : MutableList<Triple<Any?, Any?, Any?>> = mutableListOf()
        
        var idx = 0
        while (recommend_list.size < num) {
            val row = recommend_candidates_df.row(idx)
            val triple = Triple(row["rating"], row["movieId"], row["movieName"])
            if (!recommend_list.contains(triple)) {
                recommend_list.add(triple)
                //println(triple)
            }
            idx++
        }
        return Collections.unmodifiableList(recommend_list)
    }

    fun movieNeighbors(movieId: String, num: Int = 20)
    {

    }

    data class Candidate(val userId: String?, val movieId: String?, val movieName: String?, val rating: Double?)
}