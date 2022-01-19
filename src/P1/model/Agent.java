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
    /*private int lastStep;*/ // Do nothing ?
    private Environnement env;
    private int memorySize;
    private int nbTurn;
    private Random r = new Random();
    private double error;
    private Agent agentHelper;
    private int helpCallCooldown;
    private int waitingCooldown;
    // In case agent needs to smell again.
    private int moveBeforeSmellingAgain;
    private boolean smelling;
    private boolean isSlave;
    private int smellingCooldown = 0;
    private Direction smellDirection;

    public Agent(double kMinus, double kPlus, int memorySize, int nbTurn, double error){
        this.memory = new ArrayList<>();
        this.kMinus = kMinus;
        this.kPlus = kPlus;
        this.hold = '0';
        /*this.lastStep = 8;*/ // Do nothing ?
        this.memorySize = memorySize;
        this.nbTurn = nbTurn;
        this.error = error;
        this.helpCallCooldown = 0;
        this.waitingCooldown = -1;
        this.smelling = false;
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
        System.out.println("Fin de l'agent");
    }

    public void action(){
        boolean action = false;
        if(this.hold == '0'){
            if(this.smellDirection != null && smellingCooldown == 0){
                action = this.followSmell();
            }
            // Smelling with no direction meaning he is on the case the signal was emmited.
            else if(this.smelling){
                action = helpAgent();
            }
            else if(this.actualCase != '0'){
                action = this.pick();
            }
        }
        else if(this.hold == 'C'){
            action = this.actionObjectC();
        }
        else if(this.hold != '0' && this.actualCase == '0'){
            action = this.drop();
        }
        if(this.hold == '0' && !action && this.smellDirection != null){
            this.followSmell();
        } this.move();
    }

    public void perception(){
        this.memory.add(0, this.actualCase);
        this.actualCase = this.env.getAgentCase(this);
        if(this.memory.size() > this.memorySize){
            this.memory.remove(this.memorySize);
        }
        if(this.env.isAgentOnPheromon(this) && smellingCooldown == 0){
            this.smellDirection = this.env.getSmellDirection(this);
            // Case of on the central case and agent already got helped
            if(this.smellDirection == null && this.hold == '0'){
                this.smellingCooldown = 10;
            }
        };
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
        if(Math.random() < p){
            if (env.agentDeposeNourriture(this, this.hold)) {
                this.hold = '0';
                return true;
            }
        }
        return false;
    }

    private void masterDrop(){
        this.hold = '0';
        this.isSlave = false;
    }

    private Direction move(){
        int nextStep = r.nextInt(8);
        Direction dir;
        switch (nextStep){
            case 0:
                dir = Direction.NE;
                break;
            case 1:
                dir = Direction.E;
                break;
            case 2:
                dir = Direction.SE;
                break;
            case 3:
                dir = Direction.S;
                break;
            case 4:
                dir = Direction.SO;
                break;
            case 5:
                dir = Direction.O;
                break;
            case 6:
                dir = Direction.NO;
                break;
            default:
                dir = Direction.N;
                break;
        }
        env.agentMove(this, dir);
        /*this.lastStep = nextStep;*/ // Do nothing ????
        return dir;
    }

    public Direction move(Direction dir){
        env.agentMove(this, dir);
        return dir;
    }

    public boolean followSmell(){
        this.move(this.smellDirection);
        return true;
    }

    private synchronized boolean actionObjectC(){
        if(isSlave) return true;
        if(agentHelper != null){
            if(drop()){
                this.agentHelper.masterDrop();
                this.agentHelper = null;
            }
            else {
                Direction dir = this.move();
                agentHelper.move(dir);
            }
        }else{
            if(waitingCooldown == 0){
                if (env.agentDeposeNourriture(this, this.hold)) {
                    this.hold = '0';
                    waitingCooldown = -1;
                    helpCallCooldown = 0;
                    return true;
                }
            }
            if(helpCallCooldown == 0){
                this.env.agentCallHelp(this);
                if(waitingCooldown == -1){
                    waitingCooldown = 10;
                }
            }
        }
        return true;
    }

    public synchronized boolean helpAgent(){
        this.isSlave = env.agentCameHelp(this);
        return this.isSlave;
    }

    public void setAgentHelper(Agent a){
        this.agentHelper = a;
    }

    public Agent getAgentHelper(){
        return this.getAgentHelper();
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
