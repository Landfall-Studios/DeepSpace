package world.landfall.deepspace.server;

import com.mojang.logging.LogUtils;
import dev.egg.SubLevelWarper;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;
import org.slf4j.Logger;
import world.landfall.deepspace.Deepspace;
import dev.ryanhcode.sable.sublevel.SubLevel;
import world.landfall.deepspace.planet.Planet;
import world.landfall.deepspace.planet.PlanetRegistry;
import world.landfall.deepspace.planet.PlanetTeleportHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = Deepspace.MODID)
public class SubLevelEvents {

    private static final float GravitationalConstant = 300f;
    private static final float DistanceScale = 1f;
    private static final int Tickrate = 20;
    private static final float PlanetTeleportOffset = 1.2345f;

    private static final Logger LOGGER = LogUtils.getLogger();



    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        var server = e.getServer();
        var spaceContainer = Objects.requireNonNull(ServerSubLevelContainer.getContainer(server.getLevel(ResourceKey.create(
                Registries.DIMENSION,
                Deepspace.path("space")
        ))));
        var sublevels = spaceContainer.getAllSubLevels();

        sublevels.forEach(s -> {
            if (s != null) {
                var pos = s.logicalPose().position();

                var closestPlanet = PlanetRegistry.getAllPlanets().stream().min((p1, p2) -> {
                    var d1 = p1.getCenter().distanceTo(new Vec3(pos.x, pos.y, pos.z));
                    var d2 = p2.getCenter().distanceTo(new Vec3(pos.x, pos.y, pos.z));
                    var s1 = (int) p1.getBoundingBoxMax().subtract(p1.getBoundingBoxMin()).length();
                    var s2 = (int) p2.getBoundingBoxMax().subtract(p2.getBoundingBoxMin()).length();
                    d1 *= s1;
                    d2 *= s2;
                    if (d1 == d2) return 0;
                    return d2 < d1 ? 1 : -1;
                }).get();
                var handle = RigidBodyHandle.of(s);
                var delta = calculateGravitationalSpeedDelta(new Vec3(pos.x, pos.y, pos.z), closestPlanet.getCenter(), (float) closestPlanet.getBoundingBoxMax().distanceTo(closestPlanet.getBoundingBoxMin()));
                handle.addLinearAndAngularVelocity(
                        new Vector3d(
                                delta.x,
                                delta.y,
                                delta.z
                        ),
                        new Vector3d()
                );

                var hasBeenMerked = new AtomicBoolean(false);
                PlanetRegistry.getAllPlanets().forEach(planet -> {
                    var size = planet.getBoundingBoxMax().subtract(planet.getBoundingBoxMin()).length(); // Length from one corner to another, i.e. the largest continuous line inside the planet

                    var isWithinBounds = planet.isWithinBounds(new Vec3(
                            pos.x,
                            pos.y,
                            pos.z
                    ));
//                    var debugCheck = planet.getName().equals("Overworld");


                    if (isWithinBounds) {
                        var newPos = new Vector3d(
                                0, 300, 0
                        );
                        var dist = planet.getCenter().subtract(new Vec3(
                                pos.x, pos.y, pos.z
                        ));
                        dist.multiply(planet.blockScale(), planet.blockScale(), planet.blockScale());
                        var flyingSidways = Math.abs(dist.x) > Math.abs(dist.y) || Math.abs(dist.z) > Math.abs(dist.y);
                        if (flyingSidways) {

                            if (Math.abs(dist.x) > Math.abs(dist.z)) {
                                // Coming from either +x or -X

                                if (dist.x > 0) {
                                    newPos =
                                            new Vector3d(
                                                -dist.z, 300, dist.y
                                            );
                                } else {
                                    newPos =
                                            new Vector3d(
                                                    dist.z, 300, dist.y
                                            );
                                }

                            } else {
                                if (dist.z > 0) {
                                    newPos =
                                            new Vector3d(
                                                    dist.x, 300, dist.y
                                            );
                                } else {
                                    newPos =
                                            new Vector3d(
                                                    -dist.x, 300, dist.y
                                            );
                                }
                            }
                        } else {
                            if (dist.y > 0) {
                                newPos =
                                        new Vector3d(
                                                -dist.x, 300, dist.z
                                        );
                            } else {
                                newPos =
                                        new Vector3d(
                                                dist.x, 300, -dist.z
                                        );

                            }

                        }
//                        RigidBodyHandle.of(s).addLinearAndAngularVelocity(
//                                s.latestLinearVelocity.mul(planet.blockScale()), s.latestAngularVelocity
//                        );


//                        container.removeSubLevel(s, SubLevelRemovalReason.REMOVED);

//                        var savedData = s.getPlot().save();
//                        var newContainer = Objects.requireNonNull(ServerSubLevelContainer.getContainer(server.getLevel(planet.getDimension())));
//                        var newSublevel = newContainer.allocateNewSubLevel(new Pose3d(
//                                newPos, new Quaterniond(), new Vector3d(), new Vector3d()
//                        ));
//                        ((ServerSubLevel) newSublevel).getPlot().load(savedData);

//                        s.markRemoved();
                        // TODO dimensional sable
//                        SubLevelWarper.WarpSubLevel(
//                                s,
//                                server.getLevel(planet.getDimension()),
//                                newPos
//                        );
//                        ServerSubLevelContainer sourceContainer = ServerSubLevelContainer.getContainer(s.getLevel());
//                        ServerSubLevelContainer destinationContainer = ServerSubLevelContainer.getContainer(server.getLevel(planet.getDimension()));
//                        Collection<SubLevel> subLevels;
//                        var warpConnected = true;
//                        if (warpConnected) {
//                            subLevels = SubLevelHelper.getConnectedChain(s);
//                        } else {
//                            subLevels = Set.of(s);
//                        }

//                        Vector3d center = s.logicalPose().position();
//                        WarpSubLevels(subLevels, sourceContainer, destinationContainer, center, newPos);
                        SubLevelWarper.WarpSubLevel(s, server.getLevel(planet.getDimension()), newPos);
                        hasBeenMerked.set(true);


                    }
                });
            }
        });

        PlanetRegistry.getAllPlanets().forEach(planet -> {
            var level = server.getLevel(planet.getDimension());
            if (level == null) return;
            var container = SubLevelContainer.getContainer(level);
            if (container == null) return;

            container.getAllSubLevels().forEach(s -> {
                var buildHeight = level.getMaxBuildHeight();
                if (s.logicalPose().position().y > buildHeight + PlanetTeleportHandler.SPACE_DISTANCE_FROM_CEILING) {
                    var sPos = s.logicalPose().position();
                    var exitPos = calculateExitPosition(new Vec3(
                            sPos.x,
                            sPos.y,
                            sPos.z
                    ), planet, level);

                    LOGGER.info("Teleporting sublevel to position {} in space, time fraction is {}", exitPos, (float) (level.getGameTime() % ServerLevel.TICKS_PER_DAY) / ServerLevel.TICKS_PER_DAY);

                    Collection<SubLevel> subLevels;
                    var warpConnected = true;
                    if (warpConnected) {
                        subLevels = SubLevelHelper.getConnectedChain(s);
                    } else {
                        subLevels = Set.of(s);
                    }
//                    WarpSubLevels(subLevels, container, spaceContainer, sPos, new Vector3d(
//                            exitPos.x,
//                            exitPos.y,
//                            exitPos.z
//                    ));
                    SubLevelWarper.WarpSubLevel(s, server.getLevel(ResourceKey.create(Registries.DIMENSION, Deepspace.path("space"))), new Vector3d(
                            exitPos.x,
                            exitPos.y,
                            exitPos.z
                    ));
                }
            });
        });


    }

    private static Vec3 calculateExitPosition(Vec3 previousPosition, Planet planet, Level level) {
//        int offset = (int) Math.floor(level.getDayTimeFraction() * 4);
//
//
        var sunPos = Objects.requireNonNull(PlanetRegistry.getSun()).getCenter();
        var planetPos = planet.getCenter();

        var angleBetween = Math.atan2(
                sunPos.subtract(planetPos).x,
                sunPos.subtract(planetPos).z
        ) + Math.PI;
        angleBetween = (angleBetween) / (Math.PI * 2);
//        var timeFactor = (float) (level.dayTime() % ServerLevel.TICKS_PER_DAY) / ServerLevel.TICKS_PER_DAY;
        var timeFactor = level.getTimeOfDay(0);
        int offset = (int) Math.floor(
                (
                        angleBetween +
                        timeFactor
                ) * 4
        ) % 4;

        var exitPos = planetPos;
        var scaledPos = calculateWorldToPlanetScale(
                new Vec2((float) previousPosition.x, (float) previousPosition.z),
                planet
        );
        var planetRadius = planet.getBoundingBoxMax().x / 2 - planet.getBoundingBoxMin().x / 2;
        switch (offset) {
            case 0 ->
                    exitPos = exitPos.add(
                            scaledPos.x * PlanetTeleportOffset,
                            scaledPos.y,
                            planetRadius * PlanetTeleportOffset
                    );
            case 1 ->
                    exitPos = exitPos.add(
                            -planetRadius * PlanetTeleportOffset,
                            scaledPos.y,
                            scaledPos.x * PlanetTeleportOffset
                    );

            case 2 ->
                    exitPos = exitPos.add(
                            -scaledPos.x * PlanetTeleportOffset,
                            scaledPos.y,
                            -planetRadius * PlanetTeleportOffset
                    );

            case 3 ->
                    exitPos = exitPos.add(
                            planetRadius * PlanetTeleportOffset,
                            scaledPos.y,
                            -scaledPos.x * PlanetTeleportOffset
                    );

        }

        LOGGER.info("Planet teleport offset: {}, Planet scaled position: {} {}", offset, scaledPos.x, scaledPos.y);
        LOGGER.info("Time factor: {}, angle between: {}", timeFactor, angleBetween);

        return exitPos;
    }

    private static Vec2 calculateWorldToPlanetScale(Vec2 insidePlanet, Planet planet) {
        var scale = planet.blockScale();
        var planetSize = planet.getBoundingBoxMax().x - planet.getBoundingBoxMin().x;
        return new Vec2(
                (float) (insidePlanet.x / scale),
                (float) (insidePlanet.y / scale)
        );
    }

    private static Vec3 calculateGravitationalSpeedDelta(Vec3 targetPos, Vec3 planetPos, float planetSize) {
        var direction = targetPos.subtract(planetPos).normalize().reverse();
        var distance = targetPos.subtract(planetPos).length() * DistanceScale;
        var magnitude = GravitationalConstant * (planetSize / (distance * distance)) / Tickrate;
        return new Vec3(direction.toVector3f()).scale(magnitude);


    }

}
