package world.landfall.deepspace.integration;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import world.landfall.deepspace.ModItems;
import world.landfall.deepspace.item.JetHelmetItem;

import java.util.List;

public class CreateIntegration {
    public static void handleAir(List<Entity> entities, List<Pair<TransportedItemStackHandlerBehaviour, FanProcessingType>> handlers) {
        for (var x : handlers) {
            var behavior = x.getLeft().blockEntity.getBehaviour(DepotBehaviour.TYPE);
            if (behavior == null) continue;
            var stack = behavior.itemHandler.getStackInSlot(0);
            if (stack.is(ModItems.JET_HELMET_ITEM)) {
                var data = stack.get(JetHelmetItem.JetHelmetComponent.SUPPLIER);
                if (data == null) continue;
                if (data.maxOxygen() < 0 || data.currentOxygen() >= data.maxOxygen()) continue;
                if (x.getLeft().getWorld().getBlockTicks().count() % 4 == 0)
                    stack.set(JetHelmetItem.JetHelmetComponent.SUPPLIER, new JetHelmetItem.JetHelmetComponent(data.currentOxygen()+1, data.maxOxygen()));
            }
        }
        for (var x : entities) {
            if (x instanceof ItemEntity itemEntity) {
                if (itemEntity.getItem().is(ModItems.JET_HELMET_ITEM.get())) {
                    if (itemEntity.tickCount % 4 != 0) continue;
                    var item = itemEntity.getItem();
                    var components = item.getComponents();
                    if (!components.has(JetHelmetItem.JetHelmetComponent.SUPPLIER.get())) continue;
                    var component = components.get(JetHelmetItem.JetHelmetComponent.SUPPLIER.get());
                    if (component.maxOxygen() < 0 || component.currentOxygen() >= component.maxOxygen()) continue;

                    item.applyComponents(DataComponentMap.builder()
                                    .set(JetHelmetItem.JetHelmetComponent.SUPPLIER.get(), new JetHelmetItem.JetHelmetComponent(
                                            component.currentOxygen()+1,
                                            component.maxOxygen()
                                    ))
                            .build());
                }
            }
        }
    }
    public static void register(IEventBus eventBus) {
        

        //FanProcessingTypeRegistry.init();
    }
    public static class AerateType implements FanProcessingType {

        @Override
        public boolean isValidAt(Level level, BlockPos pos) {
            return true;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public boolean canProcess(ItemStack stack, Level level) {
            return stack.is(ModItems.JET_HELMET_ITEM.asItem()) && stack.has(JetHelmetItem.JetHelmetComponent.SUPPLIER);
        }

        @Override
        public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
            if (!stack.has(JetHelmetItem.JetHelmetComponent.SUPPLIER))
                return List.of();
            var component = stack.getComponents().get(JetHelmetItem.JetHelmetComponent.SUPPLIER.get());
            var add = level.getBlockTicks().count() % 4 == 0 ? 1 : 0;
            stack.set(JetHelmetItem.JetHelmetComponent.SUPPLIER, new JetHelmetItem.JetHelmetComponent(component.currentOxygen() + add, component.maxOxygen()));
            return List.of(
                stack
            );
        }

        @Override
        public void spawnProcessingParticles(Level level, Vec3 pos) {

        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {

        }

        @Override
        public void affectEntity(Entity entity, Level level) {

        }
    }
}
