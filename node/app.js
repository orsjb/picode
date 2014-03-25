var app = require('http').createServer(handler);

app.listen(3001);


var io = require('socket.io').listen(app);
var fs = require('fs');
var path = require('path');
var osc = require('node-osc');

/* io.listen(3000); */

//http://stackoverflow.com/questions/11438589/socket-io-and-differents-folders-solution-found
function handler (req, res)
{
    var filePath = req.url;
    
    if (filePath == '/')
        filePath = '/main.html';
   // else
   //     filePath = '.' + filePath;
    filePath = "../html" + filePath;

    var extname = path.extname(filePath);
    var contentTypesByExtention = {
        '.html': 'text/html',
        '.js':   'text/javascript',
        '.css':  'text/css',
        '.wav': 'audio/wav'
    };
    var contentType = contentTypesByExtention[extname] || 'text/plain';
    
    fs.exists(filePath,
        function(exists)
        {
            if (exists)
            {
                fs.readFile(filePath,
                    function(error, content)
                    {
                        if (error)
                        {
                            res.writeHead(500);
                            res.end();
                        }
                        else
                        {
                            res.writeHead(200, { 'Content-Type': contentType });
                            res.end(content, 'utf-8');
                        }
                    });
            }
            else
            {
                res.writeHead(404);
                res.end();
            }
        });
    
}

//Begin constants
var namesConfPath = "../conf/names.conf";
//The time between receiving an alive message from a client and marking
//that client as having a bad connection to the server.
var clientGoodT = 3300;
//The time between receiving an alive message and removing that client
//from the list of connected clients, expressed as a multiple of
//integrityTimeout
var clientRemoveT = 5;

var sendPort = 2225;
var receivePort = 2224;
//End constants

var clients = {};
var highestId = 0;
var availIds = [];
var macMap = {};

var oscServer = new osc.Server(receivePort, '0.0.0.0');
var oscClient = new osc.Client("0.0.0.0", sendPort);

function writeClientsFile()
{
    var clientsFile = '';
    for (var mac in clients)
        clientsFile += clients[mac].id.toString()  + ' ' + clients[mac].name + ' ' + clients[mac].addr + '\n';
    fs.writeFile('../temp/clients.temp', clientsFile, null);
}

var clientTimeout =
    function(mac)
    {
        var timeouts = ++clients[mac].numTimeouts;
        if (timeouts == clientRemoveT)
        {
            clearInterval(clients[mac].intervalId);
            io.sockets.emit('remove', clients[mac].id);
            availIds.push(clients[mac].id);
            delete clients[mac];
            writeClientsFile();
        	io.sockets.emit('update', clients);
        }
        else if (timeouts == 2) {
//            io.sockets.emit('bad', clients[mac].id);
        	clients[mac].status = 'bad';
        	io.sockets.emit('update', clients);
        }
    };

var onOSCMessage =
    function (msg, rinfo)
    {
        switch (msg[0])
        {
            case '/PI/alive/announce':
//            	process.stdout.write("announce message \n");
            	var mac = msg[1];
                //http://stackoverflow.com/questions/776950/javascript-undefined-undefined
                if (msg.length >= 3)
                {
                	var theId = msg[2];
                	if(typeof clients[mac] == 'undefined') {
	                	var name = macMap[mac];
	                	if(typeof name == 'undefined') {
	                		name = 'unknown';
	                	}
	                	clients[mac] =
	                    {
	                        'name': name,
	                        'id': getId(),				//was using "theId", but getting weird behaviour.
	                        'addr': rinfo.address,
	                        'numTimeouts': 0,
	                        'intervalId': setInterval(clientTimeout, clientGoodT, mac),
	                        'status' : 'good'
	                    };
	                	
	                	writeClientsFile();
                        io.sockets.emit('update', clients);
                	}
                    //Respond so that the client knows if it has a good connection to the server
                    oscClient.host = rinfo.address;
                    oscClient.send('/PI/alive/acknowledge', mac);

                    if (clients[mac].numTimeouts != 0)
                    {
                    	clients[mac].status = 'good';
                        clients[mac].numTimeouts = 0;
                    	io.sockets.emit('update', clients);
                    }
                    //clearInterval(clients[mac].intervalId);
                    //clients[mac].intervalId = setInterval(clientTimeout, clientGoodT, clients[mac].name);
                }
                break;
            case '/PI/hshake/announce':
                var mac = msg[1];
                if (msg.length >= 2)
                {
                    var name = macMap[mac];
                    if(typeof name == 'undefined') {
                		name = 'unknown';
                	}
                    if (typeof clients[mac] == 'undefined')
                    {
                        clients[mac] =
                        {
                            'name': name,
                            'id': getId(),
                            'addr': rinfo.address,
                            'numTimeouts': 0,
                            'intervalId': setInterval(clientTimeout, clientGoodT, mac),
                            'status' : 'good'
                        };
                        writeClientsFile();
                        io.sockets.emit('update', clients);
                    }
                    //Even if the client has already been added, respond with name anyway.
                    oscClient.host = rinfo.address;
                    oscClient.send('/PI/hshake/respond', mac, name, clients[mac].id);
                }
                break;
        }
    };
    
    function getId() {
    	if (availIds.length == 0)
            thisId = highestId++;
        else
        {
            //http://www.javascriptkit.com/javatutors/arraysort.shtml
            if (availIds.length > 1)
            {
                availIds.sort(
                    function(a, b)
                    {
                        return a - b;
                    });
            }
            thisId = availIds.shift();
        }
    	return thisId;
    }

//http://stackoverflow.com/questions/6831918/node-js-read-a-text-file-into-an-array-each-line-an-item-in-the-array
fs.readFile(namesConfPath,
    function(error, data)
    {
        var sliced = data.toString().split(/\s/);
        for (var i = 0; i < sliced.length; i += 2)
            macMap[sliced[i]] = sliced[i+1];

        oscServer.on('message', onOSCMessage);
    }
);