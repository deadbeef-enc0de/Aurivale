package net.mctitan.rpg.dimension.biome;

import net.mctitan.rpg.dimension.BiomeGenerator;
import org.bukkit.block.Biome;
import org.bukkit.generator.WorldInfo;

import java.util.List;

public abstract class SimpleBiomeGenerator extends BiomeGenerator {
    private Biome biome;

    protected SimpleBiomeGenerator(Biome biome) { this.biome = biome; }

    @Override public Biome getBiome(WorldInfo info, int x, int y, int z) { return biome; }

    @Override public List<Biome> getBiomes(WorldInfo info) { return List.of(biome); }
}
