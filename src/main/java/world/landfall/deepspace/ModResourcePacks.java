package world.landfall.deepspace;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber
public class ModResourcePacks {
    @SubscribeEvent
    public static void onRegisterREsourcePacks(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addPackFinders(
                    Deepspace.path("resourcepacks/deepspace_particles"),
                    PackType.CLIENT_RESOURCES,
                    Component.literal("Deepspace Particle Assets"),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.BOTTOM
            );
        }
    }
}
