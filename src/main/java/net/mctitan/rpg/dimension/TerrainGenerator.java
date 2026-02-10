package net.mctitan.rpg.dimension;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import org.jspecify.annotations.NonNull;
import java.util.LinkedList;
import java.util.List;

public abstract class TerrainGenerator extends ChunkGenerator {
    private List<BlockPopulator> populators = new LinkedList<>();

    public void add(Populator populator) { populators.add(populator); }

    @Override public @NonNull List<BlockPopulator> getDefaultPopulators(@NonNull World world) { return populators; }
}
