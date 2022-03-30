package me.iron.WarpSpace.Mod.client;

import java.util.LinkedList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.12.2020
 * TIME: 18:10
 */
public class WarpProcessController {
    /**
     * enum containing available processes that can happen to a player like jumping to warp
     */
    public enum WarpProcess {
        WARPSECTORBLOCKED,
        RSPSECTORBLOCKED,
        JUMPDROP,
        JUMPEXIT,
        JUMPENTRY,
        JUMPPULL,
        TRAVEL,

        SECTOR_NOEXIT,
        SECTOR_NOENTRY,
        PARTNER_NOEXIT,
        PARTNER_NOENTRY,
        IS_IN_WARP,
        IS_INHIBITED,
        WARP_STABILITY,
        HAS_JUMPED;
        private static LinkedList<WarpProcess> changedValues = new LinkedList<>();

        /**
         * update the "map" with values from these arrays, will auto fire events AFTER ALL values were set.
         */
        public static void update(byte[] arr) {
            assert arr.length == values().length;
            for (int i = 0; i < arr.length; i++)
                values()[i].setCurrentValue(arr[i]);

            for (WarpProcess wp: changedValues)
                for (WarpProcessListener l : wp.listeners)
                    l.onValueChange(wp);
        }
        private byte currentValue = 0;
        private byte previousValue = 0;
        private LinkedList<WarpProcessListener> listeners = new LinkedList();
        public byte getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(byte currentValue) {
            if (currentValue != this.currentValue) {
                changedValues.add(this);
                previousValue = this.currentValue;
                this.currentValue = currentValue;
            }
        }

        public byte getPreviousValue() {
            return previousValue;
        }

        public void addListener(WarpProcessListener listener) {
            listeners.add(listener);
        }

        public void removeListener(WarpProcessListener listener) {
            listeners.remove(listener);
        }
    }
}
