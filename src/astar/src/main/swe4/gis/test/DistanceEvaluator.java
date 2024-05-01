package swe4.gis.test;

import swe4.gis.Edge;
import swe4.gis.SphericMath;
import swe4.gis.Vertex;

public class DistanceEvaluator implements CostEvaluator {
    public double cost(Vertex current, Edge edge) {
        return edge.getLength();
    }

    public double estimatedCost(Vertex current, Vertex target) {
        return SphericMath.earthDistance(current.getCoordinates(), target.getCoordinates());
    }
}