package EulerFD;

/**
 * Preprocess: Partition（返回分区和数据表）
 * Sampling: 多级队列 + shuffle + non-FD权重
 * Inversion: AID-FD多线程
 */

import AIDFD.bitset.IBitSet;
import AIDFD.bitset.LongBitSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.lucene.util.RamUsageEstimator;

import java.text.SimpleDateFormat;
import java.util.*;

public class EulerFD {

    private List<String> columnNames;
    public static int numberAttributes;
    //private int numberPartitions;
    //private int recordsNum;
    private static int[][] tuples;
    private ArrayList<ArrayList<IntArrayList>> plis;

    public static PrefixTreeResultThread resTest;
    private double ncoverThreshold;
    //private double posThreshold;
    private int ncoverWindowSize;
    //private int posWindowSize;
    private IBitSet constantColumns;
    //private ArrayList<Integer> partitionMaxSize;
    private ResultController resultController;
    private int numberCluster;

    public double runtime;
    public int fdNum;

    public EulerFD(double ncoverThreshold, double posThreshold, int ncoverWindowSize, int posWindowSize) {
        this.ncoverThreshold = ncoverThreshold;
        //this.posThreshold = posThreshold;
        this.ncoverWindowSize = ncoverWindowSize;
        //this.posWindowSize = posWindowSize;
    }

    private void loadData(String filePath) {
        long t1 = System.currentTimeMillis();
        Partition par = new Partition(filePath);
        tuples = par.getData();
        columnNames = par.getAttributes();    //取属性
        //recordsNum = par.getDataNum();
        numberAttributes = par.getAttributeNum();
        //numberPartitions = par.getAttrNumAfterPar();
        plis = par.getPartition();
        //partitionMaxSize = par.getMaxPartitionSize();    //记录每个分区里最大的cluster大小，为后面建优先级队列时的窗口大小选择作准备
        constantColumns = par.getConstantColumns();
        numberCluster = par.getPartitionAllNum();
        System.out.println("Partition time: " + (double)(System.currentTimeMillis() - t1)/1000 + "s");
    }

    public static void violated(int t1, int t2){
        IBitSet bitSet = LongBitSet.FACTORY.create(numberAttributes);
        for (int i = 0; i < numberAttributes; i++) {
            bitSet.set(i, tuples[t1][i] == tuples[t2][i]);    // 相同的属性置为1
        }
        resTest.add(bitSet);
    }

    public void testDataSet(String fileName, String filePath) {
        long start = System.currentTimeMillis();
        loadData(filePath);
        execute();
        long end = System.currentTimeMillis();
        System.out.print("《" + fileName  + "》");
        for (int i = 0; i < 20 - fileName.length(); i++) System.out.print(" ");
        System.out.println((double)(end - start)/1000 + "s             "  +  fdNum);
        runtime = (double)(end - start)/1000;
    }

    public void execute() {
        resultController = new ResultController(columnNames);
        resTest = new PrefixTreeResultThread(numberAttributes, resultController);
        resTest.setConstantColumns(constantColumns);
        //double NCOVER_threshold = Math.log(recordsNum) / Math.log(10) / Math.log(numberAttributes) * 0.1;
        double[] lastNegCoverRatios = new double[ncoverWindowSize];
        //double[] lastPositiveRatios = new double[posWindowSize];
        // 全置1，因为后面比较阈值是固定窗口大小内的速率的均值，方便前几轮的采样能够大于阈值。
        Arrays.fill(lastNegCoverRatios, 1);
        //Arrays.fill(lastPositiveRatios, 1);
        int sample_cnt = 0, invert_cnt = 0;
        long sampleTime = 0, negativeTime = 0, invertTime = 0;
        Sampling sampling = new Sampling(plis, numberCluster, 0.01, 3);
        while (true) {
            int lastSize = resTest.getNegCoverSize();
            long t0 = System.currentTimeMillis();
            sampling.sample();
//            sampling.sample_SingleQueue();
            sampleTime += System.currentTimeMillis() - t0;
            sample_cnt++;
            double NcoverGrowth = (double)(resTest.getNegCoverSize() - lastSize)/resTest.getNegCoverSize();
            lastNegCoverRatios[sample_cnt % ncoverWindowSize] = NcoverGrowth;
            double averageRatio = Arrays.stream(lastNegCoverRatios).sum() / ncoverWindowSize;
            if (averageRatio < ncoverThreshold){
                long t1 = System.currentTimeMillis();
                resTest.clear();
                fdNum = numberAttributes > 20 ? resTest.generateResultsThreads(): resTest.generateResults();
                invert_cnt++;
                invertTime += System.currentTimeMillis() - t1;
                break;
            }
        }
//         resultController.printResult();
        // 目前采取的方法是保底一次转化，比较牺牲性能
        System.out.println("Sample times: " + sample_cnt + "    Time: " + (double)sampleTime/1000 + "s");
        System.out.println("Invert times: " + invert_cnt + "    Ncover Time:" +(double)negativeTime/1000 + "s"+ "    Positive Time: " + (double)invertTime/1000 + "s");
    }

    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(new Date()));
        EulerFD fd = new EulerFD(0.01, 0.01, 3, 3);
        String fileName = "fd-reduced-15c";
        String filePath = "additional_data/" + fileName + ".csv";
        fd.testDataSet(fileName, filePath);

        System.out.println(RamUsageEstimator.humanSizeOf(tuples));
    }

}
