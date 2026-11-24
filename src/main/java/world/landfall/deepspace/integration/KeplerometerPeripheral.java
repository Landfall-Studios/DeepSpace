package world.landfall.deepspace.integration;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jspecify.annotations.Nullable;
import world.landfall.deepspace.blockentity.KeplerometerBlockEntity;

public class KeplerometerPeripheral implements IPeripheral {

    private final KeplerometerBlockEntity blockEntity;

    public KeplerometerPeripheral(KeplerometerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "keplerometer";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return false;
    }

    @LuaFunction
    public String getMethods() {
        return """
                getApogee() -> number
                getPerigee() -> number
                willEscape() -> boolean
                willCrash() -> boolean
                getMethods() -> string""";
    }
    @LuaFunction
    public float getApogee() {
        return blockEntity.getLastApogee();
    }
    @LuaFunction
    public float getPerigee() {
        return blockEntity.getLastPerigee();
    }
    @LuaFunction
    public boolean willCrash() {
        return blockEntity.willCrash();
    }
    @LuaFunction
    public boolean willEscape() {
        return blockEntity.willEscape();
    }

}
