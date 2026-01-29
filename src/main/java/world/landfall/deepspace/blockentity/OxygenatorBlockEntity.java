package world.landfall.deepspace.blockentity;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.types.Type;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.foundation.utility.CreateLang;
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
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModAttatchments;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.block.OxygenatorBlock;
import world.landfall.deepspace.integration.IrisIntegration;
import world.landfall.deepspace.render.shapes.Sphere;

import java.util.List;
import java.util.Set;

public class OxygenatorBlockEntity extends KineticBlockEntity {
    public static final BlockEntityType<OxygenatorBlockEntity> TYPE = BlockEntityType.Builder.of(
            OxygenatorBlockEntity::new,
            ModBlocks.OXYGENATOR_BLOCK.get()
    ).build(null);
    private boolean enabled = false;
    private int radius = 5;
    public OxygenatorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        var vPos = pos.getCenter().toVector3f();

    }

    @Override
    public void onLoad() {
        super.onLoad();

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
        CreateLang.text(" " + this.lastStressApplied + "SU")
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
        var corner1 = pos.offset(radius, radius, radius);
        var corner2 = pos.offset(-radius, -radius, -radius);
        var ticks = blockEntity.lazyTickCounter;
        if (ticks % 10 != 0)
            return;

//        var newstate = state
//                .setValue(OxygenatorBlock.ACTIVE, Math.abs(blockEntity.speed) >= 4f)
//                .setValue(OxygenatorBlock.RADIUS, Math.clamp((int)(blockEntity.speed / 2f),5, 30));

        blockEntity.enabled = (Math.abs(blockEntity.speed) >= 4f) && !blockEntity.overStressed;
        blockEntity.radius = Math.clamp((int)(Math.abs(blockEntity.speed) / 2f), 4, 32);
        level.getNearbyPlayers(TargetingConditions.DEFAULT, null, AABB.of(BoundingBox.fromCorners(
                corner1,
                corner2
        ))).forEach((player) -> {
            if (blockEntity.enabled && player.position().distanceTo(pos.getCenter()) < radius) {
                player.setData(ModAttatchments.LAST_OXYGENATED, 0f);
            }
        });

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
                    .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
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
            var mesh = new Sphere(oxygenatorBlockEntity.radius, 32, 32);
            var cam = Minecraft.getInstance().gameRenderer.getMainCamera();
            var type = type(IrisIntegration.isShaderPackEnabled());
            var shaftBuf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            var buf = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
//            ShaftRenderer.renderRotatingKineticBlock(oxygenatorBlockEntity, state, poseStack, shaftBuf, i);
            VeilRenderSystem.setShader(Deepspace.path("bubble"));
            var enabled = oxygenatorBlockEntity.enabled;
            var TIME_UNIFORM = VeilRenderSystem.getShader().getOrCreateUniform("Time");
            TIME_UNIFORM.setFloat((oxygenatorBlockEntity.level.getDayTime() + v) / 2f);
            var fakeShaft = AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, state.getValue(BlockStateProperties.AXIS));
            KineticBlockEntityRenderer.renderRotatingKineticBlock(oxygenatorBlockEntity, fakeShaft, poseStack, shaftBuf, i);

            getRenderType(oxygenatorBlockEntity, fakeShaft).draw(shaftBuf.buildOrThrow());
            if (!enabled)
                return;
            RenderSystem.setShaderTexture(0, Deepspace.path("textures/atmosphere.png"));
            poseStack.pushPose();
            mesh.render(poseStack, buf, oxygenatorBlockEntity.worldPosition.getCenter().toVector3f().sub(cam.getPosition().toVector3f()), new Quaternionf());
            type.draw(buf.buildOrThrow());

//            super.renderSafe(oxygenatorBlockEntity, v, poseStack, multiBufferSource, i, i1);
            poseStack.popPose();


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
