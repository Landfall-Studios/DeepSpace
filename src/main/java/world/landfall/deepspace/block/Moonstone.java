package world.landfall.deepspace.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.ModItems;

import java.util.List;

public class Moonstone extends Block {
    public Moonstone(Properties properties) {
        super(properties.requiresCorrectToolForDrops());
    }
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(ModItems.LUNAR_COBBLE_ITEM.toStack(1));

    }

}
