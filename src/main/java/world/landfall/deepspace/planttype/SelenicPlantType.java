package world.landfall.deepspace.planttype;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import world.landfall.deepspace.ModBlocks;

import java.util.List;
import java.util.Optional;

public class SelenicPlantType extends PlantType {
    public SelenicPlantType() {
        // !TODO add list
        super(List.of(
                "deepspace:selenic_grass_block",
                "deepspace:selenic_fauna_block",
                "deepspace:selenic_core_block",
                "deepspace:selenic_vine_block"
        ).stream().map(ResourceLocation::parse).toList());
    }

    @Override
    public Optional<BlockState> convert(BlockState in, Level level, BlockPos pos) {
        var above = level.getBlockState(pos.above());
        var below = level.getBlockState(pos.below());
        if (in.is(ModBlocks.LUNAR_SOIL) && (above.is(Blocks.AIR) || above.is(ModBlocks.SELENIC_FAUNA_BLOCK)))
            return Optional.of(ModBlocks.SELENIC_GRASS_BLOCK.get().defaultBlockState());
        List<Direction> vineCheckDirections = List.of(
                Direction.NORTH,
                Direction.SOUTH,
                Direction.EAST,
                Direction.WEST
        );
        var hasSurroundingFauna = vineCheckDirections.stream().anyMatch(d -> level.getBlockState(pos.relative(d)).is(ModBlocks.SELENIC_FAUNA_BLOCK));
        var safeForVine = vineCheckDirections.stream().anyMatch(d -> MultifaceBlock.canAttachTo(
                level, d.getOpposite(), pos.relative(d), level.getBlockState(pos.relative(d))
        )) || above.is(ModBlocks.SELENIC_VINE_BLOCK);
        if (below.is(ModBlocks.SELENIC_GRASS_BLOCK) && in.is(Blocks.AIR) && above.is(Blocks.AIR)) {
            if (level.random.nextFloat() > .1f && !hasSurroundingFauna)
                return Optional.of(ModBlocks.SELENIC_FAUNA_BLOCK.get().defaultBlockState());
        }
        if (safeForVine && in.is(Blocks.AIR)) {
            return Optional.of(ModBlocks.SELENIC_VINE_BLOCK.get().defaultBlockState());
        }

        return Optional.empty();
    }
}
