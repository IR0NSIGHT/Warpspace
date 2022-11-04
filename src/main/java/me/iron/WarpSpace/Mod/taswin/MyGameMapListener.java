package me.iron.WarpSpace.Mod.taswin;

import static me.iron.WarpSpace.Mod.taswin.WarpSpaceMap.stars;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.gamemap.GameMapDrawer;
import org.schema.game.server.data.Galaxy;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.Sprite;

import api.listener.fastevents.GameMapDrawListener;
import me.iron.WarpSpace.Mod.WarpManager;

public class MyGameMapListener implements GameMapDrawListener
{
	public static void drawCube(Vector3f pos, float size, Vector4f color)
	{
		//pos.scale(100 / 16);
		
		GlUtil.glBegin(7);
		GlUtil.glColor4f(color.x, color.y, color.z, color.w);
		GL11.glNormal3f(0.0F, 0.0F, 1.0F);
		GL11.glVertex3f(pos.x + size, pos.y + size, pos.z + -size);
		GL11.glVertex3f(pos.x + size, pos.y + -size, pos.z + -size);
		GL11.glVertex3f(pos.x + -size, pos.y + -size, pos.z + -size);
		GL11.glVertex3f(pos.x + -size, pos.y + size, pos.z + -size);
		GL11.glNormal3f(0.0F, 0.0F, -1.0F);
		GL11.glVertex3f(pos.x + size, pos.y + -size, pos.z + size);
		GL11.glVertex3f(pos.x + size, pos.y + size, pos.z + size);
		GL11.glVertex3f(pos.x + -size, pos.y + size, pos.z + size);
		GL11.glVertex3f(pos.x + -size, pos.y + -size, pos.z + size);
		GL11.glNormal3f(-1.0F, 0.0F, 0.0F);
		GL11.glVertex3f(pos.x + size, pos.y + -size, pos.z + -size);
		GL11.glVertex3f(pos.x + size, pos.y + size, pos.z + -size);
		GL11.glVertex3f(pos.x + size, pos.y + size, pos.z + size);
		GL11.glVertex3f(pos.x + size, pos.y + -size, pos.z + size);
		GL11.glNormal3f(1.0F, 0.0F, 0.0F);
		GL11.glVertex3f(pos.x + -size, pos.y + -size, pos.z + size);
		GL11.glVertex3f(pos.x + -size, pos.y + size, pos.z + size);
		GL11.glVertex3f(pos.x + -size, pos.y + size, pos.z + -size);
		GL11.glVertex3f(pos.x + -size, pos.y + -size, pos.z + -size);
		GL11.glNormal3f(0.0F, -1.0F, 0.0F);
		GL11.glVertex3f(pos.x + size, pos.y + size, pos.z + size);
		GL11.glVertex3f(pos.x + size, pos.y + size, pos.z + -size);
		GL11.glVertex3f(pos.x + -size, pos.y + size, pos.z + -size);
		GL11.glVertex3f(pos.x + -size, pos.y + size, pos.z + size);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glVertex3f(pos.x + size, pos.y + -size, pos.z + -size);
		GL11.glVertex3f(pos.x + size, pos.y + -size, pos.z + size);
		GL11.glVertex3f(pos.x + -size, pos.y + -size, pos.z + size);
		GL11.glVertex3f(pos.x + -size, pos.y + -size, pos.z + -size);
		GlUtil.glEnd();
		GlUtil.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public void system_PreDraw(GameMapDrawer drawer, Vector3i system, boolean explored)
	{
	
	}
	
	@Override
	public void system_PostDraw(GameMapDrawer drawer, Vector3i system, boolean explored)
	{
	
	}
	
	@Override
	public void galaxy_PreDraw(GameMapDrawer drawer)
	{
	
	}
	
	@Override
	public void galaxy_PostDraw(GameMapDrawer drawer)
	{
	
	}
	
	@Override
	public void galaxy_DrawLines(GameMapDrawer drawer)
	{
	
	}
	
	@Override
	public void galaxy_DrawSprites(GameMapDrawer drawer)
	{
		if (!WarpManager.getInstance().isInWarp(drawer.getPlayerSector())) return;
		
		Vector3i realCoords = WarpManager.getInstance().getRealSpacePos(drawer.getPlayerSector());
		realCoords.scaleFloat(1 / 16f);
		
		Vector3i galaxyPos = new Vector3i(drawer.getState().getCurrentGalaxy().galaxyPos);
		Galaxy.getContainingGalaxyFromSystemPos(realCoords, galaxyPos);
		
		Sprite sprite = Controller.getResLoader().getSprite("stellarSprites-2x2-c-");
		if (stars == null) return;
		if (stars.get(galaxyPos) == null) return;
		for(PositionableSubColorSprite[] sp : stars.get(galaxyPos).values())
		{
			DrawUtils.drawSprite(drawer, sprite, sp);
		}
	}
	
	@Override
	public void galaxy_DrawQuads(GameMapDrawer drawer)
	{
		Vector3i sector = drawer.getPlayerSector();
		if (WarpManager.getInstance().isInWarp(sector))
		{
			float r = WarpManager.getInstance().getScale() / 2f;
			
			Vector3i realPos = WarpManager.getInstance().getRealSpacePos(sector);
			drawCube(new Vector3f(
				(realPos.x - 8f) * GameMapDrawer.sectorSize,
				(realPos.y - 8f) * GameMapDrawer.sectorSize,
				(realPos.z - 8f) * GameMapDrawer.sectorSize), r * GameMapDrawer.sectorSize, new Vector4f(0F,1F,1F, 0.25F));
			/*drawCube(new Vector3f(
				(realPos.x - 7.5f) * 100f / 16,
				(realPos.y - 7.5f) * 100f / 16,
				(realPos.z - 7.5f) * 100f / 16), 0.5f * 100 / 16, new Vector4f(1F,1F,1F, 1F));*/
		}
	}
}
