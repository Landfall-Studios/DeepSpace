package world.landfall.deepspace;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import world.landfall.deepspace.planttype.PicklePlantType;
import world.landfall.deepspace.planttype.PlantType;

import java.util.function.Supplier;

public class ModPlantTypes {
    public static final DeferredRegister<PlantType> TYPES = DeferredRegister.create(ModRegistries.PLANT_TYPES, Deepspace.MODID);
    public static final DeferredHolder<PlantType, PicklePlantType> PICKLE = TYPES.register("pickle", PicklePlantType::new);
    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
