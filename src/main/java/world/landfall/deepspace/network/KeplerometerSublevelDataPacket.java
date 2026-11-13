package world.landfall.deepspace.network;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.slf4j.Logger;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.Util;
import world.landfall.deepspace.blockentity.KeplerometerBlockEntity;
import world.landfall.deepspace.planet.PlanetRegistry;

import java.util.UUID;

public class KeplerometerSublevelDataPacket {
    private static final Logger LOGGER = LogUtils.getLogger();
    public record Serverbound(BlockPos blockEntity, UUID sublevel) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Serverbound> TYPE = new Type<>(Deepspace.path("keplerometer_sublevel_data_serverbound"));
        public static final StreamCodec<FriendlyByteBuf, Serverbound> STREAM_CODEC = StreamCodec.ofMember(
                Serverbound::encode,
                Serverbound::decode
        );
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void encode(@NotNull Serverbound packet, @NotNull FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.blockEntity);
            buffer.writeUUID(packet.sublevel);
        }
        public static @NotNull Serverbound decode(@NotNull FriendlyByteBuf buffer) {
            return new Serverbound(
                    buffer.readBlockPos(),
                    buffer.readUUID()
            );
        }
        public static void handle(@NotNull Serverbound packet, @NotNull IPayloadContext ctx) {
            var level = ctx.player().level();
            if (!level.dimension().location().equals(Deepspace.path("space")))
                return;
            var container = SubLevelContainer.getContainer(level);
            if (container == null) return;
            var sublevel = container.getSubLevel(packet.sublevel);
            if (sublevel == null) return;
            if (!(sublevel instanceof ServerSubLevel serverSubLevel)) return;
            var handle = RigidBodyHandle.of(serverSubLevel);
            var temp_velocity = handle.getLinearVelocity();
            var temp_position = sublevel.logicalPose().position();
            var position = new Vector3f(
                    (float) temp_position.x(),
                    (float) temp_position.y(),
                    (float) temp_position.z()
            );
            var velocity = new Vector3f(
                    (float) temp_velocity.x(),
                    (float) temp_velocity.y(),
                    (float) temp_velocity.z()
            );
            var blockEntity = level.getBlockEntity(packet.blockEntity);
            if (blockEntity instanceof KeplerometerBlockEntity keplerometerBlockEntity) {
                var pos = Util.fromVector3f(position);
                var planet = PlanetRegistry.getClosestPlanet(pos);
                if (planet == null) return;
                float[] result = Util.calculateOrbitData(
                        planet.getCenter(),
                        pos,
                        Util.fromVector3f(velocity),
                        (float) planet.getBoundingBoxMax().distanceTo(planet.getBoundingBoxMin())
                );
                keplerometerBlockEntity.setLastPerigee(result[0]);
                keplerometerBlockEntity.setLastApogee(result[1]);
            }

            PacketDistributor.sendToPlayer((ServerPlayer) ctx.player(), new Clientbound(packet.blockEntity, packet.sublevel, position, velocity));
        }

        @Override
        public @NotNull String toString() {
            return "KeplerometerSublevelDataPacketServerbound {\n   blockEntity = %s\n  sublevel = %s\n}".formatted(blockEntity.toString(), sublevel.toString());
        }
    }
    public record Clientbound(BlockPos blockEntity, UUID sublevel, Vector3f position, Vector3f velocity) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Clientbound> TYPE = new Type<>(Deepspace.path("keplerometer_sublevel_data_clientbound"));
        public static final StreamCodec<FriendlyByteBuf, Clientbound> STREAM_CODEC = StreamCodec.ofMember(
                Clientbound::encode,
                Clientbound::decode
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
        public static void encode(@NotNull Clientbound packet, @NotNull FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.blockEntity);
            buffer.writeUUID(packet.sublevel);
            buffer.writeVector3f(packet.position);
            buffer.writeVector3f(packet.velocity);
        }
        public static @NotNull Clientbound decode(@NotNull FriendlyByteBuf buffer) {
            return new Clientbound(
                    buffer.readBlockPos(),
                    buffer.readUUID(),
                    buffer.readVector3f(),
                    buffer.readVector3f()
            );
        }
        public static void handle(@NotNull Clientbound packet, @NotNull IPayloadContext ctx) {
            var level = ctx.player().level();
            if (!level.dimension().location().equals(Deepspace.path("space")))
                return;
            var container = SubLevelContainer.getContainer(level);
            if (container == null) return;
            var sublevel = container.getSubLevel(packet.sublevel);
            if (sublevel == null) return;
            var blockEntity = level.getBlockEntity(packet.blockEntity);
            if (blockEntity instanceof KeplerometerBlockEntity keplerometerBlockEntity) {
                var pos = Util.fromVector3f(packet.position);
                var planet = PlanetRegistry.getClosestPlanet(pos);
                if (planet == null) return;
                float[] result = Util.calculateOrbitData(
                        planet.getCenter(),
                        pos,
                        Util.fromVector3f(packet.velocity),
                        (float) planet.getBoundingBoxMax().distanceTo(planet.getBoundingBoxMin())
                );
                keplerometerBlockEntity.setLastPerigee(result[0]);
                keplerometerBlockEntity.setLastApogee(result[1]);
            }
        }

        @Override
        public @NotNull String toString() {
            return "KeplerometerSublevelDataPacketClientbound {\n\tblockEntity = %s\n\tsublevel = %s\n\tposition = %s\n\tvelocity = %s\n}".formatted(blockEntity.toString(), sublevel.toString(), position.toString(), velocity.toString());
        }
    }
}
