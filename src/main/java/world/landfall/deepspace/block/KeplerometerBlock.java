package world.landfall.deepspace.block;

import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import world.landfall.deepspace.ModBlockEntities;
import world.landfall.deepspace.blockentity.KeplerometerBlockEntity;

public class KeplerometerBlock extends Block implements EntityBlock {
    public KeplerometerBlock(Properties properties) {
        super(properties
                .destroyTime(2f)
                .strength(3.5f)
                .noOcclusion());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.KEPLEROMETER_BLOCK_ENTITY_TYPE.get().create(blockPos, blockState);
    }
    private static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> type, BlockEntityType<E> checkedType, BlockEntityTicker<? super E> ticker
    ) {
        return checkedType == type ? (BlockEntityTicker<A>) ticker : null;
    }
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return KeplerometerBlockEntity::tick;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
//        return super.getSignal(state, level, pos, direction);
        var blockEntity = (KeplerometerBlockEntity) level.getBlockEntity(pos);
        if (blockEntity == null) return 0;
        if (blockEntity.getLastApogee() == 0)
            return 0;
        if (blockEntity.willCrash())
            return 15;
        return 0;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.create(new AABB(0, 0, 0, 1, 10d/16d, 1));
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        var blockEntity = (KeplerometerBlockEntity) level.getBlockEntity(pos);
        if (blockEntity == null) return;
        blockEntity.updateNeighbors();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        var blockEntity = (KeplerometerBlockEntity) level.getBlockEntity(pos);
        if (blockEntity == null) return;
        blockEntity.updateNeighbors();

    }
}
