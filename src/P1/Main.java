package P1;

import P1.model.Agent;
import P1.model.EnvironnementObservable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ArrayList;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        int nbAgents = 20;
        double kPlus = 0.1;
        double kMinus = 0.3;
        int memorySize = 10;
        int sizeX = 50;
        int sizeY = 50;
        int nA = 200;
        int nB = 200;
        int nbTurn = 100_000;
        double error = 0.1;
        ArrayList<Agent> agents = new ArrayList<>();
        for(int i = 0; i<nbAgents; i++){
            Agent a = new Agent(kMinus, kPlus, memorySize, nbTurn, error);
            agents.add(a);
        }
        EnvironnementObservable env = new EnvironnementObservable(agents, sizeX, sizeY, nA, nB);
        TilePane pane = new TilePane();
        View v = new View(pane, env, sizeX, sizeY);
        env.addPropertyChangeListener(v);
        primaryStage.setTitle("SMA TP2");
        primaryStage.setScene(new Scene(pane));
        primaryStage.setResizable(false);
        primaryStage.show();
        Thread.sleep(4000);
        env.start();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
