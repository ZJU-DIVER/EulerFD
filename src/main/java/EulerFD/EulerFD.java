package EulerFD;

import EulerFD.Bitset.IBitSet;
import EulerFD.Bitset.LongBitSet;
import EulerFD.Helpers.Partition;
import EulerFD.Helpers.PrefixTreeResultThread;
import EulerFD.Helpers.ResultController;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.*;

public class EulerFD {

    private List<String> columnNames;
    public static int numberAttributes;
    private static int[][] tuples;
    private ArrayList<ArrayList<IntArrayList>> plis;

    public static PrefixTreeResultThread resTest;
    private double ncoverThreshold;
    private double posThreshold;
    private int ncoverWindowSize;
    private int posWindowSize;
    private IBitSet constantColumns;
    private ResultController resultController;
    private int numberCluster;
    public int fdNum;

    public EulerFD(double ncoverThreshold, double posThreshold, int ncoverWindowSize, int posWindowSize) {
        this.ncoverThreshold = ncoverThreshold;
        this.posThreshold = posThreshold;
        this.ncoverWindowSize = ncoverWindowSize;
        this.posWindowSize = posWindowSize;
    }

    private void loadData(String filePath) {
        long t1 = System.currentTimeMillis();
        Partition par = new Partition(filePath);
        tuples = par.getData();
        columnNames = par.getAttributes();
        numberAttributes = par.getAttributeNum();
        plis = par.getPartition();
        constantColumns = par.getConstantColumns();
        numberCluster = par.getPartitionAllNum();
    }

    public static void violated(int t1, int t2){
        IBitSet bitSet = LongBitSet.FACTORY.create(numberAttributes);
        for (int i = 0; i < numberAttributes; i++) {
            bitSet.set(i, tuples[t1][i] == tuples[t2][i]);
        }
        resTest.add(bitSet);
    }

    public void testDataSet(String fileName, String filePath) {
        long start = System.currentTimeMillis();
        loadData(filePath);
        execute();
        long end = System.currentTimeMillis();
        System.out.println("dataset: " + fileName);
        System.out.println("runtime: " + (double)(end - start)/1000 + "s");
        System.out.print("number of FDs: " + fdNum);
    }

    public void execute() {
        resultController = new ResultController(columnNames);
        resTest = new PrefixTreeResultThread(numberAttributes, resultController);
        resTest.setConstantColumns(constantColumns);
        double[] lastNegCoverRatios = new double[ncoverWindowSize];
        double[] lastPositiveRatios = new double[posWindowSize];
        Arrays.fill(lastNegCoverRatios, 1);
        Arrays.fill(lastPositiveRatios, 1);
        int sample_cnt = 0;
        Sampling sampling = new Sampling(plis, numberCluster, 0.01, 3);
        while (true) {
            int lastNcoverSize = resTest.getNegCoverSize();
            sampling.sample();
            sample_cnt++;
            double NcoverGrowth = (double)(resTest.getNegCoverSize() - lastNcoverSize)/resTest.getNegCoverSize();
            lastNegCoverRatios[sample_cnt % ncoverWindowSize] = NcoverGrowth;
            double averageNcoverRatio = Arrays.stream(lastNegCoverRatios).sum() / ncoverWindowSize;
            if (averageNcoverRatio < ncoverThreshold){
                resTest.clear();
                int lastPcoverSize = fdNum;
                fdNum = numberAttributes > 20 ? resTest.generateResultsThreads(): resTest.generateResults();
                double PcoverGrowth = (double)(fdNum - lastPcoverSize)/lastPcoverSize;
                lastPositiveRatios[sample_cnt % posWindowSize] = PcoverGrowth;
                double averagePcoverRatio = Arrays.stream(lastPositiveRatios).sum() / posWindowSize;
                if (averagePcoverRatio < posThreshold) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        EulerFD fd = new EulerFD(0.01, 0.01, 3, 3);
        String fileName = "iris";
        String filePath = "dataset/" + fileName + ".csv";
        fd.testDataSet(fileName, filePath);
    }

}
