package me.iron.WarpSpace.Mod.client;


import java.util.Collection;

import javax.annotation.Nullable;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import api.DebugFile;
import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.beacon.BeaconObject;
import me.iron.WarpSpace.Mod.client.sounds.SoundQueueManager;


/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 21.12.2020
 * TIME: 14:27
 */

/**
 * runs clientside for HUD arranging on runtime
 */
public class DebugUI implements CommandInterface {

    public static Integer[] parseText(String text, String keyword, String separator) {
        if (!text.contains(keyword)) {
            DebugFile.err("text does not contain keyword.");
            return null;
        }

        String s = text;
        s = s.replace(keyword,""); //remove keyword
        s = s.replace(" ",""); //remove space
        DebugFile.err("pasreText: after removing keyword " + keyword + ", string is: " + s);
        String[] parts = s.split(separator);
        Integer[] arr = new Integer[parts.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(parts[i]);
        }
        return arr;
    }

    @Override
    public String getCommand() {
        return "WS";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"Warpspace","wsp","ws"};
    }

    @Override
    public String getDescription() {
        return "Debug command for warpspace" +
                "beacon pulls: print all pulling beacons\n" +
                "beacon manager: print all beacon objects\n" +
                "beacon clear: delete all beacon objects\n" +
                "beacon toggle: invert state of all beacons\n" +
                "sound print: print all sounds\n" +
                "warp: warp my vessel now";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState playerState, String[] strings) {
        int l = strings.length;
        if (strings.length==2 && strings[0].equalsIgnoreCase("beacon")) {
        //print active pullings
            if (strings[1].equalsIgnoreCase("pulls")) {
                StringBuilder b = new StringBuilder("All sectors with active beacons:\n");
                Collection<Vector3i> ss = WarpMain.instance.beaconManagerServer.getBeaconSectors();
                for (Vector3i s: ss) {
                    b.append("Sector ").append(WarpManager.getRealSpacePos(s)).append("-->").append(WarpJumpManager.getDropPoint(s));
                    b.append("\n");
                }
                echo(b.toString(),playerState);
                return true;
            }
        //print manager
            if (strings[1].equalsIgnoreCase("manager")) {
                echo(WarpMain.instance.beaconManagerServer.print(),playerState);
                return true;
            }

        //clear all beacons.
            if (strings[1].equalsIgnoreCase("clear")) {
                WarpMain.instance.beaconManagerServer.clearBeacons();
                echo("cleared beacons",playerState);
                return true;
            }

        //invert state for all beacons
            if (strings[1].equalsIgnoreCase("toggle")) {
                for (BeaconObject b:WarpMain.instance.beaconManagerServer.getBeacons()) {
                    b.setActive(!b.isActive());
                }
                echo("inverted all beacon states",playerState);
                return true;
            }

        }
        //sounds
        if (l>0 && strings[0].equalsIgnoreCase("sound")) {
            if (l>1 &&strings[1].equalsIgnoreCase("print")) {
                StringBuilder b = new StringBuilder("All warp-sounds:");
                int i = 0;
                for (SoundQueueManager.SoundEntry s: SoundQueueManager.SoundEntry.values())
                    b.append(i++).append(s.toString()).append("\n");
                echo(b.toString(),playerState);
                return true;
            }
            if (l>1 && strings[1].equalsIgnoreCase("play")) {
                int ordinal = -1;
                if (l>2) {
                    ordinal = Integer.parseInt(strings[2]);
                }
                boolean loop = false;
                if (l>3) {
                    loop = Boolean.parseBoolean(strings[3]);
                }
                if (ordinal == -1) {
                    for (SoundQueueManager.SoundEntry s: SoundQueueManager.SoundEntry.values())
                        SoundQueueManager.instance.queueSound(s,"debug");
                    echo("Playing all sounds in a row",playerState);
                } else {
                    ordinal = ordinal% SoundQueueManager.SoundEntry.values().length;
                    SoundQueueManager.SoundEntry s = SoundQueueManager.SoundEntry.values()[ordinal];
                    echo("playing sound with ordinal="+ordinal+" - "+s + " looping: " + loop,playerState);
                    SoundQueueManager.instance.playSound(s,1,1); //TODO allow pitch and volume control
                }

                return true;
            }
        }

        if (l>=1 && strings[0].equalsIgnoreCase("warp")) {
            SimpleTransformableSendableObject obj = playerState.getFirstControlledTransformableWOExc();
            if (l>=2 && strings[1].equalsIgnoreCase("-j"))  {
                WarpJumpManager.invokeJumpdriveUsed(obj, false);
                echo("normal jump", playerState);
            } else if (l>= 2 && strings[1].equalsIgnoreCase("-s")) {
                Sendable selected = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerState.getSelectedEntityId());
                if (selected instanceof SimpleTransformableSendableObject) {
                    WarpJumpManager.invokeJumpdriveUsed((SimpleTransformableSendableObject) selected, true);
                } else {
                    echo("Nothing or wrong type of entity selected, can not warp entity.",playerState);
                }
            }
            else {
                echo("immediate forced warp", playerState);
                if (WarpManager.isInWarp(obj)) { //is in warpspace, get realspace pos
                    WarpJumpManager.invokeDrop(0,obj,true, true);
                } else { //is in realspace, get warppos
                    WarpJumpManager.invokeEntry(0,obj,true);
                }

            }

            echo("admin-warping "+ obj.getUniqueIdentifier(),playerState);
            return true;
        }
        return false;
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {

    }

    @Override
    public StarMod getMod() {
        return null;
    }

    public static void echo(String mssg, @Nullable PlayerState p) {
        if (p == null) {
            if (GameServerState.instance!=null) {
                for (PlayerState pl: GameServerState.instance.getPlayerStatesByName().values()) {
                //    PlayerUtils.sendMessage(pl,mssg);
                }
            }
        } else {
            PlayerUtils.sendMessage(p,mssg);
        }
    }
}
