package world.landfall.deepspace.planet;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import world.landfall.deepspace.Deepspace;

@EventBusSubscriber(modid = Deepspace.MODID)
public class SunHurtHandler {

    @SubscribeEvent
    public static void serverPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().getServer() == null) return;
        var player = event.getEntity();
        var sun = PlanetRegistry.getSun();
        var dimension = player.level().dimension().location();
        if (sun == null) return;
        if (sun.isPlayerTouching(player) && dimension.equals(ResourceLocation.parse("deepspace:space"))) {
            player.hurt(player.damageSources().inFire(),Float.MAX_VALUE);
        }
        if (player.position().distanceTo(sun.getCenter()) <= sun.getHurtRadius() && dimension.equals(ResourceLocation.parse("deepspace:space"))) {
            player.setRemainingFireTicks(20);
        }
    }
}
