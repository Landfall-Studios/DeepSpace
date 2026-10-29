package world.landfall.deepspace.server;

import com.mojang.logging.LogUtils;
import dev.egg.dimensionalsable.SubLevelWarper;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
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
import world.landfall.deepspace.planet.Planet;
import world.landfall.deepspace.planet.PlanetRegistry;
import world.landfall.deepspace.planet.PlanetTeleportHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = Deepspace.MODID)
public class SubLevelEvents {

    public static final float GravitationalConstant = 300f;
    private static final float DistanceScale = 1f;
    private static final int Tickrate = 20;
    private static final float PlanetTeleportOffset = 1.2345f;
    private static final float PlanetTeleportAddedVelocity = 28f;

    private static final Logger LOGGER = LogUtils.getLogger();



    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        var server = e.getServer();
        var spaceContainer = ServerSubLevelContainer.getContainer(server.getLevel(ResourceKey.create(
                Registries.DIMENSION,
                Deepspace.path("space")
        )));
        if (spaceContainer == null) return;
        var sublevels = spaceContainer.getAllSubLevels();

        sublevels.forEach(s -> {
            if (s != null && !s.isRemoved()) {
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
                        dist = dist.multiply(planet.blockScale(), planet.blockScale(), planet.blockScale());
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
                        SubLevelWarper.warpConnectedSubLevels(s, server.getLevel(planet.getDimension()), newPos);
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

                    var oldSubLevels = List.copyOf(spaceContainer.getAllSubLevels());
                    var newLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, Deepspace.path("space")));
                    SubLevelWarper.warpConnectedSubLevels(s, newLevel, new Vector3d(
                            exitPos.x,
                            exitPos.y,
                            exitPos.z
                    ));
                    var newSubLevels = List.copyOf(spaceContainer.getAllSubLevels());
                    var diff = new ArrayList<ServerSubLevel>();
                    for (var x : newSubLevels) {
                        if (!oldSubLevels.contains(x))
                            diff.add(x);
                    }

                    LOGGER.info("Lenghts: {}, {}", oldSubLevels.size(), newSubLevels.size());
                    Vector3d finalNewPos = exitPos.toVector3f().get(new Vector3d());
//                    var newSublevelUUID = Util.OLD_TO_NEW.get(s.getUniqueId()).first();
//                    var newSublevel = (ServerSubLevel) spaceContainer.getSubLevel(newSublevelUUID);
                    var newSublevel = diff.getFirst();
                    LOGGER.info("Running delayed velocity addition !");
                    if (newSublevel == null) {
                        LOGGER.error("Couldn't find sublevel in new dimension !");
                        return;
                    };
                    var newHandle = RigidBodyHandle.of(newSublevel);
                    var center = planet.getCenter();
                    var toPlanet = finalNewPos.sub(
                            new Vector3d(center.x, center.y, center.z)
                    ).normalize().mul(PlanetTeleportAddedVelocity).rotateY(Math.PI / 8);
                    newHandle.addLinearAndAngularVelocity(
                            toPlanet, new Vector3d(0, 0, 0)
                    );
                    var pos = newSublevel.logicalPose().position();
                    LOGGER.info("Sublevel at: {} {} {}", pos.x, pos.y, pos.z);
                    LOGGER.info("UUID: {}", newSublevel.getUniqueId());

                    diff.forEach(subLevel -> {
//                        spaceContainer.addForceLoadTicket(subLevel, SubLevelLoadingTicketType.COMMAND_FORCED, Unit.INSTANCE);
                        if (subLevel.getMassTracker() == null || subLevel.getMassTracker().getCenterOfMass() == null) {
                            spaceContainer.removeSubLevel(subLevel, SubLevelRemovalReason.REMOVED);
                        }

                    });

                }
            });
        });
        WaitingOnAddedVelocity.tick(server);


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
    private static class WaitingOnAddedVelocity {
        public static final Set<WaitingOnAddedVelocity> SUBLEVELS_WAITING = new HashSet<>();

        private final int targetRunTick;
        private final Runnable ref;

        private WaitingOnAddedVelocity(int targetRunTick, Runnable ref) {
            this.targetRunTick = targetRunTick;
            this.ref = ref;
        }

        public static void tick(MinecraftServer server) {
            var ticks = server.getTickCount();
            List<WaitingOnAddedVelocity> l = new ArrayList<>();
            for (var s : SUBLEVELS_WAITING) {
                if (s.targetRunTick <= ticks)
                    l.add(s);
            }
            for (var s : l) {
                SUBLEVELS_WAITING.remove(s);
                s.ref.run();

            }

        }
        public static void add(int targetRunTick, Runnable ref) {
            SUBLEVELS_WAITING.add(new WaitingOnAddedVelocity(targetRunTick, ref));
        }
    }

}
