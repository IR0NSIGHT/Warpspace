package me.iron.WarpSpace.Mod.client.map;

import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.schine.graphicsengine.forms.Sprite;

import api.listener.fastevents.FastListenerCommon;
import api.mod.StarMod;
import libpackage.drawer.MapDrawer;
import libpackage.drawer.SpriteLoader;
import libpackage.markers.SimpleMapMarker;
import me.iron.WarpSpace.Mod.WarpJumpManager;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.server.config.ConfigManager;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 25.10.2021
 * TIME: 16:20
 */
public class DropPointMapDrawer extends MapDrawer {
    private Sprite mapSprite;
    private final Vector3i lastSector = new Vector3i();
    private long nextRefresh = 0;
    private final Vector4f markerColor = new Vector4f(0,1,1,0.8f);
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
            SpriteLoader sl = new SpriteLoader("resources/image/","mapsprite.png",30,30,2,2);
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
            Vector3i currentWarpPos = WarpManager.getInstance().getWarpSpaceSector(currentPos);
            if (lastSector.equals(currentWarpPos) && !updateFlag && System.currentTimeMillis()<nextRefresh)
                return;
            nextRefresh = System.currentTimeMillis()+2000;
            updateFlag = false;
        }
        lastSector.set(WarpManager.getInstance().getWarpSpaceSector(currentPos));

        clearMarkers();

        if (WarpManager.getInstance().isInWarp(currentPos))
            return;

        Vector3i warpPos = WarpManager.getInstance().getWarpSpaceSector(currentPos);
        Vector3i tempDrop;
        Vector3i tempWarp = new Vector3i();
        int range = (int)ConfigManager.ConfigEntry.map_draw_droppoints_range.getValue();
        for (int x =-range; x <= range; x++) {
            for (int y =-range; y <= range; y++){
                for (int z = -range; z <= range; z++){
                    tempWarp.set(x,y,z);
                    if (tempWarp.length()>range)
                        continue;
                    tempWarp.add(warpPos);
                    tempDrop = WarpJumpManager.getDropPoint(tempWarp, null);
                    boolean dropShift = (WarpJumpManager.isDroppointShifted(tempWarp));
                    SimpleMapMarker drop = new SimpleMapMarker(
                            mapSprite,
                            dropShift?1:0,
                            markerColor,
                            posFromSector(tempDrop,true));
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
