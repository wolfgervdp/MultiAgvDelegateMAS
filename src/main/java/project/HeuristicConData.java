package project;

import com.github.rinde.rinsim.geom.ConnectionData;
import com.google.common.base.Optional;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class HeuristicConData implements ConnectionData {

    private double length;
    private int heuristicValue;

    @Override
    public Optional<Double> getLength() {
        return Optional.of(length);
    }

    public double getHeuristicValue(){
        //Todo
        throw new NotImplementedException();
    }
}
