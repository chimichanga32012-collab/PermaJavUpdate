package tech.sebazcrc.permadeath.world.beginning.generator;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.jetbrains.annotations.NotNull;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TreePopulator extends BlockPopulator {
    private static final Set<Coordinates> chunks = ConcurrentHashMap.newKeySet();
    private static final Set<Coordinates> unpopulatedChunks = ConcurrentHashMap.newKeySet();

    @Override
    @Deprecated
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        Coordinates chunkCoordinates = new Coordinates(chunkX, chunkZ);

        if (!chunks.contains(chunkCoordinates)) {
            chunks.add(chunkCoordinates);
            unpopulatedChunks.add(chunkCoordinates);
        }

        for (Coordinates unpopulatedChunk : unpopulatedChunks) {
            if (chunks.contains(unpopulatedChunk.left())
                    && chunks.contains(unpopulatedChunk.right())
                    && chunks.contains(unpopulatedChunk.above())
                    && chunks.contains(unpopulatedChunk.below())
                    && chunks.contains(unpopulatedChunk.upperLeft())
                    && chunks.contains(unpopulatedChunk.upperRight())
                    && chunks.contains(unpopulatedChunk.lowerLeft())
                    && chunks.contains(unpopulatedChunk.lowerRight())) {
                actuallyPopulate(world, random, world.getChunkAt(unpopulatedChunk.x, unpopulatedChunk.z));
                unpopulatedChunks.remove(unpopulatedChunk);
            }
        }
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // En versiones modernas de Bukkit/Paper (1.21.5), se prefiere usar LimitedRegion, 
        // pero mantenemos la lógica de herencia compatible con las llamadas anteriores si el generador las usa.
    }

    private void actuallyPopulate(World world, Random random, Chunk chunk) {
        int x = random.nextInt(16);
        int z = random.nextInt(16);
        int y = world.getMaxHeight() - 1;

        while (y > world.getMinHeight() && chunk.getBlock(x, y, z).getType() == Material.AIR) {
            --y;
        }

        if (y > world.getMinHeight() && y < world.getMaxHeight() - 1) {
            if (y >= 100 && y < 105) {
                Location treeLoc = chunk.getBlock(x, y + 1, z).getLocation();
                
                world.generateTree(treeLoc, TreeType.CHORUS_PLANT, new BlockChangeDelegate() {
                    @Override
                    public boolean setBlockData(int i, int i1, int i2, @NotNull BlockData blockData) {
                        if (blockData.getMaterial() == Material.CHORUS_FLOWER) {
                            world.getBlockAt(i, i1, i2).setType(Material.SEA_LANTERN, false);
                        } else if (blockData.getMaterial() == Material.CHORUS_PLANT) {
                            world.getBlockAt(i, i1, i2).setType(Material.END_STONE_BRICK_WALL, false);
                        }
                        return true;
                    }

                    @Override
                    @NotNull
                    public BlockData getBlockData(int i, int i1, int i2) {
                        return world.getBlockAt(i, i1, i2).getBlockData();
                    }

                    @Override
                    public int getHeight() {
                        return world.getMaxHeight();
                    }

                    @Override
                    public boolean isEmpty(int i, int i1, int i2) {
                        return world.getBlockAt(i, i1, i2).getType().isAir();
                    }
                });
            }
        }
    }

    private static class Coordinates {
        public final int x;
        public final int z;

        public Coordinates(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public Coordinates left() {
            return new Coordinates(x - 1, z);
        }

        public Coordinates right() {
            return new Coordinates(x + 1, z);
        }

        public Coordinates above() {
            return new Coordinates(x, z - 1);
        }

        public Coordinates below() {
            return new Coordinates(x, z + 1);
        }

        public Coordinates upperLeft() {
            return new Coordinates(x - 1, z - 1);
        }

        public Coordinates upperRight() {
            return new Coordinates(x + 1, z - 1);
        }

        public Coordinates lowerLeft() {
            return new Coordinates(x - 1, z + 1);
        }

        public Coordinates lowerRight() {
            return new Coordinates(x + 1, z + 1);
        }

        @Override
        public int hashCode() {
            return (x + z) * (x + z + 1) / 2 + x;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Coordinates other = (Coordinates) obj;
            return x == other.x && z == other.z;
        }
    }
}
