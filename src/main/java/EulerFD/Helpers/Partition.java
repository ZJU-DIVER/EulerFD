package EulerFD.Helpers;

import EulerFD.Bitset.IBitSet;
import EulerFD.Bitset.LongBitSet;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Partition {

    private List<HashMap<String, Integer>> valueIndex;
    private int[] clusterNum;
    private List<String> attributes;
    private int attrNum;
    private int[][] data;
    private int dataNum = -1;
    private ArrayList<ArrayList<IntArrayList>> partition;
    private int attrNumAfterPar;
    private int partitionAllNum = 0;
    private IBitSet constantColumns;

    public Partition(String filePath) {
        readAndPartition(filePath);
        strippedAndCluster();
    }

    private void readAndPartition(String filePath) {
        valueIndex = new ArrayList<>();
        partition = new ArrayList<>();
        attributes = new ArrayList<>();
        constantColumns = LongBitSet.FACTORY.create();
        String[] rawRecord = null;
        try {
            CSVReader csvReader = new CSVReader(new FileReader(filePath));
            while(csvReader.readNextSilently() != null) {
                dataNum++;
            }
            csvReader = new CSVReader(new FileReader(filePath));
            while((rawRecord = csvReader.readNext()) != null) {
                break;
            }
            for (int i = 0; i < rawRecord.length; i++) {
                attributes.add(rawRecord[i]);
            }
            attrNum = rawRecord.length;
            clusterNum = new int[attrNum];
            data = new int[dataNum][attrNum];
            for (int i = 0; i < attrNum; i++) {
                valueIndex.add(new HashMap<String, Integer>());
                partition.add(new ArrayList<>());
            }
            while((rawRecord = csvReader.readNext()) != null) {
                buildValueIndex(rawRecord);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("not found");
        } catch (IOException e){
            throw new RuntimeException(e.getMessage());
        } catch (CsvException e) {
            e.printStackTrace();
        }
    }

    private int curIndex = 1;
    private int tupleID = 0;

    private void buildValueIndex(String[] rawRecord) {
        for (int i = 0; i < attrNum; i++) {
            curIndex = clusterNum[i];
            if (!valueIndex.get(i).containsKey(rawRecord[i])) {
                curIndex++;
                valueIndex.get(i).put(rawRecord[i], curIndex);
                partition.get(i).add(new IntArrayList());
                clusterNum[i]++;
            }
            curIndex = valueIndex.get(i).get(rawRecord[i]);
            data[tupleID][i] = curIndex;
            partition.get(i).get(curIndex - 1).add(tupleID);
        }
        tupleID++;
    }

    private void strippedAndCluster() {
        int curMax = 0, curSize = 0;
        for (int i = attrNum - 1; i >= 0 ; i--) {
            if (partition.get(i).size() == 1) {
                partition.remove(i);
                constantColumns.set(i);
                continue;
            }
            else if (partition.get(i).size() == dataNum) {
                partition.remove(i);
                continue;
            }
            for (int j = partition.get(i).size() - 1; j >= 0 ; j--) {
                curSize = partition.get(i).get(j).size();
                if (curSize == 1) {
                    partition.get(i).remove(j);
                } else {
                    if (curSize > curMax) {
                        curMax = curSize;
                    }
                    if (curSize != 2) {
                        Collections.shuffle(partition.get(i).get(j));
                    }
                }
            }
            partitionAllNum += partition.get(i).size();
            curMax = 0;
        }
        attrNumAfterPar = partition.size();
    }

    public ArrayList<ArrayList<IntArrayList>> getPartition() {
        return partition;
    }

    public int[][] getData() {
        return data;
    }

    public int getAttributeNum() {
        return attrNum;
    }

    public int getDataNum() {
        return dataNum;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public int getPartitionAllNum() {
        return partitionAllNum;
    }

    public int getAttrNumAfterPar() {
        return attrNumAfterPar;
    }

    public IBitSet getConstantColumns() {
        return constantColumns;
    }

}
