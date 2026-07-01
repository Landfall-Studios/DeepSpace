package world.landfall.deepspace;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import world.landfall.deepspace.server.SubLevelEvents;

import java.util.concurrent.atomic.AtomicBoolean;

public class Util {

    public static boolean isPlayerBeingTracked(ServerPlayer player, Level level) {
        var sublevelContainer = SubLevelContainer.getContainer(level);
        var isTrackingSublevel = new AtomicBoolean(false);
        sublevelContainer.getAllSubLevels().forEach(s -> {
            if (s instanceof ServerSubLevel subLevel) {
                if (subLevel.getTrackingPlayers().contains(player.getUUID()))
                    isTrackingSublevel.set(true);
            }
        });
        return isTrackingSublevel.get();
    }

    public static float[] calculateOrbitData(Vec3 planet, Vec3 ship, Vec3 velocity, float mass) {
        var r = ship.subtract(planet);
        var absR = r.length();
        var absV = velocity.length();
        var specificOrbitalEnergy = (absV * absV) / 2 - mass * SubLevelEvents.GravitationalConstant / absR;
        var axis = - (mass * SubLevelEvents.GravitationalConstant) / (2 * specificOrbitalEnergy);
        var angularMomentum = r.cross(velocity);
        var absAngularMomentum = angularMomentum.length();
        var eccentricity = Math.sqrt(1 - (absAngularMomentum * absAngularMomentum) / (mass * SubLevelEvents.GravitationalConstant * axis));
        var perigee = axis * (1 - eccentricity);
        var apogee = axis * (1 + eccentricity);
        return new float[] {(float) perigee, (float) apogee};
    }
}
