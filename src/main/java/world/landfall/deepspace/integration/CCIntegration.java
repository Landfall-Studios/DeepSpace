package world.landfall.deepspace.integration;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import world.landfall.deepspace.ModBlockEntities;
import world.landfall.deepspace.blockentity.KeplerometerBlockEntity;
@EventBusSubscriber
public class CCIntegration {
    @SubscribeEvent
    public static void initCapability(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            PeripheralCapability.get(),
                ModBlockEntities.KEPLEROMETER_BLOCK_ENTITY_TYPE.get(),
                (be, ctx) -> new KeplerometerPeripheral((KeplerometerBlockEntity) be)
        );
    }
}
