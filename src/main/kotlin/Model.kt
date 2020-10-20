package se4ai.group4.model

import es.upm.etsisi.cf4j.data.*
import es.upm.etsisi.cf4j.util.plot.LinePlot
import es.upm.etsisi.cf4j.qualityMeasure.QualityMeasure
import es.upm.etsisi.cf4j.recommender.Recommender
import es.upm.etsisi.cf4j.util.Range
import es.upm.etsisi.cf4j.data.DataModel
import es.upm.etsisi.cf4j.qualityMeasure.prediction.RMSE
import es.upm.etsisi.cf4j.recommender.matrixFactorization.*
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

    val DATA_FILE_NAME : String = ""

    protected lateinit var dataModel : DataModel
    protected lateinit var dataSet : RandomSplitDataSet
    protected lateinit var recommender : Recommender
    protected lateinit var movies : MutableMap<String, String>

    init {
        initDataSet()
    }

    fun initDataSet() {
        // dataSet = RandomSplitDataSet(DATA_FILE_NAME, 30.0, 30.0, ",")
        // dataModel = DataModel(dataSet) 
        dataModel = BenchmarkDataModels.MovieLens100K()
        readFile("u.txt", "|")
    }

    fun print() {
        println("hello world")
    }

    fun readFile(fileName:String, separator:String) {
        val inputStream: InputStream = File(fileName).inputStream()
        movies = mutableMapOf()
        inputStream.bufferedReader().useLines { 
            lines -> lines.forEach { 
                val s = it.split(separator)
                val movieId : String = s[0]
                val movieName : String = s[1]
                movies.put(movieId, movieName)
            }
        }
    }

    fun train() {
        // DataModel load
        if (dataModel == null) {
            println("Data loaded failed")
            return
        }
        println(dataModel.toString())

        // To store results
        //val plot =  LinePlot(NUM_FACTORS, "Regulation", "RMSE")

        // Evaluate PMF Recommender
        //plot.addSeries("PMF")
        val pmf : Recommender =  PMF(dataModel, 5, NUM_ITERS, 0.125, RANDOM_SEED)
        pmf.fit()

        val rmse : QualityMeasure=  RMSE(pmf)
        val rmseScore : Double = rmse.getScore()
        
        //plot.setValue("PMF", 0.125, rmseScore)

        // Print results
        //plot.printData("0", "0.0000")
        println(rmseScore)

        recommender = pmf
    }

    fun predict(userId : String?, num : Int = 20, exclude_rated : Boolean = false) : List<Triple<Any?, Any?, Any?>>
    {
        // find user id in dataModel
        var id : Int = dataModel.findUserIndex(userId)
        if (id == -1) 
            id = dataModel.findTestUserIndex(userId)
        if (id == -1) {
            println("Cannot predict because userId not found")
            return emptyList()
        }
        // pass in id and calculate score with every movie
        val items = dataModel.getItems()
        val testItems = dataModel.getTestItems()
        val allItems = (items + testItems).toList()
        
        if (exclude_rated) {
            // remove rated item from allItems
        }

        val recommend_candidates : MutableList<Candidate> = mutableListOf()
        for (item in allItems) {
            val score : Double = recommender.predict(id, item.getItemIndex())
            recommend_candidates.add(Candidate(userId, item.getId(), movies[item.getId()], score))
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
                println(triple)
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