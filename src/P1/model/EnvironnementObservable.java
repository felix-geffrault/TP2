package P1.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class EnvironnementObservable extends Environnement{
    private PropertyChangeSupport support;

    public EnvironnementObservable(ArrayList<Agent> agents, int sizeX, int sizeY, int diffusionDistance, double initialSignalIntensity,int nA, int nB, int nC) {
        super( agents,  sizeX,  sizeY,  diffusionDistance,  initialSignalIntensity, nA,  nB,  nC);
        support = new PropertyChangeSupport(this);
    }

    public void agentMove(Agent a, Direction d){
        int[] oldPos = agentsCoordinates.get(a).clone();
        super.agentMove(a, d);
        int[] newPos = agentsCoordinates.get(a);
        Map.Entry<Agent, int[]> oldAgentPosValue = new AbstractMap.SimpleEntry<>(a, oldPos);
        Map.Entry<Agent, int[]> newAgentPosValue = new AbstractMap.SimpleEntry<>(a, newPos);
        support.firePropertyChange("pos", oldAgentPosValue, newAgentPosValue);
    }

    @Override
    public Character setAgentCase(Agent a, char c){
        Character n = super.setAgentCase(a, c);
        int[] pos = this.agentsCoordinates.get(a);
        Map.Entry<int[], Character> oldCaseValue = new AbstractMap.SimpleEntry<>(pos, n);
        Map.Entry<int[], Character> newCaseValue = new AbstractMap.SimpleEntry<>(pos, c);
        support.firePropertyChange("case", oldCaseValue, newCaseValue);
        Map.Entry<int[], Character> oldAgentValue = new AbstractMap.SimpleEntry<>(pos, c);
        Map.Entry<int[], Character> newAgentValue = new AbstractMap.SimpleEntry<>(pos, n);
        support.firePropertyChange("hold", oldAgentValue, newAgentValue);
        return n;
    }

    public Character getGridAtPos(int x, int y) {
        return super.grid.get(y).get(x);
    }

    public Map<Agent, int[]> getAgentsCoordinates(){
        return agentsCoordinates;
    }
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }
}
