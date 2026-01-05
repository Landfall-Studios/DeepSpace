package world.landfall.deepspace.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModItems;
import world.landfall.deepspace.item.JetHelmetItem;

import java.util.List;

@JeiPlugin
public class JustEnoughItemsIntegration implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Deepspace.MODID, "jet_suit_jei");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IModPlugin.super.registerRecipes(registration);
        var stacks = List.of(
                getJetHelmetWithOxygen(0),
                getJetHelmetWithOxygen(20),
                getJetHelmetWithOxygen(40),
                getJetHelmetWithOxygen(60),
                getJetHelmetWithOxygen(80),
                getJetHelmetWithOxygen(100)
        );
        registration.addItemStackInfo(stacks, Component.translatable("item.deepspace.jet_helmet.jei"));

    }
    private static ItemStack getJetHelmetWithOxygen(int oxygen) {
        var stack = ModItems.JET_HELMET_ITEM.toStack();
        stack.set(JetHelmetItem.JetHelmetComponent.SUPPLIER, new JetHelmetItem.JetHelmetComponent(oxygen, 100));
        return stack;
    }
}
