package world.landfall.deepspace.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import world.landfall.deepspace.Deepspace;

@EventBusSubscriber(modid = Deepspace.MODID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent e) {
        var generator = e.getGenerator();
        generator.addProvider(e.includeServer(), new ModDatapackProvider(generator.getPackOutput(), e.getLookupProvider()));

    }
}
