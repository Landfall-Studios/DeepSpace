package world.landfall.deepspace.integration;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class IrisIntegration {
    private static Logger logger = LogUtils.getLogger();
    private static Class IRIS_INSTANCE_CLASS;
    private static Object IRIS_INSTANCE;
    private static boolean hasFailedPipeline;
    static {
        try {
            IRIS_INSTANCE_CLASS = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            IRIS_INSTANCE = IRIS_INSTANCE_CLASS.getDeclaredMethod("getInstance").invoke(null);
        } catch (ReflectiveOperationException | NullPointerException e) {
//            logger.error("", e);
        }
        hasFailedPipeline = false;
    }
    public static boolean isShaderPackEnabled() {
        try {
            return (Boolean)IRIS_INSTANCE_CLASS.getDeclaredMethod("isShaderPackInUse").invoke(IRIS_INSTANCE);
        } catch (ReflectiveOperationException  | NullPointerException e) {
//            logger.error("", e);
            return false;
        }
    }
    public static void bindPipeline() {
        try {
            Class iris = Class.forName("net.irisshaders.iris.Iris");
            var pipelineManager = iris.getDeclaredMethod("getPipelineManager").invoke(null);
            Optional<?> pipeline = (Optional<?>)Class.forName("net.irisshaders.iris.pipeline.PipelineManager").getDeclaredMethod("getPipeline").invoke(pipelineManager);
            pipeline.map(i -> {
                try {
                    if (Class.forName("net.irisshaders.iris.pipeline.IrisRenderingPipeline").isInstance(i))
                        return i;
                    return null;
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }).ifPresent(i -> {
                try {
                    Class.forName("net.irisshaders.iris.pipeline.IrisRenderingPipeline").getDeclaredMethod("bindDefault").invoke(i);
                } catch (ReflectiveOperationException e) {
                    logger.error("", e);
                }
            });
        } catch (ReflectiveOperationException | NullPointerException e) {
            if (!hasFailedPipeline) {
                logger.warn("Iris isn't present in the pack, or is an unsupported version");
                hasFailedPipeline = true;
            }
        }
    }
}