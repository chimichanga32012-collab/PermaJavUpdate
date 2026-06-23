package tech.sebazcrc.permadeath.world.beginning.generator;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import java.util.Random;

public class EmptyGenerator extends ChunkGenerator {
    
    @Override
    @NotNull
    public ChunkData generateChunkData(@NotNull World world, @NotNull Random cRandom, int chunkX, int chunkZ, @NotNull BiomeGrid biomes) {
        return createChunkData(world);
    }
}
