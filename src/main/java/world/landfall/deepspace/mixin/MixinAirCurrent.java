package world.landfall.deepspace.mixin;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.AirFlowParticleData;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.landfall.deepspace.integration.CreateIntegration;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = AirCurrent.class, remap = false)
public abstract class MixinAirCurrent {
    @Shadow public final IAirCurrentSource source;
    @Shadow protected List<Entity> caughtEntities = new ArrayList<>();
    @Shadow public boolean pushing;
    @Shadow public float maxDistance;
    @Shadow public Direction direction;
    @Shadow protected List<Pair<TransportedItemStackHandlerBehaviour, FanProcessingType>> affectedItemHandlers;

    protected MixinAirCurrent(IAirCurrentSource source) {

        this.source = source;
    }
    @Inject(at = @At("TAIL"), method = "<init>", remap = false)
    private void init(CallbackInfo ci) {

    }
    @Inject(at = @At("HEAD"), method = "tick", remap = false, cancellable = true)
    private void tick(CallbackInfo ci) {
        if (direction == null)
            rebuild();
        Level world = source.getAirCurrentWorld();
        if (world != null && world.isClientSide) {
            float offset = pushing ? 0.5f : maxDistance + .5f;
            Vec3 pos = VecHelper.getCenterOf(source.getAirCurrentPos())
                    .add(Vec3.atLowerCornerOf(direction.getNormal())
                            .scale(offset));
            if (world.random.nextFloat() < AllConfigs.client().fanParticleDensity.get())
                world.addParticle(new AirFlowParticleData(source.getAirCurrentPos()), pos.x, pos.y, pos.z, 0, 0, 0);
        }

        tickAffectedEntities(world);
        tickAffectedHandlers();

        CreateIntegration.handleAir(caughtEntities, affectedItemHandlers);
        ci.cancel();
    }
    @Shadow public abstract void rebuild();
    @Shadow protected abstract void tickAffectedEntities(Level world);
    @Shadow public abstract void tickAffectedHandlers();
}
