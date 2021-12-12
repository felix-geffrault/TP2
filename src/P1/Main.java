package P1;

import P1.model.Agent;
import P1.model.Environnement;
import P1.model.EnvironnementObservable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

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
        ArrayList<Agent> agents = new ArrayList<>();
        for(int i = 0; i<nbAgents; i++){
            Agent a = new Agent(kMinus, kPlus, memorySize, nbTurn);
            agents.add(a);
        }
        EnvironnementObservable env = new EnvironnementObservable(agents, sizeX, sizeY, nA, nB);
        TilePane pane = new TilePane();
        View v = new View(pane, env, sizeX, sizeY);
        env.addPropertyChangeListener(v);
        primaryStage.setTitle("SMA TP2");
        primaryStage.setScene(new Scene(pane));
        primaryStage.setResizable(false);
        env.start();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
