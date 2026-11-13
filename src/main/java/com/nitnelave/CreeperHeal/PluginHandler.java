package com.nitnelave.CreeperHeal;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * This class handles all the interaction with external plugins.
 *
 * The Paper port ships without the legacy LWC / MobArena / Factions hooks
 * (they were compiled against decade-old jars). The seams are kept so that
 * integrations can be plugged back in.
 *
 * @author nitnelave
 */
public class PluginHandler
{

    /**
     * Get whether the block is protected by a protection plugin.
     *
     * @param block
     *            The block to check.
     * @return Whether the block is protected.
     */
    public static boolean isProtected(Block block)
    {
        return false; // seam: hook a chest-protection plugin here
    }

    /**
     * Get whether the block is inside a mob arena.
     *
     * @param location
     *            The location of the block.
     * @return Whether the block is inside an arena.
     */
    public static boolean isInArena(Location location)
    {
        return false; // seam: hook MobArena here
    }

}
