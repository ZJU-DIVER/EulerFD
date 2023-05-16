package EulerFD.Helpers;

import EulerFD.Bitset.IBitSet;

public class FuncDependency {
    private IBitSet lhs;
    private int rhs;

    public FuncDependency(IBitSet lhs, int rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public IBitSet getLhs() {
        return lhs;
    }

    public int getRhs() {
        return rhs;
    }

    @Override
    public boolean equals(Object object){
        if (this == object){
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        FuncDependency funcDependency = (FuncDependency) object;
        if (this.lhs.equals(funcDependency.lhs) && this.rhs == funcDependency.rhs) {
            return true;
        }
        return false;
    }
}
