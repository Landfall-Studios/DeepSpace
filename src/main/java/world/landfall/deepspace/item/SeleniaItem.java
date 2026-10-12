package world.landfall.deepspace.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import world.landfall.deepspace.ModMobEffects;

public class SeleniaItem extends Item {
    public SeleniaItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        var random = level.random;
        if (random.nextFloat() > 0.1f) return super.finishUsingItem(stack, level, livingEntity);
        var effect = livingEntity.getEffect(ModMobEffects.SELINIA);
        if (effect == null) {
            livingEntity.addEffect(new MobEffectInstance(ModMobEffects.SELINIA, 20 * 120));
        } else {
            livingEntity.addEffect(new MobEffectInstance(ModMobEffects.SELINIA, 20 * 30 + effect.getDuration()));
        }
        return super.finishUsingItem(stack, level, livingEntity);

    }
}
