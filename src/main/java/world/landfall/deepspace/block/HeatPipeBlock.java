package world.landfall.deepspace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import world.landfall.deepspace.blockentity.GroundSolarPanelBlockEntity;
import world.landfall.deepspace.blockentity.HeatPipeBlockEntity;
import world.landfall.deepspace.blockentity.HeatTransferable;

import java.util.List;
import java.util.stream.Stream;

public class HeatPipeBlock extends Block implements EntityBlock {

    public static final BooleanProperty INSULATED = BooleanProperty.create("insulated");

    public HeatPipeBlock() {
        super(Properties.of()

        );
        registerDefaultState(getStateDefinition().any()
                .setValue(INSULATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSULATED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (Level a, BlockPos b, BlockState c, T d) -> HeatPipeBlockEntity.tick(a, b, c, (HeatPipeBlockEntity) d);
    }
    public static Stream<Direction> validTransferDirections(BlockState state, BlockPos pos, Level level) {
        return Direction.stream().filter(direction -> {
            var relative = pos.relative(direction);
            var adjacentState = level.getBlockState(relative);
            return adjacentState.hasBlockEntity() && level.getBlockEntity(relative) instanceof HeatTransferable;
        });
    }
    public static int adjacentAirBlocks(BlockState state, BlockPos pos, Level level) {
        var insulated = state.getValue(INSULATED);
        // Magic shit idunno
        return insulated ? 0 : (int)Direction.stream().filter(direction -> {
            var relative = pos.relative(direction);
            var adjacentState = level.getBlockState(relative);
            return adjacentState.is(Blocks.AIR);
        }).count();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
