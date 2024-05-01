import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import swe4.gis.Edge;
import swe4.gis.Graph;
import swe4.gis.InvalidVertexIdException;
import swe4.gis.test.TimeCostCalculator;
import swe4.gis.test.TimeEvaluator;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class GraphTest {
    private Graph graph;
    private long id0, id1, id2, id3;  // Halte die IDs der Knoten
    private TimeCostCalculator timeCalculator;

    @BeforeEach
    public void setUp() throws InvalidVertexIdException {
        graph = new Graph();
        timeCalculator = new TimeCostCalculator();
        // Knoten hinzufügen und ID speichern
        id0 = graph.addVertex(0, 0, 0); // Start
        id1 = graph.addVertex(1,0, 1); // Midway
        id2 = graph.addVertex(2,1, 1); // End
        id3 = graph.addVertex(3, 2, 1); // Isolated

        graph.addEdge("Edge0", id0, id1, 100, (short) 1); // Autobahn, 100 km
        graph.addEdge("Edge1", id1, id2, 70, (short) 2);  // Bundesstraße, 70 km
        graph.addEdge("Edge2", id0, id2, 120, (short) 4); // Stadtstraße, 120 km

        graph.printAllVertices();
        graph.printAllEdges();
    }

    @AfterEach
    public void tearDown() {
        // Optional: Clear the graph if it's not reconstructed in each test
        graph.clear(); // You would need to implement this method in your Graph class
    }

    @Test
    public void testDirectPath() {
        Collection<Edge> path = graph.findShortestPath(id0, id1);
        assertNotNull(path, "Path should not be null");
        assertEquals(1, path.size(), "Path should have 1 edge");
    }

    @Test
    public void testComplexPath() {
        Collection<Edge> path = graph.findShortestPath(id0, id2);
        assertNotNull(path, "Path should not be null");
        assertEquals(id1, path.size(), "Path should have 2 edges");
    }

    @Test
    public void testNoPath() {
        graph.removeEdge(id1, id2); // Entferne die Kante, um den Pfad zu unterbrechen
        Collection<Edge> path = graph.findShortestPath(id1, id2);
        assertTrue(path.isEmpty(), "Path should be empty");
    }

    @Test
    public void testInvalidVertex() {
        assertThrows(InvalidVertexIdException.class, () -> {
            graph.addEdge("FailingEdge", 4, 5, 1.0); // IDs existieren nicht
        }, "Expected InvalidVertexIdException");
    }

    @Test
    public void testFastestPath() {
        Collection<Edge> path = graph.findMinimalPath(id0, id2, timeCalculator);
        assertNotNull(path, "Path should not be null");
        assertEquals(2, path.size(), "Path should take the fastest route with 2 edges");
        double totalTravelTime = graph.pathCosts(path, timeCalculator);
        assertTrue(totalTravelTime < 150, "Total travel time should be less than 150 minutes");
    }

    @Test
    public void testNoPathMinimal() {
        graph.removeEdge(id1, id2); // Entferne die Kante, um den Pfad zu unterbrechen
        Collection<Edge> path = graph.findMinimalPath(id1, id2, timeCalculator);
        assertTrue(path.isEmpty(), "Path should be empty");
    }

    @Test
    public void testMinimalPathByTime() {
        Collection<Edge> path = graph.findPath(id0, id2, new TimeEvaluator(timeCalculator));
        assertNotNull(path, "Path should not be null");
        assertEquals(2, path.size(), "Path should include two edges using the fastest route based on time");
        double totalTime = graph.pathCosts(path, timeCalculator);
        assertTrue(totalTime < 140, "Total travel time should be realistic and calculated correctly based on speeds");
    }

    @Test
    public void testNoAvailablePath() {
        Collection<Edge> path = graph.findPath(id0, id3, new TimeEvaluator(timeCalculator));
        assertTrue(path.isEmpty(), "Path should be empty as there is no available route");
    }

    @Test
    public void testPathWithSingleConnection() {
        graph.removeEdge(id0, id2); // Remove the longer direct connection
        Collection<Edge> path = graph.findPath(id0, id2, new TimeEvaluator(timeCalculator));
        assertEquals(2, path.size(), "Path should route through the middle vertex since direct is removed");
    }

}
