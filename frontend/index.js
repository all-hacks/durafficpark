var express = require("express");
var path = require("path");
var app = express();
var server = require("http").Server(app);
var io = require("socket.io").listen(server);
var net = require("net");
var currentDirectory = process.env.PORT ? process.cwd() : __dirname;
var simServer = new net.Socket();
app.set("port", process.env.PORT || 3000);

app.use("/jquery", express.static(path.join(currentDirectory, "node_modules/jquery/dist")));
app.use("/bootstrap", express.static(path.join(currentDirectory, "node_modules/bootstrap/dist")));
app.use("/leaflet", express.static(path.join(currentDirectory, "node_modules/leaflet/dist")));
app.use("/socketio", express.static(path.join(currentDirectory, "node_modules/socket.io-client/dist")));
app.use(express.static(path.join(currentDirectory, "static")));

app.get("*", function(req, res) {
    // console.log("404 request", req)
    res.status(404).send("File not found");
});

server.listen(app.get("port"), function() {
    console.log("Server started on port " + app.get("port"));
});


function bundleRequestToSend(client, data) {
    var newJSON = JSON.parse(data);
    newJSON['client'] = client.id;
    return JSON.stringify(newJSON);
}
// --------- Socket IO Stuff ------------
io.on("connection", function(client) {
    client.on("sim-start", function(data) {
        var payload = bundleRequestToSend(client, data);
        // console.log("Sim start");
        simServer.connect(56428, '127.0.0.1', function() {
        	// console.log('Connected');
        	simServer.write(payload + '\n');
          var buffer = "";
          simServer.on('data', function(data) {
            buffer += data;
          });
          simServer.on('close', function() {
            var returnedInformation = JSON.parse(buffer);
            var clientid = returnedInformation.client;
            var sendTo = io.to(clientid);
            // console.log(client);
            sendTo.emit("sim-client-update", returnedInformation.mapstuff);
            buffer = "";
          })
        });

    });
});
