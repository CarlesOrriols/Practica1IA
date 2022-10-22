package aima;

import aima.search.framework.HeuristicFunction;

public class EnergiaHeuristicFunction implements HeuristicFunction {
    @Override
    public double getHeuristicValue(Object o) {
        return -((EnergiaEstado) o).beneficioTotal();
    }
}
