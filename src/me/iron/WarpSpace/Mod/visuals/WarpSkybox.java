package me.iron.WarpSpace.Mod.visuals;

import api.common.GameClient;
import api.listener.Listener;
import api.listener.events.draw.RegisterWorldDrawersEvent;
import api.utils.draw.ModWorldDrawer;
import com.bulletphysics.linearmath.Transform;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.WarpManager;
import org.lwjgl.opengl.GL11;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.DrawableScene;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.shader.Shader;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;
import org.schema.schine.graphicsengine.shader.Shaderable;
import org.schema.schine.resource.MeshLoader;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static api.mod.StarLoader.registerListener;
import static me.iron.WarpSpace.Mod.WarpMain.*;
import static org.lwjgl.opengl.GL11.*;
import static org.schema.schine.graphicsengine.core.Controller.getCamera;

/**
 * STARMADE MOD
 * CREATOR: Ithirahad Ivrar'kiim
 * DATE: 21.10.2021
 * TIME: 10:15
 *
 * Draws a large inside-out sphere with a special shader while in warpspace.
 */

public class WarpSkybox extends ModWorldDrawer implements Shaderable {
    public static Shader shader;
    private static Mesh mesh; //sphere mesh
    private static float scaleFactor = 1f; //scale factor of skybox sphere = this value x sector size, also used for shader coord compensation
    static float time = 0;

    public static void instantiate() {
        registerListener(RegisterWorldDrawersEvent.class, new Listener<RegisterWorldDrawersEvent>() {
            @Override
            public void onEvent(RegisterWorldDrawersEvent ev) {
                ev.getModDrawables().add(new WarpSkybox());
            }
        }, instance);
    }

    public static void loadResources(MeshLoader mloader, WarpMain mod) {
        try {
            mloader.loadModMesh(mod, "planet_sphere", mod.getJarResource("me/iron/WarpSpace/Mod/res/planet_sphere.zip"), null);
            System.err.println("[MOD][WarpSpace] Successfully loaded sphere model");

            shader = Shader.newModShader(mod.getSkeleton(), "WarpShader",
                    mod.getJarResource("me/iron/WarpSpace/Mod/res/warp.vert"),
                    mod.getJarResource("me/iron/WarpSpace/Mod/res/warp.frag"));

            shader = ShaderLibrary.tubesStreamShader;
        } catch(Exception ex){
            System.err.println("[MOD][WarpSpace][ERROR] Failed to load skybox draw resources!");
            ex.printStackTrace();
        }
    }

    @Override
    public void onInit() {
        mesh = (Mesh) Controller.getResLoader().getMeshLoader().getModMesh(instance, "planet_sphere").getChilds().iterator().next();
    }

    @Override
    public void update(Timer timer) {
        time += timer.getDelta()*10.1F;
    } //for shader

    @Override
    public void updateShader(DrawableScene drawableScene) {
        // textures would be bound here,
        // e.g.: GlUtil.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        // but the shader used doesn't care about textures and draws everything programmatically anyway
    }

    @Override
    public void updateShaderParameters(Shader shader) {
        GlUtil.updateShaderFloat(shader, "time", time / 50f); //speed of effect 'animation'
        //TODO: Darken/lighten over time when entering/leaving
    }

    @Override
    public void preCameraPrepare() { //Basically background draw; everything else except skybox draws on top of this stuff
        if (!WarpManager.IsInWarp(GameClient.getClientPlayerState().getCurrentSector()))return;

        float sectorSize = GameClientState.instance.getSectorSize();
        float scale = -1 * scaleFactor * sectorSize;

        Transform tr = new Transform();
        tr.origin.set(getCamera().getOffsetPos(new Vector3f())); //lock sphere to camera

        Vector3f modelScale = new Vector3f(scale,scale,scale);

        shader.setShaderInterface(this);
        shader.load();

        GlUtil.glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        GlUtil.scaleModelview(modelScale.x, modelScale.y, modelScale.z); //lol, why is there no scaleModelview(vector3f)

        //GL11.glCullFace(GL_BACK);

        mesh.renderVBO();
        glCullFace(GL_BACK);
        shader.unload();
        GlUtil.glEnable(GL_DEPTH_TEST);
        GlUtil.glDisable(GL_BLEND);
        GlUtil.glEnable(GL_CULL_FACE);
        GlUtil.glPopMatrix();
        glDepthRange(0.0D, 1.0D);
        GlUtil.glDepthMask(true);
        mesh.unloadVBO(true);
        GlUtil.glBlendFunc(770, 1); //shrug
    }

    @Override
    public void cleanUp() {
        mesh.cleanUp(); //???
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public void onExit() {

    }
}
