package me.iron.WarpSpace.Mod.client.map;

import api.listener.fastevents.FastListenerCommon;
import api.mod.StarMod;
import libpackage.drawer.MapDrawer;
import libpackage.drawer.SpriteLoader;
import libpackage.markers.SimpleMapMarker;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.beacon.BeaconObject;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.schine.graphicsengine.forms.Sprite;
import javax.vecmath.Vector4f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 25.10.2021
 * TIME: 16:20
 */
public class DropPointMapDrawer extends MapDrawer {
    private Sprite mapSprite;
    private Vector3i lastSector = new Vector3i();
    private Vector4f markerColor = new Vector4f(0,1,1,0.8f);
    private boolean updateFlag;
    public DropPointMapDrawer(StarMod mod) {
        super(mod);
        FastListenerCommon.gameMapListeners.remove(this);
    }

    public void activate() {
        FastListenerCommon.gameMapListeners.add(this);
    }

    public void flagForUpdate() {
        synchronized (this) {
            updateFlag = true;
        }
    }
    public void loadSprite() {
        synchronized (DropPointMapDrawer.class) {
            SpriteLoader sl = new SpriteLoader("me/iron/WarpSpace/Mod/res/","mapsprite.png",30,30,2,2);
            sl.loadSprite(WarpMain.instance);
            mapSprite = sl.getSprite();
        }
    }

    /**
     * create new markers around selected sector, marking where the dropsectors are.
     * @param currentPos
     */
    private void updateDropMarkers(Vector3i currentPos) {
        if (mapSprite == null)
            return;
        synchronized (this) {
            //only update if the camera pos has changed.
            Vector3i currentWarpPos = WarpManager.getWarpSpacePos(currentPos);
            if (lastSector.equals(currentWarpPos) && !updateFlag)
                return;
            updateFlag = false;
        }
        lastSector.set(WarpManager.getWarpSpacePos(currentPos));

        clearMarkers();

        if (WarpManager.isInWarp(currentPos))
            return;

        Vector3i warpPos = WarpManager.getWarpSpacePos(currentPos);
        Vector3i tempDrop;
        Vector3i tempWarp = new Vector3i();
        int range = (int)ConfigManager.ConfigEntry.map_draw_droppoints_range.getValue();
        for (int x =-range; x <= range; x++) {
            for (int y =-range; y <= range; y++){
                for (int z = -range; z <= range; z++){
                    tempWarp.set(warpPos);
                    tempWarp.add(x,y,z);
                    tempDrop = WarpManager.getRealSpacePos(tempWarp);
                    if (WarpJumpManager.isDroppointShifted(tempWarp))
                       WarpMain.instance.beaconManagerClient.modifyDroppoint(tempWarp,tempDrop);

                    int subsprite = (WarpJumpManager.isDroppointShifted(tempWarp))?1:0;
                    SimpleMapMarker drop = new SimpleMapMarker(mapSprite,subsprite,markerColor,posFromSector(tempDrop,true));
                    drop.setScale(0.2f);
                    addMarker(drop);
                }
            }
        }
    }

    @Override
    public void galaxy_DrawSprites(GameMapDrawer gameMapDrawer) {
        Vector3i currentCenteredSector = gameMapDrawer.getGameMapPosition().get(new Vector3i()); //should be current sector
        updateDropMarkers(currentCenteredSector);
        super.galaxy_DrawSprites(gameMapDrawer);
    }
}
