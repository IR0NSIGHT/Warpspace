package Mod.server;

import api.listener.fastevents.FastListenerCommon;

/**
 * class handles the replacing of config values. Listens to the game checking the config, intercepts and modifies the value
 *
 * POSTPONED UNTIL NEXT UPDATE FROM STARLAODER
 */
public class ConfigOverwriteManager {
    /**
        public void ConfigOverwriteManager {
        FastListenerCommon.statusEffectApplyListeners.add(new StatusEffectApplyListener() {
            @Override
            public int apply(ConfigEntityManager manager, StatusEffectType type, int value) {
                return value;
            }

            @Override
            public double apply(ConfigEntityManager manager, StatusEffectType type, double value) {
                if(type == StatusEffectType.WARP_INTERDICTION_POWER_CONSUMPTION){
                    //If the applied effect was WARP_INTERDICTION_POWER_CONSUMPTION, scale it by 100000
                    return value*100000;
                }else {
                    return value;
                }
            }

            @Override
            public float apply(ConfigEntityManager manager, StatusEffectType type, float value) {
                return value;
            }

            @Override
            public boolean apply(ConfigEntityManager manager, StatusEffectType type, boolean value) {
                return value;
            }

            @Override
            public Vector3f apply(ConfigEntityManager manager, StatusEffectType type, Vector3f value) {
                return value;
            }
        });
        }
    */
}
