package P1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Agent extends Thread{
    private ArrayList<Character> memory;
    private char actualCase;
    private double kMinus;
    private double kPlus;
    private char hold;
    private int lastStep;
    private Environnement env;
    private int memorySize;
    private int nbTurn;
    private Random r = new Random();

    public Agent(double kMinus, double kPlus, int memorySize, int nbTurn){
        this.memory = new ArrayList<Character>();
        this.kMinus = kMinus;
        this.kPlus = kPlus;
        this.hold = '0';
        this.lastStep = 8;
        this.memorySize = memorySize;
        this.nbTurn = nbTurn;
    }

    @Override
    public void run(){
        while (this.nbTurn-- > 1){
            /// Do Actions
            try{
                synchronized(env) {
                    this.perception();
                    this.action();
                }
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("fin de l'agent");
    }

    public void action(){
        if(this.hold == '0' && this.actualCase != '0'){
            this.pick();
        }
        else if(this.hold != '0' && this.actualCase == '0'){
            this.drop();
        }
        this.move();
    }

    public void perception(){
        this.memory.add(0, this.actualCase);
        this.actualCase = this.env.getAgentCase(this);
        if(this.memory.size() > this.memorySize){
            this.memory.remove(this.memorySize);
        }
    }

    private void pick(){
        double f = getMemoryFrequency(this.actualCase);
        double p = Math.pow(this.kPlus / (this.kPlus + f), 2);
        if(Math.random() < p){
            this.hold = this.env.agentPickNourriture(this);
        }
    }

    private void drop(){
        double f = getMemoryFrequency(this.actualCase);
        double p = Math.pow(f / (this.kMinus + f), 2);
        if(Math.random() < p){
            if (env.agentDeposeNourriture(this, this.hold)) {
                this.hold = '0';
            }
        }
    }

    private void move(){
        int nextStep = r.nextInt(8);
        while (nextStep == (this.lastStep + 4) % 8){
            nextStep = r.nextInt(8);
        }
        switch (nextStep){
            case 0:
                env.agentMove(this, Direction.NE);
                break;
            case 1:
                env.agentMove(this, Direction.E);
                break;
            case 2:
                env.agentMove(this, Direction.SE);
                break;
            case 3:
                env.agentMove(this, Direction.S);
                break;
            case 4:
                env.agentMove(this, Direction.SO);
                break;
            case 5:
                env.agentMove(this, Direction.O);
                break;
            case 6:
                env.agentMove(this, Direction.NO);
                break;
            case 7:
                env.agentMove(this, Direction.N);
                break;
        }
        this.lastStep = nextStep;
    }

    public void setEnv(Environnement env) {
        this.env = env;
    }

    private int getNbMemoryOccurrences(char c){
        return Collections.frequency(this.memory ,this.actualCase);
    }

    private double getMemoryFrequency(char c){
        return getNbMemoryOccurrences(c) * 1.0 / this.memory.size(); // 1.0 transforme en double
    }

    public char getHold() {
        return hold;
    }
}
