package world.landfall.deepspace.server;

import foundry.veil.api.quasar.particle.ParticleSystemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModAttatchments;
import world.landfall.deepspace.ModDamageTypes;
import world.landfall.deepspace.ModItems;
import world.landfall.deepspace.item.JetHelmetItem;
import world.landfall.deepspace.item.JetpackItem;

public class SpacePlayerEvents {
    @EventBusSubscriber(modid = Deepspace.MODID)
    public static class Tick {
        private static void jetpackTick(Player player, Level level, ItemStack jetpack, boolean noGravity) {
            var hasJetpack = jetpack.is(ModItems.JETPACK_ITEM.get());
            if (!player.hasData(ModAttatchments.IS_FLYING_JETPACK)) return;
            var isFlying = player.getData(ModAttatchments.IS_FLYING_JETPACK.get());

//        var isEquipped = stack.getEquipmentSlot() == EquipmentSlot.CHEST;
//        if (!isEquipped) {
//            return;
//        }
            //player.setSwimming(isFlying);


            var jetpackComponent = jetpack.getComponents().get(JetpackItem.JetpackComponent.SUPPLIER.get());
            if (isFlying) {
                player.getAbilities().flying = false;
                if (noGravity)
                    player.setPose(Pose.FALL_FLYING);
                if (player.isShiftKeyDown() || player.onGround()) {
                    player.setData(ModAttatchments.IS_FLYING_JETPACK, false);
                    player.setData(ModAttatchments.IS_ROCKETING_FORWARD, false);
                    player.setData(ModAttatchments.JETPACK_VELOCITY, new Vector3f());
                    return;
                } else if ((jetpackComponent != null && !jetpackComponent.canFly())) {

                    return;
                }
                var lookAngle = player.getLookAngle();
                var f = Minecraft.getInstance().options.keyUp.isDown() ? 1 : 0;
                var b = Minecraft.getInstance().options.keyDown.isDown() ? 1 : 0;
                var l = Minecraft.getInstance().options.keyLeft.isDown() ? 1 : 0;
                var r = Minecraft.getInstance().options.keyRight.isDown() ? 1 : 0;
                var moveDir = new Vector3f(f-b, 0, l-r).normalize();

                var deltas = player.getDeltaMovement();
                Vector3f storedVelocity = player.getData(ModAttatchments.JETPACK_VELOCITY);

                var keyPressed = player.getData(ModAttatchments.IS_ROCKETING_FORWARD);
                var rocketVelocity = lookAngle.toVector3f().mul(.04f).rotateY(moveDir.angle(new Vector3f(1, 0, 0)) * ((l - r < 0 ? -1 : 1)));
                if (moveDir.x < 0) {
                    rocketVelocity.y *= -1;
                }
                Vector3f newVelocity = new Vector3f(storedVelocity);
                if (!noGravity) {
                    rocketVelocity.add(new Vector3f(0f, .04f, 0f)).mul(.1f, 2f, .1f);
                    newVelocity.add(0, -.06f, 0);
                }
                if (keyPressed) {
                    newVelocity.add(rocketVelocity);
                    var random = level.getRandom();
                    for (int i = 0; i < 4; i++) {
                        var offset = new Vector3f(random.nextFloat() * 2 - 1,random.nextFloat() * 2 - 1,random.nextFloat() * 2 - 1).mul(.4f);
                        var oppositeForce = new Vector3f(newVelocity).normalize();
                        offset.sub(oppositeForce.mul(2));
                        level.addParticle(ParticleTypes.FLAME,
                                player.getX() + offset.x, player.getY() + offset.y, player.getZ() + offset.z,
                                oppositeForce.x * -.1, oppositeForce.y * -.1, oppositeForce.z * -.1
                        );

                    }
                }

                if (!keyPressed) {
                    newVelocity.add(new Vector3f(0, -.01f, 0));
                    newVelocity.mul(.99f);
                }
                if (newVelocity.length() > 8) newVelocity.mul(.9f);
                //player.setPos(player.getPosition(0).add(new Vec3(newVelocity.x, newVelocity.y, newVelocity.z)));
                player.setData(ModAttatchments.JETPACK_VELOCITY, newVelocity);
                player.setDeltaMovement(new Vec3(newVelocity.x, newVelocity.y, newVelocity.z));
                //player.gameEvent(GameEvent.ELYTRA_GLIDE);
                var rot = angle(newVelocity.x, newVelocity.z);
                rot = Float.isNaN(rot) ? 0 : rot;
                player.setYBodyRot(rot);
                var headRot = player.getYHeadRot() - rot;
                var headAngle = (Math.abs(headRot) *
                        (headRot < 0 ? -1 : 1) +
                        (headRot < 0 ? 360+180 : 180))
                        % 360 - 180;
//                if (Math.abs(headAngle) + rot > 180)
//                    player.setYHeadRot((headAngle < 0 ? -180 : 180)-rot);
                if (Float.isNaN(headAngle))
                    headAngle = -rot;
                var maxHeadTurn = 70;
                player.setYHeadRot(Math.clamp(headAngle, -maxHeadTurn, maxHeadTurn) * (1 - Math.abs((float)player.getLookAngle().y)) + rot);
            } else {
                var deltas = player.getDeltaMovement();
                player.setData(ModAttatchments.JETPACK_VELOCITY, deltas.toVector3f());
            }
        }
        private static void jetHelmetTick(Player player, Level level, ItemStack jetHelmet, boolean noGravity) {
            var hasJetHelmet = jetHelmet.is(ModItems.JET_HELMET_ITEM.get());
            var component = jetHelmet.getComponents().get(JetHelmetItem.JetHelmetComponent.SUPPLIER.get());
            var isOxygenated = player.hasData(ModAttatchments.LAST_OXYGENATED) && player.getData(ModAttatchments.LAST_OXYGENATED) < 3;
            var tick = player.tickCount;
            if (component != null && noGravity && !player.isCreative() && !isOxygenated) {
                player.setAirSupply(component.playerOxygen());
                if (component.playerOxygen() < 1 && tick % 10 == 0)
                    player.hurt(ModDamageTypes.noAirDamage(player), 1);
            } else if (!hasJetHelmet && noGravity && !isOxygenated) {
                player.setAirSupply(0);
                if (tick % 10 == 0)
                    player.hurt(ModDamageTypes.noAirDamage(player), 2);


            }
        }
        private static void airTick(Player player, Level level, boolean noGravity) {
            if (!noGravity) return;
            var ticks = player.tickCount;
            if (ticks % 20 != 0) return;
        }

        @SubscribeEvent
        public static void playerTick(PlayerTickEvent.Post event) {

            Player player = event.getEntity();
            var dimension = player.level().dimension().location();
            var noGravity = dimension.equals(ResourceLocation.parse("deepspace:space")) || dimension.equals(ResourceLocation.parse("deepspace:luna"));
            player.setNoGravity(noGravity);
            //player.setIgnoreFallDamageFromCurrentImpulse(noGravity);
            if (noGravity && !player.getAbilities().flying) {
                player.setDeltaMovement(player.getDeltaMovement().add(new Vec3(0, -.01f, 0)));
            }
            var jetpackSlot = player.getItemBySlot(EquipmentSlot.CHEST);
            var jetHelmetSlot = player.getItemBySlot(EquipmentSlot.HEAD);
            jetpackTick(player, player.level(), jetpackSlot, noGravity);
            jetHelmetTick(player, player.level(), jetHelmetSlot, noGravity);
            airTick(player, player.level(), noGravity);
            var lastOxygenated = player.getData(ModAttatchments.LAST_OXYGENATED);
            player.setData(ModAttatchments.LAST_OXYGENATED, lastOxygenated + .05f);
        }
        private static float angle(float x, float y) {
            var rot = (float)Math.atan(y/x) / ((float)Math.PI*2) * 360;
            if (x < 0)
                rot += 180;
            else if (y < 0)
                rot += 360;
            return rot - 90;
        }
        @SubscribeEvent
        public static void fallEvent(LivingFallEvent event) {
            ;
            if (event.getEntity() instanceof Player player) {
                var dimension = player.level().dimension().location();
                var noGravity = dimension.equals(ResourceLocation.parse("deepspace:space")) || dimension.equals(ResourceLocation.parse("deepspace:luna"));

                event.setDistance(noGravity ? 0f : event.getDistance());
            }
        }
        @SubscribeEvent
        public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            var player = event.getEntity();
            player.setData(ModAttatchments.IS_FLYING_JETPACK, false);
            player.setData(ModAttatchments.IS_ROCKETING_FORWARD, false);
            player.setData(ModAttatchments.LAST_OXYGENATED, 0f);
        }
    }
}
