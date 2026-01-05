package world.landfall.deepspace.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import world.landfall.deepspace.Deepspace;

import java.util.List;

public class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> MOONSTONE_ZINC_PLACED_KEY = registerKey("moonstone_zinc_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> ctx) {
        var features = ctx.lookup(Registries.CONFIGURED_FEATURE);
        register(ctx, MOONSTONE_ZINC_PLACED_KEY, features.getOrThrow(ModConfiguredFeatures.MOONSTONE_ZINC_ORE_KEY),
                ModOrePlacement.commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(80))));
    }
    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Deepspace.path(name));
    }
    private static void register(BootstrapContext<PlacedFeature> ctx,
                                 ResourceKey<PlacedFeature> key,
                                 Holder<ConfiguredFeature<?, ?>> config,
                                 List<PlacementModifier> modifiers) {
        ctx.register(key, new PlacedFeature(config, List.copyOf(modifiers)));
    }
}
