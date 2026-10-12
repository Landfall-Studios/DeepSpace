package world.landfall.deepspace.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import foundry.veil.Veil;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModOptions;
import world.landfall.deepspace.integration.IrisIntegration;
import world.landfall.deepspace.planet.PlanetRegistry;
import world.landfall.deepspace.render.shapes.Cube;

public class SunRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Cube MESH;
    private static final ResourceLocation TEXTURE = Deepspace.path("textures/sun.png");
    private static final ResourceLocation SUN_SHADER = Deepspace.path("sun");
    private static final RenderStateShard.ShaderStateShard SUN_RENDER_TYPE = new RenderStateShard.ShaderStateShard(() -> {
        ShaderProgram shader = VeilRenderSystem.setShader(SUN_SHADER);
        return VeilRenderBridge.toShaderInstance(shader);
    });
    private static final RenderType SUN_MAIN_TYPE = RenderType.create(
            "sun",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.TRIANGLES,
            786432, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(SUN_RENDER_TYPE)
                    .setOutputState(RenderStateShard.OutputStateShard.MAIN_TARGET)
                    .createCompositeState(true)
    );
    private static final RenderType SUN_BLOOM_INNER_TYPE = RenderType.create(
            "sun",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.TRIANGLES,
            786432, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(SUN_RENDER_TYPE)
                    .setOutputState(VeilRenderSystem.BLOOM_SHARD)
                    .createCompositeState(true)
    );
    private static final RenderType SUN_LAYERED = VeilRenderType.layered(SUN_MAIN_TYPE);
    private static final RenderType SUN_BLOOM_LAYERED = VeilRenderType.layered(SUN_MAIN_TYPE, SUN_BLOOM_INNER_TYPE);

    private static RenderType sunRenderType() {
        return SUN_LAYERED;
    }
    private static RenderType sunBloomRenderType() {
        return SUN_BLOOM_LAYERED;
    }
    public static void refreshMeshes() {
        var sun = PlanetRegistry.getSun();
        if (sun == null) return;
        MESH = new Cube(sun.getBoundingBoxMin().toVector3f(), sun.getBoundingBoxMax().toVector3f(), 1f, false);
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
        var instance = Minecraft.getInstance();
//        if (!instance.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Deepspace.MODID,"space")))
//            return;

        var dim = instance.level.dimension().location();
        if (dim.equals(ResourceLocation.parse("minecraft:overworld")))
            return;
        // MESH is populated from the sun in planets.json when the sync packet arrives.
        // No sun defined (or sync not yet delivered) means there is nothing to draw here.
        if (MESH == null)
            return;
        var in_skybox = dim.equals(Deepspace.path("sarrion")) || dim.equals(Deepspace.path("luna"));

        RenderType sunRenderType = sunRenderType();
        var poseStack = matrixStack.toPoseStack();
        poseStack.pushPose();
        BufferBuilder sunBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
        var time = instance.level.getTimeOfDay(partialTicks.getGameTimeDeltaTicks());
        var x = (float)Math.cos(time * 2 * 3.14159f);
        var y = (float)Math.sin(time * 2 * 3.14159f);
        float dist = in_skybox ? (float) PlanetRegistry.getPlanetByDimension(instance.level.dimension()).getCenter().distanceTo(PlanetRegistry.getSun().getCenter()): 0f;
        var pos = in_skybox ?
            new Vector3f(-dist * y, dist * x, 300f * y).sub(MESH.center).add(new Vector3f(0, -MESH.radius, 0)):
            camera.getPosition().toVector3f().mul(-1);
        MESH.render(poseStack, sunBuilder, pos, new Quaternionf());
        VeilRenderSystem.setShader(Deepspace.path("sun"));
        RenderSystem.setShaderTexture(0, TEXTURE);
        var overloadedColor = 2f;
        if (IrisIntegration.isShaderPackEnabled())
            RenderSystem.setShaderColor(overloadedColor, overloadedColor, overloadedColor, 3f);
        IrisIntegration.bindPipeline();
        switch (ModOptions.options().atmosphereDetail) {
            case NONE, BASIC -> sunRenderType.draw(sunBuilder.buildOrThrow());
            case EXPENSIVE -> sunBloomRenderType().draw(sunBuilder.buildOrThrow());
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        poseStack.popPose();
    }
    public static void init() {
        refreshMeshes();
        SpaceRenderSystem.registerRenderer(SunRenderer::render, VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS);
    }


}
