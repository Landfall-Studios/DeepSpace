package world.landfall.deepspace.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.ryanhcode.sable.Sable;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModItems;
import world.landfall.deepspace.Util;
import world.landfall.deepspace.planet.PlanetRegistry;

import java.util.List;
import java.util.function.Supplier;

public class SpaceNavigationItem extends Item implements NavigationTarget {

    private static final SimulatedRegistrate REGISTRATE = Simulated.getRegistrate();
    private static final RegistryEntry<NavigationTarget, SpaceNavigationItem> TARGET = REGISTRATE.navTarget(
            "navigator", ModItems.NAVIGATOR_ITEM::get, ModItems.NAVIGATOR_ITEM
    );

    public enum Mode {
        PROGRADE("item.deepspace.navigator.prograde", "item.deepspace.navigator.prograde.description"),
        RETROGRADE("item.deepspace.navigator.retrograde", "item.deepspace.navigator.retrograde.description"),
        NORMAL("item.deepspace.navigator.normal", "item.deepspace.navigator.normal.description"),
        ANTINORMAL("item.deepspace.navigator.antinormal", "item.deepspace.navigator.antinormal.description"),
        ;
        public final String name_component;
        public final String description_component;
        Mode(String y, String s) {
            name_component = y;
            description_component = s;
        }
    }

    public SpaceNavigationItem(Properties properties) {
        super(properties.component(SpaceNavigationItemComponent.SUPPLIER, new SpaceNavigationItemComponent(Mode.PROGRADE)));
    }

    @Override
    public @Nullable Vec3 getTarget(NavTableBlockEntity navBE, ItemStack self) {
        if (!navBE.getLevel().dimension().location().equals(Deepspace.path("space")))
            return null;
        var mode = self.get(SpaceNavigationItemComponent.SUPPLIER);
        if (mode == null)
            return null;
        var sublevel = Sable.HELPER.getContaining(navBE);
        if (sublevel == null)
            return null;
        var real_pos = Sable.HELPER.projectOutOfSubLevel(navBE.getLevel(), navBE.getBlockPos().getCenter());
        var planet = PlanetRegistry.getClosestPlanet(real_pos);
        if (planet == null) return null;
        var velocity = new Vector3d(sublevel.logicalPose().position()).sub(sublevel.lastPose().position());
        var proper_velocity = Util.fromVector3f(new Vector3f(
                (float) velocity.x,
                (float) velocity.y,
                (float) velocity.z
        ));
        var planetDiff = real_pos.subtract(planet.getCenter());
        var component = self.get(SpaceNavigationItemComponent.SUPPLIER);
        if (component == null) return null;
        return (switch (component.mode) {
            case NORMAL -> planetDiff.normalize().scale(10).add(real_pos);
            case ANTINORMAL -> planetDiff.normalize().reverse().scale(10).add(real_pos);
            case PROGRADE -> proper_velocity.normalize().scale(10).add(real_pos);
            case RETROGRADE -> proper_velocity.normalize().reverse().scale(10).add(real_pos);
            case null -> null;
        });

    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        var component = stack.get(SpaceNavigationItemComponent.SUPPLIER);
        if (component == null) return;
        tooltipComponents.add(Component.translatable("item.deepspace.navigator.explain").withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.translatable(component.mode.description_component).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        var component = stack.get(SpaceNavigationItemComponent.SUPPLIER);
        if (component == null)
            return super.getName(stack);
        return super.getName(stack).copy().append(
                Component.literal(" (").append(Component.translatable(component.mode.name_component).append(")")).withStyle(ChatFormatting.AQUA)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        var component = stack.get(SpaceNavigationItemComponent.SUPPLIER);
        if (component == null)
            return InteractionResultHolder.fail(stack);
        var new_mode = Mode.values()[(component.mode.ordinal() + 1) % Mode.values().length];
        stack.set(SpaceNavigationItemComponent.SUPPLIER, new SpaceNavigationItemComponent(
                new_mode
        ));
        stack.set(DataComponents.ITEM_NAME, stack.getItem().getName(stack));
        player.displayClientMessage(Component.translatable(new_mode.name_component), true);
        player.swing(usedHand);
        return InteractionResultHolder.success(stack);
    }

    public record SpaceNavigationItemComponent(Mode mode) {
        public static final Codec<SpaceNavigationItemComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("mode").forGetter((SpaceNavigationItemComponent c) -> c.mode.ordinal())
        ).apply(i, mode -> new SpaceNavigationItemComponent(Mode.values()[mode])));
        public static final StreamCodec<ByteBuf, SpaceNavigationItemComponent> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, c -> c.mode.ordinal(),
                mode -> new SpaceNavigationItemComponent(Mode.values()[mode])
        );
        public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Deepspace.MODID);
        public static final Supplier<DataComponentType<SpaceNavigationItemComponent>> SUPPLIER = REGISTRAR.registerComponentType(
                "navigator",
                builder -> builder
                        .persistent(CODEC)
                        .networkSynchronized(STREAM_CODEC)
        );

        public static void register(IEventBus eventBus) {
            REGISTRAR.register(eventBus);
        }
    }
}
