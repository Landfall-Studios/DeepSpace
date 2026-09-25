package world.landfall.deepspace.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import world.landfall.deepspace.ModBlocks;
import world.landfall.deepspace.ModPlantTypes;

public class SelenicPlantBlock extends AbstractPlantBlock {
    public SelenicPlantBlock(Properties properties) {
        super(properties
                        .sound(SoundType.AZALEA)
                , ModPlantTypes.SELENIC.getId());
    }
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !hasCollision;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        var player = level.getNearestPlayer(
                TargetingConditions.forNonCombat(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
        if (player == null) return;
        var distance = pos.getCenter().distanceTo(player.position());
        if (distance > 5) return;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        double d0 = (double)i + random.nextDouble();
        double d1 = (double)j + random.nextDouble();
        double d2 = (double)k + random.nextDouble();
        if ((state.is(ModBlocks.SELENIC_CORE_BLOCK) || random.nextDouble() > 0.7F) && !state.canOcclude())
            level.addParticle(ParticleTypes.DRAGON_BREATH, d0, d1, d2, 0.0F, 0.0F, 0.0F);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for(int l = 0; l < 1; ++l) {
            blockpos$mutableblockpos.set(i + Mth.nextInt(random, -10, 10), j + random.nextInt(7)-2, k + Mth.nextInt(random, -10, 10));
            BlockState blockstate = level.getBlockState(blockpos$mutableblockpos);
            if (!blockstate.isCollisionShapeFullBlock(level, blockpos$mutableblockpos)) {
                level.addParticle(ParticleTypes.MYCELIUM, (double)blockpos$mutableblockpos.getX() + random.nextDouble(), (double)blockpos$mutableblockpos.getY() + random.nextDouble(), (double)blockpos$mutableblockpos.getZ() + random.nextDouble(), 0.0F, 0.0F, 0.0F);
            }
        }
    }

    @Override
    public float spreadSpeed() {
        return .2f;
    }

    @Override
    public int spreadRadius() {
        return 1;
    }
}
