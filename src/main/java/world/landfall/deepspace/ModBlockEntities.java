package world.landfall.deepspace;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import world.landfall.deepspace.blockentity.OxygenatorBlockEntity;

import java.util.function.Supplier;
@EventBusSubscriber(modid = Deepspace.MODID)
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Deepspace.MODID);
    public static final Supplier<BlockEntityType<OxygenatorBlockEntity>> OXYGENATOR_BLOCK_ENTITY_TYPE = BLOCK_ENTITIES.register(
            "oxygenator_block_entity",() -> OxygenatorBlockEntity.TYPE
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(OxygenatorBlockEntity.TYPE, OxygenatorBlockEntity.Renderer::new);
    }
}
