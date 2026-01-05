package world.landfall.deepspace.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModBlocks;

import java.util.List;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> MOONSTONE_ZINC_ORE_KEY = registerKey("moonstone_zinc_ore");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        RuleTest moonstoneReplaceables = new BlockMatchTest(ModBlocks.MOON_STONE.get());

        List<OreConfiguration.TargetBlockState> moonstoneZincOres = List.of(
                OreConfiguration.target(moonstoneReplaceables, ModBlocks.MOONSTONE_ZINC_ORE_BLOCK.get().defaultBlockState())
        );
        register(ctx, MOONSTONE_ZINC_ORE_KEY, Feature.ORE, new OreConfiguration(moonstoneZincOres, 9));
    }
    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Deepspace.path(name));
    }
    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> ctx,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key,
                                                                                          F feature,
                                                                                          FC configuration) {
        ctx.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
