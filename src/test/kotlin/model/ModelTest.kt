package model

import es.upm.etsisi.cf4j.qualityMeasure.recommendation.F1
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Precision
import es.upm.etsisi.cf4j.qualityMeasure.recommendation.Recall
import es.upm.etsisi.cf4j.util.Range
import es.upm.etsisi.cf4j.util.plot.LinePlot
import es.upm.etsisi.cf4j.util.plot.ScatterPlot
import es.upm.etsisi.cf4j.util.plot.XYPlot
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class ModelTest {
    var prefix_path = "./saved/eval/"

    @Test
    @DisplayName("model regularization test")
    fun regularizationTest() {
        val model = CFmodel()
        if (!model.loadDataModel()) return
        val regValues = doubleArrayOf(
            0.000,
            0.025,
            0.05,
            0.075,
            0.100,
            0.125,
            0.150,
            0.175,
            0.200,
            0.225,
            0.250,
            0.275,
            0.300,
            0.325
        )
        val plot =  LinePlot(regValues, "REG", "RMSE")
        // Evaluate PMF Recommender
        plot.addSeries("PMF")
        for (reg in regValues) {
            val rmse = model.trainWithParam(5, 50, reg)
            plot.setValue("PMF", reg, rmse)
            assertNotEquals(-1, rmse, "rmse error should not be -1")
        }
        // Print results
        plot.exportData(prefix_path + "reg_eval.csv")
        plot.exportPlot(prefix_path + "reg_eval_plot.png")
    }

    @Test
    @DisplayName("model factor test")
    fun factorTest() {
        val model = CFmodel()
        if (!model.loadDataModel()) return
        val NUM_FACTORS = Range.ofIntegers(5, 5, 5)
        // Evaluate PMF Recommender
        val plot =  LinePlot(NUM_FACTORS, "Num of Factor", "RMSE")
        // Evaluate PMF Recommender
        plot.addSeries("PMF")
        for (factors in NUM_FACTORS) {
            val rmse = model.trainWithParam(5, 50, 0.25)
            plot.setValue("PMF", factors, rmse)
            assertNotEquals(-1, rmse, "rmse error should not be -1")
        }
        // Print results
        plot.exportData(prefix_path + "factor_eval.csv")
    }

    //comparing the number of ratings of each test user with his/her averaged prediction error using BiasedMF as recommender.
    @Test
    @DisplayName("scatter plot test")
    fun ratingPlotTest() {
        val model = CFmodel()
        if (!model.loadDataModel()) return
        model.trainWithParam(10, 50, 0.25)

        val plot = ScatterPlot("Number of ratings", "Averaged user prediction error")

        for (testUser in model.dataModel.testUsers) {
            val predictions = model.recommender.predict(testUser)
            var sum = 0.0
            for (pos in 0 until testUser.numberOfTestRatings) {
                val rating = testUser.getTestRatingAt(pos)
                val prediction = predictions[pos]
                sum += Math.pow(rating - prediction, 2.0)
            }
            val userError = sum / testUser.numberOfTestRatings
            plot.addPoint(testUser.numberOfRatings.toDouble(), userError)
        }

        plot.exportData(prefix_path + "exports/rating-plot-data.csv")
        plot.exportPlot(prefix_path + "exports/rating-plot.png")
    }

    @Test
    @DisplayName("Precision and Recall XY plot test")
    fun xyPlotTest() {
        val model = CFmodel()
        if (!model.loadDataModel()) return
        val numberOfRecommendations = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val labels = arrayOfNulls<String>(numberOfRecommendations.size)
        for (i in labels.indices) {
            labels[i] = numberOfRecommendations[i].toString()
        }

        val plot = XYPlot(labels, "Recall", "Precision")

        model.trainWithParam(5, 50, 0.25)
        plot.addSeries("PMF")
        plot.setLabelsVisible("PMF")

        for (N in numberOfRecommendations) {
            val precision = Precision(model.recommender, N, 4.0)
            val precisionScore: Double = precision.getScore()
            val recall = Recall(model.recommender, N, 4.0)
            val recallScore = recall.score
            plot.setXY("PMF", N.toString(), precisionScore, recallScore)
        }

        plot.exportPlot(prefix_path + "exports/precision-recall-plot.png")
        plot.exportData(prefix_path + "exports/precision-recall-plot-data.csv")
    }

    @Test
    @DisplayName("F1 score plot")
    fun F1plotTest() {
        val numberOfRecommendations = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val plot = LinePlot(numberOfRecommendations, "Number of recommendations", "F1")

        val model = CFmodel()
        if (!model.loadDataModel()) return
        model.trainWithParam(5, 50, 0.25)
        plot.addSeries("PMF")

        for (N in numberOfRecommendations) {
            val f1 = F1(model.recommender, N, 4.0)
            val score = f1.score
            plot.setValue("PMF", N, score)
        }

        plot.exportPlot(prefix_path + "exports/F1-plot.png")
        plot.exportData(prefix_path + "exports/F1-plot-data.csv")
    }

}