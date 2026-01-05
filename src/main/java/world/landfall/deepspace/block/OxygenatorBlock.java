package world.landfall.deepspace.block;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import world.landfall.deepspace.ModBlockEntities;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.ModItems;
import world.landfall.deepspace.blockentity.OxygenatorBlockEntity;

import java.util.List;

public class OxygenatorBlock extends AbstractSimpleShaftBlock implements EntityBlock {
    public OxygenatorBlock() {
        super(Properties.of().noOcclusion()
                .strength(2)
        );

    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(ModItems.OXYGENATOR_BLOCK_ITEM.toStack(1));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.create(new AABB(1/16f,1/16f,1/16f,15/16f,15/16f,15/16f));
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        var ent = new OxygenatorBlockEntity(blockPos, blockState);
        var vPos = blockPos.getCenter();
//        var hasLightAlready = VeilRenderSystem.renderer().getLightRenderer().getLights(LightTypeRegistry.POINT.get()).stream().anyMatch(light ->
//                light.getLightData().getPosition().equals(new Vector3d(vPos.x, vPos.y, vPos.z),.5f));
//
//        if (!hasLightAlready) { // This is terrible
//            ent.LIGHT = VeilRenderSystem.renderer().getLightRenderer().addLight(new PointLightData()
//                    .setRadius(7)
//                    .setBrightness(5)
//                    .setPosition(new Vector3d(vPos.x, vPos.y, vPos.z))
//            );
//        }
        return ent;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
//        var ent = (OxygenatorBlockEntity)level.getBlockEntity(pos);
//        if (ent != null && ent.LIGHT != null)
//            ent.LIGHT.free();
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
//        var ent = (OxygenatorBlockEntity)level.getBlockEntity(pos);
//        if (ent != null && ent.LIGHT != null)
//            ent.LIGHT.free();
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return stateDefinition.any()
                .setValue(BlockStateProperties.WATERLOGGED, false)
                .setValue(BlockStateProperties.AXIS, context.getNearestLookingDirection().getAxis());
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return ModBlockEntities.OXYGENATOR_BLOCK_ENTITY_TYPE.get();
    }
}
