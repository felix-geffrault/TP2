package P1.model;

import java.awt.*;
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
    private double error;

    public Agent(double kMinus, double kPlus, int memorySize, int nbTurn, double error){
        this.memory = new ArrayList<Character>();
        this.kMinus = kMinus;
        this.kPlus = kPlus;
        this.hold = '0';
        this.lastStep = 8;
        this.memorySize = memorySize;
        this.nbTurn = nbTurn;
        this.error = error;
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
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("fin de l'agent");
    }

    public void action(){
        boolean action = false;
        if(this.hold == '0' && this.actualCase != '0'){
            action = this.pick();
        }
        else if(this.hold != '0' && this.actualCase == '0'){
            action = this.drop();
        }
        if(!action) this.move();
    }

    public void perception(){
        this.memory.add(0, this.actualCase);
        this.actualCase = this.env.getAgentCase(this);
        if(this.memory.size() > this.memorySize){
            this.memory.remove(this.memorySize);
        }
    }

    private boolean pick(){
        double f = getMemoryFrequency(this.actualCase);
        double p = Math.pow(this.kPlus / (this.kPlus + f), 2);
        if(Math.random() < p){
            this.hold = this.env.agentPickNourriture(this);
            return true;
        }
        return false;
    }

    private boolean drop(){
        double f = getMemoryFrequency(this.hold);
        double p = Math.pow(f / (this.kMinus + f), 2);
/*        System.out.println("p " + p + " f " + f + " hold " + this.hold);
        System.out.println(memory);*/
        if(Math.random() < p){
/*
            System.out.println("Dropped");
*/
            if (env.agentDeposeNourriture(this, this.hold)) {
                this.hold = '0';
                return true;
            }
        }
        return false;
    }

    private void move(){
        int nextStep = r.nextInt(8);

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
        return Collections.frequency(this.memory , c);
    }

    private double getMemoryFrequency(char c){
        int otherObjectFrequency = getNbMemoryOccurrences(this.hold == 'A' ? 'B' : 'A');
        return (getNbMemoryOccurrences(c) * 1.0 + otherObjectFrequency * this.error) / this.memory.size();
    }

    public char getHold() {
        return hold;
    }
}
