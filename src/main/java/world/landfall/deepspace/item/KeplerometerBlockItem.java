package world.landfall.deepspace.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import world.landfall.deepspace.ModBlocks;

import java.util.List;
import java.util.Optional;

public class KeplerometerBlockItem extends BlockItem {
    public KeplerometerBlockItem(Properties properties) {
        super(ModBlocks.KEPLEROMETER_BLOCK.get(), properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(
                Component.literal("Hover with goggles to view the low and high points of an orbit.")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
        );
    }
}
