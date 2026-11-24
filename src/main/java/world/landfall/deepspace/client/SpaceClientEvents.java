package world.landfall.deepspace.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModAttatchments;
import world.landfall.deepspace.ModItems;
import world.landfall.deepspace.ModKeyMappings;
import world.landfall.deepspace.dimension.SpaceDimensionEffects;
import world.landfall.deepspace.item.JetpackItem;
import world.landfall.deepspace.network.JetpackPacket;

@EventBusSubscriber(modid = Deepspace.MODID, value = Dist.CLIENT)
public class SpaceClientEvents {
    private static final ModelPart MODEL = HumanoidArmorModel.createBodyLayer(
            new CubeDeformation(1)
    ).getRoot().bake(64, 64);
    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(Deepspace.MODID, "space"), new SpaceDimensionEffects());
    }
    @SubscribeEvent
    public static void renderLayer(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
//                var layer = new HumanoidArmorLayer<>(renderer, new HumanoidArmorModel<>(MODEL), new HumanoidArmorModel<>(MODEL), event.getContext().getModelManager()) {
//                    @Override
//                    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//                        if (livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.JET_HELMET_ITEM))
//                            super.render(poseStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
//                    }
//
//                    @Override
//                    protected ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
//                        return Deepspace.path("models/armor/jet_layer_1");
//                    }
//
//                    @Override
//                    protected void setPartVisibility(HumanoidArmorModel<AbstractClientPlayer> model, EquipmentSlot slot) {
//                        super.setPartVisibility(model, slot);
//                    }
//                };
                var layer = new CustomHeadLayer<> (
                        renderer,
                        Minecraft.getInstance().getEntityModels(),
                        Minecraft.getInstance().gameRenderer.itemInHandRenderer
                ) {
                    @Override
                    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                        if (livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.JET_HELMET_ITEM))
                            super.render(poseStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
                    }

                    @Override
                    protected ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
                        return Deepspace.path("models/armor/jet_layer_1");
                    }
                };
                renderer.addLayer(layer);
            }
        }
    }
    @EventBusSubscriber(modid = Deepspace.MODID, value = Dist.CLIENT)
    public static class Tick {
        @SubscribeEvent
        public static void onClientTick(PlayerTickEvent.Pre event) {
            var player = event.getEntity();
            var item = player.getItemBySlot(EquipmentSlot.CHEST);
            if (item.is(ModItems.JETPACK_ITEM) && player.level().isClientSide) {
                var component = item.getComponents().get(JetpackItem.JetpackComponent.SUPPLIER.get());
                if (component == null) return;
                var rocketing = Minecraft.getInstance().options.keyJump.isDown() && component.canFly();

                PacketDistributor.sendToServer(
                        new JetpackPacket.RocketForward(rocketing)
                );
                event.getEntity().setData(ModAttatchments.IS_ROCKETING_FORWARD, rocketing);


                if (Minecraft.getInstance().options.keySprint.isDown() && component.canFly()) {
                    if (!player.onGround()) {
                        PacketDistributor.sendToServer(
                                new JetpackPacket.BeginFlying(true)
                        );
                        event.getEntity().setData(ModAttatchments.IS_FLYING_JETPACK, true);
                    }
                }
                if (Minecraft.getInstance().options.keyShift.isDown()) {
                    PacketDistributor.sendToServer(
                            new JetpackPacket.BeginFlying(false)
                    );
                    event.getEntity().setData(ModAttatchments.IS_FLYING_JETPACK, false);
                    event.getEntity().setData(ModAttatchments.IS_ROCKETING_FORWARD, false);
                }
            }
        }
        @SubscribeEvent
        public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            var player = event.getEntity();
            player.setData(ModAttatchments.IS_FLYING_JETPACK, false);
            player.setData(ModAttatchments.IS_ROCKETING_FORWARD, false);

        }
    }

} 