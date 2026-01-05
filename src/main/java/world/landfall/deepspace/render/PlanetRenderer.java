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
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModOptions;
import world.landfall.deepspace.integration.IrisIntegration;
import world.landfall.deepspace.planet.PlanetRegistry;
import world.landfall.deepspace.render.shapes.Cube;

import java.util.HashMap;
import org.slf4j.Logger;

public class PlanetRenderer {

    private static final Logger logger = LogUtils.getLogger();

    private static final HashMap<String, Cube> MESHES = new HashMap<>();
    private static final HashMap<String, ResourceLocation> TEXTURES = new HashMap<>();
    private static final ResourceLocation PLANET_SHADER = Deepspace.path("planet");
    private static final RenderStateShard.ShaderStateShard PLANET_RENDER_TYPE = new RenderStateShard.ShaderStateShard(() -> {
        ShaderProgram shader = VeilRenderSystem.setShader(PLANET_SHADER);
        return VeilRenderBridge.toShaderInstance(shader);
    });
    private static final ResourceLocation PLANET_UNSHADED_SHADER = Deepspace.path("planet_unshaded");
    private static final RenderStateShard.ShaderStateShard PLANET_UNSHADED_RENDER_TYPE = new RenderStateShard.ShaderStateShard(() -> {
        ShaderProgram shader = VeilRenderSystem.setShader(PLANET_UNSHADED_SHADER);
        return VeilRenderBridge.toShaderInstance(shader);
    });

    public static RenderType planetRenderType() {
        var renderType = RenderType.CompositeState.builder()
                .setShaderState(PLANET_RENDER_TYPE)
                .createCompositeState(true);
        return RenderType.create(
                "planet",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.TRIANGLES,
                786432, true, false,
                renderType
        );
    }
    public static RenderType planetUnshadedRenderType() {
        var renderType = RenderType.CompositeState.builder()
                .setShaderState(PLANET_UNSHADED_RENDER_TYPE)
                .setCullState(RenderStateShard.CullStateShard.NO_CULL)
                .createCompositeState(true);
        return RenderType.create(
                "planet_unshaded",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.TRIANGLES,
                786432, true, false,
                renderType
        );
    }

    public static void refreshMeshes() {
        MESHES.clear();
        TEXTURES.clear();
        for (var x : PlanetRegistry.getAllPlanets()) {
            MESHES.put(x.getId(),new Cube(x.getBoundingBoxMin().toVector3f(), x.getBoundingBoxMax().toVector3f(), 1f, false));
            TEXTURES.put(x.getId(), Deepspace.path("textures/"+x.getId()+".png"));
            logger.info("Made mesh for planet {}",x.getName());
        }

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
        if (!instance.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Deepspace.MODID,"space")))
            return;
        var requiredStage = IrisIntegration.isShaderPackEnabled() ? VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS : VeilRenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS;
        if (!stage.equals(requiredStage))
            return;
        RenderType planetRenderType = planetRenderType();
        RenderType planetUnshadedRenderType = planetUnshadedRenderType();
        var poseStack = matrixStack.toPoseStack();
        for (var x : MESHES.entrySet()) {
            // Planet surface
            BufferBuilder planetBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            var texture = TEXTURES.get(x.getKey());

            var sun = PlanetRegistry.getSun();
            var center = sun.getCenter();
            VeilRenderSystem.setShader(Deepspace.path("planet"))
                    .getOrCreateUniform("SunPosition")
                    .setVector(center.toVector3f().sub(camera.getPosition().toVector3f()));

            var rot = IrisIntegration.isShaderPackEnabled() ? new Quaternionf() : camera.rotation();
            x.getValue().render(poseStack, planetBuilder, camera.getPosition().toVector3f().mul(-1), rot);

            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            IrisIntegration.bindPipeline();
            switch (ModOptions.options().shadingDetail) {
                case NONE -> planetUnshadedRenderType.draw(planetBuilder.buildOrThrow());
                case BASIC, EXPENSIVE -> planetRenderType.draw(planetBuilder.buildOrThrow());
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
    }
    public static void init() {
        refreshMeshes();
        SpaceRenderSystem.registerRenderer(PlanetRenderer::render, VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS);
        SpaceRenderSystem.registerRenderer(PlanetRenderer::render, VeilRenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS);

    }
    private static Matrix4f projectionMatrix(double fov, GameRenderer gameRenderer) {
        Matrix4f mat = new Matrix4f();
        return mat.perspective(
                (float)(fov * (float)(Math.PI / 180.0)),
                (float)gameRenderer.getMinecraft().getWindow().getWidth() / (float)gameRenderer.getMinecraft().getWindow().getHeight(),
                0.05f,
                gameRenderer.getDepthFar() * 4f
        );
    }
}
