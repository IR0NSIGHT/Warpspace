package me.iron.WarpSpace.Mod.HUD.client;

import me.iron.WarpSpace.Mod.HUD.client.glossar.GlossarCatergory;
import me.iron.WarpSpace.Mod.HUD.client.glossar.GlossarEntry;
import me.iron.WarpSpace.Mod.HUD.client.glossar.GlossarPage;
import me.iron.WarpSpace.Mod.HUD.client.glossar.GlossarPageList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 06.12.2021
 * TIME: 18:44
 */
public class GlossarInit {
    public static void addEntries() {
        GlossarCatergory cat = new GlossarCatergory("WarpSpace");
        GlossarPageList.addGlossarCat(cat);

        cat.addEntry(new GlossarEntry("Introduction","WarpSpace changes the jumping mechanic. Instead of being teleported to your waypoint or in the direction of the waypoint, instead you enter a parallel dimension that is a scaled down version of realspace: the warp. Here you can travel, just as in realspace, but distances are ten times shorter. \nThis means that you can follow others/be followed when you are travelling faster than light. The core feature is, that fast-travel becomes predictable.\n This renders raid-attacks, that rely on jumping away to hide, useless and greatly improves the ability to defend your territory.\nSince the warp is shared by everyone, its not unlikely to meet other players in it.\n "));
        cat.addEntry(new GlossarEntry("Jumping","Set your desired destination as your navigation waypoint ('N'). Then activate your jumpdrive. After a couple seconds you will switch dimensions and enter the Warp. " +
                "The Warp is a parallel dimension where distances are 10 times shorter. Follow your waypoint while you are in warp, until you reach it. You will see a notification 'droppoint' reached. Activate your jumpdrive again or slow down below 50 m/s for more than 10 seconds, to drop out of warp. You will re-enter realspace at the corresponding droppoint. This is your waypoint rounded to 10. After dropping, fly the remaining distance to your waypoint in realspace."));

        cat.addEntry(new GlossarEntry("Warpbeacon","The natural droppoints can be shifted to a more desirable position, by using a spacestation that deploys a beacon chamber. The chamber does not use energy, but 50% of the reactors chamber capacity. Once the beacon addon is activated in the reactor menu, every ship will drop out at the stations sector instead of the natural droppoint. The chamber stays active across loading/unloading the sector and server restarts.\n Beacons can only be deployed on non-homebase stations with undamaged chambers. It is recommended to reboot the station with 'y' before activating the chamber."));

        cat.addEntry(new GlossarEntry("Map","While in warp, a scaled down version of the universe is visible on the map. \nIn realspace, the droppoints are marked with small, blue spirals. Shifted droppoints through the use of warpbeacons, are marked by the same symbol inside a box."));

        cat.addEntry(new GlossarEntry("Inhibition","Inhibition has been reworked and enhanced. Your HUD will tell you, if an inhibitor is impacting you. It will show red symbols for inhibitors denying you leaving your current dimension or entering the other one. An active inhibitor will prevent ships from entering your sector and leaving your sector by using the jumpdrive. Note that slowing down and 'speeddropping' in warp pierces the inhibitor. Ships/Stations that field an inhibitor can deny warping to any ships, that have up to 3 times bigger reactors in a 3 sector radius. Inhibitors only work while being loaded."));

        cat = new GlossarCatergory("mGine missions");
        cat.addEntry(new GlossarEntry("Introduction","TBD"));
        cat.addEntry(new GlossarEntry("Finding/Accepting missions","fly close to a ! on the map, if its got a circle, press left-control + m to open to GUI"));
        cat.addEntry(new GlossarEntry("Transport mission","TBD"));
        cat.addEntry(new GlossarEntry("Scout mission","TBD"));
        cat.addEntry(new GlossarEntry("Patrol mission","TBD"));
        cat.addEntry(new GlossarEntry("Kill mission","TBD"));
        GlossarPageList.addGlossarCat(cat);


    }
}
