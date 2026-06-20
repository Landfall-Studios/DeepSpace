package world.landfall.deepspace.server;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.egg.DimensionalSable;
import dev.egg.SubLevelTemplate;
import dev.egg.SubLevelWarper;
import dev.egg.registries.BlockEntityRegistry;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.KinematicContraption;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import world.landfall.deepspace.Deepspace;
import dev.ryanhcode.sable.sublevel.SubLevel;
import world.landfall.deepspace.planet.PlanetRegistry;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = Deepspace.MODID)
public class SubLevelEvents {

    private static final float GravitationalConstant = 100f;
    private static final float DistanceScale = 1.0f;
    private static final int Tickrate = 20;


    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        var server = e.getServer();
        var container = Objects.requireNonNull(ServerSubLevelContainer.getContainer(server.getLevel(ResourceKey.create(
                Registries.DIMENSION,
                Deepspace.path("space")
        ))));
        var sublevels = container.getAllSubLevels();
        sublevels.forEach(s -> {
            if (s != null) {
                var handle = RigidBodyHandle.of(s);
                var hasBeenMerked = new AtomicBoolean(false);
                PlanetRegistry.getAllPlanets().forEach(planet -> {
                    var size = planet.getBoundingBoxMax().subtract(planet.getBoundingBoxMin()).length(); // Length from one corner to another, i.e. the largest continuous line inside the planet
                    var pos = s.logicalPose().position();
                    var delta = calculateGravitationalSpeedDelta(new Vec3(pos.x, pos.y, pos.z), planet.getCenter(), (float) size);
                    var isWithinBounds = planet.isWithinBounds(new Vec3(
                            pos.x,
                            pos.y,
                            pos.z
                    ));
                    if (!hasBeenMerked.get())
                        handle.addLinearAndAngularVelocity(
                                new Vector3d(
                                        delta.x,
                                        delta.y,
                                        delta.z
                                ),
                                new Vector3d()
                        );
                    if (isWithinBounds) {
                        var newPos = new Vector3d(
                                0, 300, 0
                        );
                        var dist = planet.getCenter().subtract(new Vec3(
                                pos.x, pos.y, pos.z
                        ));
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
                        ServerSubLevelContainer sourceContainer = ServerSubLevelContainer.getContainer(s.getLevel());
                        ServerSubLevelContainer destinationContainer = ServerSubLevelContainer.getContainer(server.getLevel(planet.getDimension()));
                        Collection<SubLevel> subLevels;
                        var warpConnected = true;
                        if (warpConnected) {
                            subLevels = SubLevelHelper.getConnectedChain(s);
                        } else {
                            subLevels = Set.of(s);
                        }

                        Vector3d center = s.logicalPose().position();
                        WarpSubLevels(subLevels, sourceContainer, destinationContainer, center, newPos);
                        hasBeenMerked.set(true);


                    }
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
    private static void WarpSubLevels(Collection<SubLevel> compoundSubLevel, ServerSubLevelContainer sourceContainer, ServerSubLevelContainer destinationContainer, Vector3d center, Vector3d position) {
        HashMap<UUID, DimensionalSable.Pair<UUID, Vec3i>> oldToNew = new HashMap();
        HashMap<UUID, CompoundTag> subLevelTags = new HashMap();
        HashMap<UUID, ServerLevelPlot> subLevelPlots = new HashMap();
        HashMap<UUID, Set<Entity>> visitedEntities = new HashMap();

        for(SubLevel subLevel : compoundSubLevel) {
            ServerSubLevel serverSubLevel = (ServerSubLevel)subLevel;
            double boxX = subLevel.boundingBox().width();
            double boxY = subLevel.boundingBox().height();
            double boxZ = subLevel.boundingBox().length();
            AABB box = new AABB(-boxX / (double)2.0F + center.x, -boxY / (double)2.0F + center.y, -boxZ / (double)2.0F + center.z, boxX / (double)2.0F + center.x, boxY / (double)2.0F + center.y, boxZ / (double)2.0F + center.z);
            box.inflate((double)1.0F);
            List<Entity> candidates = sourceContainer.getLevel().getEntities((Entity)null, box);
            visitedEntities.put(subLevel.getUniqueId(), new HashSet(candidates));
            var var20 = serverSubLevel.getPlot().getContraptions().iterator();

            while(var20.hasNext()) {
                KinematicContraption contraption = (KinematicContraption)var20.next();
                ((AbstractContraptionEntity)contraption).disassemble();
            }

            CompoundTag tag = SubLevelTemplate.save(serverSubLevel.getPlot());
            Pose3d pose = new Pose3d();
            pose.position().set((new Vector3d(subLevel.logicalPose().position())).sub(new Vector3d(center)).add(position));
            pose.orientation().set(subLevel.logicalPose().orientation());
            ServerSubLevel copy = (ServerSubLevel)destinationContainer.allocateNewSubLevel(pose);
            subLevelTags.put(subLevel.getUniqueId(), tag);
            Vec3i start = serverSubLevel.getPlot().getCenterBlock().offset(0, sourceContainer.getLevel().dimensionType().minY(), 0);
            Vec3i end = copy.getPlot().getCenterBlock().offset(0, destinationContainer.getLevel().dimensionType().minY(), 0);
            Vec3i offset = end.subtract(start);
            oldToNew.put(subLevel.getUniqueId(), DimensionalSable.Pair.of(copy.getUniqueId(), offset));
            subLevelPlots.put(subLevel.getUniqueId(), copy.getPlot());
        }

        Set<UUID> visited = new HashSet();
        SubLevelPhysicsSystem physics = SubLevelPhysicsSystem.get(destinationContainer.getLevel());

        for(SubLevel subLevel : compoundSubLevel) {
            ServerLevelPlot plot = (ServerLevelPlot)subLevelPlots.get(subLevel.getUniqueId());
            ServerSubLevel copy = plot.getSubLevel();
            Pose3d pose = new Pose3d(copy.logicalPose());
            SubLevelTemplate.load(plot, (CompoundTag)subLevelTags.get(subLevel.getUniqueId()), new BlockEntityRegistry.MoveInfo(oldToNew, (new Vector3d(position)).sub(center), sourceContainer.getLevel(), destinationContainer.getLevel()));
            physics.getPipeline().teleport(copy, pose.position(), pose.orientation());
            if (subLevel.getName() != null) {
                copy.setName(subLevel.getName());
            }

            for(Entity entity : visitedEntities.get(subLevel.getUniqueId())) {
                TeleportEntity(entity, sourceContainer, destinationContainer, center, position, subLevel, oldToNew, visited);
            }
        }

        for(SubLevel subLevel : compoundSubLevel) {
            sourceContainer.removeSubLevel(subLevel, SubLevelRemovalReason.REMOVED);
        }

    }
    private static void TeleportEntity(Entity entity, ServerSubLevelContainer sourceContainer, ServerSubLevelContainer destinationContainer, Vector3d center, Vector3d position, SubLevel subLevel, HashMap<UUID, DimensionalSable.Pair<UUID, Vec3i>> oldToNew, Set<UUID> visited) {
        if (!visited.contains(entity.getUUID())) {
            visited.add(entity.getUUID());
            Vector3d newPos;
            if (!EntitySubLevelUtil.shouldKick(entity) && !entity.isPassenger()) {
                Vec3 pos = entity.trackingPosition();
                Vec3i offset = (Vec3i)((DimensionalSable.Pair)oldToNew.get(subLevel.getUniqueId())).second();
                newPos = new Vector3d(pos.x + (double)offset.getX(), pos.y + (double)offset.getY(), pos.z + (double)offset.getZ());
            } else {
                Vector3d offset = (new Vector3d(position)).sub(center);
                Vec3 pos = Sable.HELPER.projectOutOfSubLevel(sourceContainer.getLevel(), entity.position());
                newPos = new Vector3d(pos.x + offset.x, pos.y + offset.y, pos.z + offset.z);
            }

            entity.unRide();
            entity.teleportTo(destinationContainer.getLevel(), newPos.x, newPos.y, newPos.z, Set.of(), entity.getYRot(), entity.getXRot());
        }
    }
}
