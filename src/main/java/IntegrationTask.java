import kafka.*;
import model.CFmodel;

public class IntegrationTask {

    public static void main(String[] args) {
        System.out.println("Executing task...");

        System.out.println("Loading stream integration...");
        KafkaIntegration kafkaIntegration = new KafkaIntegration();

        System.out.println("Running data collection...");
        kafkaIntegration.run();

    }
}
