package world.landfall.deepspace;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import world.landfall.deepspace.planttype.PlantType;

@EventBusSubscriber(modid = Deepspace.MODID)
public class ModRegistries {
    public static final ResourceKey<Registry<PlantType>> PLANT_TYPE_KEY = ResourceKey.createRegistryKey(Deepspace.path("plant_type_registry"));
    public static final Registry<PlantType> PLANT_TYPES = new RegistryBuilder<PlantType>(PLANT_TYPE_KEY).sync(true).create();
    @SubscribeEvent
    public static void createRegistries(NewRegistryEvent e) {
        e.register(PLANT_TYPES);
    }
}
