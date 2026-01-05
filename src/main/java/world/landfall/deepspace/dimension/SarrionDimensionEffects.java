package world.landfall.deepspace.dimension;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class SarrionDimensionEffects extends DimensionSpecialEffects {
    public SarrionDimensionEffects() {
        super(-200, false, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 vec3, float v) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int i, int i1) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        //return super.renderSky(level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog);
        return true;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        return true;

    }
}
