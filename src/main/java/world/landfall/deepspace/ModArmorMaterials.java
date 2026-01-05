package world.landfall.deepspace;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, Deepspace.MODID);
    public static final Holder<ArmorMaterial> JET_ARMOR_MATERIAL = ARMOR_MATERIALS.register(
            "jet", () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.HELMET, 2);
                        map.put(ArmorItem.Type.CHESTPLATE, 2);
                    }),
                    0,
                    SoundEvents.ARMOR_EQUIP_ELYTRA,
                    () -> Ingredient.EMPTY,
                    List.of(
                            new ArmorMaterial.Layer(
                                    ResourceLocation.fromNamespaceAndPath(Deepspace.MODID,"jet")
                            )
                    ),
                    1000000000,
                    0
            )
    );

    public static void register(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
    }
}
