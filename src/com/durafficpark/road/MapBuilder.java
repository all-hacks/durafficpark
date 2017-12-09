package com.durafficpark.road;

import com.durafficpark.Traffic.Map;
import com.durafficpark.osm.OSMNode;
import com.durafficpark.osm.OSMObject;
import com.durafficpark.osm.OSMWay;

import java.util.ArrayList;

// builds a full set of map data, using a defined set of osm object
public class MapBuilder {

    // produces a full set of nodes
    public static ArrayList<MapObject> buildMap(ArrayList<OSMObject> osmObjects){
        /*
            I'm gonna go ahead and explain how this works so that you can understand it because it's fair dirty
            - iterate through all of the osm objects that we made from the json data
            - sort it into two other lists; osm nodes and osm ways
            - iterate over all of the ways first!
                - for each way, we'll need the ordered nodes in it as well as some details for the road itself
                    - such as the speed limit, number of lanes...
                - so, for each pairing of adjacent nodes in the ordered layers we need to construct a Node and Road
                - iterate through each pairing in the ordered nodes...
                    - find the equivalent OSMNode objects (from the osm node list) with the matching id
                    - construct the equivalent Nodes for these two OSMNodes
                    - now, we construct a path between them!
                    - create a road object that has them as start and end Nodes (both ways if it's a two-way road!)
                        - also, chuck in the extra data by checking the OSMWays' tags (maxspeed, lane size etc.)
        */

        // initialise two empty lists to hold nodes and roads
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<Road> roads = new ArrayList<>();

        // initialise two empty lists to hold the OSM nodes and OSM ways
        ArrayList<OSMNode> osmNodes = new ArrayList<>();
        ArrayList<OSMWay> osmWays = new ArrayList<>();

        // iterate through every OSM objects, sorting them into OSM Nodes and OSM Ways
        for(OSMObject osmObject : osmObjects){
            if(osmObject instanceof OSMNode)
                osmNodes.add((OSMNode) osmObject);
            else if (osmObject instanceof OSMWay)
                osmWays.add((OSMWay) osmObject);
        }

        // iterate over all of the ways
        for(OSMWay way : osmWays){

            // get the ordered ways for the road
            ArrayList<String> wayNodes = way.getNodes();

            // iterate through all of the node ids in the way
            for (int i = 0; i < wayNodes.size()-1; i++) {

                // get the node id's for each node pairing
                String aNodeID = wayNodes.get(i);
                String bNodeID = wayNodes.get(i+1);

                // get the equivalent OSMNodes for these given id's
                OSMNode aOSMNode = getNode(aNodeID, osmNodes);
                OSMNode bOSMNode = getNode(bNodeID, osmNodes);

                // if both nodes could be found (i.e. are within the bounding box) then we can create a road for them
                if(aOSMNode != null && bOSMNode != null){

                    // now we can create the Nodes which are equivalent to these two OSM Nodes
                    Node aNode = new Node(aOSMNode.getLat(), aOSMNode.getLon());
                    Node bNode = new Node(bOSMNode.getLat(), bOSMNode.getLon());

                    // and save them to our arraylist of nodes
                    nodes.add(aNode);
                    nodes.add(bNode);

                    // calculate the distance between the two roads (using the haversine formula)
                    double roadDistance = calcDistance(aNode, bNode);

                    // getting the speed limit of the road

                    double speedLimit = -1;

                    // lets see if this road has a speed limit (default: no speed limit)
                    if(way.getTags().containsKey("maxspeed")){

                        // try and get a string which can be parsed into a double (remove all characters and whitespace)
                        String speedLimitStr = ((String) way.getTags().get("maxspeed"))
                                .replace(" ","").replace("mph", "");

                        // update the double speed limit value
                        speedLimit = Double.parseDouble(speedLimitStr);
                    }

                    // getting the number of lanes in the road

                    int lanes = 1;

                    // lets see if this road defines how many lanes it has (default: 1 lane)
                    if(way.getTags().containsKey("lanes")){
                        lanes = Integer.parseInt((String) way.getTags().get("lanes"));
                    }


                    // determining whether the road is one way or not (default: two way)
                    boolean oneWay = way.getTags().containsKey("oneway")
                            && ( (String) way.getTags().get("oneway")).equals("yes");

                    // now, we construct the road...

                    // add a road per lane for each of the two points
                    for (int j = 0; j < lanes; j++) {
                        roads.add(new Road(aNode, bNode, roadDistance, speedLimit));
                    }

                    // if it's two way, then we need to add the road going the other direction
                    if(! oneWay){
                        for (int j = 0; j < lanes; j++) {
                            roads.add(new Road(bNode, aNode, roadDistance, speedLimit));
                        }
                    }
                }
                else {
                    System.out.println("[!] Could not find the Node as part of this way");
                }
            }

        }

        // you may be asking why I left this step until now, and bothered with separating them into nodes and roads
        // well, we don't know whether Ollie needs just nodes, or just nodes, or just all of it!
        // so, I'm just gonna give him it all and let him deal with it! :)
        ArrayList<MapObject> mapObjects = new ArrayList<>();
        mapObjects.addAll(nodes);
        mapObjects.addAll(roads);

        return mapObjects;
    }

    // calculates the distance (IN METRES) between two nodes
    public static double calcDistance(Node nodeA, Node nodeB){

        // calculates the distance between two nodes using the Haversine formula...

        // get the latitude and longitude values for the two nodes
        double startLat = nodeA.getLatitude();
        double startLong = nodeA.getLongitude();
        double endLat = nodeB.getLatitude();
        double endLong = nodeB.getLongitude();

        // calculate the latitude and longitude differences between the two nodes
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        // do some weird af maths (I didn't write this code, but I'm still tryna comment it)

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(startLat) * Math.cos(endLat)
                * Math.pow(Math.sin(dLong / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c;
    }

    // builds a full set of nodes using the osm nodes, BUT (!) does not add the adjacent roads for these nodes!
    public static ArrayList<Node> buildNodes(ArrayList<OSMNode> osmNodes){

        // initialise an empty list of nodes
        ArrayList<Node> nodes = new ArrayList<>();

        // iterate over all over the osm nodes, converting them into their equivalent
        for(OSMNode osmNode : osmNodes)
            nodes.add(buildNode(osmNode));

        return nodes;
    }

    // builds an equivalent node from an osm node
    public static Node buildNode(OSMNode osmNode){
        return new Node(osmNode.getLat(), osmNode.getLon());
    }

    // builds a full set of roads using the osm ways, and node data
    public static ArrayList<Road> buildRoads(ArrayList<OSMWay> osmWays, ArrayList<Node> nodes){
        return null;
    }


    // gets the equivalent node
    private static OSMNode getNode(String nodeID, ArrayList<OSMNode> osmNodes){
        for (OSMNode node: osmNodes)
            if(node.getId().equals(nodeID))
                return node;
        return null;
    }

}
