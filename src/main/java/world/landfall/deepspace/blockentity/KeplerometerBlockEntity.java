package world.landfall.deepspace.blockentity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import world.landfall.deepspace.Deepspace;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.Util;
import world.landfall.deepspace.network.KeplerometerSublevelDataPacket;
import world.landfall.deepspace.planet.PlanetRegistry;

import java.util.Arrays;
import java.util.List;

public class KeplerometerBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation {
    public static final BlockEntityType<KeplerometerBlockEntity> TYPE = BlockEntityType.Builder.of(
            KeplerometerBlockEntity::new,
            ModBlocks.KEPLEROMETER_BLOCK.get()
    ).build(null);

    private boolean wasLastCrashing = false;

    private float lastPerigee = 0f;
    private float lastApogee = 0f;

    public KeplerometerBlockEntity(BlockPos pos, BlockState blockState) {
        super(TYPE, pos, blockState);
    }

    public void setLastPerigee(float lastPerigee) {
        this.lastPerigee = lastPerigee;
    }
    public void setLastApogee(float lastApogee) {
        this.lastApogee = lastApogee;
    }
    public float getLastPerigee() { return this.lastPerigee; }
    public float getLastApogee() { return this.lastApogee; }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        var sublevelAccess = SableCompanion.INSTANCE.getContaining(this);

        var planet = PlanetRegistry.getClosestPlanet(new Vec3(
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ()
        ));


        CreateLang.text("Orbit Stats:")
                .forGoggles(tooltip);
        if (lastPerigee == 0f || sublevelAccess == null || planet == null) {
//            tooltip.add(Component.literal("Not in orbit!").withStyle(ChatFormatting.RED));
            CreateLang.text("Not in orbit!").style(ChatFormatting.RED)
                    .forGoggles(tooltip);
            return true;
        }
        var willCrash = willCrash();
        var willEscape = willEscape();

        CreateLang.text("Minimum Distance:")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.text(" " + lastPerigee)
                .style(willCrash ? ChatFormatting.RED : ChatFormatting.AQUA)
                .forGoggles(tooltip); // buffer
        if (willEscape && !willCrash) {
            CreateLang.text("Maximum Distance:")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            CreateLang.text(" Will Escape Orbit!")
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip);
            return true;
        }
        CreateLang.text("Maximum Distance:")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.text(" " + lastApogee)
                .style(willCrash ? ChatFormatting.RED : ChatFormatting.AQUA)
                .forGoggles(tooltip); // buffer
        if (willCrash)
            CreateLang.text("Orbital Trajectory Will Fail!")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
//
//        tooltip.add(
//                Component.literal("Minimum: ").append(
//                        Component.literal(""+lastPerigee).withStyle(ChatFormatting.AQUA)
//                )
//        );tooltip.add(
//                Component.literal("Maximum: ").append(
//                        Component.literal(""+lastApogee).withStyle(ChatFormatting.AQUA)
//                )
//        );


        return true;
    }

    public void updateNeighbors() {
        level.updateNeighborsAt(
                worldPosition,
                level.getBlockState(worldPosition).getBlock()
        );
        Arrays.stream(Direction.values()).forEach(d -> {
            level.updateNeighborsAt(
                    worldPosition.relative(d),
                    level.getBlockState(worldPosition.relative(d)).getBlock()
            );
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.dimension().location().equals(Deepspace.path("space")))
            return;
        if (willCrash() && !wasLastCrashing) {
            wasLastCrashing = true;
            updateNeighbors();

        }
        if (!willCrash() && wasLastCrashing) {
            wasLastCrashing = false;
            updateNeighbors();

        }
        var sublevelAccess = SableCompanion.INSTANCE.getContaining(this);
        if (sublevelAccess == null) return;
        if (level.getGameTime() % 20 == 0) {
            if (level.isClientSide)
                PacketDistributor.sendToServer(new KeplerometerSublevelDataPacket.Serverbound(worldPosition, sublevelAccess.getUniqueId()));
            updateNeighbors();
        }
    }
    public boolean willCrash() {

        var planet = PlanetRegistry.getClosestPlanet(new Vec3(
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ()
        ));
        if (planet == null) return false;
        var safeDistance = planet.getBoundingBoxMax().distanceTo(planet.getBoundingBoxMin()) / 2f;
        return lastPerigee <= safeDistance + 20f;
    }
    public boolean willEscape() {

        var planet = PlanetRegistry.getClosestPlanet(new Vec3(
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ()
        ));
        if (planet == null) return false;
        return lastPerigee < -1 || lastApogee < -1;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (t instanceof KeplerometerBlockEntity blockEntity) blockEntity.tick();
    }

}
