package world.landfall.deepspace.block;

import com.simibubi.create.AllItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class MoonstoneOreBlock extends Block {
    private final Supplier<Item> ORE_ITEM;
    public MoonstoneOreBlock(Supplier<Item> item) {
        super(Properties.of()
                .strength(4, 3)
                .sound(SoundType.STONE));
        ORE_ITEM = item;
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.Builder params) {
        return params.getParameter(LootContextParams.TOOL).isCorrectToolForDrops(state) ?
                List.of(ORE_ITEM.get().getDefaultInstance()) :
                List.of();
    }
}
