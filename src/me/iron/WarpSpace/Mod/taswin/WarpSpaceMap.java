package me.iron.WarpSpace.Mod.taswin;

import api.listener.Listener;
import api.listener.events.world.GalaxyFinishedGeneratingEvent;
import api.listener.events.world.StarCreationAttemptEvent;
import api.listener.fastevents.FastListenerCommon;
import api.mod.StarLoader;
import api.mod.StarMod;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.iron.WarpSpace.Mod.WarpManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.server.data.Galaxy;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.HashMap;

public class WarpSpaceMap
{
	public static HashMap<Vector3i, HashMap<Vector3f, PositionableSubColorSprite[]>> stars = new HashMap<>();
	
	private static MyGameMapListener myGameMapListener;
	
	public static void disable()
	{
		FastListenerCommon.gameMapListeners.remove(myGameMapListener);
	}
	
	public static void enable(final StarMod instance)
	{
		myGameMapListener = new MyGameMapListener();
		FastListenerCommon.gameMapListeners.add(myGameMapListener);
		
		StarLoader.registerListener(GalaxyFinishedGeneratingEvent.class, new Listener<GalaxyFinishedGeneratingEvent>()
		{
			@Override
			public void onEvent(GalaxyFinishedGeneratingEvent event)
			{
				final Galaxy galaxy = event.getGalaxy();
				
				final ObjectArrayList<Vector3f> starPoses = new ObjectArrayList<>();
				galaxy.getPositions(starPoses, new FloatArrayList());
				
				for (int i = 0; i < starPoses.size(); i++)
				{
					final int finalI = i;
					final Vector4f color = galaxy.getSunColor(new Vector3i(starPoses.get(finalI)));
					final int subSprite = galaxy.getSystemType(new Vector3i(starPoses.get(finalI)));
					
					Vector3f systemPos = starPoses.get(finalI);
					
					Vector3i sectorPos = new Vector3i((systemPos.x - Galaxy.halfSize) * 16, (systemPos.y - Galaxy.halfSize) * 16, (systemPos.z - Galaxy.halfSize) * 16);
					final Vector3f pos = WarpManager.getWarpSpacePos(sectorPos).toVector3f();
					
					Vector3f loaclOffset = galaxy.getSunPositionOffset(new Vector3i(systemPos), new Vector3i()).toVector3f();
					loaclOffset.scale(1f / WarpManager.scale);
					
					pos.x = (pos.x + loaclOffset.x + (8f / WarpManager.scale) - 8 + 0.5f) * GameMapDrawer.sectorSize;
					pos.y = (pos.y + loaclOffset.y + (8f / WarpManager.scale) - 8 + 0.5f) * GameMapDrawer.sectorSize;
					pos.z = (pos.z + loaclOffset.z + (8f / WarpManager.scale) - 8 + 0.5f) * GameMapDrawer.sectorSize;
					
					PositionableSubColorSprite[] s = new PositionableSubColorSprite[]
						{
							new PositionableSubColorSprite()
							{
								@Override
								public Vector4f getColor()
								{
									return color;
								}
								
								@Override
								public float getScale(long l)
								{
									return 0.025f;
								}
								
								@Override
								public int getSubSprite(Sprite sprite)
								{
									return subSprite;
								}
								
								@Override
								public boolean canDraw()
								{
									return true;
								}
								
								@Override
								public Vector3f getPos()
								{
									return pos;
								}
							}
						};
					
					if (!stars.containsKey(galaxy.galaxyPos))
						stars.put(galaxy.galaxyPos, new HashMap<Vector3f, PositionableSubColorSprite[]>());
					stars.get(galaxy.galaxyPos).put(starPoses.get(i), s);
				}
			}
		}, instance);
		
		//addMiniMapListener(instance);
		
		StarLoader.registerListener(StarCreationAttemptEvent.class, new Listener<StarCreationAttemptEvent>()
		{
			@Override
			public void onEvent(StarCreationAttemptEvent event)
			{
				Vector3i sectorPos = new Vector3i();
				sectorPos.x = (event.getGalaxy().galaxyPos.x * Galaxy.size + event.getPosition().x - Galaxy.halfSize) * 16;
				sectorPos.y = (event.getGalaxy().galaxyPos.y * Galaxy.size + event.getPosition().y - Galaxy.halfSize) * 16;
				sectorPos.z = (event.getGalaxy().galaxyPos.z * Galaxy.size + event.getPosition().z - Galaxy.halfSize) * 16;
				
				if (WarpManager.isInWarp(sectorPos))
				{
					event.setStarWeight((byte) 0);
				}
			}
		}, instance);
	}
}