package world.landfall.deepspace;

import net.minecraft.core.BlockPos;
import world.landfall.deepspace.blockentity.OxygenatorBlockEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class ClientOxygenatorTracker {
    private static final HashMap<BlockPos, OxygenatorBlockEntity> MAP = new HashMap<>();

    public static void add(OxygenatorBlockEntity blockEntity) {
        MAP.put(blockEntity.getBlockPos(), blockEntity);
    }
    public static void remove(OxygenatorBlockEntity blockEntity) {
        remove(blockEntity.getBlockPos());
    }
    public static void remove(BlockPos blockPos) {
        MAP.remove(blockPos);
    }
    public static Collection<OxygenatorBlockEntity> loop() {
        return MAP.values();
    }
}
