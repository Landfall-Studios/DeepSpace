package world.landfall.deepspace.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.joml.Vector3d;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.particle.OrbitModule;
import world.landfall.deepspace.planet.Planet;
import world.landfall.deepspace.planet.PlanetRegistry;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class AsteroidRenderSystem {

    public static final ResourceLocation ASTEROID_EMITTER_ID = Deepspace.path("asteroids");
    public static List<ParticleEmitter> ASTEROID_EMITTERS = new ArrayList<>();


//    @SubscribeEvent
//    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
//        Player player = event.getEntity();
//        if (!(player.level() instanceof ClientLevel level)) return;
//        System.out.println("Logged in");
//        resetParticles(player, level);
//    }
//    @SubscribeEvent
//    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
//        Player player = event.getEntity();
//        if (!(player.level() instanceof ClientLevel level)) return;
//        System.out.println("Changed dimension");
//        resetParticles(player, level);
//    }
    @SubscribeEvent
    public static void onLoad(ClientPlayerNetworkEvent.LoggingIn event) {
        if (event.getPlayer().level() instanceof ClientLevel clientLevel)
            resetParticles(clientLevel);
    }

    public static void resetParticles(ClientLevel level) {
        if (!ASTEROID_EMITTERS.isEmpty()) {
            ASTEROID_EMITTERS.forEach(
                    ParticleEmitter::remove
            );
            ASTEROID_EMITTERS.clear();
        }
        System.out.println("Reset particles ran");
        var dim = level.dimension().location();
        if (!dim.equals(Deepspace.path("space")))
            return;
        var particleSystem = VeilRenderSystem.renderer().getParticleManager();
        for (var planet : PlanetRegistry.getAllPlanets()) {
            var decorations = planet.getDecorations();
            if (decorations.isEmpty())
                return;
            decorations.stream().filter(d -> d.type().equals(Planet.PlanetDecoration.ASTEROIDS)).findFirst().ifPresent(decoration -> {
                System.out.println("Found a decoration on planet "+planet.getName());
                var emitter = particleSystem.createEmitter(ASTEROID_EMITTER_ID);
                if (emitter == null) {
                    System.out.println("Couldn't make the emitter!");
                    return;
                }
                emitter.addCodeModule(new OrbitModule.OrbitModuleData(
                        new Vector3d(), planet.getBoundingBoxMax().distanceTo(planet.getBoundingBoxMin()), 3
                ));
                emitter.setPosition(planet.getCenter().add(planet.getBoundingBoxMax().distanceTo(planet.getBoundingBoxMin()) * decoration.scale(), 0, 0));
                particleSystem.addParticleSystem(
                        emitter
                );
                ASTEROID_EMITTERS.add(emitter);
            });
        }
        System.out.println("Finished loop");
    }
}
