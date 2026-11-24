package world.landfall.deepspace.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.planet.Planet;
import world.landfall.deepspace.planet.PlanetRegistry;
import world.landfall.deepspace.planet.Sun;
import world.landfall.deepspace.render.PlanetDecorationsRenderer;
import world.landfall.deepspace.render.PlanetRenderer;
import world.landfall.deepspace.render.SunRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Network packet for synchronizing planet data from server to client.
 */
public record PlanetSyncPacket(List<Planet> planets, @Nullable Sun sun) implements CustomPacketPayload {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static final Type<PlanetSyncPacket> TYPE = new Type<>(
        Deepspace.path("planet_sync")
    );
    
    public static final StreamCodec<FriendlyByteBuf, PlanetSyncPacket> STREAM_CODEC = StreamCodec.ofMember(
        PlanetSyncPacket::encode,
        PlanetSyncPacket::decode
    );
    
    /**
     * Creates a new planet sync packet with the given planets.
     *
     * @param planets The planets to sync
     */
    public PlanetSyncPacket(@NotNull Collection<Planet> planets, @Nullable Sun sun) {
        this(new ArrayList<>(Objects.requireNonNull(planets, "Planets cannot be null")), sun);
    }
    
    /**
     * Creates a planet sync packet with all currently registered planets.
     *
     * @return A new sync packet
     */
    @NotNull
    public static PlanetSyncPacket createSyncPacket() {
        return new PlanetSyncPacket(PlanetRegistry.getAllPlanets(), PlanetRegistry.getSun());
    }
    
    /**
     * Encodes the packet data to the network buffer.
     *
     * @param packet The packet to encode
     * @param buffer The buffer to write to
     */
    public static void encode(@NotNull PlanetSyncPacket packet, @NotNull FriendlyByteBuf buffer) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        Objects.requireNonNull(buffer, "Buffer cannot be null");

        buffer.writeInt(packet.planets.size());
        for (Planet planet : packet.planets) {
            planet.toNetwork(buffer);
        }
        boolean hasSun = packet.sun != null;
        buffer.writeBoolean(hasSun);
        if (hasSun) {
            packet.sun.toNetwork(buffer);
        }
    }
    
    /**
     * Decodes packet data from the network buffer.
     *
     * @param buffer The buffer to read from
     * @return The decoded packet
     */
    @NotNull
    public static PlanetSyncPacket decode(@NotNull FriendlyByteBuf buffer) {
        Objects.requireNonNull(buffer, "Buffer cannot be null");
        
        int planetCount = buffer.readInt();
        List<Planet> planets = new ArrayList<>(planetCount);

        for (int i = 0; i < planetCount; i++) {
            planets.add(Planet.fromNetwork(buffer));
        }
        Sun sun = buffer.readBoolean() ? Sun.fromNetwork(buffer) : null;

        return new PlanetSyncPacket(planets, sun);
    }
    
    /**
     * Handles the packet on the client side.
     *
     * @param context The payload context
     */
    public static void handle(@NotNull PlanetSyncPacket packet, @NotNull IPayloadContext context) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        Objects.requireNonNull(context, "Context cannot be null");
        
        context.enqueueWork(() -> {
            // Clear existing planets and load the synced ones
            PlanetRegistry.clear();
            
            for (Planet planet : packet.planets) {
                PlanetRegistry.registerPlanet(planet);
            }
            LOGGER.info("Synchronized {} planets from server", packet.planets.size());
            PlanetRegistry.setSun(packet.sun);
            LOGGER.info("Synchronized sun from server");
            PlanetRenderer.refreshMeshes();
            PlanetDecorationsRenderer.refreshMeshes();
            SunRenderer.refreshMeshes();
        });
    }
    
    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
} 