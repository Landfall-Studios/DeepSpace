package world.landfall.deepspace;

import dev.egg.dimensionalsable.DimensionalSable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;
import world.landfall.deepspace.server.SubLevelEvents;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    public static boolean isPlayerBeingTracked(Player player, Level level) {
        var sublevelContainer = SubLevelContainer.getContainer(level);
        var isTrackingSublevel = new AtomicBoolean(false);
        sublevelContainer.getAllSubLevels().forEach(s -> {
            var newBoundingBox = new BoundingBox3d(s.boundingBox()).expand(5);
            if (newBoundingBox.contains(new Vector3d(
                    player.position().x,
                    player.position().y,
                    player.position().z
            )))
                isTrackingSublevel.set(true);

        });
        return isTrackingSublevel.get();
    }
    public static Vec3 fromVector3f(Vector3f vector3f) {
        return new Vec3(
                vector3f.x,
                vector3f.y,
                vector3f.z
        );
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

    public static void delayByTicks(Runnable runnable, int ticks, MinecraftServer server) {
//        server.execute(() -> {
//            if (ticks > 1)
//                delayByTicks(runnable, ticks - 1, server);
//            else
//                runnable.run();
//        });

        server.doRunTask(new TickTask(ticks, runnable));

    }
}
