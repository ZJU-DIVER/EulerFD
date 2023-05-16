package EulerFD;

import EulerFD.Helpers.ClusterCB;
import EulerFD.Helpers.ClusterQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.*;

public class Sampling {

    private int curClusterCBNum = 0;
    private int cid = 0;

    private double effRange = 0.001;
    private ClusterQueue[] clusterQueues = new ClusterQueue[5];
    private PriorityQueue<ClusterCB> lastQueue;

    private ArrayList<ArrayList<IntArrayList>> plis;
    private int numberCluster;
    private double effThreshold;
    private int effWindow;

    public Sampling(ArrayList<ArrayList<IntArrayList>> plis, int numberCluster, double effThreshold, int effWindow) {
        this.plis = plis;
        this.numberCluster = numberCluster;
        this.effThreshold = effThreshold;
        this.effWindow = effWindow;
    }

    public void sample() {
        if (lastQueue == null) {
            initParams();
            initEfficiency();
        } else {
            startMLFQ();
        }
    }

    public void initParams() {
        for(int i = 0; i < clusterQueues.length; i++) {
            clusterQueues[i] = new ClusterQueue(effRange);
            effRange *= 10;
        }
        lastQueue = new PriorityQueue<>(numberCluster);
    }

    public void initEfficiency() {
        ClusterCB clusterCB;
        for (int i = 0; i < plis.size(); i++) {
            for (int j = 0; j < plis.get(i).size(); j++) {
                clusterCB = new ClusterCB(++cid, effWindow);
                clusterCB.addAll(plis.get(i).get(j));
                double eff = clusterCB.sampleInCluster();
                if (eff < 0.001) {
                    lastQueue.add(clusterCB);
                    continue;
                }
                int clusterQueueId = (int) Math.floor(Math.log10(eff)) + 3;
                clusterQueueId = clusterQueueId > 4 ? 4 : clusterQueueId;
                clusterQueues[clusterQueueId].add(clusterCB);
                curClusterCBNum++;
            }
        }
        effThreshold = Math.min(effThreshold, lastQueue.peek().getAvgEff() * 0.5);
    }

    public void startMLFQ() {
        ClusterCB clusterCB = null;
        while (curClusterCBNum != 0) {
            for (int i = clusterQueues.length - 1; i >= 0; i--) {
                while (clusterQueues[i].size() > 0) {
                    clusterCB = clusterQueues[i].poll();
                    curClusterCBNum--;
                    double eff = clusterCB.sampleInCluster();
                    double avgEff = clusterCB.getAvgEff();
                    if (avgEff == 0) {
                        continue;
                    } else if (avgEff < 0.001) {
                        lastQueue.add(clusterCB);
                    } else {
                        int clusterQueueId = (int) Math.floor(Math.log10(avgEff)) + 3;
                        clusterQueues[clusterQueueId].add(clusterCB);
                        curClusterCBNum++;
                    }
                }
            }
        }
        if (lastQueue.size() != 0) {
            effThreshold = Math.min(effThreshold / 4, lastQueue.peek().getAvgEff() * 0.5f);
        }
        while (lastQueue.size() != 0 && lastQueue.peek().getAvgEff() >= effThreshold) {
            clusterCB = lastQueue.poll();
            clusterCB.sampleInCluster();
            if (clusterCB.getAvgEff() > 0) {
                lastQueue.add(clusterCB);
            }
        }
    }

}
