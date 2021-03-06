package com.durafficpark;

import com.durafficpark.traffic.Controller;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class SimulatorThread extends Thread {
    protected Socket socket;

    public SimulatorThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        System.out.println("Running");
        InputStream inp = null;
        BufferedReader brinp = null;
        PrintWriter out = null;
        BufferedWriter bwrite = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String input;
        while (true) {
            try {
                System.out.println("Now process inputs");
                input = brinp.readLine();
//                System.out.println(input);
                HashMap<String, String> inputs = new Gson().fromJson(
                        input,
                        new TypeToken<HashMap<String, String>>(){}.getType()
                );
                System.out.println(inputs);
                float dt = Float.parseFloat(inputs.get("dt"));
                int runtime = Integer.valueOf(inputs.get("runtime"));
                int savegap = Integer.valueOf(inputs.get("saveGap"));
                float density = Float.parseFloat(inputs.get("density"));
                float a = Float.parseFloat(inputs.get("a"));
                float b = Float.parseFloat(inputs.get("b"));
                float c = Float.parseFloat(inputs.get("c"));
                Controller cont = new Controller(a, b, c, dt, runtime, savegap, density);
                String[] things = cont.run();
                String mapRepresentation = new Gson().toJson(things);
//                System.out.println(input);
                HashMap<String, String> output = new HashMap<>();
                output.put("client", inputs.get("client"));
                output.put("mapstuff", mapRepresentation);
                String outputStr = new Gson().toJson(output);

                String value = new String(outputStr.getBytes("UTF-8"));
//                System.out.println("Output:" + value);
                out.write(value);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
 }
