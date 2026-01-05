package world.landfall.deepspace.planet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.network.PlanetSyncPacket;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing planets and their configurations.
 * Handles loading from JSON files and synchronization between server and client.
 */
@EventBusSubscriber(modid = Deepspace.MODID)
public class PlanetRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PLANETS_CONFIG_FILE = "planets.json";
    
    private static final Map<String, Planet> planets = new ConcurrentHashMap<>();
    private static volatile Sun sun;
    private static final Map<ResourceKey<Level>, Planet> planetsByDimension = new ConcurrentHashMap<>();
    private static final Object registryLock = new Object();
    private static volatile Path configPath;
    
    /**
     * Data class for JSON serialization of planet configurations.
     */

    public static class SunConfig {
        public double[] boundingBoxMin;
        public double[] boundingBoxMax;
        public double hurtRadius;
        public SunConfig(Sun sun) {
            var boundingBoxMin = sun.getBoundingBoxMin();
            this.boundingBoxMin = new double[]{
                    boundingBoxMin.x,
                    boundingBoxMin.y,
                    boundingBoxMin.z
            };
            var boundingBoxMax = sun.getBoundingBoxMax();
            this.boundingBoxMax = new double[]{
                    boundingBoxMax.x,
                    boundingBoxMax.y,
                    boundingBoxMax.z
            };
            this.hurtRadius = sun.getHurtRadius();
        }
    }
    public static class PlanetConfig {
        public String id;
        public String name;
        public String dimension;
        public double[] boundingBoxMin;
        public double[] boundingBoxMax;
        public Collection<Planet.PlanetDecoration> decorations;
        public String description = "";
        public double[] physicalMin;
        public double[] physicalMax;

        public PlanetConfig() {}
        
        public PlanetConfig(Planet planet) {
            this.id = planet.getId();
            this.name = planet.getName();
            this.dimension = planet.getDimension().location().toString();
            this.boundingBoxMin = new double[]{
                planet.getBoundingBoxMin().x,
                planet.getBoundingBoxMin().y,
                planet.getBoundingBoxMin().z
            };
            this.boundingBoxMax = new double[]{
                planet.getBoundingBoxMax().x,
                planet.getBoundingBoxMax().y,
                planet.getBoundingBoxMax().z
            };
            if (planet.getDecorations().isPresent())
                this.decorations = planet.getDecorations().get();
            else
                this.decorations = List.of();
            this.description = planet.getDescription();
        }
    }
    
    /**
     * Container class for the JSON configuration file.
     */
    public static class PlanetsConfig {
        public List<PlanetConfig> planets = new ArrayList<>();
        public SunConfig sun;
        public PlanetsConfig() {}
        
        public PlanetsConfig(Collection<Planet> planets, Sun sun) {
            this.planets = planets.stream()
                .map(PlanetConfig::new)
                .toList();
            this.sun = new SunConfig(sun);
        }
    }
    
    /**
     * Initializes the planet registry with the given config directory.
     *
     * @param configDir The configuration directory path
     */
    public static void initialize(@NotNull Path configDir) {
        Objects.requireNonNull(configDir, "Config directory cannot be null");
        configPath = configDir.resolve(PLANETS_CONFIG_FILE);
        LOGGER.info("Planet registry initialized with config path: {}", configPath);
    }
    
    /**
     * Loads planets from the JSON configuration file.
     */
    public static void loadPlanets() {
        Path currentConfigPath = configPath;
        if (currentConfigPath == null) {
            LOGGER.error("Planet registry not initialized - cannot load planets");
            return;
        }
        
        synchronized (registryLock) {
            try {
                if (!Files.exists(currentConfigPath)) {
                    LOGGER.info("No planets configuration file found, creating default configuration");
                    createDefaultConfigurationUnsafe();
                    return;
                }
                
                String json = Files.readString(currentConfigPath);
                PlanetsConfig config = GSON.fromJson(json, PlanetsConfig.class);
                
                if (config == null || config.planets == null) {
                    LOGGER.warn("Invalid planets configuration file, creating default configuration");
                    createDefaultConfigurationUnsafe();
                    return;
                }
                
                planets.clear();
                planetsByDimension.clear();
                
                for (PlanetConfig planetConfig : config.planets) {
                    try {
                        Planet planet = createPlanetFromConfig(planetConfig);
                        registerPlanetUnsafe(planet);
                        LOGGER.debug("Loaded planet: {}", planet.getId());
                    } catch (Exception e) {
                        LOGGER.error("Failed to load planet configuration: {}", planetConfig.id, e);
                    }
                }
                
                LOGGER.info("Loaded {} planets from configuration", planets.size());
                try {
                    sun = createSunFromConfig(config.sun);
                } catch (Exception e) {
                    LOGGER.error("Failed to load sun configuration: ", e);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read planets configuration file", e);
                createDefaultConfigurationUnsafe();
            } catch (JsonSyntaxException e) {
                LOGGER.error("Invalid JSON in planets configuration file", e);
                createDefaultConfigurationUnsafe();
            }
        }
    }
    
    /**
     * Creates a default configuration with example planets. Must be called within registryLock.
     */
    private static void createDefaultConfigurationUnsafe() {
        planets.clear();
        planetsByDimension.clear();
        
        // Add example planets
        try {
            // Overworld planet
            Planet overworld = new Planet(
                "overworld",
                "Overworld",
                Level.OVERWORLD,
                new Vec3(-100, -100, -100),
                new Vec3(100, 100, 100),
                List.of(new Planet.PlanetDecoration(Planet.PlanetDecoration.ATMOSPHERE, 1.05f, Color.WHITE.getRGB())),
                "The main world where players spawn",
                new Vec2(-1000, -1000),
                new Vec2(1000, 1000)
            );
            registerPlanetUnsafe(overworld);
            
            // Nether planet
            Planet nether = new Planet(
                "nether",
                "The Nether",
                Level.NETHER,
                new Vec3(200, -50, -100),
                new Vec3(400, 150, 100),
                List.of(
                        new Planet.PlanetDecoration(Planet.PlanetDecoration.ATMOSPHERE, 1f, Color.RED.getRGB()),
                        new Planet.PlanetDecoration(Planet.PlanetDecoration.RINGS, 1.2f, Color.RED.getRGB())
                ),
                "A hellish dimension filled with lava and dangerous creatures",
                new Vec2(-1000, -1000),
                new Vec2(1000, 1000)
            );
            registerPlanetUnsafe(nether);
            
            // End planet
            Planet end = new Planet(
                "end",
                "The End",
                Level.END,
                new Vec3(-200, 100, -100),
                new Vec3(0, 300, 100),
                List.of(new Planet.PlanetDecoration(Planet.PlanetDecoration.ATMOSPHERE, 1f, Color.WHITE.getRGB())),
                "The final dimension, home to the Ender Dragon",
                new Vec2(-1000, -1000),
                new Vec2(1000, 1000)
            );
            registerPlanetUnsafe(end);
            sun = new Sun(
                    new Vec3(-500, 0, 0),
                    new Vec3(-300, 200, 200),
                    1500
            );
            LOGGER.info("Created default planet configuration with {} planets", planets.size());
            
        } catch (Exception e) {
            LOGGER.error("Failed to create default planet configuration", e);
        }
    }
    @NotNull
    private static Sun createSunFromConfig(@NotNull SunConfig config) {
        Objects.requireNonNull(config, "Sun config cannot be null");
        if (config.boundingBoxMin == null || config.boundingBoxMin.length != 3) {
            throw new IllegalArgumentException("Invalid bounding box minimum coordinates");
        }
        if (config.boundingBoxMax == null || config.boundingBoxMax.length != 3) {
            throw new IllegalArgumentException("Invalid bounding box maximum coordinates");
        }
        return new Sun(
                new Vec3(config.boundingBoxMin[0], config.boundingBoxMin[1], config.boundingBoxMin[2]),
                new Vec3(config.boundingBoxMax[0], config.boundingBoxMax[1], config.boundingBoxMax[2]),
                config.hurtRadius
        );
    }
    /**
     * Creates a Planet instance from a PlanetConfig.
     */
    @NotNull
    private static Planet createPlanetFromConfig(@NotNull PlanetConfig config) {
        Objects.requireNonNull(config, "Planet config cannot be null");
        
        if (config.id == null || config.id.trim().isEmpty()) {
            throw new IllegalArgumentException("Planet ID cannot be null or empty");
        }
        if (config.name == null || config.name.trim().isEmpty()) {
            throw new IllegalArgumentException("Planet name cannot be null or empty");
        }
        if (config.dimension == null || config.dimension.trim().isEmpty()) {
            throw new IllegalArgumentException("Planet dimension cannot be null or empty");
        }
        if (config.boundingBoxMin == null || config.boundingBoxMin.length != 3) {
            throw new IllegalArgumentException("Invalid bounding box minimum coordinates");
        }
        if (config.boundingBoxMax == null || config.boundingBoxMax.length != 3) {
            throw new IllegalArgumentException("Invalid bounding box maximum coordinates");
        }
        
        ResourceKey<Level> dimension = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            net.minecraft.resources.ResourceLocation.parse(config.dimension)
        );
        
        Vec3 boundingBoxMin = new Vec3(config.boundingBoxMin[0], config.boundingBoxMin[1], config.boundingBoxMin[2]);
        Vec3 boundingBoxMax = new Vec3(config.boundingBoxMax[0], config.boundingBoxMax[1], config.boundingBoxMax[2]);
        
        return new Planet(config.id, config.name, dimension, boundingBoxMin, boundingBoxMax, config.decorations, config.description, new Vec2((float)config.physicalMin[0], (float)config.physicalMin[1]),  new Vec2((float)config.physicalMax[0], (float)config.physicalMax[1]));
    }
    
    /**
     * Registers a planet in the registry.
     *
     * @param planet The planet to register
     */
    public static void registerPlanet(@NotNull Planet planet) {
        Objects.requireNonNull(planet, "Planet cannot be null");
        
        synchronized (registryLock) {
            registerPlanetUnsafe(planet);
        }
    }
    
    /**
     * Registers a planet in the registry. Must be called within registryLock.
     *
     * @param planet The planet to register
     */
    private static void registerPlanetUnsafe(@NotNull Planet planet) {
        planets.put(planet.getId(), planet);
        planetsByDimension.put(planet.getDimension(), planet);
        
        LOGGER.debug("Registered planet: {} for dimension: {}", planet.getId(), planet.getDimension().location());
    }
    
    /**
     * Unregisters a planet from the registry.
     *
     * @param planetId The ID of the planet to unregister
     * @return The unregistered planet, or null if not found
     */
    @Nullable
    public static Planet unregisterPlanet(@NotNull String planetId) {
        Objects.requireNonNull(planetId, "Planet ID cannot be null");
        
        synchronized (registryLock) {
            Planet planet = planets.remove(planetId);
            if (planet != null) {
                planetsByDimension.remove(planet.getDimension());
                LOGGER.debug("Unregistered planet: {}", planetId);
            }
            return planet;
        }
    }
    
    /**
     * Gets a planet by its ID.
     *
     * @param planetId The planet ID
     * @return The planet, or null if not found
     */
    @Nullable
    public static Planet getPlanet(@NotNull String planetId) {
        Objects.requireNonNull(planetId, "Planet ID cannot be null");
        return planets.get(planetId);
    }

    /**
     * Gets the current sun.
     * @return The sun, or null if doesn't exist
     */
    @Nullable
    public static Sun getSun() {
        return sun;
    }
    /**
     * Sets the current sun
     * @param _sun The sun to set
     */
    public static boolean setSun(Sun _sun) {
        sun = _sun;
        return true;
    }
    /**
     * Gets a planet by its dimension.
     *
     * @param dimension The dimension key
     * @return The planet, or null if not found
     */
    @Nullable
    public static Planet getPlanetByDimension(@NotNull ResourceKey<Level> dimension) {
        Objects.requireNonNull(dimension, "Dimension cannot be null");
        return planetsByDimension.get(dimension);
    }
    
    /**
     * Gets all registered planets.
     *
     * @return An unmodifiable collection of all planets
     */
    @NotNull
    public static Collection<Planet> getAllPlanets() {
        synchronized (registryLock) {
            return Collections.unmodifiableCollection(new ArrayList<>(planets.values()));
        }
    }
    
    /**
     * Finds planets that contain the given position within their bounding boxes.
     *
     * @param position The position to check
     * @return A list of planets containing the position
     */
    @NotNull
    public static List<Planet> getPlanetsAtPosition(@NotNull Vec3 position) {
        Objects.requireNonNull(position, "Position cannot be null");
        
        synchronized (registryLock) {
            return planets.values().stream()
                .filter(planet -> planet.isWithinBounds(position))
                .toList();
        }
    }
    
    /**
     * Checks if a planet with the given ID exists.
     *
     * @param planetId The planet ID to check
     * @return true if the planet exists, false otherwise
     */
    public static boolean hasPlanet(@NotNull String planetId) {
        Objects.requireNonNull(planetId, "Planet ID cannot be null");
        return planets.containsKey(planetId);
    }
    
    /**
     * Gets the number of registered planets.
     *
     * @return The number of planets
     */
    public static int getPlanetCount() {
        return planets.size(); // ConcurrentHashMap.size() is thread-safe
    }
    
    /**
     * Clears all registered planets.
     */
    public static void clear() {
        synchronized (registryLock) {
            planets.clear();
            planetsByDimension.clear();
            sun = null;
            LOGGER.debug("Cleared all planets from registry");
        }
    }

    public static void init() {
        // Initialize with server config directory
        Path serverConfigDir = Paths.get("config");
        initialize(serverConfigDir);
        loadPlanets();
        
        LOGGER.info("Planet registry loaded on server start");
    }
    
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LOGGER.info("Planet registry server stopped");
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Sync planets to the player when they join
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlanetSyncPacket syncPacket = PlanetSyncPacket.createSyncPacket();
            PacketDistributor.sendToPlayer(serverPlayer, syncPacket);
            LOGGER.debug("Synchronized planets to player: {}", serverPlayer.getName().getString());
        }
    }
    
    /**
     * Synchronizes planets to all connected players.
     */
    public static void syncToAllPlayers() {
        PlanetSyncPacket syncPacket = PlanetSyncPacket.createSyncPacket();
        PacketDistributor.sendToAllPlayers(syncPacket);
        LOGGER.info("Synchronized planets to all players");
    }
} 