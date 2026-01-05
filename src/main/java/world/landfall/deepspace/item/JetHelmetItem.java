package world.landfall.deepspace.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.infrastructure.data.CreateRegistrateTags;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModArmorMaterials;
import world.landfall.deepspace.ModAttatchments;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class JetHelmetItem extends ArmorItem {
    public JetHelmetItem() {
        super(ModArmorMaterials.JET_ARMOR_MATERIAL, Type.HELMET,new Properties()
                .durability(Integer.MAX_VALUE)
                .component(JetHelmetComponent.SUPPLIER, new JetHelmetComponent(100, 100))
                .component(DataComponents.RARITY, Rarity.EPIC)
                .component(DataComponents.CUSTOM_DATA, CustomData.of(
                        createModTag()
                ))
        );

    }
    private static CompoundTag createModTag() {

        var createTag = new CompoundTag();
        var data = new CompoundTag();
        data.put("Processing", StringTag.valueOf("BLASTING"));
        createTag.put("CreateData", data);
        return createTag;

    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<net.minecraft.network.chat.Component> tooltipComponents, TooltipFlag tooltipFlag) {
        //super.appendHoverText(stack, contexttranslatable("item.deepspace.jetpack.tooltip"), tooltipComponents, tooltipFlag);
        var jetHelmetComponent = stack.getComponents().get(JetHelmetComponent.SUPPLIER.get());
        if (jetHelmetComponent == null) return;
        if (jetHelmetComponent.maxOxygen < 0)
            tooltipComponents.add(net.minecraft.network.chat.Component.translatable("item.deepspace.jet_helmet.tooltip").append(net.minecraft.network.chat.Component.literal("Infinite").setStyle(Style.EMPTY.withColor(0xFF00FFE))));
        else
            tooltipComponents.add(net.minecraft.network.chat.Component.translatable("item.deepspace.jet_helmet.tooltip").append(Component.literal(jetHelmetComponent.currentOxygen + "/" + jetHelmetComponent.maxOxygen)));
    }
    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        var equipped = slotId == 39;
        if (!(entity instanceof Player player)) return;
        var dim = player.level().dimension().location();
        if (equipped &&
                !player.isCreative() && (dim.equals(Deepspace.path("space")) || dim.equals(Deepspace.path("luna"))) &&
                player.getData(ModAttatchments.LAST_OXYGENATED) > 3
        ) {
            var tick = player.tickCount;
            var component = stack.getComponents().get(JetHelmetComponent.SUPPLIER.get());
            if (component == null) return;
            if (tick % 40 == 0 && component.maxOxygen >= 0 && component.currentOxygen > 0) {
                stack.set(JetHelmetComponent.SUPPLIER, new JetHelmetComponent(component.currentOxygen - 1, component.maxOxygen));
            }
        }

    }

    @Override
    public Component getName(ItemStack stack) {
        return (stack.has(JetHelmetComponent.SUPPLIER) && stack.get(JetHelmetComponent.SUPPLIER.get()).maxOxygen >= 0) ?
                Component.translatable("item.deepspace.jet_helmet") :
                Component.translatable("item.deepspace.jet_helmet.creative");
    }
    @Override
    public boolean isBarVisible(ItemStack stack) {
        var component = stack.getComponents().get(JetHelmetComponent.SUPPLIER.get());
        return component != null && component.maxOxygen >= 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var component = stack.getComponents().get(JetHelmetComponent.SUPPLIER.get());
        return (component != null) ? (int)((float)component.playerOxygen()/Player.TOTAL_AIR_SUPPLY*12f) : 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        var component = stack.getComponents().get(JetHelmetComponent.SUPPLIER.get());
        return (component != null && component.playerOxygen() > 100) ? Color.WHITE.getRGB() : Color.RED.getRGB();
    }



//    @Override
//    public @NotNull Model getGenericArmorModel(@NotNull LivingEntity livingEntity, @NotNull ItemStack itemStack, @NotNull EquipmentSlot equipmentSlot, @NotNull HumanoidModel<?> original) {
//        return new JetSuitArmorModel<Player>(original.head);
//    }

    public record JetHelmetComponent(int currentOxygen, int maxOxygen) {

        public static final Codec<JetHelmetComponent> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("current_oxygen").forGetter(JetHelmetComponent::currentOxygen),
                        Codec.INT.fieldOf("max_oxygen").forGetter(JetHelmetComponent::maxOxygen)
                ).apply(instance, JetHelmetComponent::new)
        );
        public static final StreamCodec<ByteBuf, JetHelmetComponent> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, JetHelmetComponent::currentOxygen,
                ByteBufCodecs.INT, JetHelmetComponent::maxOxygen,
                JetHelmetComponent::new
        );
        public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Deepspace.MODID);
        public static final Supplier<DataComponentType<JetHelmetComponent>> SUPPLIER = REGISTRAR.registerComponentType(
                "jet_helmet",
                builder -> builder
                        .persistent(CODEC)
                        .networkSynchronized(STREAM_CODEC)
        );

        public static void register(IEventBus eventBus) {
            REGISTRAR.register(eventBus);
        }
        public int playerOxygen() {
            return maxOxygen < 0 ? Player.TOTAL_AIR_SUPPLY : (int)Math.clamp((float)currentOxygen / maxOxygen * Player.TOTAL_AIR_SUPPLY, 0, Player.TOTAL_AIR_SUPPLY);
        }
    }
}
