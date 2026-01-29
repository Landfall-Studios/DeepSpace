package world.landfall.deepspace.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2d;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.planet.PlanetRegistry;

public class GroundSolarPanelBlockEntity extends BlockEntity {
    private float angle;
    private float targetAngle;
    private static float MIN_ANGLE = -(float)Math.PI / 4;
    private static float MAX_ANGLE = -MIN_ANGLE;
    public static final BlockEntityType<OxygenatorBlockEntity> TYPE = BlockEntityType.Builder.of(
            OxygenatorBlockEntity::new,
            ModBlocks.GROUND_SOLAR_PANEL_BLOCK.get()
    ).build(null);
    public GroundSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        angle = 0;
        targetAngle = 0;
    }
    public static void tick(Level level, BlockPos pos, BlockState state, GroundSolarPanelBlockEntity entity) {
        var angleDiff = entity.targetAngle - entity.angle;
        entity.angle = entity.angle + angleDiff * .1f;
        if (level.dimension().location().equals(ResourceLocation.parse("deepspace:space"))) {
            var sun = PlanetRegistry.getSun();
            if (sun == null) return;
            var sunPos = sun.getCenter();
            var blockPos = pos.getCenter();
            var diff = sunPos.subtract(blockPos);
            var diff2D = new Vector2d(diff.x, diff.y);
            var sunAngle = diff2D.angle(new Vector2d(1, 0));
            entity.targetAngle = (float)sunAngle;
        }

    }
    public static class Renderer implements BlockEntityRenderer<GroundSolarPanelBlockEntity> {

        @Override
        public void render(GroundSolarPanelBlockEntity groundSolarPanelBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {

        }
    }

}
