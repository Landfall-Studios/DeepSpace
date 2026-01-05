package world.landfall.deepspace.planet;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;
import org.slf4j.Logger;
import world.landfall.deepspace.Deepspace;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@EventBusSubscriber(modid = Deepspace.MODID)
public class PlanetTeleportHandler {
    private static final Vec3[] directions = new Vec3[] {
            new Vec3(1, 0, 0),
            new Vec3(-1, 0, 0),
            new Vec3(0, 1, 0),
            new Vec3(0, -1, 0),
            new Vec3(1, 0, 1),
            new Vec3(1, 0, -1),
            new Vec3(1, 1, 1),
            new Vec3(-1, 1, 1),
            new Vec3(1, -1, 1),
            new Vec3(-1, -1, 1),
            new Vec3(1, 1, -1),
            new Vec3(-1, 1, -1),
            new Vec3(1, -1, -1),
            new Vec3(-1, -1, -1),
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RandomSource random = RandomSource.create();
    private static final float DISTANCE_FROM_PLANET_TO_TELEPORT_FROM = 1.5f;
    // How high above the height limit do you need to go to teleport to deep space
    private static final int SPACE_DISTANCE_FROM_CEILING = 10;
    @SubscribeEvent
    public static void serverPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        var level = player.level();
        if (level.getServer()==null) return;
        var planet = PlanetUtils.getPlayerPlanet(player);
        var closestPlanet = PlanetUtils.getNearestPlanet(player.position());
        var dimension = level.dimension().location();
        var height = level.getMaxBuildHeight();

        if (player.position().y > height + SPACE_DISTANCE_FROM_CEILING && planet != null) {
            LOGGER.info("Teleporting player {} to planet {}", player.getDisplayName().getString(), planet.getName());
            var pos = getSafePlanetExitLocation(planet);
            player.teleportTo(
                    Objects.requireNonNull(player.getServer().getLevel(
                            ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("deepspace:space"))
                    )),
                    pos.x,
                    pos.y,
                    pos.z,
                    Set.of(),
                    0,
                    0
            );
        } else if (closestPlanet!=null&&dimension.equals(ResourceLocation.parse("deepspace:space")) && (closestPlanet.isPlayerTouching(player))) {
            var newLevel = player.getServer().getLevel(closestPlanet.getDimension());
            var playerPos = player.position();
            var planetPos = closestPlanet.getCenter();
            var relativePos = planetPos.subtract(playerPos);
            var planetRadius = (float)Math.abs(closestPlanet.getBoundingBoxMin().x - planetPos.x);
            var pRadius = (float)Math.sqrt(relativePos.x * relativePos.x + relativePos.y * relativePos.y + relativePos.z * relativePos.z);
            var pAzimuth = (float)Math.atan2(relativePos.z,relativePos.x);
            var pTheta = (float)Math.acos(relativePos.y/pRadius);
            var levelRadius = Math.abs(closestPlanet.getPhysicalMin().x - closestPlanet.getPhysicalMax().x)/2f;
            var levelCenter = new Vec3((closestPlanet.getPhysicalMin().x + closestPlanet.getPhysicalMax().x) / 2, 0, (closestPlanet.getPhysicalMin().y + closestPlanet.getPhysicalMax().y) / 2);

            float[] finalPos;
            if (pTheta > Math.PI * .75) {
                // top
                finalPos = new float[] {(float)relativePos.x / planetRadius, -(float)relativePos.z / planetRadius};
            } else if (pTheta < Math.PI * .25) {
                // bottom
                finalPos = new float[] {-(float)relativePos.x / planetRadius, (float)relativePos.z / planetRadius};
            } else {
                // mid
                if (pAzimuth - Math.PI/4 < -Math.PI || pAzimuth - Math.PI/4 > Math.PI / 2) {
                    finalPos = new float[]{(float) relativePos.z / planetRadius, -(float) relativePos.y / planetRadius};
                } else if (pAzimuth - Math.PI/4 < -Math.PI / 2) {
                    finalPos = new float[]{-(float) relativePos.x / planetRadius, -(float) relativePos.y / planetRadius};
                } else if (pAzimuth - Math.PI/4 < 0) {
                    finalPos = new float[]{-(float) relativePos.z / planetRadius, -(float) relativePos.y / planetRadius};
                } else {
                    finalPos = new float[]{(float) relativePos.x / planetRadius, -(float) relativePos.y / planetRadius};
                }
            }
            if (!level.getWorldBorder().isWithinBounds(playerPos))
                finalPos = new float[] {0f, 0f};
            player.teleportTo(
                    newLevel,
                    finalPos[0] * levelRadius + levelCenter.x, newLevel.getHeight(), finalPos[1] * levelRadius + levelCenter.z,
                    Set.of(),
                    0, 0
            );
            player.forceAddEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 120, 1, false, true), null);

        }
    }

    private static Vec3 getSafePlanetExitLocation(Planet planet) {
        var center = planet.getCenter();
        var size = planet.getBoundingBoxMax().subtract(planet.getBoundingBoxMin());

        var maxSize = Math.max(
                Math.max(
                        size.x,size.y
                ),size.z
        ) / 2;

        var pos = directions[random.nextIntBetweenInclusive(0, directions.length-1)];
        return center.add(pos.scale(maxSize).scale(DISTANCE_FROM_PLANET_TO_TELEPORT_FROM));
        //return center;
    }
}
