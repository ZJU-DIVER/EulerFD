package EulerFD.Helpers;

import java.util.ArrayList;
import java.util.Arrays;

import static EulerFD.EulerFD.resTest;
import static EulerFD.EulerFD.violated;

public class ClusterCB extends ArrayList implements Comparable<ClusterCB> {

    private int cid;
    private double curEff;
    private int window = 0;
    private int newTuplePair = 0;
    private int newNonFD = 0;
    private double[] histEffs;
    private int effsWindow;
    private int sampleCnt = 0;

    private String status;
    private int priority;
    private int life;

    public ClusterCB(int cid, String status, int priority, int life) {
        super();
        this.cid = cid;
        this.status = status;
        this.priority = priority;
        this.life = life;
    }

    public ClusterCB(int cid, int effsWindow) {
        super();
        this.cid = cid;
        this.effsWindow = effsWindow;
        this.histEffs = new double[effsWindow];
        Arrays.fill(histEffs, 1);
    }

    public double sampleInCluster() {
        newTuplePair = newNonFD = 0;
        window++;
        sampleCnt++;
        for (int i = 0; i < this.size() - window; i++) {
            violated((int)this.get(i), (int)this.get(i + window));
            newTuplePair++;
        }
        if (newTuplePair == 0) {
            curEff = 0;
        } else {
            newNonFD = resTest.getNewNonFd();
            curEff = (double) newNonFD / newTuplePair;
        }
        histEffs[sampleCnt % effsWindow] = curEff;
        return curEff;
    }

    public double getAvgEff() {
        double sumEff = 0;
        for (int i = 0; i < histEffs.length; i++) {
            sumEff += histEffs[i];
        }
        return sumEff/effsWindow;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public double getCurEff() {
        return curEff;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public int getNewTuplePair() {
        return newTuplePair;
    }

    public int getNewNonFD() {
        return newNonFD;
    }

    public String toString() {
        return "clusterCB: id--" + cid + " priority--" + priority + " life--" + life;
    }

    @Override
    public int compareTo(ClusterCB o) {
        return (int)Math.signum(o.getCurEff() - this.getCurEff());
    }
}
