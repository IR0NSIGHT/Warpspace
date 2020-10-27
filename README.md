# Warpspace
# an alternative way to faster-than-light travel in starmade.
# this mod is a proof of concept and not balanced or stable.
This mod explores the idea to have a space in which you travel to get from point a to point b, but faster than normal flight.
The mod creates a warpspace, similar to the minecraft nether in concept. Every meter travelled in the warp translates into 10 meters travelled in realspace. 
So instead of teleporting from a to b (vanilla FTL), you change into the warp, fly the distance which is 10 times shorter, and drop back out of warp.
Some core effects are:
- no more instant travel, longer distances take longer time.
- a shared space where you can meet other travellers, be attacked or attack others (actual the likelyhood is increased by factor 1000 since 1 warpsector represents 10x10x10 realspace sectors.)
- you can now follow warping players, as the warp behaves similar to realspace in its flight dynamics.
- warp entry points are created (this is a sideeffect of the downscaling by factor 10). since all ships entering warp in a 10x10x10 sector cube end up in the same warpsector,
  all ships exiting the warp end up in the same realspace sector. This creates warpnodes, or travel routes where each starsystem has 4 nodes. Any ship entering the starsystem through warp will end up at one of them.
  These nodes could be defended or used for trade, taxing, piracy etc. 
- Planned features are:
   + get interdiction to work (is ignored atm)
   + make damaged ships automatically fall out of warp
   + create means to force-pull warping ships out, like the Star-Wars interdictors.
   + give warp custom visual effects to highlight the difference to realspace
   + make the thrust strength (= possible travel speed) in warp depend on the FTL drives level
   + explore means of making warp more interesting and different to realspace
        + core principles here are that warp should not be just a smaller realspace 2.0 but behave differently
        + one such difference could be that station building is impossible
        + shields not working
        + a limited time in warp, where a counter autodrops the ship back out

#How to use inGame
- the mod will notice any FTL jump a ship performs. instead of arriving at your location, you will enter the warp instead.
- Your navmarker will be changed to its warp position as well. Just follow the marker to get to the correct position in warp.
- to drop out of warp you can either use your FTL drive again, or slow down to below 50m/s.
- FTL usage will drop you out of warp instantly, slowing down will give you a 10 second countdown and show you a warning.
- if you spawn a spacestation in warp, it will drop out to a random sector! thats wanted behaviour to prohibit warpcamping.
- astronauts will not drop out of warp automatically.
- if you want to avoid the warp completely, you can create Warpgates. they keep their vanilla behaviour and offer a way to travel instantly, precisely and safely.

Gain insight to how the mod handles ingame with this showcase video (very early version):
https://www.youtube.com/watch?v=0t-y4ZppfLg

Find the documentation here:
https://ir0nsight.github.io/Warpspace/

You can try the mod out yourself, you will need starloader to run it. If you dont know how starloader is used, head over to its discord where we will help you set it up.
https://discord.gg/hcpSphM

# Install guide
If you already have a working starloader:
Download the latest WarpSpace.jar file from the builds repo (right hand table here) and place it into your /mods folder in starmade.
Run the game, make sure the mod is active in the main menu -> "tools and mods". If its highlighted in green, its active.
Start any gameworld and use a ships warpdrive.

If you have the suspicion that something is not working or the mod is not running at all, contact me or the starloader discord.
