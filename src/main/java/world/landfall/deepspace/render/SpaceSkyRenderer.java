package world.landfall.deepspace.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import foundry.veil.api.event.VeilRenderLevelStageEvent;

import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import world.landfall.deepspace.Deepspace;

import world.landfall.deepspace.integration.IrisIntegration;
import world.landfall.deepspace.render.shapes.Sphere;


public class SpaceSkyRenderer {
    private static final Sphere skySphere = new Sphere(30, 32, 64);
    private static final ResourceLocation SPACE_SKY_TEXTURE = ResourceLocation.fromNamespaceAndPath(Deepspace.MODID, "textures/space_sky.png");
    private static final ResourceLocation SPACE_SKY_SHADER = Deepspace.path("space_sky");
    private static final ShaderStateShard SPACE_SKY_RENDER_TYPE = new ShaderStateShard(() -> {
        ShaderProgram shader = VeilRenderSystem.setShader(SPACE_SKY_SHADER);
        return VeilRenderBridge.toShaderInstance(shader);
    });

    private static RenderType skyShaderType(ResourceLocation texture) {
        var renderType = RenderType.CompositeState.builder()
                .setShaderState(SPACE_SKY_RENDER_TYPE)
                .setTextureState(new RenderStateShard.TextureStateShard(texture,false,false))
                .setCullState(RenderStateShard.NO_CULL)
                .setLayeringState(RenderStateShard.LayeringStateShard.VIEW_OFFSET_Z_LAYERING)
                .setWriteMaskState(RenderStateShard.WriteMaskStateShard.COLOR_WRITE)
                .createCompositeState(true);
        return RenderType.create(
                "space_sky",
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.TRIANGLES,
                786432, true, false,
                renderType
        );
    }
    public static void render(
            VeilRenderLevelStageEvent.Stage stage,
            LevelRenderer levelRenderer,
            MultiBufferSource.BufferSource bufferSource,
            MatrixStack matrixStack,
            Matrix4fc frustumMatrix,
            Matrix4fc projectionMatrix,
            int renderTick,
            DeltaTracker partialTicks,
            Camera camera,
            Frustum frustum
    ) {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        var instance = Minecraft.getInstance();
//        if (!instance.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Deepspace.MODID,"space")))
//            return;
        var dim = instance.level.dimension().location();
        if (dim.equals(ResourceLocation.parse("minecraft:overworld")))
            return;
        var in_sarrion = dim.equals(ResourceLocation.parse("deepspace:sarrion"));
        RenderType renderType = skyShaderType(SPACE_SKY_TEXTURE);
        var poseStack = matrixStack.toPoseStack();
        poseStack.pushPose();
        skySphere.render(poseStack, builder, new Vector3f(), new Quaternionf(camera.rotation()).invert());

        IrisIntegration.bindPipeline();
        RenderSystem.setShaderColor(0f, 0f, 0f, 0f);
        if (in_sarrion)
            RenderSystem.setShaderColor(.4f, .1f, .1f, 1f);
        renderType.draw(builder.buildOrThrow());


        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        //VeilRenderType.endGateway().draw(builder.buildOrThrow());
        bufferSource.endBatch(renderType);
        poseStack.popPose();

    }
    public static void init() {

//        SpaceRenderSystem.registerRenderer(SpaceSkyRenderer::render, VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS);
        SpaceRenderSystem.registerRenderer(SpaceSkyRenderer::render, VeilRenderLevelStageEvent.Stage.AFTER_SKY);
    }
}