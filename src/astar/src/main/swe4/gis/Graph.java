package swe4.gis;

import swe4.gis.test.CostCalculator;
import swe4.gis.test.CostEvaluator;
import swe4.gis.test.DistanceEvaluator;
import swe4.gis.test.TimeEvaluator;

import java.io.IOException;
import java.util.*;

public class Graph {
  private Map<Long, Vertex> vertices = new HashMap<>();
  private Map<Long, List<Edge>> adjacencyList = new HashMap<>();

  public long addVertex(long id, double longitude, double latitude) {
    Vertex newVertex = new Vertex(id, new SphericPoint(longitude, latitude));
    vertices.put(id, newVertex);
    adjacencyList.put(id, new ArrayList<>());
    return id;
  }

  // Entfernt einen Vertex und alle damit verbundenen Kanten
  public void removeVertex(long vertexId) {
    if (!vertices.containsKey(vertexId)) {
      throw new IllegalArgumentException("Der Vertex mit der ID " + vertexId + " existiert nicht.");
    }

    // Entferne alle Kanten, die von diesem Vertex ausgehen
    adjacencyList.remove(vertexId);

    // Entferne alle Kanten, die zu diesem Vertex führen
    adjacencyList.forEach((id, edges) -> edges.removeIf(edge -> edge.getEnd().getId() == vertexId));

    // Entferne den Vertex selbst
    vertices.remove(vertexId);
  }

  public Vertex getVertex(long vertexId) {
    return vertices.get(vertexId);
  }

  public void printAllVertices() {
    System.out.println("Alle Knoten im Graphen:");
    for (Vertex vertex : vertices.values()) {
      SphericPoint coords = vertex.getCoordinates();
      System.out.println("Vertex ID: " + vertex.getId() + ", Koordinaten: (" +
              String.format("%.5f", coords.getLatitude()) + ", " +
              String.format("%.5f", coords.getLongitude()) + ")");
    }
  }

  // Überladene Methode ohne Kategorie-Parameter
  public void addEdge(String name, long startVertexId, long endVertexId, double length) throws InvalidVertexIdException {
    addEdge(name, startVertexId, endVertexId, length, (short) 4); // Standardkategorie ist 4
  }

  public void addEdge(String name, long startVertexId, long endVertexId, double length, short category) throws InvalidVertexIdException {
    if (!vertices.containsKey(startVertexId) || !vertices.containsKey(endVertexId)) {
      throw new InvalidVertexIdException("One or both vertex IDs are invalid.");
    }
    Vertex startVertex = vertices.get(startVertexId);
    Vertex endVertex = vertices.get(endVertexId);
    Edge newEdge = new Edge(startVertex, endVertex, name, length, category);
    adjacencyList.get(startVertexId).add(newEdge);
  }

  public void removeEdge(long startVertexId, long endVertexId) {
    List<Edge> edges = adjacencyList.get(startVertexId);
    if (edges != null) {
      edges.removeIf(edge -> edge.getEnd().getId() == endVertexId);
    }
  }

  public void printAllEdges() {
    System.out.println("Alle Kanten im Graphen:");
    for (List<Edge> edgeList : adjacencyList.values()) {
      for (Edge edge : edgeList) {
        System.out.println("Kante von Vertex " + edge.getStart().getId() +
                " zu Vertex " + edge.getEnd().getId() +
                ", Länge: " + edge.getLength() +
                ", Kategorie: " + edge.getCategory());
      }
    }
  }

  public Collection<Vertex> getVertices() {
    return vertices.values();
  }

  public Collection<Edge> getEdges() {
    List<Edge> edges = new ArrayList<>();
    for (List<Edge> edgeList : adjacencyList.values()) {
      edges.addAll(edgeList);
    }
    return edges;
  }

  private Collection<Edge> reconstructPath(Map<Long, Long> cameFrom, long currentId) {
    List<Edge> path = new ArrayList<>();
    Long curId = currentId;
    while (cameFrom.containsKey(curId)) {
      Long parentId = cameFrom.get(curId);
      Edge edge = findEdgeBetween(parentId, curId);
      if (edge != null) {
        path.add(0, edge);
      }
      curId = parentId;
    }
    return path;
  }

  public Collection<Edge> findPath(long startId, long targetId, CostEvaluator evaluator) {
    Map<Long, Double> gScore = new HashMap<>();
    Map<Long, Double> fScore = new HashMap<>();
    Map<Long, Long> cameFrom = new HashMap<>();
    PriorityQueue<Vertex> openSet = new PriorityQueue<>(Comparator.comparingDouble(v -> fScore.get(v.getId())));

    for (Vertex v : vertices.values()) {
      gScore.put(v.getId(), Double.POSITIVE_INFINITY);
      fScore.put(v.getId(), Double.POSITIVE_INFINITY);
    }
    gScore.put(startId, 0.0);
    fScore.put(startId, evaluator.estimatedCost(vertices.get(startId), vertices.get(targetId)));
    openSet.add(vertices.get(startId));

    while (!openSet.isEmpty()) {
      Vertex current = openSet.poll();
      if (current.getId() == targetId) {
        return reconstructPath(cameFrom, targetId);
      }

      for (Edge edge : adjacencyList.get(current.getId())) {
        Vertex neighbor = edge.getEnd();
        double tentativeGScore = gScore.get(current.getId()) + evaluator.cost(current, edge);
        if (tentativeGScore < gScore.get(neighbor.getId())) {
          cameFrom.put(neighbor.getId(), current.getId());
          gScore.put(neighbor.getId(), tentativeGScore);
          fScore.put(neighbor.getId(), tentativeGScore + evaluator.estimatedCost(neighbor, vertices.get(targetId)));
          if (!openSet.contains(neighbor)) {
            openSet.add(neighbor);
          }
        }
      }
    }
    return new ArrayList<>();
  }

  public Collection<Edge> findShortestPath(long startId, long endId) {
    return findPath(startId, endId, new DistanceEvaluator());
  }

  public Collection<Edge> findMinimalPath(long startId, long endId, CostCalculator calc) {
    return findPath(startId, endId, new TimeEvaluator(calc));
  }

  private Edge findEdgeBetween(long parentId, long childId) {
    return adjacencyList.get(parentId).stream()
            .filter(edge -> edge.getEnd().getId() == childId)
            .findFirst()
            .orElse(null);
  }

  public double pathCosts(Collection<Edge> path, CostCalculator calc) {
    return path.stream().mapToDouble(calc::costs).sum();
  }

  public void clear() {
    vertices.clear();
    adjacencyList.clear();
  }

}
