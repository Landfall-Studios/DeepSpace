package world.landfall.deepspace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import world.landfall.deepspace.ModRegistries;
import world.landfall.deepspace.planttype.PlantType;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractPlantBlock extends Block {
    private final ResourceLocation PLANT_TYPE;
    public AbstractPlantBlock(Properties properties, ResourceLocation _PLANT_TYPE) {
        super(properties.randomTicks());
        PLANT_TYPE = _PLANT_TYPE;
    }

    public abstract float spreadSpeed();
    public abstract int spreadRadius();

    public PlantType getPlantType() {
        return ModRegistries.PLANT_TYPES.get(PLANT_TYPE);
    }
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (random.nextFloat()>spreadSpeed()) {
            for (int i = 0; i < 5; i++) {
                var rad = spreadRadius();
                int x = random.nextIntBetweenInclusive(-rad, rad);
                int y = random.nextIntBetweenInclusive(-1, 1);
                int z = random.nextIntBetweenInclusive(-rad, rad);
                if (x == 0 && y == 0 && z == 0)
                    return;
                var oldState = level.getBlockState(pos.offset(x, y, z));
                AtomicBoolean breakLoop = new AtomicBoolean(false);
                getPlantType().convert(oldState, level, pos.offset(x, y, z)).ifPresent((newState) -> {
                    level.setBlockAndUpdate(pos.offset(x, y, z), newState);
                    breakLoop.set(true);
                });
                if (breakLoop.get()) break;
            }
        }
    }
}
