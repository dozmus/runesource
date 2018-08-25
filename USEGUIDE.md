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
Create a Groovy class file (i.e. something `.groovy`) extending an `EventListener` (or multiple) under `/plugins/`.
You can find a full list of listeners under `com.rs.plugin.listener`.
Each listener will give your script some information regarding the server.

## How to create a new listener?
Firstly, create a new subclass of `EventListener` in `com.rs.plugin.listener`.

Next, add this interface to the `Bootstrap` interface.
Make sure you implement and handle this method, analogously to the others in `GroovyBootstrap`.

Finally, create a corresponding static `dispatch*` in `PluginHandler`.
Make sure that this dispatch method is called whenever this event occurs.
