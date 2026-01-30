package world.landfall.deepspace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import world.landfall.deepspace.blockentity.GroundSolarPanelBlockEntity;
import world.landfall.deepspace.blockentity.HeatTransferable;

import java.util.stream.Stream;

public class GroundSolarPanelBlock extends Block implements EntityBlock {
    public GroundSolarPanelBlock() {
        super(Properties.of());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GroundSolarPanelBlockEntity(GroundSolarPanelBlockEntity.TYPE, blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (Level a, BlockPos b, BlockState c, T d) -> GroundSolarPanelBlockEntity.tick(a, b, c, (GroundSolarPanelBlockEntity) d);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {

    }
    public static Stream<Direction> validTransferDirections(BlockState state, BlockPos pos, Level level) {
        return Direction.stream().filter(direction -> {
            var relative = pos.relative(direction);
            var adjacentState = level.getBlockState(relative);
            return adjacentState.hasBlockEntity() && level.getBlockEntity(relative) instanceof HeatTransferable;
        });
    }


}
