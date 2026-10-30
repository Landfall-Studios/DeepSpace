package world.landfall.deepspace.feature;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

public class TemplateTreeFeature extends Feature<TemplateTreeFeatureConfig> {
    public TemplateTreeFeature() {
        super(TemplateTreeFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<TemplateTreeFeatureConfig> ctx) {
        var level = ctx.level();
        var random = ctx.random();
        var origin = ctx.origin();
        List<ResourceLocation> templates = ctx.config().templates();

        var id = templates.get(random.nextInt(templates.size()));
        var manager = level.getLevel().getServer().getStructureManager();
        Optional<StructureTemplate> opt = manager.get(id);
        if (opt.isEmpty()) return false;
        StructureTemplate template = opt.get();

        var rotation = Rotation.getRandom(random);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setRandom(random)
                .setIgnoreEntities(true);

        var size = template.getSize(rotation);
        var corner = origin.offset(-size.getX() / 2, 0, size.getZ() / 2);

        return template.placeInWorld(level, corner, corner, settings, random, 2);
    }
}
