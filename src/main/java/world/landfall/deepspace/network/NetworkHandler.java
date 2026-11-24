package world.landfall.deepspace.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import world.landfall.deepspace.Deepspace;

/**
 * Handles network packet registration and management
 */
@EventBusSubscriber(modid = Deepspace.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    
    /**
     * Registers network packets during the mod initialization phase.
     *
     * @param event The payload registration event
     */
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        
        // Register planet sync packet
        registrar.playToClient(
            PlanetSyncPacket.TYPE,
            PlanetSyncPacket.STREAM_CODEC,
            PlanetSyncPacket::handle
        );

        registrar.playToServer(
                KeplerometerSublevelDataPacket.Serverbound.TYPE,
                KeplerometerSublevelDataPacket.Serverbound.STREAM_CODEC,
                KeplerometerSublevelDataPacket.Serverbound::handle
        );

        registrar.playToClient(
                KeplerometerSublevelDataPacket.Clientbound.TYPE,
                KeplerometerSublevelDataPacket.Clientbound.STREAM_CODEC,
                KeplerometerSublevelDataPacket.Clientbound::handle
        );

        registrar.playToServer(
                JetpackPacket.RocketForward.TYPE,
                JetpackPacket.RocketForward.STREAM_CODEC,
                JetpackPacket.RocketForward::handle
        );
        registrar.playToServer(
                JetpackPacket.BeginFlying.TYPE,
                JetpackPacket.BeginFlying.STREAM_CODEC,
                JetpackPacket.BeginFlying::handle
        );
    }
} 