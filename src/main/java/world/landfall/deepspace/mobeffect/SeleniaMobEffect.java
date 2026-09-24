package world.landfall.deepspace.mobeffect;

import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.ModDamageTypes;
import world.landfall.deepspace.ModMobEffects;

@EventBusSubscriber
public class SeleniaMobEffect extends MobEffect {
    private static final int HURT_DELAY_SECONDS = 5;
    private static final int HURT_AMOUNT = 4;
    public SeleniaMobEffect() {
        super(MobEffectCategory.HARMFUL, Color.PURPLE.getRGB());
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
//        return super.applyEffectTick(livingEntity, amplifier);
        var tick = livingEntity.tickCount;
        if (tick % (20 * HURT_DELAY_SECONDS) == 0) {
//            livingEntity.hurt((DamageSource) BuiltInRegistries.REGISTRY.get(Registries.DAMAGE_TYPE.location()).get(
//                    Deepspace.path("selenia")
//            ), HURT_AMOUNT + amplifier);
            livingEntity.hurt(ModDamageTypes.seleniaDamage(livingEntity), HURT_AMOUNT + amplifier);
            var pos = BlockPos.containing(livingEntity.position());
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration < 30 * 20 || amplifier > 1;
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        var entity = event.getEntity();

        var level = entity.level();
        var pos = BlockPos.containing(entity.position());
        if (level.getBlockState(pos.below()).canOcclude() && entity.hasEffect(ModMobEffects.SELINIA))
            level.setBlockAndUpdate(pos, ModBlocks.SELENIC_CORE_BLOCK.get().defaultBlockState());
    }

}
