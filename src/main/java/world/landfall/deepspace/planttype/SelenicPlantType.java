package world.landfall.deepspace.planttype;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import world.landfall.deepspace.ModBlocks;

import java.util.List;
import java.util.Optional;

public class SelenicPlantType extends PlantType {
    public SelenicPlantType() {
        // !TODO add list
        super(List.of("deepspace:selenic_grass_block").stream().map(ResourceLocation::parse).toList());
    }

    @Override
    public Optional<BlockState> convert(BlockState in, Level level, BlockPos pos) {
        if (in.is(ModBlocks.LUNAR_SOIL))
            return Optional.of(ModBlocks.SELENIC_GRASS_BLOCK.get().defaultBlockState());
        return Optional.empty();
    }
}
