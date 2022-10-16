- automatic reactivation of beacon addon upon loading
- persistent saving across restart
- deactivation
    - damage
    - sector change
    - unpowered

power consumption:
- depends of distance of beacon to natural drop.
```
baseCost = 1000;
modifier = 0.15;
powerBlocksNeeded(dist) = baseCost + modifier * (dist-1)^2;
```

- oneshotting the beaconstation doesnt direclty delete the beacon wrapper, only after dropping near it.
- beacon addon doesnt activate on server after a couple on/off runs?

