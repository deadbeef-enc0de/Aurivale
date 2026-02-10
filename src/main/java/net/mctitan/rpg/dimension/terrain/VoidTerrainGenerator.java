package net.mctitan.rpg.dimension.terrain;

import net.mctitan.rpg.dimension.TerrainGenerator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinNoiseGenerator;

import java.util.Random;

public class VoidTerrainGenerator extends TerrainGenerator {
    private static final double BOUND = 0.05;
    public static final int FLOOR_HEIGHT = 4;
    public static final Material FLOOR_BLOCK = Material.STONE;

    @Override public boolean canSpawn(World world, int x, int z) { return true; }

    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return false; }
    @Override public boolean shouldGenerateMobs() { return false; }
    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateStructures() { return false; }
    @Override public boolean shouldGenerateSurface() { return false; }

    @Override
    public void generateNoise(WorldInfo info, Random random, int cx, int cz, ChunkGenerator.ChunkData data) {
    }

    @Override
    public void generateSurface(WorldInfo info, Random random, int cx, int cz, ChunkGenerator.ChunkData data) {
        PerlinNoiseGenerator noise = new PerlinNoiseGenerator(info.getSeed());

        for(int x = 0; x < 16; ++x) {
            for(int z = 0; z < 16; ++z) {
                double dx = 16 * cx + x;
                double dz = 16 * cz + z;
                double roll = Math.abs(noise.noise(dx / 30, dz / 30));
                if(roll < BOUND) {
                    data.setBlock(x, FLOOR_HEIGHT, z, FLOOR_BLOCK);
                }
            }
        }
    }

    @Override
    public void generateBedrock(WorldInfo info, Random random, int cx, int cz, ChunkGenerator.ChunkData data) {
    }

    @Override
    public void generateCaves(WorldInfo info, Random random, int cx, int cz, ChunkGenerator.ChunkData data) {
    }
}
