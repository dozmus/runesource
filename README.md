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
* Powerful Plugins using Groovy
* Task System
* Full Player Support
* Full Npc Support
* Friends List and Private Messaging (with privacy settings)
* JSON Data Serialization (optional: passwords are hashed using SHA-256)
* Banning and muting with expiration dates
* Misc stuff
  * Validate equipped items
  * Emotes
  * Running
  * Correct weapon interfaces
  * Persistent settings
  * Buffer caching
  * Weapon stack merging (i.e. if you have 10 arrows equipped, and equip 5 more they combine into 15)
  * Login attempt throttling
* [More planned...](https://github.com/PureCS/runesource/issues)

### Configuration
You can configure your RuneSource by reading the [use guide](USEGUIDE.md).

### Dependencies
* Groovy (v2.4.3)
* JSON-IO (v3.3.1)

### Copyright
Copyright (c) 2010  [Blake Beaupain](https://github.com/blakeman8192)  
Copyright (c) 2015-2018  [PureCS](https://github.com/purecs)
RuneScape is copyright and a registered trademark of Jagex Ltd.  
RuneSource and its authors are in no way affiliated with Jagex or RuneScape.  
RuneSource exists solely for educational purposes, and is licensed via GPLv3.
