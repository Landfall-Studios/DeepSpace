package world.landfall.deepspace.integration;


import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import world.landfall.deepspace.Deepspace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ConfigEntryPointForge("deepspace")
public class SodiumIntegration implements ConfigEntryPoint {
    private final DeepspaceOptionsStorage storage = new DeepspaceOptionsStorage();
    private final StorageEventHandler handler = storage::save;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        var allowedValues = new HashSet<DeepspaceOptions.Detail>();
        allowedValues.addAll(Arrays.stream(DeepspaceOptions.Detail.values()).toList());
        builder.registerModOptions("deepspace")
                .setIcon(Deepspace.path("textures/sarrion.png"))
                .addPage(builder.createOptionPage()
                        .setName(Component.literal("Deepspace"))
                        .addOption(builder.createEnumOption(Deepspace.path("decoration_detail"), DeepspaceOptions.Detail.class)
                                .setAllowedValues(allowedValues)
                                .setElementNameProvider(d -> Component.literal(d.name()))
                                .setName(Component.translatable("options.deepspace.decorationDetail"))
                                .setTooltip(Component.translatable("options.deepspace.decorationDetail.tooltip"))
                                .setImpact(OptionImpact.MEDIUM)
                                .setStorageHandler(this.handler)
                                .setDefaultValue(DeepspaceOptions.Detail.EXPENSIVE)
                                .setBinding((value) -> storage.getData().atmosphereDetail = value, () -> storage.getData().atmosphereDetail))
                        .addOption(builder.createEnumOption(Deepspace.path("shading_detail"), DeepspaceOptions.Detail.class)
                                .setAllowedValues(allowedValues)
                                .setElementNameProvider(d -> Component.literal(d.name()))
                                .setName(Component.translatable("options.deepspace.shadingDetail"))
                                .setTooltip(Component.translatable("options.deepspace.shadingDetail.tooltip"))
                                .setImpact(OptionImpact.MEDIUM)
                                .setStorageHandler(this.handler)
                                .setDefaultValue(DeepspaceOptions.Detail.EXPENSIVE)
                                .setBinding((value) -> storage.getData().shadingDetail = value, () -> storage.getData().shadingDetail))
                );
    }


}
