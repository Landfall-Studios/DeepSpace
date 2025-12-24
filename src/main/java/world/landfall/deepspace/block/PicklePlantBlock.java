package world.landfall.deepspace.block;

import net.minecraft.resources.ResourceLocation;
import world.landfall.deepspace.ModPlantTypes;

public class PicklePlantBlock extends AbstractPlantBlock {
    public PicklePlantBlock(Properties properties) {
        super(properties, ModPlantTypes.PICKLE.getId());
    }

    @Override
    public float spreadSpeed() {
        return .2f;
    }

    @Override
    public int spreadRadius() {
        return 1;
    }
}
