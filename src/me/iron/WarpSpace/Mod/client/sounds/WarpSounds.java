package me.iron.WarpSpace.Mod.client.sounds;

import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import api.utils.sound.AudioUtils;
import me.iron.WarpSpace.Mod.WarpMain;
import me.iron.WarpSpace.Mod.client.WarpProcess;
import org.apache.commons.io.IOUtils;
import org.schema.schine.graphicsengine.core.Controller;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
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
        installSounds();

        String path = WarpMain.instance.getSkeleton().getResourcesFolder().getPath().replace("\\","/")+"/resources/sounds/"; //in moddata

          File f;
          for (int i = 0; i< Sound.values().length; i++) {
              f = new File(path + Sound.values()[i].getSoundName()+".wav");
              if (f.exists()) {
                  addSound(Sound.values()[i].getSoundName(), f);
                  Sound sound = Sound.values()[i];
                  try {
                      AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                      AudioFormat format = ais.getFormat();
                      long frames = ais.getFrameLength();
                      double durationInSeconds = (frames+0.0)/format.getFrameRate();
                      sound.setDuration((long)(durationInSeconds*1000));
                  } catch (UnsupportedAudioFileException | IOException e) {
                      e.printStackTrace();
                  }
              } else {
                  new FileNotFoundException("warp sounds file " + Sound.values()[i].getSoundName()).printStackTrace();
              }
          }
        initLoop();
        initEventSounds();
    }

    /**
     * will add sounds and install soundfiles to the client if they dont already exist.
     */
    private void installSounds() {
        String folderPath = WarpMain.instance.getSkeleton().getResourcesFolder().getPath().replace("\\","/")+"/resources/sounds/"; //in moddata
        File dir = new File(folderPath);
        if (!dir.exists())
            dir.mkdirs();

        File file;
        String name;
        String path;
        for (int i = 0; i< Sound.values().length; i++) {
            name = Sound.values()[i].getSoundName();
            path = folderPath + name +".wav";
            System.out.println("trying to load sound '"+name+"' at :"+path);
            file = new File(".",path);
            if (!file.exists()) {
                try {
                    //install sound files to client
                    String jarPath = "me/iron/WarpSpace/Mod/res/sounds/" +Sound.values()[i].getSoundName()+".wav";

                    InputStream source = WarpMain.instance.getSkeleton().getClassLoader().getResourceAsStream(jarPath);

                    File targetFile = new File(".",path);
                    targetFile.createNewFile();


                    FileOutputStream outStream = new FileOutputStream(targetFile);

                    IOUtils.copy(source,outStream);

                    source.close();
                    outStream.close();

                    file = new File(path);
                    if (!file.exists()) {
                        new FileNotFoundException().printStackTrace();
                    } else {
                        System.out.println("installed file at " + file.getCanonicalPath());
                    }
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            assert file.exists():"installation of warpspace soundfiles failed";
            addSound(name,file);

        }
    }

    /**
     *
     * @param name name of sound
     * @param file
     */
    private void addSound(String name, File file) {
        Controller.getAudioManager().addSound(name,file);
    }

    public void initEventSounds() {
        VoiceAnnouncer vc = new VoiceAnnouncer();
        WarpProcess.JUMPDROP.addListener(vc);
        WarpProcess.JUMPENTRY.addListener(vc);
        WarpProcess.JUMPEXIT.addListener(vc);
        WarpProcess.JUMPPULL.addListener(vc);
        WarpProcess.WARP_STABILITY.addListener(vc);

        EngineSounds es = new EngineSounds();
        WarpProcess.JUMPENTRY.addListener(es);
        WarpProcess.JUMPEXIT.addListener(es);
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

    public void playSound(Sound s) {
        String name = s.soundName;
        AudioUtils.clientPlaySound(name,1,1);
    }

    private void initLoop() {
        new StarRunnable(){
            long last;
            long timeout;
            @Override
            public void run() {
                if (last + Math.max(500,timeout) < System.currentTimeMillis() && soundQueue.size() >0) {
                    Sound s = soundQueue.remove(0);
                    playSound(s);

                    timeout = s.getDuration();
                    last = System.currentTimeMillis();
                }
            }
        }.runTimer(WarpMain.instance,10);
    }
    public enum Sound {
        warping("01-warping"),
        dropping("02-dropping"),
        warp_signature_detected("03-warp_sig_det"),
        inhibitor_detected("04-inh_det"),
        inhibitor_activated("05-inh_act"),
        inhibitor_deactivated("06-inh_deact"),
        beacon_detected("07-beacon_det"),
        beacon_activated("08-beacon_act"),
        beacon_deactivated("09-beacon_deac"),
        jump_charge("10-warp_entry_effect");
;

        Sound(String path) {
            this.soundName = path;
        }
        private String soundName;
        private long duration;

        /**
         * in millis
         * @return
         */
        public long getDuration() {
            return duration;
        }

        public void setDuration(long millis) {
            this.duration = millis;
        }

        public String getSoundName() {
            return soundName;
        }

        @Override
        public String toString() {
            return "Sound{" +
                    "enum='"+this.name() + '\'' +
                    ", soundName='" + soundName + '\'' +
                    ", duration=" + duration +
                    '}';
        }
    }
}
