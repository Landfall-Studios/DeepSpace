package world.landfall.deepspace.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.block.HeatPipeBlock;

public class HeatPipeBlockEntity extends BlockEntity implements HeatTransferable {
    private float lastHeat;
    private float newHeat;
    public static final BlockEntityType<HeatPipeBlockEntity> TYPE = BlockEntityType.Builder.of(
            HeatPipeBlockEntity::new,
            ModBlocks.HEAT_PIPE_BLOCK.get()
    ).build(null);
    public HeatPipeBlockEntity(BlockPos pos, BlockState blockState) {
        super(TYPE, pos, blockState);
        lastHeat = 0;
        newHeat = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("lastHeat", FloatTag.valueOf(lastHeat));
        tag.put("newHeat", FloatTag.valueOf(newHeat));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tag.contains("lastHeat") || !tag.contains("newHeat"))
            return;
        lastHeat = ((FloatTag)tag.get("lastHeat")).getAsFloat();
        newHeat = ((FloatTag)tag.get("newHeat")).getAsFloat();
    }

    @Override
    public void addHeat(float celc) {
        newHeat+=celc;
    }

    @Override
    public float getLastHeat() {
        return lastHeat;
    }
    public static void tick(Level level, BlockPos pos, BlockState state, HeatPipeBlockEntity blockEntity) {
        blockEntity.lastHeat = blockEntity.newHeat;
        var transfers = HeatPipeBlock.validTransferDirections(state, pos, level).toList();
        var numTransfers = transfers.size();
        var addFactor = blockEntity.lastHeat / (numTransfers + 1);
        blockEntity.newHeat = addFactor;
        transfers.forEach(dir -> {
            var ent = level.getBlockEntity(pos.relative(dir));
            if (ent instanceof HeatTransferable heatTransferable) {
                heatTransferable.addHeat(addFactor);
            }
        });
        var airBlocks = HeatPipeBlock.adjacentAirBlocks(state, pos, level);
        blockEntity.newHeat *= (float) (1-(airBlocks / 24.));
    }
    public static class Renderer implements BlockEntityRenderer<HeatPipeBlockEntity> {
        public Renderer(BlockEntityRendererProvider.Context context) {

        }
        @Override
        public void render(HeatPipeBlockEntity heatPipeBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {

        }
    }
}
