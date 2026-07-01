package world.landfall.deepspace;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicBoolean;

public class Util {

    public static boolean isPlayerBeingTracked(ServerPlayer player, Level level) {
        var sublevelContainer = SubLevelContainer.getContainer(level);
        var isTrackingSublevel = new AtomicBoolean(false);
        sublevelContainer.getAllSubLevels().forEach(s -> {
            if (s instanceof ServerSubLevel subLevel) {
                if (subLevel.getTrackingPlayers().contains(player.getUUID()))
                    isTrackingSublevel.set(true);
            }
        });
        return isTrackingSublevel.get();
    }
}
