package world.landfall.deepspace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.ModItems;

import java.util.List;

public class AngelBlock extends Block {
    public AngelBlock(Properties properties) {
        super(properties
                .destroyTime(2f)
                .strength(3.5f)
                .noOcclusion()
                .dynamicShape()
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.create(new AABB(1/16f,0,1/16f,15/16f,1f,15/16f));
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        //return super.getDrops(state, params);
        return List.of(new ItemStack(ModItems.ANGEL_BLOCK_ITEM.asItem()));

    }

}
