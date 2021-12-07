package P1;

import P1.model.Agent;
import P1.model.Environnement;
import P1.model.EnvironnementObservable;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class View implements PropertyChangeListener {

    private TilePane pane;
    private EnvironnementObservable env;
    private int tileWidth = 10;
    private int tileHeight = 10;
    private Color colorA = Color.rgb(0, 255, 255);
    private Color colorB = Color.rgb(0, 255, 0);
    private Color color0 = Color.WHITE;
    private Image imgAgent = new Image("/Agent.png", 8, 8, false, true);

    public View(TilePane pane, EnvironnementObservable env, int sizeX, int sizeY) {
        this.pane = pane;
        pane.setPrefTileHeight(tileHeight);
        pane.setPrefTileWidth(tileWidth);
        pane.setPrefColumns(sizeY);
        pane.setPrefRows(sizeX);
        pane.setPrefSize(tileWidth*sizeX,tileHeight*sizeY);
        this.env = env;

        for(int y=0; y<sizeY; y++){
            for(int x=0; x<sizeX; x++){
                char c = env.getGridAtPos(x, y);
                pane.getChildren().add(generateFoodRectangle(c));
            }
        }
        for(Map.Entry<Agent, int[]> entry: env.getAgentsCoordinates().entrySet()){
            int[] pos = entry.getValue();
            Agent a = entry.getKey();
            StackPane stackPane = new StackPane();
            Rectangle r = (Rectangle) pane.getChildren().set(pos[1]*50+pos[0], stackPane);
            stackPane.getChildren().add(r);
            stackPane.getChildren().add(generateAgent(a));
        }
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch(evt.getPropertyName()) {
                    case "pos" -> agentPositionChanged(evt);
                    case "case" -> caseValueChanged(evt);
                    case "hold" -> agentHoldChanged(evt);
                }
                semaphore.release();
            }
        });
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void agentHoldChanged(PropertyChangeEvent evt) {
        Map.Entry<Agent, Character> newAgentValue = (Map.Entry<Agent, Character>) evt.getNewValue();
        Agent a = newAgentValue.getKey();
        int[] pos = env.getAgentsCoordinates().get(a);
        StackPane stackPane = (StackPane) pane.getChildren().get(pos[1]*50+pos[0]);
        stackPane.getChildren().set(1, generateAgent(a));
    }

    private void agentPositionChanged(PropertyChangeEvent evt){
        Map.Entry<Agent, int[]> oldAgentPosValue = (Map.Entry<Agent, int[]>) evt.getOldValue();
        Map.Entry<Agent, int[]> newAgentPosValue = (Map.Entry<Agent, int[]>) evt.getNewValue();
        int[] oldPos = oldAgentPosValue.getValue();
        int oldIndex = oldPos[1]*50+oldPos[0];
        StackPane stackPane = (StackPane) pane.getChildren().get(oldIndex);
        Rectangle r = (Rectangle) stackPane.getChildren().get(0);
        int stackSize = stackPane.getChildren().size();
        if(stackSize > 2){ // Contain more than one agent
            Node agentNode = stackPane.getChildren().get(2);
            stackPane = new StackPane();
            stackPane.getChildren().add(agentNode);
        }
        else pane.getChildren().set(oldIndex, r);
        int[] newPos = newAgentPosValue.getValue();
        int newIndex = newPos[1]*50+newPos[0];
        if(pane.getChildren().get(newIndex) instanceof Rectangle) {
            Rectangle r2 = (Rectangle) pane.getChildren().set(newIndex, stackPane);
            stackPane.getChildren().add(0, r2);
        }else{
            Node agentNode = stackPane.getChildren().get(0);
            StackPane stackPaneDest = (StackPane) pane.getChildren().get(newIndex);
            stackPaneDest.getChildren().add(agentNode);
        }
    }

    private void caseValueChanged(PropertyChangeEvent evt){
        Map.Entry<int[], Character> newCaseValue = (Map.Entry<int[], Character>) evt.getNewValue();
        int[] pos = newCaseValue.getKey();
        char c = newCaseValue.getValue();
        Node n = pane.getChildren().get(pos[1]*50+pos[0]);
        if(n instanceof StackPane){
            StackPane stackPane = (StackPane) n;
            stackPane.getChildren().set(0, generateFoodRectangle(c));
        }
        else pane.getChildren().set(pos[1]*50+pos[0], generateFoodRectangle(c));
    }

    private Rectangle generateFoodRectangle(Character c){
        return switch (c){
            case 'A' -> generateRectangle(colorA);
            case 'B' -> generateRectangle(colorB);
            case '0' -> generateRectangle(color0);
            default -> throw new IllegalStateException("Unexpected value while generating FoodRectangle: " + c);
        };
    }

    private Rectangle generateRectangle(Color color){
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(tileWidth);
        rectangle.setHeight(tileHeight);
        rectangle.setFill(color);
        return rectangle;
    }

    private Node generateAgent(Agent a){
        char hold = a.getHold();
        if(hold == '0'){
            ImageView imViewA = new ImageView(imgAgent);
            imViewA.relocate(1,1);
            return imViewA;
        }else{
            StackPane pane = new StackPane();
            pane.setPrefSize(tileWidth, tileHeight);
            ImageView imViewA = new ImageView(imgAgent);
            imViewA.relocate(1,1);
            pane.getChildren().add(imViewA);
            Rectangle r = generateFoodRectangle(hold);
            r.setStroke(Color.BLACK);
            r.resize(3,3);
            r.relocate(tileWidth-4, 1);
            pane.getChildren().add(r);
            return pane;
        }
    }
}
