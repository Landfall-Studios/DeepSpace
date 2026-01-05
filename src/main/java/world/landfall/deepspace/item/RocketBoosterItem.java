package world.landfall.deepspace.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import world.landfall.deepspace.ModAttatchments;

public class RocketBoosterItem extends Item {
    public RocketBoosterItem() {
        super(
                new Properties()
                        .rarity(Rarity.UNCOMMON)
        );

    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        player.setData(ModAttatchments.IS_FLYING_JETPACK, true);
        var velocity = player.getData(ModAttatchments.JETPACK_VELOCITY);
        var item = player.getItemInHand(usedHand);
        var newVelocity = player.getLookAngle().toVector3f().mul(2);
        player.setData(ModAttatchments.JETPACK_VELOCITY, new Vector3f(velocity).add(newVelocity));
        player.getCooldowns().addCooldown(item.getItem(), 40);
        item.consume(1, player);
        player.level().playSound(
                player,
                player.position().x, player.position().y, player.position().z,
                SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.PLAYERS
        );
        var random = level.getRandom();
        for (int i = 0; i < 128; i++) {
            var offset = new Vector3f(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1).mul(.4f);
            var oppositeForce = new Vector3f(newVelocity).normalize().mul(-.1f);
            offset.sub(oppositeForce.mul(2));
            level.addParticle(ParticleTypes.FLAME,
                    player.getX() + offset.x + oppositeForce.x * i, player.getY() + offset.y + oppositeForce.y * i, player.getZ() + offset.z + oppositeForce.z * i,
                    oppositeForce.x, oppositeForce.y, oppositeForce.z
            );
        }
        return InteractionResultHolder.consume(item);
    }
}
