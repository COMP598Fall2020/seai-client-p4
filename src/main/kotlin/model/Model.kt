package model

import es.upm.etsisi.cf4j.data.DataModel
import es.upm.etsisi.cf4j.data.RandomSplitDataSet
import es.upm.etsisi.cf4j.qualityMeasure.QualityMeasure
import es.upm.etsisi.cf4j.qualityMeasure.prediction.RMSE
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Precision
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Recall
import es.upm.etsisi.cf4j.recommender.Recommender
import es.upm.etsisi.cf4j.recommender.matrixFactorization.PMF
import krangl.DataFrame
import krangl.asDataFrame
import krangl.readCSV
import java.io.*
import java.util.*

class CFmodel()
{
    val NUM_ITERS : Int = 50
    val RANDOM_SEED : Long = 43
    val DATA_FILE_NAME : String = "./data/ratings.csv"
    val DATA_SAVE_PATH : String = "./saved/datamodel.txt"
    val MODEL_SAVE_PATH : String = "./saved/model.txt"
    val TRAIN_PERCENT : Double = 0.3
    val TEST_PERCENT : Double = 0.3
    val REGULATION : Double = 0.250

    lateinit var dataModel : DataModel
    protected lateinit var dataSet : RandomSplitDataSet
    lateinit var recommender : Recommender
    //protected lateinit var movies : MutableMap<String, String>
    protected lateinit var movies : DataFrame

    init {
        if (!loadDataSet())
            initDataSet()
    }

    fun initDataSet() {
        dataSet = RandomSplitDataSet(DATA_FILE_NAME, TRAIN_PERCENT, TEST_PERCENT, ",")
        dataModel = DataModel(dataSet) 
        //dataModel = BenchmarkDataModels.MovieLens100K()
        //readFile("data/movies.csv", ",")
        movies = DataFrame.readCSV("data/movies_all.csv")
        //println(movies.filterByRow { it["id"] as Int > 1130 })
    }

    fun saveDataSet() {
        dataModel.save(DATA_SAVE_PATH)
    }

    fun loadDataSet() : Boolean {
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
        val pmf = PMF(dataModel, 5, NUM_ITERS, REGULATION, RANDOM_SEED);
        pmf.fit();

        val rmse : QualityMeasure=  RMSE(pmf)
        val rmseScore : Double = rmse.getScore()

        recommender = pmf
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
        if (id == -1) {
            //println("Cannot predict because userId not found")
            val list : MutableList<Triple<Any?, Any?, Any?>> = mutableListOf()
            for (i in 1..num) {
                list.add(Triple(userId, (0..movies.nrow).random(), null))
            }
            return Collections.unmodifiableList(list)
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