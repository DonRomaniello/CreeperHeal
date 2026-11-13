package com.nitnelave.CreeperHeal.utils;

import java.util.List;

import org.bukkit.block.Block;

import com.nitnelave.CreeperHeal.config.WorldConfig;

/**
 * A handler for the Factions plugin.
 *
 * The Paper port ships without the Factions hook; nothing is ever ignored.
 *
 * @author nitnelave
 */
public abstract class FactionHandler
{

    /**
     * Check if an explosion should be ignored (blocks not replaced).
     *
     * @param list
     *            The list of exploded blocks.
     * @param world
     *            The CH configuration for the world.
     * @return Whether the explosion should be ignored.
     */
    public static boolean shouldIgnore(List<Block> list, WorldConfig world)
    {
        return false; // seam: hook a Factions implementation here
    }

    /**
     * Check if a block should be ignored (not replaced).
     *
     * @param block
     *            The block to be checked.
     * @param world
     *            The CH configuration for the world.
     * @return Whether the replacement should be ignored.
     */
    public static boolean shouldIgnore(Block block, WorldConfig world)
    {
        return false;
    }

}
