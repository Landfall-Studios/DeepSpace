package world.landfall.deepspace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import world.landfall.deepspace.ModBlockEntities;

public class KeplerometerBlock extends Block implements EntityBlock {
    public KeplerometerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.KEPLEROMETER_BLOCK_ENTITY_TYPE.get().create(blockPos, blockState);
    }
}
