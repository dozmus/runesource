RuneSource
=======================

### What is RuneSource?
RuneSource is an [open-source](http://www.opensource.org/) server for the popular game [RuneScape](https://runescape.com),
written in the Java programming language.
It is important to note that RuneSource is in no way endorsed or affiliated with RuneScape or its creator company,
Jagex. Use of RuneSource is also against the RuneScape Terms of Service (which you may be bound by).
RuneSource is licensed via the GNU General Public License Version 3.
Please note that this server is not ready for production - it has minimal content implemented.

### Why was RuneSource made?
RuneSource was made by [blakeman8192](https://github.com/blakeman8192) for the general RuneScape emulation community,
which is spread across many internet forums and IRC channels.
The purpose of RuneSource is to provide a stable, high-performance, and simple RuneScape server emulator for people to
use.
At the current time of release (late 2010), all existing RuneScape server emulators are either very unstable and slow,
or very complicated and hard to work with.
RuneSource aims to solve these problems by being a stable and efficient server, and above all, by being simple and
easy to use.

### New Features
* Improved Plugin System (uses Groovy)
* Task System
* Full Player Support
* Full Npc Support
* Friends List and Private Messaging
* JSON Data Serialization (optional: passwords are hashed using SHA-256)
* Lots of misc. stuff (emotes, running, etc.)
* [More planned...](https://github.com/PureCS/runesource/issues)

### F.A.Q.
#### What is the 'mopar' account's password?
`test`

#### How to change server name?
#### How to change starting position?
#### How to change maximum connections per host?
#### How to change whether passwords are hashed?
Edit the server config file in /data/settings.json.
Note: If you change the hash passwords variable any existing accounts will be rendered inaccessible, unless if you change it back.

#### How to save a new variable in a player file?
Add a new variable into the PlayerAttributes class (under com.rs.entity.player).
The next time a player logs in and out it their save file will be updated with no problems!

#### How to add a new command?
Add any commands into the Groovy script in the CommandHandler class (under /plugins/bindings/packets/).

#### How to add a new plugin?
Create a Groovy class file extending `Plugin` under `/plugins/scripts/`.
Type its relative file name (without extension) in the `data/plugins.ini` file.  
The next time you start the server it should be active.
You can also have your script inherit further behaviours by registering a binding in its `onEnable` method (see the other plugins for examples).

### Dependencies
* Groovy (v2.4.3)
* JSON-IO (v3.3.1)

### Copyright
Copyright (c) 2010  [Blake Beaupain](https://github.com/blakeman8192)  
Copyright (c) 2015, 2017  [PureCS](https://github.com/purecs) (aka [Pure_](https://www.moparscape.org/smf/index.php?action=profile;u=350406))  
RuneScape is copyright and a registered trademark of Jagex Ltd.  
RuneSource and its authors are in no way affiliated with Jagex or RuneScape.  
RuneSource exists solely for educational purposes, and is licensed via GPLv3.
