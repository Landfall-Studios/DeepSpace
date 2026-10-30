package world.landfall.deepspace;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import world.landfall.deepspace.feature.TemplateTreeFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, Deepspace.MODID);
    public static final DeferredHolder<Feature<?>, TemplateTreeFeature> TEMPLATE_TREE = FEATURES.register(
            "template_tree", TemplateTreeFeature::new
    );
    public static void init(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
