package swe4.gis.test;

import swe4.gis.Edge;
import swe4.gis.Vertex;

public interface CostEvaluator {
    double cost(Vertex current, Edge edge);
    double estimatedCost(Vertex current, Vertex target);
}
