package me.iron.WarpSpace.Mod.HUD.client.map;

import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.StarRunnable;
import api.utils.textures.StarLoaderTexture;
import libpackage.drawer.MapDrawer;
import libpackage.drawer.SpriteLoader;
import libpackage.markers.SimpleMapMarker;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import me.iron.WarpSpace.Mod.beacon.BeaconManager;
import me.iron.WarpSpace.Mod.beacon.BeaconObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
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

    public DropPointMapDrawer(StarMod mod) {
        super(mod);
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
        if (lastSector.equals(WarpManager.getWarpSpacePos(currentPos))) //only update if the camera pos has changed.
            return;
        lastSector.set(WarpManager.getWarpSpacePos(currentPos));

        clearMarkers();

        if (WarpManager.IsInWarp(currentPos))
            return;

        Vector3i warpPos = WarpManager.getWarpSpacePos(currentPos);
        Vector3i tempDrop;
        Vector3i tempWarp = new Vector3i();
        int range = 1;
        for (int x =-range; x <= range; x++) {
            for (int y =-range; y <= range; y++){
                for (int z = -range; z <= range; z++){
                    tempWarp.set(warpPos);
                    tempWarp.add(x,y,z);
                    tempDrop = WarpManager.getRealSpacePos(tempWarp);
                    BeaconObject beaconObject = WarpMain.instance.beaconManager.modifyDroppoint(tempWarp,tempDrop);
                    int subsprite = (beaconObject == null)?0:1;
                    addMarker(new SimpleMapMarker(mapSprite,subsprite,markerColor,posFromSector(tempDrop,true)));
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
