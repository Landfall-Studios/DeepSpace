package world.landfall.deepspace.mixin;

import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(WindmillBearingBlockEntity.class)
public abstract class MixinWindmillBearingBlockEntity extends MechanicalBearingBlockEntity {
    @Unique
    private static Map<String, Float> atmosphereDensityMap = new HashMap<>();

    static {
        atmosphereDensityMap.put("deepspace:space", 0.05f);
        atmosphereDensityMap.put("deepspace:luna", 0.1f);
        atmosphereDensityMap.put("deepspace:sarrion", 1.5f);
    }

    @Shadow
    protected float lastGeneratedSpeed;

    @Shadow
    protected abstract float getAngleSpeedDirection();

    public MixinWindmillBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(at = @At("HEAD"), method = "Lcom/simibubi/create/content/contraptions/bearing/WindmillBearingBlockEntity;getGeneratedSpeed()F",remap = false, cancellable = true)
    public void getGeneratedSpeed(CallbackInfoReturnable<Float> cir) {
        if (!this.running) {
            cir.setReturnValue(0f);
            cir.cancel();
            return;
        }
        if (movedContraption == null) {
            cir.setReturnValue(this.lastGeneratedSpeed);
            cir.cancel();
            return;
        }
        int sails = ((BearingContraption) movedContraption.getContraption()).getSailBlocks()
                / AllConfigs.server().kinetics.windmillSailsPerRPM.get();
        var dim = level.dimension().location();
        cir.setReturnValue(
                Mth.clamp(sails, 1, 16) *
                this.getAngleSpeedDirection() *
                atmosphereDensityMap.getOrDefault(dim.toString(), 1f)
        );

        cir.cancel();

    }

}
