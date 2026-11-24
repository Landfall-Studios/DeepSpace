package world.landfall.deepspace.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModItems;
import world.landfall.deepspace.item.SpaceNavigationItem;
@EventBusSubscriber
public class ClientRenderSetup {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                ItemProperties.register(
                        ModItems.NAVIGATOR_ITEM.get(),
                        ResourceLocation.fromNamespaceAndPath(Deepspace.MODID, "navigator_state"),
                        (stack, level, entity, seed) ->
                                stack.getOrDefault(SpaceNavigationItem.SpaceNavigationItemComponent.SUPPLIER.get(), new SpaceNavigationItem.SpaceNavigationItemComponent(
                                        SpaceNavigationItem.Mode.PROGRADE
                                )).mode().ordinal()
                )
        );
    }
}
