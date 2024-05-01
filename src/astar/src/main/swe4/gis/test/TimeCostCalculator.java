package swe4.gis.test;

import swe4.gis.Edge;
import swe4.gis.Vertex;
import swe4.gis.test.CostCalculator;

import java.util.Map;

public class TimeCostCalculator implements CostCalculator {
    private static final Map<Short, Double> speedByCategory = Map.of(
            (short)1, 100.0,  // Autobahn
            (short)2, 80.0,   // Bundesstraße
            (short)3, 60.0,   // Landstraße
            (short)4, 50.0,   // Stadtstraße
            (short)5, 30.0,   // Dorfstraße
            (short)6, 20.0    // Feldweg
    );

    private static final double MAX_SPEED = speedByCategory.get((short)1); // Geschwindigkeit der Autobahn

    @Override
    public double costs(Edge edge) {
        double speed = speedByCategory.getOrDefault(edge.getCategory(), 50.0); // Standardgeschwindigkeit, wenn keine Kategorie gefunden wird
        return edge.getLength() / speed * 60;  // Zeit in Minuten
    }

    @Override
    public double estimatedCosts(Vertex v1, Vertex v2) {
        // Einfache Heuristik: Distanz / Höchstgeschwindigkeit
        double distance = Math.hypot(v1.getCoordinates().getLatitude() - v2.getCoordinates().getLatitude(),
                v1.getCoordinates().getLongitude() - v2.getCoordinates().getLongitude());
        return distance / MAX_SPEED * 60;  // Geschätzte Zeit in Minuten bei Höchstgeschwindigkeit
    }
}
