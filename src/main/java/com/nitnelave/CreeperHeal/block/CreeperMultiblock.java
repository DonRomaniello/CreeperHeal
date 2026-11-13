package com.nitnelave.CreeperHeal.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a block that spans several blocks of the world (door, bed,
 * double chest...). All the parts are removed and replaced together.
 *
 * @author nitnelave
 *
 */
public abstract class CreeperMultiblock extends CreeperBlock
{

    private final List<BlockState> dependents = new ArrayList<BlockState>();

    CreeperMultiblock(BlockState blockState)
    {
        super(blockState);
    }

    void addDependent(BlockState state)
    {
        dependents.add(state);
    }

    List<BlockState> getDependents()
    {
        return dependents;
    }

    @Override
    public List<Block> getParts()
    {
        List<Block> parts = super.getParts();
        for (BlockState dependent : dependents)
            parts.add(dependent.getBlock());
        return parts;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.CreeperBlock#update()
     */
    @Override
    public void update()
    {
        getLocation().getChunk().load();
        // Place every part before the neighbors are notified, so that the
        // halves don't break each other.
        blockState.update(true, false);
        for (BlockState dependent : dependents)
            dependent.update(true, false);
        playSound();
    }

}
