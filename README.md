# Warpspace
# an alternative way to faster-than-light travel in starmade.

This mod explores the idea to have a space in which you travel to get from point a to point b, but faster than normal flight.
The mod creates a warpspace, similar to the minecraft nether in concept. Every meter travelled in the warp translates into 10 meters travelled in realspace. 
So instead of teleporting from a to b (vanilla FTL), you change into the warp, fly the distance which is 10 times shorter, and drop back out of warp.
Some core effects are:
- no more instant travel, longer distances take longer time.
- a shared space where you can meet other travellers, be attacked or attack others (actual the likelyhood is increased by factor 1000 since 1 warpsector represents 10x10x10 realspace sectors.)
- you can now follow warping players, as the warp behaves similar to realspace in its flight dynamics.
- warp entry points are created (this is a sideeffect of the downscaling by factor 10). since all ships entering warp in a 10x10x10 sector cube end up in the same warpsector,
  all ships exiting the warp end up in the same realspace sector. This creates warpnodes, or travel routes where each starsystem has 4 nodes. Any ship entering the starsystem  through warp will end up at one of them.
  These nodes could be defended or used for trade, taxing, piracy etc. 


# Ingame behaviour/How to use
- the mod will notice any FTL jump a ship performs. instead of arriving at your location, you will enter the warp.
- Your navmarker will be changed to its warp position as well. Just follow the marker to get to the correct position in warp.
- to drop out of warp you can either use your FTL drive again, or slow down to below 50m/s.
- FTL usage will drop you out of warp instantly, slowing down will give you a 10 second countdown and show you a warning.
- if you spawn a spacestation in warp, it will drop out to a random sector! thats wanted behaviour to prohibit warpcamping.
- astronauts will not drop out of warp automatically.
- if you want to avoid the warp completely, you can create Warpgates. they keep their vanilla behaviour and offer a way to travel instantly, precisely and safely.

# Planned features are:
   + ~~t interdiction to work (is ignored atm)~~ done, even better than vanilla
   + ~~build custom HUD~~ done
   + make damaged ships automatically fall out of warp
   + create means to force-pull warping ships out, like the Star-Wars interdictors.
   + ~~give warp custom visual effects to highlight the difference to realspace~~ semi done with recolored backgrounds
   + ~~make the thrust strength (= possible travel speed) in warp depend on the FTL drives level~~ waiting for SM update for hook
   + explore means of making warp more interesting and different to realspace
        + core principles here are that warp should not be just a smaller realspace 2.0 but behave differently
        + ~~make station building impossible~~ (done, autodrop)
        + shields not working
        + ~~a limited time in warp, where a counter autodrops the ship back out (using speed limit, done)~~
   + Make AI/fleets be able to use warp

Gain insight to how the mod handles ingame with this showcase video (very early version):
https://www.youtube.com/watch?v=0t-y4ZppfLg

StarmadeDock mod page:
https://starmadedock.net/content/warpspace.8166/

StarmadeDock blog page:
https://starmadedock.net/threads/an-alternative-ftl-system-warpspace.31607/page-2#post-380687

Find the documentation here:
https://ir0nsight.github.io/Warpspace/

Starloader community discord for bugreports/feedback/help:
https://discord.gg/hcpSphM

# Install guide
- use the builtin, ingame modbrowser, find WarpSpace and click "install".
- make sure the mod is activated.

If you have the suspicion that something is not working or the mod is not running at all, contact me or the starloader discord.
Since Starloader is a community project and still in developement, its likely that it will break mods when it updates. I do my best to fix this fast, but it can take a couple days. Let me know if its broken, so i can update it.
