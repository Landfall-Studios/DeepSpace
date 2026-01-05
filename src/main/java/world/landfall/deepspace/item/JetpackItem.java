package world.landfall.deepspace.item;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerFlyableFallEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.slf4j.Logger;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModAttatchments;
import world.landfall.deepspace.ModKeyMappings;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
@EventBusSubscriber(modid = Deepspace.MODID)
public class JetpackItem extends Item implements Equipable {
    private static Logger LOGGER = LogUtils.getLogger();
    public JetpackItem() {
        super(new Properties()
                .durability(Integer.MAX_VALUE)
                .component(JetpackComponent.SUPPLIER, new JetpackComponent(100, 100))
                .component(DataComponents.RARITY, Rarity.EPIC)

        );
    }

    @Override
    public Component getName(ItemStack stack) {
        return (stack.has(JetpackComponent.SUPPLIER) && stack.get(JetpackComponent.SUPPLIER.get()).maxFuel >= 0) ?
        Component.translatable("item.deepspace.jetpack") :
        Component.translatable("item.deepspace.jetpack.creative");
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        var inSpace = level.dimension().location().equals(ResourceLocation.parse("deepspace:space"));
        if (!(entity instanceof Player player)) return;

        var tick = player.tickCount;

        var inSlot = slotId == 38;
        if (!player.getData(ModAttatchments.IS_FLYING_JETPACK) || !player.getData(ModAttatchments.IS_ROCKETING_FORWARD) || !inSlot)
            return;
        if (tick % 5 == 0) {
            player.level().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, .1f, .5f);

        }
        if (tick % 20 == 0) {
            var component = stack.getComponents().get(JetpackComponent.SUPPLIER.get());
            if (component == null) return;
            if (component.maxFuel > 0)
                stack.set(JetpackComponent.SUPPLIER.get(), new JetpackComponent(component.currentFuel - 1, component.maxFuel));
        }


    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        //super.appendHoverText(stack, contexttranslatable("item.deepspace.jetpack.tooltip"), tooltipComponents, tooltipFlag);
        var jetpackComponent = stack.getComponents().get(JetpackComponent.SUPPLIER.get());
        if (jetpackComponent == null) return;
        if (jetpackComponent.maxFuel < 0)
            tooltipComponents.add(Component.translatable("item.deepspace.jetpack.tooltip").append(Component.literal("Infinite").setStyle(Style.EMPTY.withColor(0xFF00FFE))));
        else
            tooltipComponents.add(Component.translatable("item.deepspace.jetpack.tooltip").append(Component.literal(jetpackComponent.currentFuel + "/" + jetpackComponent.maxFuel)));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        var component = stack.getComponents().get(JetpackComponent.SUPPLIER.get());
        return component != null && component.maxFuel > 0;
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return Color.WHITE.getRGB();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var component = stack.getComponents().get(JetpackComponent.SUPPLIER.get());
        return component != null ? (int)((float)component.currentFuel/component.maxFuel*12f): 0;
    }

    public static boolean isFlyEnabled(ItemStack stack) {
        var component = stack.getComponents().get(JetpackComponent.SUPPLIER.get());
        if (component == null) {
            LOGGER.error("A jetpack was created without its component !");
            return false;
        }
        return component.currentFuel > 0 || component.maxFuel < 0;

    }

    public record JetpackComponent(int currentFuel, int maxFuel) {
        public static final Codec<JetpackComponent> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                    Codec.INT.fieldOf("current_fuel").forGetter(JetpackComponent::currentFuel),
                    Codec.INT.fieldOf("max_fuel").forGetter(JetpackComponent::maxFuel)
                ).apply(instance, JetpackComponent::new)
        );
        public static final StreamCodec<ByteBuf, JetpackComponent> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, JetpackComponent::currentFuel,
                ByteBufCodecs.INT, JetpackComponent::maxFuel,
                JetpackComponent::new
        );
        public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Deepspace.MODID);
        public static final Supplier<DataComponentType<JetpackComponent>> SUPPLIER = REGISTRAR.registerComponentType(
                "jetpack",
                builder -> builder
                        .persistent(CODEC)
                        .networkSynchronized(STREAM_CODEC)
        );
        public static void register(IEventBus eventBus) {
            REGISTRAR.register(eventBus);
        }
        public boolean canFly() {
            return maxFuel < 0 || currentFuel > 0;
        }
    }
    @SubscribeEvent
    public static void fallFlyEvent(PlayerFlyableFallEvent event) {

    }

}
