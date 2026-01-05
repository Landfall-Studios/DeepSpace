package world.landfall.deepspace;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModAttatchments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Deepspace.MODID);
    public static final Supplier<AttachmentType<Boolean>> IS_FLYING_JETPACK = ATTACHMENT_TYPES.register(
            "is_flying_jetpack", () -> AttachmentType.<Boolean>builder(() -> false).build()
    );
    public static final Supplier<AttachmentType<Boolean>> IS_ROCKETING_FORWARD = ATTACHMENT_TYPES.register(
            "is_rocketing_forward", () -> AttachmentType.<Boolean>builder(() -> false).build()
    );
    public static final Supplier<AttachmentType<Vector3f>> JETPACK_VELOCITY = ATTACHMENT_TYPES.register(
            "jetpack_velocity", () -> AttachmentType.<Vector3f>builder(() -> new Vector3f()).build()
    );
    public static final Supplier<AttachmentType<Float>> LAST_OXYGENATED = ATTACHMENT_TYPES.register(
            "last_oxygenated", () -> AttachmentType.builder(() -> 0f).build()
    );
    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}