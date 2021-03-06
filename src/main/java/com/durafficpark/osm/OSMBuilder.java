package com.durafficpark.osm;

import com.mongodb.util.JSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// creates an OSMObject using a json file / element
public class OSMBuilder {

    // builds a B I G arraylist of OSM objects from a B I G arraylist of json objects
    public static ArrayList<OSMObject> buildAll(ArrayList<JSONObject> jsonObjects){

        // initialise an empty array list, to hold the osm objects
        ArrayList<OSMObject> osmObjects = new ArrayList<>();

        // iterate over the json objects, building each of them to their equivalent OSM object, and storing them
        for (JSONObject jsonObject : jsonObjects)
            osmObjects.add(build(jsonObject));

        return osmObjects;
    }

    // builds a single OSM object from a given json object
    public static OSMObject build(JSONObject jsonObject){
        // System.out.println(" > Building osm object from; "+jsonObject);

        if(jsonObject.containsKey("nodes"))
            return buildWay(jsonObject);
        return buildNode(jsonObject);
    }

    // returns the equivalent osm way for the data in a json object
    private static OSMWay buildWay(JSONObject jsonObject){

        // System.out.println(" > Building way from; "+jsonObject);

        // instantiate the way, using the id found in the json object
        OSMWay way = new OSMWay((String) jsonObject.get("id"));

        // get the array of ordered node id's which form the osm way
        JSONArray nodes = (JSONArray) jsonObject.get("nodes");

        // iterate over each node in the array, casting it as a string (because it is one) and then adding it to the way
        for(Object node : nodes)
            way.getNodes().add((String) node);

        return way;
    }

    // returns the equivalent osm node object for the data in a json object
    private static OSMNode buildNode(JSONObject jsonObject){

        // System.out.println(" > Building node from; "+jsonObject);

        // instantiate the node, using the id, latitude, and longitude found in the json object
        OSMNode node = new OSMNode((String) jsonObject.get("id"),
                (Double) jsonObject.get("lat"), (Double) jsonObject.get("lon"));

        // get the json object for the node's tags
        JSONObject tags = (JSONObject) jsonObject.get("tags");

        // iterate over the keys found in the tags object, adding their (key, val) pair to the node's tags field
        for(Object tagKey : tags.keySet().toArray())
            node.getTags().put((String) tagKey, tags.get(tagKey));

        return node;
    }

    public static ArrayList<JSONObject> parseFileDirty(){

        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        try {
            File f = new File("/Users/georgeprice/Documents/GitHub/durafficpark/new_json.json");
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(line);
                jsonObjects.add(json);
            }
        }
        catch (IOException e){
            System.err.println(e.getMessage());
        }
        catch (ParseException e){
            System.err.println(e.getMessage());
        }
        return jsonObjects;
    }
}
