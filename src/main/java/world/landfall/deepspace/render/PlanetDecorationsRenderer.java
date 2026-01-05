package world.landfall.deepspace.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.units.qual.C;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModOptions;
import world.landfall.deepspace.integration.IrisIntegration;
import world.landfall.deepspace.planet.Planet;
import world.landfall.deepspace.planet.PlanetRegistry;
import world.landfall.deepspace.render.shapes.Cube;
import world.landfall.deepspace.render.shapes.Plane;

import java.awt.*;
import java.util.HashMap;

public class PlanetDecorationsRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<String, Ring> RING_MESHES = new HashMap<>();
    private static final ResourceLocation RING_SHADER = Deepspace.path("ring");
    private static final RenderStateShard.ShaderStateShard RING_RENDER_TYPE = new RenderStateShard.ShaderStateShard(() -> {
        ShaderProgram shader = VeilRenderSystem.setShader(RING_SHADER);
        return VeilRenderBridge.toShaderInstance(shader);
    });
    private static final HashMap<String, Atmosphere> ATMOSPHERE_MESHES = new HashMap<>();
    private static final ResourceLocation ATMOSPHERE_SHADER = Deepspace.path("atmosphere");
    private static final RenderStateShard.ShaderStateShard ATMOSPHERE_RENDER_TYPE = new RenderStateShard.ShaderStateShard(() -> {
        ShaderProgram shader = VeilRenderSystem.setShader(ATMOSPHERE_SHADER);
        return VeilRenderBridge.toShaderInstance(shader);
    });
    public static void refreshMeshes() {
        RING_MESHES.clear();
        ATMOSPHERE_MESHES.clear();
        for (var x : PlanetRegistry.getAllPlanets()) {
            var decorations = x.getDecorations();
            if (decorations.isEmpty()) return;
            for (var decoration : decorations.get()) {
                if (decoration.type().equals(Planet.PlanetDecoration.ATMOSPHERE))
                    ATMOSPHERE_MESHES.put(x.getId(), new Atmosphere(
                            new Cube(x.getBoundingBoxMin().toVector3f(), x.getBoundingBoxMax().toVector3f(), decoration.scale(), true),
                            decoration.scale(),
                            decoration.color()
                    ));
                else if (decoration.type().equals(Planet.PlanetDecoration.RINGS)) {
                    RING_MESHES.put(x.getId(), new Ring(
                            new Plane(x.getCenter().toVector3f(), decoration.scale() * (float) Math.abs(x.getBoundingBoxMin().x - x.getBoundingBoxMax().x), new Quaternionf().rotationX((float)Math.PI/2).rotateY((float)Math.PI * .1f)),
                            decoration.scale(),
                            decoration.color()
                    ));
                }
            }
        }
    }
    public static void init() {
        refreshMeshes();
        SpaceRenderSystem.registerRenderer(PlanetDecorationsRenderer::render, VeilRenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS);
    }
    private static RenderType atmosphereRenderType() {
        var renderType = RenderType.CompositeState.builder()
                .setShaderState(ATMOSPHERE_RENDER_TYPE)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.CullStateShard.NO_CULL)
                .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                .createCompositeState(true);
        return RenderType.create(
                "atmosphere",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                786432, true, false,
                renderType
        );
    }
    private static RenderType ringBloomRenderType() {
        var ringState = RenderType.CompositeState.builder()
                .setShaderState(RING_RENDER_TYPE)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.CullStateShard.NO_CULL)
                .setOutputState(RenderStateShard.OutputStateShard.TRANSLUCENT_TARGET)
                .createCompositeState(true);
        var ringType = RenderType.create(
                "ring",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                786432, true, false,
                ringState
        );
        var bloomState = RenderType.CompositeState.builder()
                .setShaderState(RING_RENDER_TYPE)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.CullStateShard.NO_CULL)
                .setOutputState(VeilRenderSystem.BLOOM_SHARD)
                .createCompositeState(true);
        var bloomType = RenderType.create(
                "ring",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                786432, true, false,
                bloomState
        );
        return VeilRenderType.layered(
                ringType,
                bloomType
        );
    }
    private static RenderType ringRenderType() {
        var ringState = RenderType.CompositeState.builder()
                .setShaderState(RING_RENDER_TYPE)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.CullStateShard.NO_CULL)
                .setOutputState(RenderStateShard.OutputStateShard.TRANSLUCENT_TARGET)
                .createCompositeState(true);
        var ringType = RenderType.create(
                "ring",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                786432, true, false,
                ringState
        );
        return VeilRenderType.layered(
                ringType
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
        var instance = Minecraft.getInstance();
        if (!instance.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(Deepspace.MODID,"space")))
            return;
        var atmosphereRenderType = atmosphereRenderType();
        var ringRenderType = ringRenderType();
        var poseStack = matrixStack.toPoseStack();
        IrisIntegration.bindPipeline();

        for (var x : ATMOSPHERE_MESHES.entrySet()) {

            // Planet Atmosphere
            BufferBuilder atmosphereBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            x.getValue().cube.render(poseStack, atmosphereBuilder, camera.getPosition().toVector3f().mul(-1), new Quaternionf());
            var color = new Color(x.getValue().color);
            VeilRenderSystem.setShader(Deepspace.path("atmosphere"));

            var TIME_UNIFORM = VeilRenderSystem.getShader().getOrCreateUniform("Time");
            TIME_UNIFORM.setFloat(camera.getPartialTickTime() + renderTick);
            RenderSystem.setShaderColor(color.getRed()/256f, color.getGreen()/256f, color.getBlue()/256f, 1f);
            RenderSystem.setShaderTexture(0, Deepspace.path("textures/atmosphere.png"));
            switch (ModOptions.options().atmosphereDetail) {
                case NONE -> {}
                case BASIC, EXPENSIVE -> atmosphereRenderType.draw(atmosphereBuilder.buildOrThrow());
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        for (var x : RING_MESHES.entrySet()) {
            BufferBuilder ringBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            x.getValue().mesh.render(poseStack, ringBuilder, camera.getPosition().toVector3f().mul(-1), new Quaternionf());
            var color = new Color(x.getValue().color);
            VeilRenderSystem.setShader(Deepspace.path("ring"));

            var TIME_UNIFORM = VeilRenderSystem.getShader().getOrCreateUniform("Time");
            TIME_UNIFORM.setFloat(camera.getPartialTickTime() + renderTick);
            RenderSystem.setShaderColor(color.getRed()/256f, color.getGreen()/256f, color.getBlue()/256f, 1f);
            RenderSystem.setShaderTexture(0, Deepspace.path("textures/atmosphere.png"));
            switch (ModOptions.options().atmosphereDetail) {
                case NONE -> {}
                case BASIC -> ringRenderType.draw(ringBuilder.buildOrThrow());
                case EXPENSIVE -> ringBloomRenderType().draw(ringBuilder.buildOrThrow());
            }

            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

    }
    private record Atmosphere(Cube cube, float scale, int color) {}
    private record Ring(Plane mesh, float scale, int color) {}
}
