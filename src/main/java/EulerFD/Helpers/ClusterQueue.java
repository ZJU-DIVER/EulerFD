package EulerFD.Helpers;

import java.util.LinkedList;

public class ClusterQueue extends LinkedList<ClusterCB> {

    private int priority;
    private double efficiency;

    public ClusterQueue(int priority) {
        super();
        this.priority = priority;
    }

    public ClusterQueue(double efficiency) {
        super();
        this.efficiency = efficiency;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }
}
