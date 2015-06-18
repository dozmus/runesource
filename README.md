RuneSource
=======================

### What is RuneSource?
Hey there, thanks for checking out RuneSource. RuneSource is an [open-source](http://www.opensource.org/) server for the popular game [RuneScape](runescape.com), written in the Java programming language. It is important to note that RuneSource is in no way endorsed or affiliated with RuneScape or its creator company, Jagex. Use of RuneSource is also against the RuneScape Terms of Service (which you may or may not be bound by). RuneSource is licensed via the GNU General Public License Version 3. Please note that this server is not ready for production - it has minimal content implemented.

### Why was RuneSource made?
RuneSource was made by [blakeman8192](https://github.com/blakeman8192) for the general RuneScape emulation community, which is spread across many internet forums and IRC channels. The purpose of RuneSource is to provide a stable, high-performance, and simple RuneScape server emulator for people to use. At the current time of release (late 2010), all existing RuneScape server emulators are either very unstable and slow, or very complicated and hard to work with. RuneSource aims to solve these problems by being a stable and efficient server, and above all, by being simple and easy to use.

### New Features
* Improved plugin system (uses Groovy)
* Task system
* More player updating
* JSON saving/loading of data (passwords are hashed using SHA-256)
* Lots of misc. stuff (emotes, running, etc.)

### Planned Features
* Finish player appearance updating (i.e. send combat level, whether or not the player is wearing a platebody, etc)
* Add all NPC/Player mask handling
* Item equip - don't default to weapon equip
* Add friends/ignore lists and private messaging
* Buffer caching

### How To
#### Change server name/starting position/maximum connections per host/whether or not to hash password
Edit the server config file in /data/settings.json.
Note: If you change the hash passwords variable any existing accounts will be rendered inaccessible, unless if you change it back.

#### Save a new variable in a player file
Add a new variable into the PlayerAttributes class (under com.rs.entity.player), the order of variables in that file
dictates the order of the corresponding JSON output. The order should not matter when being parsed though.
So if you change the format the next time a player logs in and out it should be updated with no problems!

#### Add a new command
Add any commands into the Groovy script in the CommandHandler class (under /plugins/bindings/packets/).

### Dependencies
* Groovy (v2.4.3)
* JSON-IO (v3.3.1)

### Copyright
Copyright (c) 2010  [Blake Beaupain](https://github.com/blakeman8192)  
Copyright (c) 2015  [PureCS](https://github.com/purecs) (aka [Pure_](https://www.moparscape.org/smf/index.php?action=profile;u=350406))  
RuneScape is copyright and a registered trademark of Jagex Ltd.  
RuneSource and its authors are in no way affiliated with Jagex or RuneScape.  
RuneSource exists solely for educational purposes, and is licensed via GPLv3.