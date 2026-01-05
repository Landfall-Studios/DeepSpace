package world.landfall.deepspace;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.DoubleSupplier;

public class ModBlockStressValues {
    public static void register() {
        BlockStressValues.IMPACTS.register(ModBlocks.OXYGENATOR_BLOCK.get(), () -> 32);
    }
}
