package Mod;

import Mod.HUD.client.*;
import Mod.server.ThrustEventhandler;
import Mod.server.WarpJumpEventHandler;
import Mod.server.WarpCheckLoop;
import Mod.server.WarpJumpListener;
import api.DebugFile;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import api.utils.StarRunnable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.10.2020
 * TIME: 19:06
 */

/**
 * the main class where the mod is run from by starloader.
 */
public class WarpMain extends StarMod {

    public static void main(String[] args) {
        System.out.println("hello space!");
    }
    public static StarMod instance;
    /**
     *  sets mod information like author, name and version.
     */
    @Override
    public void onGameStart() {
        super.onGameStart();
        this.setModVersion("0.7.1 - compiler hotfix");
        this.setModName("WarpSpace");
        this.setModAuthor("IR0NSIGHT");
        this.setModSMVersion("dev - v0.202.108");
        this.setModDescription("an alternative FTL system");
        this.setSMDResourceId(8166);
       // this.addDependency("StarAPI");

    }
    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        PacketUtil.registerPacket(PacketSCUpdateWarp.class);
        PacketUtil.registerPacket(PacketHUDUpdate.class);

    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        WarpJumpListener.createListener();
    //TODO thrust    ThrustEventhandler.createListener();
        WarpCheckLoop.loop(25); //TODO use a frequency from a config
        WarpJumpEventHandler.createServerListener();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
        //DebugChatEvent.addDebugChatListener();
        WarpProcessController.initMap(); //build situation map for warp processes
        SpriteList.init();
        HUD_core.initList();
        GUIeventhandler.addHUDDrawListener();
        HUD_core.HUDLoop();

    }
}
