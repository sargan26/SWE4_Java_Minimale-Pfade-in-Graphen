package swe4.gis.test;

import swe4.gis.Edge;
import swe4.gis.Vertex;

public interface CostCalculator {
    double costs(Edge edge);
    double estimatedCosts(Vertex v1, Vertex v2);
}
