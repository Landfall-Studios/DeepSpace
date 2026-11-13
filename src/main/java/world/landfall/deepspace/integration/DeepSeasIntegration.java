package world.landfall.deepspace.integration;

import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import com.maxenonyme.createsubmarine.submarine.mixin.EntityWaterPhysicsMixin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DeepSeasIntegration {

    public static boolean isPlayerOxygenated(Player player, Level level) {
        return CompartmentTracker.isInSealedExact(level, player.getEyePosition());
    }
}
