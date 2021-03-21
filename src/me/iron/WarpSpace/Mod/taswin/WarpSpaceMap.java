package me.iron.WarpSpace.Mod.taswin;

import api.DebugFile;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.gui.HudCreateEvent;
import api.listener.events.world.GalaxyFinishedGeneratingEvent;
import api.listener.events.world.StarCreationAttemptEvent;
import api.listener.fastevents.FastListenerCommon;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.StarRunnable;
import api.utils.game.PlayerUtils;
import api.utils.game.SegmentControllerUtils;
import com.bulletphysics.dynamics.RigidBody;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.iron.WarpSpace.Mod.WarpManager;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.client.view.gui.shiphud.newhud.Radar;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.Galaxy;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.network.objects.Sendable;

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
					final Vector3f pos = WarpManager.GetWarpSpacePos(sectorPos).toVector3f();
					
					Vector3f localOffset = galaxy.getSunPositionOffset(new Vector3i(systemPos), new Vector3i()).toVector3f();
					localOffset.scale(1f / WarpManager.scale);
					
					pos.x = (pos.x + localOffset.x + (8f / WarpManager.scale) - 8 + 0.5f) * GameMapDrawer.sectorSize;
					pos.y = (pos.y + localOffset.y + (8f / WarpManager.scale) - 8 + 0.5f) * GameMapDrawer.sectorSize;
					pos.z = (pos.z + localOffset.z + (8f / WarpManager.scale) - 8 + 0.5f) * GameMapDrawer.sectorSize;
					
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
		
		StarLoader.registerListener(HudCreateEvent.class, new Listener<HudCreateEvent>()
		{
			@Override
			public void onEvent(HudCreateEvent event)
			{
				final Radar r = event.getHud().getRadar();
				GUITextOverlay l = (GUITextOverlay) (r.getChilds().get(1));
				l.setTextSimple(new Object()
				{
					@Override
					public String toString()
					{
						if (((GameClientState) r.getState()).getPlayer().isInTutorial())
						{
							return "<Tutorial>";
						}
						if (((GameClientState) r.getState()).getPlayer().isInPersonalSector())
						{
							return Lng.str("<Personal>");
						}
						if (((GameClientState) r.getState()).getPlayer().isInTestSector())
						{
							return Lng.str("<Test>");
						}
						if (WarpManager.IsInWarp(((GameClientState) r.getState()).getPlayer().getCurrentSector()))
						{
							return Lng.str("<Warp>\n" + WarpManager.GetRealSpacePos(((GameClientState) r.getState()).getPlayer().getCurrentSector()).toStringPure());
						}
						return ((GameClientState) r.getState()).getPlayer().getCurrentSector().toStringPure();
					}
				});
			}
		}, instance);
		
		StarLoader.registerListener(StarCreationAttemptEvent.class, new Listener<StarCreationAttemptEvent>()
		{
			@Override
			public void onEvent(StarCreationAttemptEvent event)
			{
				Vector3i sectorPos = new Vector3i();
				sectorPos.x = (event.getGalaxy().galaxyPos.x * Galaxy.size + event.getPosition().x - Galaxy.halfSize) * 16;
				sectorPos.y = (event.getGalaxy().galaxyPos.y * Galaxy.size + event.getPosition().y - Galaxy.halfSize) * 16;
				sectorPos.z = (event.getGalaxy().galaxyPos.z * Galaxy.size + event.getPosition().z - Galaxy.halfSize) * 16;
				
				if (WarpManager.IsInWarp(sectorPos))
				{
					event.setStarWeight((byte) 0);
				}
			}
		}, instance);
		
		new StarRunnable()
		{
			@Override
			public void run()
			{
				if (GameServer.getServerState() != null)
				{
					for (PlayerState player : GameServer.getServerState().getPlayerStatesByName().values())
					{
						PlayerControllable currentControl = PlayerUtils.getCurrentControl(player);
						if (currentControl instanceof SegmentController)
						{
							Vector3i sectorPos = ((SegmentController) currentControl).getSector(new Vector3i());
							if (WarpManager.IsInWarp(sectorPos))
								return;//sectorPos = WarpManager.GetRealSpacePos(sectorPos);
							if (Math.abs(sectorPos.y) >= WarpManager.universeHalfSize)
							{
								player.sendClientMessage("Out of bounds pushing you back in.", 3);
								Sendable[] sendables = SegmentControllerUtils.getDualSidedSendable((SegmentController)currentControl);
								float distanceOverBorder = Math.abs(sectorPos.y) - WarpManager.universeHalfSize;
								float maxSpeed = ((SegmentController)currentControl).getMaxServerSpeed();
								
								for (Sendable s : sendables)
								{
									RigidBody rigidBody = (RigidBody) ((SegmentController) s).getPhysicsObject();
									Vector3f linearVelocity = new Vector3f(rigidBody.getLinearVelocity(new Vector3f()));
									
									if (distanceOverBorder > 0)
										linearVelocity.y = 1.5f * maxSpeed * (sectorPos.y >= 0 ? -1 : 1);
									else if (Math.abs(linearVelocity.y) > maxSpeed || (linearVelocity.y * (sectorPos.y >= 0 ? 1 : -1) > 0))
										linearVelocity.y = 0;
									rigidBody.setLinearVelocity(linearVelocity);
								}
							}
						}
					}
				}
			}
		}.runTimer(instance, 1);
	}
}