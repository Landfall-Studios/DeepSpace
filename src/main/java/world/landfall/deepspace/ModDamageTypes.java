package world.landfall.deepspace;

import com.simibubi.create.foundation.damageTypes.DamageTypeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModDamageTypes {

//    public static final ResourceKey<DamageType> NO_AIR_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Deepspace.path("no_air"));
//    public static DamageSource noAirDamage(Entity receiver) {
//        return new DamageSource(
//                receiver.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(NO_AIR_DAMAGE),
//                (Entity)null
//        );
//    }
public static final ResourceKey<DamageType> NO_AIR_DAMAGE_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, Deepspace.path("no_air"));
    public static final ResourceKey<DamageType> SELENIA_DAMAGE_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, Deepspace.path("selenia"));


    public static DamageSource noAirDamage(LivingEntity player) {
        return new DamageSource(player.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(NO_AIR_DAMAGE_KEY));
    }
    public static DamageSource seleniaDamage(LivingEntity player) {
        return new DamageSource(player.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(SELENIA_DAMAGE_KEY));
    }
}
