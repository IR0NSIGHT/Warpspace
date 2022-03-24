package me.iron.WarpSpace.Mod.client.sounds;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import api.utils.sound.AudioUtils;
import me.iron.WarpSpace.Mod.WarpMain;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.MainGameGraphics;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.util.WorldToScreenConverter;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 18.12.2021
 * TIME: 19:28
 */
public class WarpSounds {
    public static WarpSounds instance;
    private ArrayList<Sound> soundQueue = new ArrayList<>(); //intended for voice sounds, times out for 2 seconds after playing each sound to not overlap.
    public static void main(String[] args) {
        new WarpSounds();
    }
    public WarpSounds() {
        instance = this;
        String path = WarpMain.instance.getSkeleton().getResourcesFolder().getPath().replace("\\","/")+"/resources/sounds/"; //in moddata

          File f;
          for (int i = 0; i< Sound.values().length; i++) {
              f = new File(path + Sound.values()[i].getSoundName()+".ogg");
              if (f.exists()) {
                  addSound(Sound.values()[i].getSoundName(), f);
              } else {
                  new FileNotFoundException("warp sounds file " + Sound.values()[i].getSoundName()).printStackTrace();
              }
          }
        initLoop();

    }

    /**
     *
     * @param name name of sound
     * @param file
     */
    private void addSound(String name, File file) {
        Controller.getAudioManager().addSound(name,file);
    }

    private void initDebug() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if (event.isServer())
                    return;
                if (event.getText().contains("ping")) {
                    for (int i = 0; i < Sound.values().length; i++) {
                        final int ii = i;
                        new StarRunnable(){
                            @Override
                            public void run() {
                                queueSound(Sound.values()[ii]);
                            }
                        }.runLater(WarpMain.instance,i*10);

                    }

                }
            }
        },WarpMain.instance);
    }

    public void queueSound(Sound s) {
        soundQueue.add(s);
    }

    private void initLoop() {
        new StarRunnable(){
            long last;
            @Override
            public void run() {
                if (last + 2000 < System.currentTimeMillis() && soundQueue.size() >0) {
                    String name = soundQueue.remove(0).soundName;

                    AudioUtils.clientPlaySound(name,1,1);
                //    ModPlayground.broadcastMessage("playing:"+name);
                    last = System.currentTimeMillis();
                }
            }
        }.runTimer(WarpMain.instance,10);
    }
    public enum Sound {
        warp_signature_detected("03-warp_sig_det"),
        inhibitor_detected("04-inh_det"),
        inhibitor_activated("05-inh_act"),
        inhibitor_deactivated("06-inh_deact"),
        beacon_detected("07-beacon_det"),
        beacon_activated("08-beacon_act"),
        beacon_deactivated("09-beacon_deac"),
        warping("01-warping"),
        dropping("02-dropping");

        Sound(String path) {
            this.soundName = path;
        }
        private String soundName;

        public String getSoundName() {
            return soundName;
        }
    }
}
