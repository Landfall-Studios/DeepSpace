package world.landfall.deepspace.planttype;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import world.landfall.deepspace.ModBlocks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PicklePlantType extends PlantType {
    public PicklePlantType() {
        super(Arrays.stream(new String[] {
                "deepspace:pickle_grass_block"
        }).map(ResourceLocation::parse).toList());
    }

    @Override
    public Optional<BlockState> convert(BlockState in, Level level, BlockPos pos) {
        var below = level.getBlockState(pos.below());
        if (in.is(Blocks.AIR)) {
            if (MultifaceBlock.canAttachTo(level, Direction.DOWN, pos, below) && !below.is(ModBlocks.PICKLE_VINE_BLOCK.get()))
                return Optional.of(ModBlocks.PICKLE_VINE_BLOCK.get().defaultBlockState());
        }
        return Optional.empty();
    }
}
