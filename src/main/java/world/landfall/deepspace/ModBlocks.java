package world.landfall.deepspace;

import com.simibubi.create.AllItems;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import world.landfall.deepspace.block.*;

import java.util.List;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Deepspace.MODID);
    public static final DeferredBlock<Block> ANGEL_BLOCK = BLOCKS.register("angel_block", () -> new AngelBlock(BlockBehaviour.Properties.of()));
    public static final DeferredBlock<Block> OXYGENATOR_BLOCK = BLOCKS.register("oxygenator", OxygenatorBlock::new);
    public static final DeferredBlock<Block> GROUND_SOLAR_PANEL_BLOCK = BLOCKS.register("ground_solar_panel", GroundSolarPanelBlock::new);
    public static final DeferredBlock<Block> MOONSTONE_ZINC_ORE_BLOCK = BLOCKS.register("moonstone_zinc_ore_block", () -> new MoonstoneOreBlock(AllItems.RAW_ZINC));
    public static final DeferredBlock<Block> MOONSTONE_QUARTZ_ORE_BLOCK = BLOCKS.register("moonstone_quartz_ore_block", () -> new MoonstoneOreBlock(() -> Items.QUARTZ));
    public static final DeferredBlock<Block> MOONSTONE_IRON_ORE_BLOCK = BLOCKS.register("moonstone_iron_ore_block", () -> new MoonstoneOreBlock(() -> Items.RAW_IRON));
    public static final DeferredBlock<Block> MOONSTONE_GOLD_ORE_BLOCK = BLOCKS.register("moonstone_gold_ore_block", () -> new MoonstoneOreBlock(() -> Items.RAW_GOLD));
    public static final DeferredBlock<Block> MOONSTONE_SILICON_ORE_BLOCK = BLOCKS.register("moonstone_silicon_ore_block", () -> new MoonstoneOreBlock(ModItems.RAW_SILICON_ITEM::asItem));
    public static final DeferredBlock<Block> SILICON_BLOCK = BLOCKS.register("silicon_block", () -> new Block(BlockBehaviour.Properties.of()
            .requiresCorrectToolForDrops()
            .destroyTime(4)
            .isRedstoneConductor((state, getter, pos) -> true)
    ));
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

            .lightLevel(state -> 1),
            Shapes.create(new AABB(0, 0, 0, 1, 2/16., 1))
    );
    public static final DeferredBlock<Block> PICKLE_MOSS_BLOCK = makePickleBlock("pickle_moss_block", BlockBehaviour.Properties.of());
    public static final DeferredBlock<Block> PICKLE_CORE_BLOCK = BLOCKS.register("pickle_core_block", () -> new PicklePlantBlock(BlockBehaviour.Properties.of()) {
        @Override
        public int spreadRadius() {
            return 5;
        }

        @Override
        public float spreadSpeed() {
            return .9f;
        }
    });

    public static final DeferredBlock<Block> SELENIC_GRASS_BLOCK = makeSelenicBlock("selenic_grass_block", BlockBehaviour.Properties.of());
    public static final DeferredBlock<Block> SELENIC_VINE_BLOCK = makeUnstableSelenicBlock("selenic_vine_block", BlockBehaviour.Properties.of(),
            Shapes.create(new AABB(0, 0, 0, 1, 2/16., 1))
    );
    public static final DeferredBlock<Block> SELENIC_FAUNA_BLOCK = makeUnstableSelenicBlock("selenic_fauna_block", BlockBehaviour.Properties.of(),
            Shapes.create(new AABB(1/16., 0, 1/16., 15/16., 15/16., 15/16.))
    );
    public static final DeferredBlock<Block> SELENIC_CORE_BLOCK = BLOCKS.register("selenic_core_block", () -> new SelenicPlantBlock(BlockBehaviour.Properties.of()) {
        @Override
        public int spreadRadius() {
            return 5;
        }

        @Override
        public float spreadSpeed() {
            return .6f;
        }
    });

    public static DeferredBlock<Block> makePickleBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new PicklePlantBlock(properties));
    }
    public static DeferredBlock<Block> makeUnstablePickleBlock(String name, BlockBehaviour.Properties properties, VoxelShape shape) {
        return BLOCKS.register(name, () -> new PicklePlantBlock(properties.instabreak()
                .dynamicShape()
                .replaceable()
                .noCollission()
                .requiresCorrectToolForDrops()) {
            @Override
            protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
                var below = level.getBlockState(pos.below());
                return !below.canBeReplaced() && !below.is(Blocks.AIR) && !below.getBlock().hasDynamicShape();
            }

            @Override
            protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                return shape;
            }

            @Override
            protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
                return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
            }

            @Override
            protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
                return List.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse(name)).getDefaultInstance());
            }
        });
    }
    public static DeferredBlock<Block> makeSelenicBlock(String name, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new SelenicPlantBlock(properties));
    }
    public static DeferredBlock<Block> makeUnstableSelenicBlock(String name, BlockBehaviour.Properties properties, VoxelShape shape) {
        return BLOCKS.register(name, () -> new SelenicPlantBlock(properties.instabreak()
                .dynamicShape()
                .replaceable()
                .noCollission()) {
            @Override
            protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
                var below = level.getBlockState(pos.below());
                return !below.canBeReplaced() && !below.is(Blocks.AIR) && !below.getBlock().hasDynamicShape();
            }

            @Override
            protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                return shape;
            }

            @Override
            protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
                return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
            }
            @Override
            protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
                return List.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse(name)).getDefaultInstance());
            }
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
