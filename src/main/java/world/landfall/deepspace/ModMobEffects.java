package world.landfall.deepspace;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import world.landfall.deepspace.mobeffect.SeleniaMobEffect;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, Deepspace.MODID);
    public static final DeferredHolder<MobEffect, SeleniaMobEffect> SELINIA = REGISTER.register("selinia",
            SeleniaMobEffect::new);

    public static void init(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
