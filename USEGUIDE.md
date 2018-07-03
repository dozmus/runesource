# USEGUIDE

## What is the 'mopar' account's password?
`test`

## Modifying server variables
You can change the:
* Server name
* Starting position
* Maximum number of connections per host
* Whether passwords are hashed

by editting the server config file in `/data/settings.json`.

Note: If you change the hash passwords variable any existing accounts will be
rendered inaccessible, unless if you change it back.

## How to save a new variable in a player file?
Add a new variable into the PlayerAttributes class (under com.rs.entity.player).
The next time a player logs in and out it their save file will be updated with no problems!

## How to add a new command?
Add any commands into the Groovy script in the CommandHandler class (under /plugins/bindings/packets/).

## How to add a new plugin?
Create a Groovy class file extending `Plugin` under `/plugins/scripts/`.
Type its relative file name (without extension) in the `data/plugins.ini` file.  
The next time you start the server it should be active.
You can also have your script inherit further behaviours by registering a binding in its `onEnable` method (see the other plugins for examples).