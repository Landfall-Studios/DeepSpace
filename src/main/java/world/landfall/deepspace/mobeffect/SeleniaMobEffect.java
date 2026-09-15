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
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.ModDamageTypes;

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
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration < 30 * 20 || amplifier > 1;
    }

    @Override
    public void onMobRemoved(LivingEntity livingEntity, int amplifier, Entity.RemovalReason reason) {
//        super.onMobRemoved(livingEntity, amplifier, reason);
        var level = livingEntity.level();
        var pos = BlockPos.containing(livingEntity.position());
        if (level.getBlockState(pos.below()).canOcclude())
            level.setBlockAndUpdate(pos, ModBlocks.SELENIC_CORE_BLOCK.get().defaultBlockState());
    }
}
