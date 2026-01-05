package world.landfall.deepspace.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.compiler.VeilShaderSource;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModItems;
import world.landfall.deepspace.model.JetpackModel;

@EventBusSubscriber(modid = Deepspace.MODID, value = Dist.CLIENT)
public class JetpackRenderer {
    private static ModelPart MODEL = JetpackModel.createBodyLayer().bakeRoot();
    private static RenderType type() {
//        var type = RenderType.entitySolid(Deepspace.path("textures/jetpack.png"));
        var type = RenderType.ARMOR_CUTOUT_NO_CULL.apply(Deepspace.path("textures/jetpack.png"));
        return type;
    }
    @SubscribeEvent
    public static void onRender(RenderPlayerEvent.Post e) {
        var stack = e.getPoseStack();
        var ent = e.getEntity();
        if (!ent.getInventory().getItem(38).is(ModItems.JETPACK_ITEM))
            return;
        var rot = ent.getPreciseBodyRotation(e.getPartialTick()) + 180;

        stack.pushPose();
        stack.rotateAround(new Quaternionf(new AxisAngle4f((float)Math.PI, 1, 0, 0)), 0, 0, 0);

        stack.translate(0, -1.4, -.3f);
        stack.rotateAround(new Quaternionf(new AxisAngle4f(rot * ((float)Math.PI) / 180, 0, 1, 0)), 0, 0, .3f);
        if (ent.hasPose(Pose.FALL_FLYING)) {
            stack.rotateAround(new Quaternionf(new AxisAngle4f(((float)Math.PI / 2), -1, 0, 0)), 0, 0, 0);
            stack.translate(0, -.7f, .8f);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);

        MODEL.render(e.getPoseStack(), e.getMultiBufferSource().getBuffer(type()), e.getPackedLight(), 0);
        stack.popPose();
    }
}