package world.landfall.deepspace.server;

import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;
import world.landfall.deepspace.Deepspace;
import dev.ryanhcode.sable.sublevel.SubLevel;
import world.landfall.deepspace.planet.PlanetRegistry;

import java.util.Objects;

@EventBusSubscriber(modid = Deepspace.MODID)
public class SubLevelEvents {

    private static final float GravitationalConstant = 100f;
    private static final float DistanceScale = 1.0f;
    private static final int Tickrate = 20;


    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        var server = e.getServer();
        var sublevels = Objects.requireNonNull(ServerSubLevelContainer.getContainer(server.getLevel(ResourceKey.create(
                Registries.DIMENSION,
                Deepspace.path("space")
        )))).getAllSubLevels();
        sublevels.forEach(s -> {
            if (s != null) {
                var handle = RigidBodyHandle.of(s);
                PlanetRegistry.getAllPlanets().forEach(planet -> {
                    var size = planet.getBoundingBoxMax().subtract(planet.getBoundingBoxMin()).length(); // Length from one corner to another, i.e. the largest continuous line inside the planet
                    var pos = s.logicalPose().position();
                    var delta = calculateGravitationalSpeedDelta(new Vec3(pos.x, pos.y, pos.z), planet.getCenter(), (float) size);
                    handle.addLinearAndAngularVelocity(
                            new Vector3d(
                                    delta.x,
                                    delta.y,
                                    delta.z
                            ),
                            new Vector3d()
                    );
                });
            }
        });
    }

    private static Vec3 calculateGravitationalSpeedDelta(Vec3 targetPos, Vec3 planetPos, float planetSize) {
        var direction = targetPos.subtract(planetPos).normalize().reverse();
        var distance = targetPos.subtract(planetPos).length() * DistanceScale;
        var magnitude = GravitationalConstant * (planetSize / (distance * distance)) / Tickrate;
        return new Vec3(direction.toVector3f()).scale(magnitude);


    }
}
