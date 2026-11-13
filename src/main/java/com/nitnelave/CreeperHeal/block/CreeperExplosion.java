package com.nitnelave.CreeperHeal.block;

import com.nitnelave.CreeperHeal.config.CfgVal;
import com.nitnelave.CreeperHeal.config.CreeperConfig;
import com.nitnelave.CreeperHeal.config.WorldConfig;
import com.nitnelave.CreeperHeal.events.CHBlockHealEvent;
import com.nitnelave.CreeperHeal.events.CHBlockHealEvent.CHBlockHealReason;
import com.nitnelave.CreeperHeal.utils.CreeperLog;
import com.nitnelave.CreeperHeal.utils.ShortLocation;
import com.nitnelave.CreeperHeal.utils.Suffocating;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

/**
 * Represents an explosion, with the list of blocks destroyed, the time of the
 * explosion, and the radius.
 *
 * @author nitnelave
 *
 */
public class CreeperExplosion
{
    /**
     * The time after which the block replacements begin.
     */
    private final LinkedList<Replaceable> blockList;
    private Location loc;
    private int locWeight = 0;
    private double radius = 0;
    private final WorldConfig world;
    private final HashSet<ShortLocation> checked = new HashSet<ShortLocation>();

    /*
     * The blocks removed from the world while recording, whose neighbors are
     * yet to be notified.
     */
    private final List<Block> removed = new ArrayList<Block>();

    /*
     * The blocks that could not be replaced yet because their support is
     * missing, with the number of times they were tried again.
     */
    private final Map<Replaceable, Integer> waiting = new LinkedHashMap<Replaceable, Integer>();

    /*
     * How many times a block is tried again before giving up on the block per
     * block replacement.
     */
    private static final int MAX_TRIES = 150;
    private ReplacementTimer timer;

    /**
     * Constructor.
     *
     * @param loc
     *            The location of the explosion.
     */
    public CreeperExplosion(Location loc)
    {
        world = CreeperConfig.getWorld(loc.getWorld());
        timer = new ReplacementTimer(new Date(new Date().getTime() + 1000
                                              * CreeperConfig.getInt(CfgVal.WAIT_BEFORE_HEAL)), world.isRepairTimed());
        blockList = new LinkedList<Replaceable>();
        this.loc = loc;
    }

    /**
     * Add blocks to an explosion, and reset the timer to the time of the last
     * explosion.
     *
     * @param blocks
     *            The list of blocks to add.
     */
    public void addBlocks(List<Block> blocks, Location newLoc)
    {
        timer = new ReplacementTimer(new Date(new Date().getTime() + 1000
                                              * CreeperConfig.getInt(CfgVal.WAIT_BEFORE_HEAL)), world.isRepairTimed());
        loc = new Location(loc.getWorld(), (locWeight * loc.getX() + newLoc.getX())
                                           / (locWeight + 1), (locWeight * loc.getY() + newLoc.getY())
                                                              / (locWeight + 1), (locWeight
                                                                                  * loc.getZ() + newLoc.getZ())
                                                                                 / (locWeight + 1));
        locWeight++;
        checked.clear();
        recordBlocks(blocks);
        if (CreeperConfig.getBool(CfgVal.EXPLODE_OBSIDIAN))
            checkForObsidian();

        Collections.sort(blockList, new CreeperComparator(loc));
        radius = computeRadius();
    }

    /**
     * Get the time of the explosion.
     *
     * @return The time of the explosion.
     */
    public Date getTime()
    {
        return timer.getTime();
    }

    /*
     * Get the distance between the explosion's location and the furthest block.
     */
    private double computeRadius()
    {
        double r = 0;
        for (Replaceable b : blockList)
        {
            Location bl = b.getBlock().getLocation();
            r = Math.max(r, loc.distance(bl));
        }
        return r + 1;
    }

    /**
     * Get the location of the explosion.
     *
     * @return The location of the explosion.
     */
    public Location getLocation()
    {
        return loc;
    }

    /**
     * Get the radius of the explosion (i.e. the distance between the location
     * and the furthest block).
     *
     * @return The radius of the explosion.
     */
    public double getRadius()
    {
        return radius;
    }

    /*
     * Replace all the blocks in the list.
     */
    protected void replace_blocks(boolean shouldDrop, CHBlockHealReason reason)
    {
        // The blocks waiting for their support are part of the lot.
        if (!waiting.isEmpty())
        {
            blockList.addAll(waiting.keySet());
            waiting.clear();
            Collections.sort(blockList, new CreeperComparator(loc));
        }
        Iterator<Replaceable> iter = blockList.iterator();
        while (iter.hasNext())
        {
            Replaceable block = iter.next();
            CHBlockHealEvent event = new CHBlockHealEvent(block, shouldDrop, reason);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && block.replace(event.shouldDrop()))
                iter.remove();
        }
        if (shouldDrop)
        {
            blockList.clear();

            if (CreeperConfig.getBool(CfgVal.TELEPORT_ON_SUFFOCATE))
                Suffocating.checkPlayerExplosion(loc, radius);
        }
    }

    /**
     * Replace the first block of the list. If it cannot be placed yet (its
     * support is missing), it is set aside and tried again later.
     */
    private void replace_one_block()
    {
        if (blockList.isEmpty())
            return;
        Replaceable block = blockList.remove();
        CHBlockHealEvent event = new CHBlockHealEvent(block, false, CHBlockHealReason.BLOCK_BY_BLOCK);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled() && !block.replace(event.shouldDrop()))
            waiting.put(block, 0);
        if (CreeperConfig.getBool(CfgVal.TELEPORT_ON_SUFFOCATE))
            Suffocating.checkPlayerOneBlock(block.getBlock().getLocation());
    }

    /*
     * Try again the blocks that were set aside. If one of them is still stuck
     * after all its tries, give up on the block per block replacement and
     * restore what is left of the explosion in one go.
     */
    private void retryWaiting()
    {
        boolean stuck = false;
        Iterator<Map.Entry<Replaceable, Integer>> iter = waiting.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<Replaceable, Integer> entry = iter.next();
            Replaceable block = entry.getKey();
            if (block.isSupported())
            {
                CHBlockHealEvent event = new CHBlockHealEvent(block, false, CHBlockHealReason.BLOCK_BY_BLOCK);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled() || block.replace(event.shouldDrop()))
                {
                    iter.remove();
                    continue;
                }
            }
            entry.setValue(entry.getValue() + 1);
            if (entry.getValue() >= MAX_TRIES)
                stuck = true;
        }
        if (stuck)
            restoreRemaining();
    }

    /*
     * Crude fallback: put back everything that is left, supports first. Go
     * over the list as long as it helps, then force what still has nothing to
     * hold on to (it ends up as an item).
     */
    private void restoreRemaining()
    {
        int left = blockList.size() + waiting.size(), before;
        CreeperLog.logInfo("Explosion at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()
                           + ": a block could not be replaced, restoring the " + left + " remaining at once", 1);
        do
        {
            before = left;
            replace_blocks(false, CHBlockHealReason.BLOCK_BY_BLOCK);
            left = blockList.size();
        } while (left > 0 && left < before);
        replace_blocks(true, CHBlockHealReason.BLOCK_BY_BLOCK);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o instanceof CreeperExplosion)
        {
            CreeperExplosion e = (CreeperExplosion) o;
            return e.timer == timer && e.loc == loc && e.radius == radius;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (int) (timer.hashCode() + radius + loc.hashCode());
    }

    /*
     * Check for dependent blocks and record them first.
     */
private void recordBlocks(List<Block> blocks)
    {
        for (Block b : blocks)
            recordBlock(b);
        // The blocks were removed silently, now let the neighbors know.
        for (Block b : removed)
            CreeperBlock.notifyRemoved(b);
        removed.clear();
    }

    /*
     * In case of possible obsidian destruction, check for obsidian around, and
     * give them a chance to be destroyed.
     */
    private void checkForObsidian()
    {
        int radius = CreeperConfig.getInt(CfgVal.OBSIDIAN_RADIUS);
        double chance = ((float) CreeperConfig.getInt(CfgVal.OBSIDIAN_CHANCE)) / 100;
        World w = loc.getWorld();

        Random r = new Random(System.currentTimeMillis());
        boolean table = CreeperConfig.getBool(CfgVal.OBSIDIAN_TABLE);

        for (int i = loc.getBlockX() - radius; i < loc.getBlockX() + radius; i++)
            for (int j = Math.max(w.getMinHeight(), loc.getBlockY() - radius); j < Math.min(w.getMaxHeight(), loc.getBlockY()
                                                                                               + radius); j++)
                for (int k = loc.getBlockZ() - radius; k < loc.getBlockZ() + radius; k++)
                {
                    Location l = new Location(w, i, j, k);
                    if (l.distance(loc) > radius)
                        continue;
                    Block b = l.getBlock();
                    if (isObsidianLike(b.getType(), table) && r.nextDouble() < chance)
                        recordBlock(b);
                }
    }

    private boolean isObsidianLike(Material m, boolean table)
    {
        return m == Material.OBSIDIAN
               || (table && (m == Material.ENCHANTING_TABLE || m == Material.ENDER_CHEST));
    }

    /**
     * Record one block and remove it. If it is protected, add to the
     * replace-immediately list. Check for dependent blocks around.
     *
     * @param block
     *            The block to record.
     */
public void recordBlock(Block block)
    {
        Material type = block.getType();
        if (type == Material.NETHER_PORTAL || type == Material.END_PORTAL || type == Material.END_GATEWAY
            || checked.contains(new ShortLocation(block)))
            return;

        if (type == Material.TNT)
        {
            if (CreeperConfig.getBool(CfgVal.PREVENT_CHAIN_REACTION))
            {
                // Take the TNT out before it gets primed, and put it right back.
                CreeperBlock tnt = new CreeperBlock(block.getState());
                tnt.record(checked);
                ToReplaceList.addToReplace(tnt);
                tnt.remove();
            }
            return;
        }

        CreeperBlock creeperBlock = CreeperBlock.newBlock(block.getState());

        if (creeperBlock == null)
            return;

        List<Block> parts = creeperBlock.getParts();
        for (Block part : parts)
            if (checked.contains(new ShortLocation(part)))
                return;

        creeperBlock.record(checked);

        if (world.isProtected(creeperBlock.getBlock()))
        {
            ToReplaceList.addToReplace(creeperBlock);
            creeperBlock.remove();
            return;
        }

        BlockId id = new BlockId(creeperBlock.getType());
        if (!world.isBlackListed(id))
        {
            // The block should be replaced.
            creeperBlock.remove();
            blockList.add(creeperBlock);
        }
        else if (CreeperConfig.getBool(CfgVal.DROP_DESTROYED_BLOCKS))
        {
            creeperBlock.drop(false);
            creeperBlock.remove();
        }
        else
            return; // Let the explosion take care of it.

        removed.addAll(parts);

        // Now that the block is gone, record whatever was hanging onto it
        // (torches, doors, plants...) before it pops off.
        for (Block part : parts)
            for (BlockFace face : CreeperBlock.CARDINALS)
            {
                Block neighbor = part.getRelative(face);
                if (!neighbor.isEmpty() && !CreeperBlock.wouldSurvive(neighbor))
                    recordBlock(neighbor);
            }
    }

    /**
     * Add a Replaceable to the list, and remove it from the world.
     *
     * @param replaceable
     *            The Replaceable to add.
     */
    public void recordEntity(Replaceable replaceable)
    {
        if (replaceable != null)
        {
            blockList.add(replaceable);
            replaceable.remove();
        }
    }

    /**
     * Check if the explosion has blocks to repair, and repair them (or one of
     * them in case of block per block).
     *
     * @return False if the explosion has not started its replacements yet
     *         because it is not time.
     */
    public boolean checkReplace()
    {
        if (timer.isTimed())
            return true;
        if (timer.checkReplace())
        {
            if (CreeperConfig.getBool(CfgVal.BLOCK_PER_BLOCK))
            {
                replace_one_block();
                retryWaiting();
            }
            else
                replace_blocks(true, CHBlockHealReason.EXPLOSION);
            return true;

        }
        return false;
    }

    /**
     * Get whether the explosion has started replacing (only in block per block
     * mode).
     *
     * @return True if the explosion has started replacing blocks
     */
    public boolean hasStartedReplacing()
    {
        if (timer.isTimed())
            return false;
        return timer.checkReplace();
    }

    /**
     * Get whether the list of blocks to be replaced is empty.
     *
     * @return Whether the list is empty.
     */
    public boolean isEmpty()
    {
        return blockList.isEmpty() && waiting.isEmpty();
    }

}
