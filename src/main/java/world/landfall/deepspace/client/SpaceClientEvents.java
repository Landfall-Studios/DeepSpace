package world.landfall.deepspace.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
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
    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(Deepspace.MODID, "space"), new SpaceDimensionEffects());
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