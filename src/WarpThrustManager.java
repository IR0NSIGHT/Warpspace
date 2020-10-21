import api.DebugFile;
import api.ModPlayground;
import com.bulletphysics.dynamics.RigidBody;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.thrust.ThrusterElementManager;
import org.schema.game.mod.Mod;

import javax.vecmath.Vector3f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 16.10.2020
 * TIME: 12:20
 */
public class WarpThrustManager {
    public static void DebugThrust(Ship ship) {
        DebugFile.log("Ship thrust for : " + ship.getName());
        ThrusterElementManager tem = ship.getManagerContainer().getThrusterElementManager();
        DebugFile.log("actual thrust: " + tem.getActualThrust());
   //     DebugFile.log("power: " + tem.getPower());
        DebugFile.log("max speed: " + tem.getMaxSpeedAbsolute());
        DebugFile.log("rule modifier on thrust: " + tem.ruleModifierOnThrust);

    }
    public static void OverwriteThrust(Ship ship, boolean inWarp) {
        DebugFile.log("overwriting ship thrust for : " + ship.getName());
        ThrusterElementManager tem = ship.getManagerContainer().getThrusterElementManager();
        DebugFile.log("collection thrust: " + tem.getCollection().getTotalThrust());
        tem.getCollection().setTotalThrust(0);
        tem.getCollection().setTotalThrustRaw(0);
        ModPlayground.broadcastMessage("total thrust raw: "+ tem.getCollection().getTotalThrustRaw());
        DebugFile.log("total thrust raw: "+ tem.getCollection().getTotalThrustRaw());
        if (inWarp) {
            tem.ruleModifierOnThrust = 0f;
        } else {
            tem.ruleModifierOnThrust = 1;
        }
    }
    public static void LimitShipSpeed(Ship ship, float maxSpeed) {

       Vector3f shipVel = ship.getPhysicsObject().getLinearVelocity(new Vector3f());
       if (shipVel.length() > maxSpeed) {
           if (!ship.isOnServer()) {
               ModPlayground.broadcastMessage("ship not on server!");
               return;
           };
           ModPlayground.broadcastMessage("youre to fast!");
           //shipVel.normalize();
           shipVel.negate();
           //going over the speedlimit, license and registration please
           //set shipVel to maximum
           //shipVel.scale(maxSpeed);
           ModPlayground.broadcastMessage("calculated vel to add: " + shipVel.length());
       //    ship.getPhysicsObject().activate();
        //   ship.getPhysicsObject().setLinearVelocity(shipVel);
       //    ship.getManagerContainer().getThrusterElementManager().getVelocity().set(0,0,0);
           //apply impusle against velocity
           RigidBody rb = ship.getPhysicsObject();
           rb.activate();
           // Convert from velocity to impulse...
           shipVel.scale(1.0F / rb.getInvMass());
           rb.applyCentralImpulse(shipVel);
       } else {
           ModPlayground.broadcastMessage("under speed limit");
       }
    }
}
