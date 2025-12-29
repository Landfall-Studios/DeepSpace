package world.landfall.deepspace.planttype;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.block.PicklePlantBlock;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PicklePlantType extends PlantType {
    public PicklePlantType() {
        super(Arrays.stream(new String[] {
                "deepspace:pickle_grass_block", "deepspace:pickle_vine_block", "deepspace:pickle_moss_block"
        }).map(ResourceLocation::parse).toList());
    }

    @Override
    public Optional<BlockState> convert(BlockState in, Level level, BlockPos pos) {
        var below = level.getBlockState(pos.below());

        int num = (int)BlockPos.betweenClosedStream(pos.below(), pos.below(8)).map(level::getBlockState).filter(state ->
                BLOCKS.stream().anyMatch(block -> state.is(BuiltInRegistries.BLOCK.get(block)))).count();
        if (in.is(Blocks.AIR)) {
            if (MultifaceBlock.canAttachTo(level, Direction.DOWN, pos, below) && !below.getBlock().hasDynamicShape())
                return level.random.nextFloat() > .2f ? Optional.of(ModBlocks.PICKLE_VINE_BLOCK.get().defaultBlockState())
                        : Optional.of(Blocks.SEA_PICKLE.defaultBlockState()
                            .setValue(SeaPickleBlock.WATERLOGGED, false)
                            .setValue(SeaPickleBlock.PICKLES, level.random.nextIntBetweenInclusive(1, 4)));
        }
//        var chanceToDiscardUp = (float)(.95 * ((8-num)/8.) + 1 * ((num)/8.));
        if (in.is(ModBlocks.PICKLE_VINE_BLOCK) && level.random.nextFloat() > .95 && level.random.nextIntBetweenInclusive(0, 7) > num) {

                System.out.println(num);
            return Optional.of(ModBlocks.PICKLE_MOSS_BLOCK.get().defaultBlockState());
        }
        return Optional.empty();
    }
}
