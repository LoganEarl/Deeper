# Simulacrum

This project is designed to be a multi-user dungeon(MUD) modeled loosley after the genre that was popular in the 1980-1990s.
It is essentially an entirly text-based MMO. All major operations are done server-side, making client implementations simple. 
The server is really the meat and potatoes of the project.<br><br>

Story wise, the concept is pretty simple. There is a multiverse, and many of them are in trouble. Players are part of a group of
individuals who go from world to world trying to stop them from being destroyed. When a player enters a world, they do so through a portal. 
Portals have a limited ability to transfer information, so either they fit one player with some gear, or a couple players without anything.
Players whith higher stats and levels take up more portal bandwidth. Once a world is accessed, it cannot be traveled to again until either
the world has been saved or the time has elapsed, destroying the world and everything in it, including the players. Each world is an
escape room if you will. Players can gain levels and experience by solving the world, but risk themselves in the process. The benefits of
this model are that there are never any immersion breaking monster respawns in a world. Everything that will ever be in that world was
there when it was instantiated, and that is all that will ever be in it. If a dungeon was cleared, its empty forever from then on. Each
world already has an expiration date so there should be no reason for the party of players to try and beat it more than once. <br><br>

Lore wise, long ago the universe was torn asunder by a currenly undecided doomsday device of some sort. Maybye vacume decay? anyways, the universe is now a series of small, twisted up pocket universes called shards seperated by allmost endless expanses of limbo. One of the pocket universes contained an expiramental FTL drive that now serves to create small portals to other shard worlds. Early on, the archetects of the drive created a subspace mapper called the Compass. It can detect the motion of the the shards and has a limited sight into the future. It detects when a shard is close to becoming destablized and the subsequent destruction. Players learn to utilize the Compass to detect these worlds and generate portals to them. Failed attempts just create portals to Limbo.

Technically speaking, the server just keeps track of database files and interprets commands sent by clients as a way of interfacing with 
the files. Each world is its own self-contained database file so they are relativly easy to make with a simple database file editor. No code
needed. Every location, item, monster, quest, story arc, and dialog option are all stored in a single file. This allows the server to copy 
world files and instantiate more than one world instance at a time. 
