package world.landfall.deepspace.blockentity;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.CullFrustum;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.client.render.light.renderer.LightRenderer;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.LightEventListener;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.slf4j.Logger;
import world.landfall.deepspace.ClientOxygenatorTracker;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModAttatchments;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.block.OxygenatorBlock;
import world.landfall.deepspace.integration.IrisIntegration;
import world.landfall.deepspace.render.shapes.Sphere;

import java.util.List;
import java.util.Set;

public class OxygenatorBlockEntity extends KineticBlockEntity {

    static Logger LOGGER = LogUtils.getLogger();
    public static final BlockEntityType<OxygenatorBlockEntity> TYPE = BlockEntityType.Builder.of(
            OxygenatorBlockEntity::new,
            ModBlocks.OXYGENATOR_BLOCK.get()
    ).build(null);
    private boolean enabled = false;
    private int radius = 5;
    private static float SPEED_INPUT_MULTIPLIER = 2f;
    public OxygenatorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        var vPos = pos.getCenter().toVector3f();

    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) {
            ClientOxygenatorTracker.add(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && level.isClientSide) {
            ClientOxygenatorTracker.remove(this);
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (level != null && level.isClientSide) {
            ClientOxygenatorTracker.remove(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null && level.isClientSide) {
            ClientOxygenatorTracker.remove(this);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
//        tooltip.addFirst(Component.literal("    Radius: " + this.radius + " blocks"));
        CreateLang.text("Kinetic Stats:")
                .forGoggles(tooltip);
        CreateLang.text("Radius: ")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.text(" " + radius + " blocks")
                .style(ChatFormatting.GOLD)
                .add(Component.literal(" at current speed").withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip);
        CreateLang.text("Kinetic Stress Impact: ")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.text("  %,dSU".formatted((int) (this.lastStressApplied * this.speed)))
                .style(ChatFormatting.AQUA)
                .add(Component.literal(" at current speed").withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        tick(this.level, this.worldPosition, level.getBlockState(this.worldPosition), this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OxygenatorBlockEntity blockEntity) {
        if (!state.is(ModBlocks.OXYGENATOR_BLOCK.get()))
            return;
        var radius = blockEntity.radius;
        var ticks = blockEntity.lazyTickCounter;
        if (ticks % 10 != 0)
            return;
        blockEntity.enabled = (Math.abs(blockEntity.speed) >= 4f) && !blockEntity.overStressed;
        var r = Math.pow(Math.abs(blockEntity.speed) * (3f / 4f) / Math.PI, 1f/3f);
        blockEntity.radius = Math.clamp(Math.round(Math.abs(r * SPEED_INPUT_MULTIPLIER)), 2, 16);
        level.players().forEach(p -> {
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(blockEntity.getLevel(), blockEntity.worldPosition);
            Vec3 realPos;
            if (subLevel != null){
                var pose = subLevel.logicalPose();
                realPos = pose.transformPosition(pos.getCenter());
            }else {
                realPos = pos.getCenter();
            }
            if (blockEntity.enabled && p.position().distanceTo(realPos) < radius) {
                p.setData(ModAttatchments.LAST_OXYGENATED, 0f);
                LOGGER.debug("Oxygenated Player");

            } else {
                LOGGER.debug("Too far away!");
            }
        });

    }
    public int getRadius() {
        return radius;
    }
    public boolean isEnabled() {
        return enabled;
    }

    public static class Renderer extends ShaftRenderer<OxygenatorBlockEntity> {


        public static final ResourceLocation BUBBLE_SHADER_LOC = Deepspace.path("bubble");
        public static final RenderStateShard.ShaderStateShard BUBBLE_SHADER_SHARD = new RenderStateShard.ShaderStateShard(() -> {
            ShaderProgram shader = VeilRenderSystem.setShader(BUBBLE_SHADER_LOC);
            return VeilRenderBridge.toShaderInstance(shader);
        });

        public Renderer(BlockEntityRendererProvider.Context context) {
            super(context);
        }


        private static RenderType type(boolean shaderPack) {
//            return RenderType.SOLID;
            var renderType = RenderType.CompositeState.builder()
                    .setShaderState(BUBBLE_SHADER_SHARD)
                    .setCullState(RenderStateShard.CullStateShard.NO_CULL)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setLayeringState(RenderStateShard.LayeringStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setWriteMaskState(RenderStateShard.WriteMaskStateShard.COLOR_WRITE)
                    .createCompositeState(true);
            var renderTypeShaderPack = RenderType.CompositeState.builder()
                    .setShaderState(BUBBLE_SHADER_SHARD)
                    .setCullState(RenderStateShard.CullStateShard.NO_CULL)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLayeringState(RenderStateShard.LayeringStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setWriteMaskState(RenderStateShard.WriteMaskStateShard.COLOR_WRITE)
                    .createCompositeState(true);
            return RenderType.create(
                    "bubble",
                    DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.TRIANGLES,
                    186432, true, false,
                    shaderPack ? renderTypeShaderPack : renderType
            );
        }


        @Override
        public void renderSafe(OxygenatorBlockEntity oxygenatorBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
            var state = oxygenatorBlockEntity.getBlockState();
            if (!state.is(ModBlocks.OXYGENATOR_BLOCK))
                return;
            var shaftBuf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            var fakeShaft = AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, state.getValue(BlockStateProperties.AXIS));
            KineticBlockEntityRenderer.renderRotatingKineticBlock(oxygenatorBlockEntity, fakeShaft, poseStack, shaftBuf, i);
            getRenderType(oxygenatorBlockEntity, fakeShaft).draw(shaftBuf.buildOrThrow());
        }

        @Override
        public boolean shouldRenderOffScreen(OxygenatorBlockEntity blockEntity) {
            return true;
        }

        @Override
        public int getViewDistance() {
            return 500;
        }
    }
}
