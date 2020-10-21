package se4ai.group4.model

import es.upm.etsisi.cf4j.data.*
import es.upm.etsisi.cf4j.util.plot.LinePlot
import es.upm.etsisi.cf4j.qualityMeasure.QualityMeasure
import es.upm.etsisi.cf4j.recommender.Recommender
import es.upm.etsisi.cf4j.util.Range
import es.upm.etsisi.cf4j.data.DataModel
import es.upm.etsisi.cf4j.qualityMeasure.prediction.RMSE
import es.upm.etsisi.cf4j.recommender.matrixFactorization.*
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.*
import org.apache.commons.csv.CSVFormat
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.IOException
import java.util.*
import krangl.*

class CFmodel() 
{
    val NUM_ITERS : Int = 50
    val RANDOM_SEED : Long = 43
    val DATA_FILE_NAME : String = "./data/ratings.csv"
    val TRAIN_PERCENT : Double = 0.3
    val TEST_PERCENT : Double = 0.3
    val REGULATION : Double = 0.250

    protected lateinit var dataModel : DataModel
    protected lateinit var dataSet : RandomSplitDataSet
    protected lateinit var recommender : Recommender
    //protected lateinit var movies : MutableMap<String, String>
    protected lateinit var movies : DataFrame

    init {
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

    fun print() {
        println("hello world")
    }

    // fun readFile(fileName:String, separator:String) {
    //     val inputStream: InputStream = File(fileName).inputStream()
    //     movies = mutableMapOf()
    //     inputStream.bufferedReader().useLines { 
    //         lines -> lines.forEach { 
    //             val s = it.split(separator)
    //             val movieId : String = s[0]
    //             val movieName : String = s[1]
    //             movies.put(movieId, movieName)
    //         }
    //     }
    // }

    fun train() {
        // DataModel load
        if (dataModel == null) {
            println("Data loaded failed")
            return
        }
        // println(dataModel.toString())

        val regValues = doubleArrayOf(0.000, 0.025, 0.05, 0.075, 0.100, 0.125, 0.150, 0.175, 0.200, 0.225, 0.250, 0.275, 0.300, 0.325)


        // To store results
        //val plot =  LinePlot(regValues, "Precision", "RMSE",)

        // Evaluate PMF Recommender
        //plot.addSeries("PMF")
        //for (reg in regValues) {

        val pmf = PMF(dataModel, 5, NUM_ITERS, REGULATION, RANDOM_SEED);
        pmf.fit();

        val rmse : QualityMeasure=  RMSE(pmf)
        val rmseScore : Double = rmse.getScore()

        val precision = Precision(pmf, 20, 4.0)
        val precisionScore : Double = precision.getScore()

        val recall = Recall(pmf, 20, 4.0)
        val recallScore : Double = recall.getScore()

        println(precisionScore.toString())
        println(recallScore.toString())
        println(rmseScore.toString())
            
        //plot.setValue("PMF", precisionScore, rmseScore)
        // Print results
        //plot.printData("0.0000")

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
    fun predict(userId : String?, num : Int = 20, exclude_rated : Boolean = false) : List<Triple<Any?, Any?, Any?>>
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
            recommend_candidates.add(Candidate(userId, item.getId(), movies.row(item.getId().toInt()-1)["id_long"] as String?, score))
        }    

        // convert to df and sort
        val recommend_candidates_df : DataFrame = recommend_candidates.asDataFrame().sortedByDescending("rating")

        val recommend_list : MutableList<Triple<Any?,Any?,Any?>> = mutableListOf()
        
        var idx = 0
        while (recommend_list.size < num) {
            val row = recommend_candidates_df.row(idx)
            val triple = Triple(row["rating"],row["movieId"],row["movieName"])
            if (!recommend_list.contains(triple)) {
                recommend_list.add(triple)
                //println(triple)
            }
            idx++
        }
        return Collections.unmodifiableList(recommend_list)
    }

    fun movieNeighbors(movieId : String, num : Int = 20)
    {

    }

    data class Candidate(val userId:String?, val movieId:String?, val movieName:String?, val rating:Double?)
}