package world.landfall.deepspace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.ModPlantTypes;

import java.util.List;

public class SelenicVineBlock extends SelenicPlantBlock {
    public static DirectionProperty FACING = DirectionProperty.create("facing");

    public SelenicVineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }


    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var above = level.getBlockState(pos.above());
        List<Direction> vineCheckDirections = List.of(
                Direction.NORTH,
                Direction.SOUTH,
                Direction.EAST,
                Direction.WEST
        );
        var safeForVine = vineCheckDirections.stream().anyMatch(d -> MultifaceBlock.canAttachTo(
                level, d.getOpposite(), pos.relative(d), level.getBlockState(pos.relative(d))
        )) || above.is(ModBlocks.SELENIC_VINE_BLOCK);
        return safeForVine;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
//        return Shapes.create(new AABB(0, 0, 0, 1, 2/16., 1));
        return switch(state.getValue(FACING)) {
            case SOUTH -> Shapes.create(new AABB(0, 0, 14/16., 1, 1, 1));
            case WEST -> Shapes.create(new AABB(0, 0, 0, 2/16., 1, 1));
            case NORTH -> Shapes.create(new AABB(0, 0, 0, 1, 1, 2/16.));
            case EAST -> Shapes.create(new AABB(14/16., 0, 0, 1, 1, 1));
            default -> Shapes.create(new AABB(0, 0, 0, 1, 1, 2/16.));
        };

    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, Direction.NORTH);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("selenic_vine_block")).getDefaultInstance());
    }
}
