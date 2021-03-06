package com.durafficpark.road;

import org.bson.Document;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

// a node on the map, with the available nodes that can be traversed to from this node
public class Node {

    // the unique (hopefully!) latitude and longitude coordinates for this point
    private double latitude;
    private double longitude;

    private ArrayList<Road> adjacentRoads;   // an arraylist of the roads which can be traversed from this node

    // constructor, requiring a latitude and longitude value to be set for it
    public Node(double lat, double lon){
        this.latitude = lat;
        this.longitude = lon;
        adjacentRoads = new ArrayList<>();
    }

    public Node(Document nodeJSON){
        this.latitude = (double) nodeJSON.get("latitude");
        this.longitude = (double) nodeJSON.get("longitude");
        adjacentRoads = new ArrayList<>();
    }

    // returns an arraylist of the nodes which can be reached through adjacent roads
    public ArrayList<Node> getAdjacentNodes(){
        ArrayList<Node> durNodes = new ArrayList<>();
        for (Road road : adjacentRoads)
            durNodes.add(road.getEndNode());
        return durNodes;
    }
    /*
    // define and add a new road to the set of adjacent roads
    public void addRoad(Node endNode, double distance, double speedLimit){
        adjacentRoads.add(new Road(this, endNode, distance, speedLimit));
    }
    */

    public void addRoad(Road road){
        adjacentRoads.add(road);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Road> getAdjacentRoads(){
        return adjacentRoads;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Node
                && (latitude == ((Node) obj).latitude) && (longitude == ((Node) obj).longitude);
    }
}
