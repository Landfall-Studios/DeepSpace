package world.landfall.deepspace.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow private float zoom = 1.0F;
    @Shadow private float zoomX;
    @Shadow private float zoomY;
    @Final @Shadow Minecraft minecraft;
    @Inject(at = @At(value = "HEAD"), method = "getProjectionMatrix", remap = false, cancellable = true)
    protected void getProjectionMatrix(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        Matrix4f matrix4f = new Matrix4f();
        if (this.zoom != 1.0F) {
            matrix4f.translate(this.zoomX, -this.zoomY, 0.0F);
            matrix4f.scale(this.zoom, this.zoom, 1.0F);
        }

        cir.setReturnValue(matrix4f.perspective(
                (float)(fov * (float) (Math.PI / 180.0)),
                (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(),
                0.05F,
                this.getDepthFar() * 16f
        ));
        cir.cancel();
    }

    @Shadow public abstract float getDepthFar();
}
