package world.landfall.deepspace.planttype;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.block.SelenicVineBlock;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

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

        Function<BlockPos, Boolean> checkForFauna = (blockPos) -> level.getBlockState(pos).is(ModBlocks.SELENIC_FAUNA_BLOCK) || level.getBlockState(pos).is(ModBlocks.SELENIC_ROOTS_BLOCK);
        var countFauna = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (checkForFauna.apply(pos.offset(i-1, 0, j-1)))
                    countFauna++;
            }
        }

        var hasSurroundingFauna = vineCheckDirections.stream().anyMatch(d -> level.getBlockState(pos.relative(d)).is(ModBlocks.SELENIC_FAUNA_BLOCK) || level.getBlockState(pos.relative(d)).is(ModBlocks.SELENIC_ROOTS_BLOCK));
        var safeForVine = vineCheckDirections.stream().anyMatch(d -> !level.getBlockState(pos.relative(d)).propagatesSkylightDown(level, pos.relative(d)) || above.is(ModBlocks.SELENIC_VINE_BLOCK));
        if (below.is(ModBlocks.SELENIC_GRASS_BLOCK) && in.is(Blocks.AIR) && above.is(Blocks.AIR)) {
            if (level.random.nextFloat() > .3f && !hasSurroundingFauna && countFauna < 2 && (pos.getX() % 3 == 0 ^ pos.getZ() % 3 == 0))
                if (level.random.nextFloat() > 0.5f)
                    return Optional.of(ModBlocks.SELENIC_FAUNA_BLOCK.get().defaultBlockState());
                else
                    return Optional.of(ModBlocks.SELENIC_ROOTS_BLOCK.get().defaultBlockState());
        }
        if (safeForVine && in.is(Blocks.AIR) && level.random.nextFloat() > .8f) {
            var chosen = vineCheckDirections.stream().filter(d -> !level.getBlockState(pos.relative(d)).propagatesSkylightDown(level, pos.relative(d))).findFirst();
            if (chosen.isEmpty())
                if (above.is(ModBlocks.SELENIC_VINE_BLOCK))
                    chosen = Optional.of(above.getValue(SelenicVineBlock.FACING));
                else
                    chosen = Optional.of(Direction.NORTH);
            return Optional.of(ModBlocks.SELENIC_VINE_BLOCK.get().getStateDefinition().any()
                    .setValue(SelenicVineBlock.FACING, chosen.get()));
        }

        return Optional.empty();
    }
}
