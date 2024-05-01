package swe4.gis;

import java.util.ArrayList;
import java.util.Collection;

public class Vertex {
	
  private long             id;
  private SphericPoint     coordinates;

  public Vertex(long id, SphericPoint coordinates) {
    this.id = id;
    this.coordinates = coordinates;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public SphericPoint getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(SphericPoint coordinates) {
    this.coordinates = coordinates;
  }

  @Override
  public String toString() {
    return "Vertex [id=" + id + ", coordinates=" + coordinates + "]";
  }
}
