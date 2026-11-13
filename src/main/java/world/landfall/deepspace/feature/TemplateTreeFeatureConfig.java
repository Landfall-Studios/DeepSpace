package world.landfall.deepspace.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public record TemplateTreeFeatureConfig(List<ResourceLocation> templates) implements FeatureConfiguration {
    public static final Codec<TemplateTreeFeatureConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.listOf().fieldOf("templates").forGetter(TemplateTreeFeatureConfig::templates)
    ).apply(i, TemplateTreeFeatureConfig::new));
}
