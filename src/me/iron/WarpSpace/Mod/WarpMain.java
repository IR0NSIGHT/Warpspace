package me.iron.WarpSpace.Mod;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import me.iron.WarpSpace.Mod.HUD.client.*;
import me.iron.WarpSpace.Mod.Interdiction.InterdictionHUDUpdateLoop;
import me.iron.WarpSpace.Mod.network.PacketHUDUpdate;
import me.iron.WarpSpace.Mod.server.NavHelper;
import me.iron.WarpSpace.Mod.server.WarpCheckLoop;
import me.iron.WarpSpace.Mod.server.WarpJumpEventHandler;
import me.iron.WarpSpace.Mod.server.WarpJumpListener;
import me.iron.WarpSpace.Mod.network.PacketSCUpdateWarp;
import me.iron.WarpSpace.Mod.taswin.WarpSpaceMap;
import me.iron.WarpSpace.Mod.visuals.BackgroundEventListener;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.10.2020
 * TIME: 19:06
 */

/**
 * the me.iron.WarpSpace.Mod.testing.main class where the mod is run from by starloader.
 */
public class WarpMain extends StarMod {

    public static void main(String[] args) {
        System.out.println("hello space!");
    }
    public static StarMod instance;

    @Override
    public void onEnable() {
        super.onEnable();
        BackgroundEventListener.AddListener(); //add background color listener
        instance = this;
        PacketUtil.registerPacket(PacketSCUpdateWarp.class);
        PacketUtil.registerPacket(PacketHUDUpdate.class);
        
        WarpSpaceMap.enable(instance);
    }
    
    @Override
    public void onDisable() {
        WarpSpaceMap.disable();
    }
    
    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        WarpJumpListener.createListener();
    //TODO thrust    ThrustEventhandler.createListener();
        WarpCheckLoop.loop(25); //TODO use a frequency from a config
        WarpJumpEventHandler.createServerListener();
        InterdictionHUDUpdateLoop.CreateServerLoop();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
    //    DebugChatEvent.addDebugChatListener();
        WarpProcessController.initMap(); //build situation map for warp processes
        SpriteList.init();
        HUD_core.initList();
        GUIeventhandler.addHUDDrawListener();
        HUD_core.HUDLoop();
        NavHelper.waypointHandleLoop();
    }

    @Override
    public byte[] onClassTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] byteCode) {
        if(className.endsWith("/HudIndicatorOverlay")){
            byte[] bytes = null;
            try {
                ZipInputStream file = new ZipInputStream(new FileInputStream(this.getSkeleton().getJarFile()));
                while (true){
                    ZipEntry nextEntry = file.getNextEntry();
                    if(nextEntry == null) break;
                    if(nextEntry.getName().endsWith("HudIndicatorOverlay.class")){
                        bytes = IOUtils.toByteArray(file);
                    }
                }
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(bytes != null){
                System.err.println("[ExtraEffects] Overwrote HUDIndicatorOverlay class.");
                return bytes;
            }
        }
        return super.onClassTransform(loader, className, classBeingRedefined, protectionDomain, byteCode);
    }


}
