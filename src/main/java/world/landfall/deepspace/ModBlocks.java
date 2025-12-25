package world.landfall.deepspace;

import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import world.landfall.deepspace.block.*;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Deepspace.MODID);
    public static final DeferredBlock<Block> ANGEL_BLOCK = BLOCKS.register("angel_block", () -> new AngelBlock(BlockBehaviour.Properties.of()));
    public static final DeferredBlock<Block> OXYGENATOR_BLOCK = BLOCKS.register("oxygenator", OxygenatorBlock::new);
    public static final DeferredBlock<Block> MOONSTONE_ZINC_ORE_BLOCK = BLOCKS.register("moonstone_zinc_ore_block", () -> new MoonstoneOreBlock(AllItems.RAW_ZINC));
    public static final DeferredBlock<Block> MOONSTONE_QUARTZ_ORE_BLOCK = BLOCKS.register("moonstone_quartz_ore_block", () -> new MoonstoneOreBlock(() -> Items.QUARTZ));
    public static final DeferredBlock<Block> MOONSTONE_IRON_ORE_BLOCK = BLOCKS.register("moonstone_iron_ore_block", () -> new MoonstoneOreBlock(() -> Items.RAW_IRON));
    public static final DeferredBlock<Block> MOONSTONE_GOLD_ORE_BLOCK = BLOCKS.register("moonstone_gold_ore_block", () -> new MoonstoneOreBlock(() -> Items.RAW_GOLD));
    public static final DeferredBlock<Block> LUNAR_SOIL = BLOCKS.register("lunar_soil", () -> new LunarSoilBlock(BlockBehaviour.Properties.of()
            .strength(1, 1)
            .sound(SoundType.SAND)
            .mapColor(MapColor.COLOR_LIGHT_GRAY)
            .speedFactor(.7f)
    ));
    public static final DeferredBlock<Block> LUNAR_COBBLE = BLOCKS.register("lunar_cobble", () -> new Block(BlockBehaviour.Properties.of()
            .strength(4, 3)
            .sound(SoundType.STONE)
            .mapColor(MapColor.COLOR_GRAY)
    ));
    public static final DeferredBlock<Block> MOON_STONE = BLOCKS.register("moonstone", () -> new Moonstone(BlockBehaviour.Properties.of()
            .strength(4, 3)
            .sound(SoundType.STONE)
            .mapColor(MapColor.COLOR_GRAY)
    ));

    public static final DeferredBlock<Block> PICKLE_VINE_BLOCK = makeUnstablePickleBlock("pickle_vine_block", BlockBehaviour.Properties.of()
            .instabreak()
            .dynamicShape()
            .replaceable()
            .lightLevel(state -> 1)
            .noCollission()
    );
    public static final DeferredBlock<Block> PICKLE_MOSS_BLOCK = makePickleBlock("pickle_moss_block", BlockBehaviour.Properties.of()
            .replaceable()
    );

    public static DeferredBlock<Block> makePickleBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new PicklePlantBlock(properties));
    }
    public static DeferredBlock<Block> makeUnstablePickleBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new PicklePlantBlock(properties) {
            @Override
            protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
                var below = level.getBlockState(pos.below());
                return !below.canBeReplaced() && !below.is(Blocks.AIR) && !below.getBlock().hasDynamicShape();
            }
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
