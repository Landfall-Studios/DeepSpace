package world.landfall.deepspace.integration;

import world.landfall.deepspace.Config;
import world.landfall.deepspace.ModOptions;

public class DeepspaceOptionsStorage  {
    private final DeepspaceOptions options = ModOptions.options();
    public DeepspaceOptions getData() {
        return options;
    }

    public void save() {
        Config.PLANET_DECORATION_DETAIL.set(options.atmosphereDetail);
        Config.PLANET_SHADING_DETAIL.set(options.shadingDetail);
        Config.PLANET_SHADING_DETAIL.save();
        Config.PLANET_DECORATION_DETAIL.save();
    }
}
