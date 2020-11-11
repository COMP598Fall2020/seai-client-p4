import model.*;

public class ModelTask {

    public static void main(String[] args) {
        System.out.println("Executing model task...");

        System.out.println("Loading dataset...");
        CFmodel model = new CFmodel();

        System.out.println("Training model...");
        model.train();

        System.out.println("Saving dataset...");
        model.save();
    }
}
