package P1.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Environnement {
    protected ArrayList<ArrayList<Character>> grid;
    private ArrayList<Agent> agents;
    protected Map<Agent, int[]> agentsCoordinates;
    private final int sizeX;
    private final int sizeY;
    protected ArrayList<ArrayList<PheromonIntensity>> pheromonIntensityGrid;
    private int diffusionDistance;
    private double initialSignalIntensity;



    public Environnement(ArrayList<Agent> agents, int sizeX, int sizeY, int diffusionDistance, double initialSignalIntensity,int nA, int nB, int nC) {
        this.agents = agents;
        this.agentsCoordinates = new HashMap<>();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.diffusionDistance = diffusionDistance;
        this.initialSignalIntensity = initialSignalIntensity;

        grid = new ArrayList<>();
        pheromonIntensityGrid = new ArrayList<>();
        ArrayList<int[]> allPositions = new ArrayList<>();
        for(int i=0; i<sizeY; i++){
            ArrayList<Character> row = new ArrayList<>();
            ArrayList<PheromonIntensity> pheromonIntensitiesRow = new ArrayList<>();
            for(int j=0; j<sizeX; j++){
                row.add('0');
                pheromonIntensitiesRow.add(null);
                int[] entry = new int[]{j, i};
                allPositions.add(entry);
            }
            grid.add(row);
            pheromonIntensityGrid.add(pheromonIntensitiesRow);
        }
        for(int i = 0; i<nA; i++){
            int a = (int) (Math.random()*allPositions.size());
            int[] pos = allPositions.remove(a);
            grid.get(pos[1]).set(pos[0], 'A');
        }
        for(int i = 0; i<nB; i++){
            int b = (int) (Math.random()*allPositions.size());
            int[] pos = allPositions.remove(b);
            grid.get(pos[1]).set(pos[0], 'B');
        }
        for(int i = 0; i<nC; i++){
            int c = (int) (Math.random()*allPositions.size());
            int[] pos = allPositions.remove(c);
            grid.get(pos[1]).set(pos[0], 'C');
        }
        for(Agent a : agents){
            agentsCoordinates.put(a, new int[]{(int) (Math.random()*sizeX), (int) (Math.random()*sizeY)});
            a.setEnv(this);
        }
    }

    private int[] getMoveResult(int[] coords, Direction d){
        int[] newCoords = coords.clone();
        switch (d){
            case N, NE, NO -> newCoords[1]--;
            case S, SO, SE -> newCoords[1]++;
        }
        switch (d){
            case E, NE, SE -> newCoords[0]++;
            case O, NO, SO -> newCoords[0]--;
        }
        return newCoords;
    }

    public void agentMove(Agent a, Direction d){
        int[] coords = agentsCoordinates.get(a);
        coords = getMoveResult(coords, d);
        // Map loop
        if(coords[0] == sizeX) coords[0] = coords[0]-1;
        else if(coords[0] == -1) coords[0] = 0;
        if(coords[1] == sizeY) coords[1] = coords[1]-1;
        else if (coords[1] == -1) coords[1] = 0;
    };

    public Character agentPickNourriture(Agent a){
        Character n = setAgentCase(a, '0');
        return n;
    }

    public boolean agentDeposeNourriture(Agent a, Character n){
        if(getAgentCase(a) != '0') return false;
        setAgentCase(a, n);
        printError();
        return true;
    }

    public void agentCallHelp(Agent a){
        int[] coords = agentsCoordinates.get(a);
        diffuseSignal(coords);
    }

    public boolean agentCameHelp(Agent a){
        int[] coords = agentsCoordinates.get(a);
        for(Map.Entry<Agent, int[]> agentCoordinates : agentsCoordinates.entrySet()){
            int[] agentCoords = agentCoordinates.getValue();
            Agent agent  = agentCoordinates.getKey();
            if(agent != a && coords == agentCoords){
                if(agent.getHold() == 'C' && agent.getAgentHelper() == null){
                    agent.setAgentHelper(a);
                    return true;
                }
            }

        }
        return false;
    }

    private void diffuseSignal(int[] coords){
        for(int y = coords[1] - diffusionDistance; y < coords[1] + diffusionDistance + 1; y++){
            for(int x = coords[0] - diffusionDistance; x < coords[0] + diffusionDistance + 1; x++){
                int transmitterDistance = Math.max(Math.abs(y-coords[1]), Math.abs(x-coords[0]));
                double intensity = initialSignalIntensity - transmitterDistance * (initialSignalIntensity/this.diffusionDistance);
                PheromonIntensity prevPheromon = pheromonIntensityGrid.get(coords[1]).get(coords[0]);
                if(prevPheromon != null && prevPheromon.getValue() > intensity) continue;
                PheromonIntensity newPheromon = new PheromonIntensity(intensity, new int[]{x, y});
                pheromonIntensityGrid.get(coords[1]).set(coords[0], newPheromon);
            }
        }
    }

    public void pheromonEvapored(PheromonIntensity pheromon){
        int[] coords = pheromon.getCoords();
        pheromonIntensityGrid.get(coords[1]).set(coords[0], null);
    }

    public boolean isAgentOnPheromon(Agent a) {
        int coords[] = agentsCoordinates.get(a);
        return pheromonIntensityGrid.get(coords[1]).get(coords[0]) != null;
    }

    public Direction getSmellDirection(Agent a){
        int coords[] = agentsCoordinates.get(a);
        PheromonIntensity initialPheromon = pheromonIntensityGrid.get(coords[1]).get(coords[0]);
        double initialIntensity = initialPheromon != null ? initialPheromon.getValue() : null;
        Direction dir = null;
        for (Direction d: Direction.values()) {
            int[] newCoords = getMoveResult(coords, d);
            PheromonIntensity casePheromon = pheromonIntensityGrid.get(newCoords[1]).get(newCoords[0]);
            if(casePheromon != null){
                double caseIntensity = casePheromon != null ? casePheromon.getValue() : 0;
                if(caseIntensity > initialIntensity){
                    dir = d;
                }
            }
        }
        return dir;
    }

    public void start(){
        for (Agent a: agents) a.start();
    }


    public Character getAgentCase(Agent a){
        int[] coords = agentsCoordinates.get(a);
        return grid.get(coords[1]).get(coords[0]);
    }

    protected Character setAgentCase(Agent a, char c){
        int[] pos = agentsCoordinates.get(a);
        Character n = grid.get(pos[1]).set(pos[0], c);
        return n;
    }

    /*private void printCountNbFood(){
        int count = 0;
        for(ArrayList<Character> row : grid){
            for(Character c : row){
                if(c != '0') count++;
            }
        }
        System.out.println("Count Food: " + count);
    }*/

    private void printError(){
        int error = 0;
        Direction[] dirs = Direction.values();
        for(int i = 0; i<sizeY; i++){
            for(int j = 0; j<sizeX; j++){
                char c = grid.get(i).get(j);
                if(c == '0') continue;
                boolean nCond = i>0;
                boolean sCond = i < sizeY-1;
                boolean eCond = j < sizeX-1;
                boolean oCond = j > 0;
                boolean[] dirCond = new boolean[] {
                        nCond,
                        nCond && eCond,
                        nCond && oCond,
                        eCond,
                        eCond && sCond,
                        sCond,
                        sCond && oCond,
                        oCond};
                for (int d = 0; d<dirs.length ; d++){
                    if (dirCond[d]){
                        switch (dirs[d]){
                            case N -> error += grid.get(i-1).get(j) == c ? 0 : 1;
                            case NE -> error += grid.get(i-1).get(j+1) == c ? 0 : 1;
                            case NO -> error += grid.get(i-1).get(j-1) == c ? 0 : 1;
                            case E -> error += grid.get(i).get(j+1) == c ? 0 : 1;
                            case SE -> error += grid.get(i+1).get(j+1) == c ? 0 : 1;
                            case S -> error += grid.get(i+1).get(j) == c ? 0 : 1;
                            case SO -> error += grid.get(i+1).get(j-1) == c ? 0 : 1;
                            case O -> error += grid.get(i).get(j-1) == c ? 0 : 1;
                        }
                    }
                }
            }
        }
        System.out.println(error);
    }

    @Override
    public String toString(){
        String str = "";
        for(int i = 0; i<sizeY; i++){
            str += "_".repeat(sizeX);
            str += "\n| ";
            for(int j = 0; j<sizeX; j++){
                str += grid.get(i).get(j) + " | ";
            }
            str += "\n";
        }
        str += "_".repeat(sizeX);
        return str;
    }
}


