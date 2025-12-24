package world.landfall.deepspace;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import world.landfall.deepspace.dimension.SpaceDimensionType;
import world.landfall.deepspace.integration.CreateIntegration;
import world.landfall.deepspace.planet.PlanetRegistry;
import world.landfall.deepspace.render.SpaceRenderSystem;
import world.landfall.deepspace.worldgen.ModPlacedFeatures;

import java.util.Objects;
import java.util.function.Supplier;

@Mod(Deepspace.MODID)
public class Deepspace {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "deepspace";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final Supplier<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("deepspace", () -> CreativeModeTab.builder()
            .displayItems((params, output) -> {
                output.accept(ModItems.JETPACK_ITEM);
                output.accept(ModItems.CREATIVE_JETPACK_ITEM.get());
                output.accept(ModItems.JET_HELMET_ITEM);
                output.accept(ModItems.CREATIVE_JET_HELMET_ITEM.get());
                output.accept(ModItems.ROCKET_BOOSTER_ITEM);
                output.accept(ModItems.ANGEL_BLOCK_ITEM);
                output.accept(ModItems.OXYGENATOR_BLOCK_ITEM);
            })
            .icon(ModItems.ANGEL_BLOCK_ITEM::toStack)
            .title(Component.translatable("menu.deepspace.creative_mode_tab"))
            .build());
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Deepspace(@NotNull IEventBus modEventBus, @NotNull ModContainer modContainer) {
        Objects.requireNonNull(modEventBus, "modEventBus cannot be null");
        Objects.requireNonNull(modContainer, "modContainer cannot be null");

        // Register setup methods
        modEventBus.addListener(this::commonSetup);
        
        // Register dimension
        try {
            SpaceDimensionType.register(modEventBus);
            LOGGER.info("Space dimension registered successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to register space dimension", e);
            throw new RuntimeException("Failed to initialize mod", e);
        }
        // Register the Deferred Register to the mod event bus so blocks get registered
        ModBlocks.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ModPlantTypes.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModAttatchments.register(modEventBus);

        CreateIntegration.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Deepspace) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    public static ResourceLocation path(String s) {
        return ResourceLocation.fromNamespaceAndPath(MODID,s);
    }
    /**
     * Handles common setup tasks for both client and server.
     *
     * @param event The common setup event
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Performing common setup for Deep Space");
        PlanetRegistry.init();
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    /**
     * Handles server startup events.
     *
     * @param event The server starting event
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Deep Space mod server components initialized");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Initialize client events
            LOGGER.info("Deep Space mod client initialized");
            SpaceRenderSystem.init();
            LOGGER.info("Initialized renderers");
        }
    }
}
