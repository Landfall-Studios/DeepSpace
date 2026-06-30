package world.landfall.deepspace.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.landfall.deepspace.Deepspace;

@Mixin(value = LivingEntity.class, remap = false)
public abstract class MixinLivingEntity extends Entity {

    protected MixinLivingEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "onBelowWorld", at = @At("HEAD"), cancellable = true)
    public void onBelowWorld(CallbackInfo callbackInfo) {
        callbackInfo.cancel();
        if (this.level().dimension().location().equals(Deepspace.path("space"))) {
        } else {
            this.hurt(this.damageSources().fellOutOfWorld(), 4f);
        }

    }
}
