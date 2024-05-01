import swe4.gis.*;
import swe4.gis.osm.OsmReader;
import swe4.gis.test.CostCalculator;
import swe4.gis.test.TimeCostCalculator;
import swe4.gis.osm.OsmWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("--- Graph 1 ---");
            Graph graph = new Graph();

            // Knoten hinzufügen
            long id1 = graph.addVertex(0,0.0, 0.0);
            long id2 = graph.addVertex(1,1.0, 1.0);
            long id3 = graph.addVertex(2,2.0, 2.0);
            long id4 = graph.addVertex(3, 3.0, 3.0);

            // Alle Knoten ausgeben
            System.out.println("Knoten im Graphen:");
            graph.printAllVertices();

            // Einen Knoten löschen
            graph.removeVertex(id4);

            // Knoten nach dem Löschen ausgeben
            System.out.println("\nKnoten im Graphen nach dem Löschen:");
            graph.printAllVertices();

            // Kanten hinzufügen
            graph.addEdge("Edge1", id1, id2, 1.0);
            graph.addEdge("Edge2", id2, id3, 1.5);
            graph.addEdge("Edge3", id1, id3, 2.0);

            // Alle Kanten ausgeben
            System.out.println("\nKanten im Graphen:");
            graph.printAllEdges();

            // Eine Kante löschen
            graph.removeEdge(id2, id3);

            // Kanten nach dem Löschen ausgeben
            System.out.println("\nKanten im Graphen nach dem Löschen:");
            graph.printAllEdges();

            System.out.println("\n\n --- Graph 2 ---");
            Graph graph2 = new Graph();
            // Knoten hinzufügen
            long id2_1 = graph2.addVertex(0, 0, 0); // Startpunkt
            long id2_2 = graph2.addVertex(1, 0, 1); // Endpunkt
            long id2_3 = graph2.addVertex(2, 1, 1); // Zwischenpunkt

            // Kanten hinzufügen
            graph2.addEdge("Straße A", id2_1, id2_2, 100, (short)2); // Bundesstraße, 100 km
            graph2.addEdge("Straße B", id2_1, id2_3, 70, (short)3);  // Landstraße, 70 km
            graph2.addEdge("Straße C", id2_3, id2_2, 50, (short)4);  // Stadtstraße, 50 km

            // Ausgabe
            graph2.printAllVertices();
            graph2.printAllEdges();

            // CostCalculator für Zeitkosten basierend auf Straßenkategorie
            CostCalculator timeCalculator = new TimeCostCalculator();

            // Finde den kürzesten Weg
            System.out.println("Finde den kürzesten Weg:");
            Collection<Edge> shortestPath = graph.findShortestPath(id2_1, id2_2);
            for (Edge edge : shortestPath) {
                System.out.println(edge.getStart().getId() + " -> " + edge.getEnd().getId() + " (" + edge.getLength() + "km)");
            }

            // Finde den schnellsten Weg
            System.out.println("\nFinde den schnellsten Weg:");
            Collection<Edge> fastestPath = graph.findMinimalPath(id2_1, id2_2, timeCalculator);
            for (Edge edge : fastestPath) {
                System.out.println(edge.getStart().getId() + " -> " + edge.getEnd().getId() + " (" + edge.getLength() + "km, Kategorie: " + edge.getCategory() + ")");
            }

            OsmWriter writer = new OsmWriter();

            // Schreiben des gesamten Graphen
            writer.writeGraph(graph2, "graph2");

            // Schreiben eines Pfads
            writer.writePath(fastestPath, "fastestPath2");
            writer.writePath(shortestPath, "shortestPath2");


            System.out.println("\n\n --- Graph 3 ---");
            // OsmReader erstellen und CSV-Datei einlesen
            OsmReader osmReader = new OsmReader("src/astar/resources/streets_linz.csv");

            Graph graph3 = new Graph();

            // Eine Map verwenden, um bereits hinzugefügte Vertices zu speichern
            Map<Long, Vertex> vertexMap = new HashMap<>();

            while (osmReader.hasMoreEdges()) {
                EdgeData edgeData = osmReader.nextEdge();

                // Überprüfen und Hinzufügen des Startknotens
                Vertex startVertex = vertexMap.get(edgeData.getStartId());
                if (startVertex == null) {
                    startVertex = new Vertex(edgeData.getStartId(), new SphericPoint(edgeData.getStart().getLongitude(), edgeData.getStart().getLatitude()));
                    graph3.addVertex(startVertex.getId(), startVertex.getCoordinates().getLongitude(), startVertex.getCoordinates().getLatitude());
                    vertexMap.put(edgeData.getStartId(), startVertex);
                }

                // Überprüfen und Hinzufügen des Endknotens
                Vertex endVertex = vertexMap.get(edgeData.getEndId());
                if (endVertex == null) {
                    endVertex = new Vertex(edgeData.getEndId(), new SphericPoint(edgeData.getEnd().getLongitude(), edgeData.getEnd().getLatitude()));
                    graph3.addVertex(endVertex.getId(), endVertex.getCoordinates().getLongitude(), endVertex.getCoordinates().getLatitude());
                    vertexMap.put(edgeData.getEndId(), endVertex);
                }

                // Erstellen und Hinzufügen der Kante
                Edge edge = new Edge(startVertex, endVertex, edgeData.getName(), edgeData.getLength(), edgeData.getCategory());

                graph3.addEdge(edge.getName(), edge.getStart().getId(), edge.getEnd().getId(), edge.getLength(), edge.getCategory());
            }
            osmReader.close();


            // Ausgabe
            graph3.printAllVertices();
            graph3.printAllEdges();


            // Testen von shortestPath und fastestPath mit verschiedenen Start- und Endpunkten
            Vertex startVertex = graph3.getVertex(25151249);
            Vertex endVertex = graph3.getVertex(1368901144);
            System.out.println("Startknoten: " + startVertex);
            System.out.println("Endknoten: " + endVertex);

            // Finde den kürzesten Weg
            shortestPath = graph3.findShortestPath(startVertex.getId(), endVertex.getId());
            System.out.println("Kürzester Weg:");
            System.out.println(shortestPath);
            for (Edge edge : shortestPath) {
                System.out.println(edge.getStart().getId() + " -> " + edge.getEnd().getId() + " (" + edge.getLength() + "km)");
            }


            // Finde den schnellsten Weg
            fastestPath = graph3.findMinimalPath(startVertex.getId(), endVertex.getId(), timeCalculator);
            System.out.println("\nSchnellster Weg:");
            for (Edge edge : fastestPath) {
                System.out.println(edge.getStart().getId() + " -> " + edge.getEnd().getId() + " (" + edge.getLength() + "km, Kategorie: " + edge.getCategory() + ")");
            }

            // Schreiben des gesamten Graphen
            writer.writeGraph(graph3, "graph3");

            // Schreiben eines Pfads
            System.out.println(fastestPath);
            System.out.println(shortestPath);
            writer.writePath(fastestPath, "fastestPath3");
            writer.writePath(shortestPath, "shortestPath3");

        } catch (IllegalArgumentException | InvalidVertexIdException | IOException e) {
            System.err.println("Fehler beim Verarbeiten: " + e.getMessage());
        }
    }
}
