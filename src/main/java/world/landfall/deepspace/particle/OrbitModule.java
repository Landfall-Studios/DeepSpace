package world.landfall.deepspace.particle;

import foundry.veil.api.quasar.data.module.CodeModule;
import foundry.veil.api.quasar.emitters.module.UpdateParticleModule;
import foundry.veil.api.quasar.particle.ParticleModuleSet;
import foundry.veil.api.quasar.particle.QuasarParticle;
import org.joml.Vector3dc;

// Made in part by Claude Sonnet 5; this stuff really confused me, and I felt like i should disclose that I didn't write this bit

public class OrbitModule implements UpdateParticleModule {
    private final Vector3dc center;
    private final double radius;
    private final double angularSpeed; // radians per tick

    public OrbitModule(Vector3dc center, double radius, double angularSpeed) {
        this.center = center;
        this.radius = radius;
        this.angularSpeed = angularSpeed;
    }

    @Override
    public void update(QuasarParticle particle) {
//        double angle = particle.getAge() * angularSpeed;
//        double x = center.x() + radius * Math.cos(angle);
//        double z = center.z() + radius * Math.sin(angle);
//        particle.getPosition().set(x, center.y(), z);
        System.out.println("ran, " + particle.getPosition());
    }
    public static class OrbitModuleData implements CodeModule {
        private final Vector3dc center;
        private final double radius;
        private final double angularSpeed;

        public OrbitModuleData(Vector3dc center, double radius, double angularSpeed) {
            this.center = center;
            this.radius = radius;
            this.angularSpeed = angularSpeed;
        }

        @Override
        public void addModules(ParticleModuleSet.Builder builder) {
            builder.addModule(new OrbitModule(this.center, this.radius, this.angularSpeed));
        }
    }
}