package world.landfall.deepspace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
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

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !hasCollision;
    }
}
