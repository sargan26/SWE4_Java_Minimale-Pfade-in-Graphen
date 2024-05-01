package swe4.gis.test;

import swe4.gis.Edge;
import swe4.gis.Vertex;

public class TimeEvaluator implements CostEvaluator {
    private CostCalculator calculator;

    public TimeEvaluator(CostCalculator calculator) {
        this.calculator = calculator;
    }

    public double cost(Vertex current, Edge edge) {
        return calculator.costs(edge);
    }

    public double estimatedCost(Vertex current, Vertex target) {
        return calculator.estimatedCosts(current, target);
    }
}
