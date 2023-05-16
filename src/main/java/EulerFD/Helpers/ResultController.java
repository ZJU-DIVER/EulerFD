package EulerFD.Helpers;

import EulerFD.Bitset.IBitSet;

import java.util.ArrayList;
import java.util.List;

public class ResultController {
    private ArrayList<FuncDependency> result;
    private List<String> ColumnNames;

    public ResultController(List<String> columnNames) {
        result = new ArrayList<>();
        ColumnNames = columnNames;
    }

    public ResultController() {
        result = new ArrayList<>();
    }

    public void add(IBitSet lhs, int rhs){
        FuncDependency fd = new FuncDependency(lhs, rhs);
        result.add(fd);
    }

    public void printResult(){
        result.forEach( fd -> {
            IBitSet lhs = fd.getLhs();
            int rhs = fd.getRhs();
            for(int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)){
                System.out.print(ColumnNames.get(i) + "   ");
            }
            System.out.println("   ——>   " + ColumnNames.get(rhs));
        });
    }

}
