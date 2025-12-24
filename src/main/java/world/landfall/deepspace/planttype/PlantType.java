package world.landfall.deepspace.planttype;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public abstract class PlantType {
    public final List<ResourceLocation> BLOCKS;
    public PlantType(List<ResourceLocation> BLOCKS) {
        this.BLOCKS = BLOCKS;
    }
    public abstract Optional<BlockState> convert(BlockState in, Level level, BlockPos pos);
}
