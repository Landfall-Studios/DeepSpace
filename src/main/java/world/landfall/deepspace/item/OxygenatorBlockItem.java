package world.landfall.deepspace.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.block.OxygenatorBlock;

public class OxygenatorBlockItem extends BlockItem {
    public OxygenatorBlockItem() {
        super(ModBlocks.OXYGENATOR_BLOCK.get(), new Properties().stacksTo(64));
    }
}
