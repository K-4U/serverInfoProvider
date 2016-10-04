ServerInfoProvider
=========

![](logo.png)

ServerInfoProvider is a simple server-only mod that'll give you access to server blocks.
This allows you to fetch more data about your server without logging in!


## Links ##
Curse: [http://minecraft.curseforge.com/mc-mods/serverinfoprovider](http://minecraft.curseforge.com/mc-mods/serverinfoprovider)

Required mod: [K4Lib](http://minecraft.curseforge.com/mc-mods/k4lib)

## License ##
This mod is released under the MMPLv2

## Pull requests ##
Yes, you can make pull requests for new variables, if implemented properly i'll merge them.

## Bugs ##
Please report bugs to the issues page. Put crashlogs in a pastebin or gist!

## API ##
There is no API as of yet, however i am planning on adding one, so mods can add their own data.

## Documentation ##
Make note that you do need to enable the query in the server settings for this mod to work.
All the data is sent in a JSON format.
By default, the server listens on 25566. However, this is configureable in the config.
To figure out what the port is that the server listens on (in case you're a mod author and want to use this API, yet having no access to the actual server)
you can use the standard [Minecraft UDP Query protocol](http://wiki.vg/Query). Make note that the query in the server settings needs to be enabled for this to work.
All you have to do is send a payload of integer value 8 and await a response. The port will be written as a string!
 
### Sending ###
This list is in the format of either just strings, or objects with a key and args. For example, to request the time, you would send this:

	["time"]

If you want to request the time in a certain dimension:

	[{"key": "time", "args": 1}]

Keys are not caps sensitive, however i do recommend you send them in lowercase.

### Receiving ###
If we send the top json to the server, we would get a return message:

	{
		"TIME": {
			"1": "13:37"
		}
	}

Ofcourse, if you request more variables, you'll get more in the return object.


### Variables ###
These are the variables that can be requested, along with arguments:

- time - Gives the time in the overworld if used without arguments
	- dimension id
- players - Gives the players on the server. Argument is optional
	- "latestdeath" - Received the players and their latest death
- daynight - Returns a boolean whether or not it's daytime in the overworld if used without arguments
	- dimension id
- dimensions - Returns all the available dimensions, including their id
- uptime - How long the server has been running
- deaths - If used without argument, this will return all the players who have died more than once
	- playername - Returns a list of all the deaths this played had since the mod was installed.
- weather - Gives the weather. Argument is optional
    - Dimension id

#### Position based variables ###
These variables will return a list of values requested.

- blockinfo - Gives you basic information about a block in the world. Arguments required:
    - x
    - y
    - z
    - dimension
- rf - Gives you basic RF information about a block in the world. Arguments required:
    - x
    - y
    - z
    - dimension
    - side - The side of the block to fetch RF info from. (up/down/north/south/east/west)
- fluid - Gives you basic information about a tank in a block in the world. Arguments required:
    - x
    - y
    - z
    - dimension
    - side - The side of the block to fetch RF info from. (up/down/north/south/east/west)
- inventory - Gives you the items in an inventory in the world. Arguments required:
    - x
    - y
    - z
    - dimension
    - side - The side of the block to fetch RF info from. (up/down/north/south/east/west)


Note that this list is not yet complete and i aim to add more stuff to it!


### API ###
There is a limited API right now. Implement the [`ISIPEntity`](https://github.com/K-4U/serverInfoProvider/blob/master/src/k4unl/minecraft/sip/api/ISIPEntity.java) on a TileEntity.
Whenever blockinfo is called on that Tile Entity, you can return a small map with keys and values to return to the requester, with info about your block.


### Examples ###
I used a python file for testing this. I've uploaded it to pastebin: [http://pastebin.com/g2zbGHHs](http://pastebin.com/g2zbGHHs)

Usage:

	#!/usr/bin/python
	import mcquery
	import time
	import socket
	import json
	
	print 'Ctrl-C to exit'
	
	host = ""
	udpport=''
	
	if host == '':
	    host = 'localhost'
	if udpport == '':
	    udpport = 25565
	else: 
	    udpport = int(udpport)

	# Connect to UDP, asking for the port
	print "Connecting to UDP"
	q = mcquery.MCQuery(host, udpport)
	print "Connected to UDP, asking for the port"
	port = q.get_port()
	#Do some trimming because null bytes are send with:
	port = port.replace("\00", "")
	port = int(port)

	clsock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	
	print "Connecting..."
	clsock.connect((host, port))
	print "Connected."

	toSend = ["time", {"key":"players", "args":"latestdeath"}, "uptime", "dimensions", {"key":"time", "args":1}, {"key":"time", "args":-1}, "deaths", {"key":"deaths", "args":"K4Unl"}]

	#Create a json string
	dString = json.dumps(toSend)
	clsock.sendall(dString + "\n") #The newline is VERY IMPORTANT! Without it it will NOT properly receive and the server WILL disconnect you!

	print "Sent data"
	print "Waiting on data returned"

	while 1:
	    data = clsock.recv(1024)
	    print data
	    if not data: break


