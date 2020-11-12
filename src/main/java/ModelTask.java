import es.upm.etsisi.cf4j.qualityMeasure.prediction.RMSE;
import es.upm.etsisi.cf4j.recommender.matrixFactorization.PMF;
import es.upm.etsisi.cf4j.util.optimization.GridSearch;
import es.upm.etsisi.cf4j.util.optimization.ParamsGrid;
import model.*;

public class ModelTask {

    public static void main(String[] args) {
        System.out.println("Executing model task...");

        System.out.println("Loading dataset...");
        CFmodel model = new CFmodel();

        System.out.println("Finding the best parameters...");
        ParamsGrid grid = new ParamsGrid();

        grid.addParam("numIters", new int[] {50, 75, 100});
        grid.addParam("numFactors", new int[] {5, 10, 15});
        grid.addParam("lambda", new double[] {0.05, 0.10, 0.15});
        grid.addParam("gamma", new double[] {0.001, 0.01, 0.1});

        grid.addFixedParam("seed", 43L);

        GridSearch gs = new GridSearch(model.getDataModel(), grid, PMF.class, RMSE.class);
        gs.fit();


        System.out.println("Printing results...");
        gs.printResults(5);
    }
}
