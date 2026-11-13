package com.nitnelave.CreeperHeal.block;

import com.nitnelave.CreeperHeal.config.CfgVal;
import com.nitnelave.CreeperHeal.config.CreeperConfig;
import com.nitnelave.CreeperHeal.config.WCfgVal;
import com.nitnelave.CreeperHeal.utils.ShortLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.Hangable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Represents a block that can be replaced. Every special type of block derives
 * from this class, and can only be constructed by using the newBlock method.
 *
 * The block is stored as a full {@link BlockState} snapshot, so block entity
 * data (sign text, inventories, banner patterns, spawners...) is restored along
 * with the block.
 *
 * @author nitnelave
 *
 */
public class CreeperBlock implements Replaceable
{

    public final static BlockFace[] CARDINALS = { BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
                                                 BlockFace.NORTH, BlockFace.UP, BlockFace.DOWN };

    private final static Random random = new Random();

    /*
     * The block represented.
     */
    BlockState blockState;

    /*
     * Whether the block needs another block to stay in place (torch, door,
     * flower, rail...). Computed once, when the block is recorded.
     */
    private Boolean dependent = null;

    /**
     * Create a new CreeperBlock of the right class. Factory method that should
     * be used as a constructor.
     *
     * @param state
     *            The block to be represented.
     * @return A new CreeperBlock of the right subclass, or null if the block
     *         should not be recorded.
     */
    public static CreeperBlock newBlock(BlockState state)
    {
        Material type = state.getType();
        if (type.isAir() || Tag.FIRE.isTagged(type) || type == Material.TNT)
            return null;
        BlockData data = state.getBlockData();
        if (state instanceof ShulkerBox)
            return new CreeperShulkerBox((ShulkerBox) state);
        if (state instanceof Container)
            return new CreeperContainer((Container) state);
        if (data instanceof Bed)
            return new CreeperBed(state);
        if (data instanceof Piston || data instanceof PistonHead)
            return new CreeperPiston(state);
        if (data instanceof Rail)
            return new CreeperRail(state);
        // Stairs and trapdoors are "bisected" too, but only take one block.
        if (data instanceof Bisected && !(data instanceof Stairs) && !(data instanceof TrapDoor))
            return new CreeperBisected(state, (Bisected) data);
        if (type.hasGravity())
            return new CreeperPhysicsBlock(state);
        return new CreeperBlock(state);
    }

    /*
     * The constructor.
     */
    CreeperBlock(BlockState blockState)
    {
        this.blockState = blockState;
    }

    /*
     * Apply the block type changes from the configuration to the stored
     * state. Called once the block has been removed from the world.
     */
    void applyConversions()
    {
        Material type = blockState.getType();
        if (type == Material.STONE && CreeperConfig.getBool(CfgVal.STONE_TO_COBBLE))
            blockState.setType(Material.COBBLESTONE);
        else if (type == Material.GRASS_BLOCK
                 && CreeperConfig.getWorld(getWorld()).getBool(WCfgVal.GRASS_TO_DIRT))
            blockState.setType(Material.DIRT);
        else if (type == Material.STONE_BRICKS && CreeperConfig.getBool(CfgVal.CRACK_DESTROYED_BRICKS))
            blockState.setType(Material.CRACKED_STONE_BRICKS);
        CreeperConfig.getWorld(getWorld()).getReplacement(blockState);
    }

    /*
     * Get whether the block is empty, i.e. if it can be replaced by other
     * blocks (air, water, fire, snow, grass...)
     */
    static boolean isEmpty(Block block)
    {
        return block.isEmpty() || block.isLiquid() || block.isReplaceable();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getLocation()
     */
    @Override
    public Location getLocation()
    {
        return blockState.getLocation();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getWorld()
     */
    @Override
    public World getWorld()
    {
        return blockState.getWorld();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getBlock()
     */
    @Override
    public Block getBlock()
    {
        return blockState.getBlock();
    }

    /**
     * Get whether blocks of a type are solid.
     *
     * @param type
     *            The type of the block.
     * @return Whether the block is solid.
     */
    @SuppressWarnings("unused")
    public static boolean isSolid(Material type)
    {
        return type.isSolid();
    }

    /**
     * Replace the block in the world.
     */
    public void update()
    {
        getLocation().getChunk().load();
        blockState.update(true, true);
        playSound();
    }

    void playSound()
    {
        Sound sound = CreeperConfig.getSound();
        if (sound != null)
            getWorld().playSound(getLocation(), sound, CreeperConfig.getInt(CfgVal.SOUND_VOLUME) / 10F, random.nextFloat() * 2);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getType()
     */
    @Override
    public Material getType()
    {
        return blockState.getType();
    }

    /**
     * Drop the corresponding items on the ground.
     *
     * @param forced
     *            If false, the block will have a chance to drop, according to
     *            the configuration value of the drop chance. If true, the block
     *            drops every time.
     * @return True if the block dropped.
     */
    @Override
    public boolean drop(boolean forced)
    {
        if (forced || CreeperConfig.shouldDrop())
        {
            BlockState current = blockState.getBlock().getState();
            blockState.update(true, false);
            Collection<ItemStack> drop = blockState.getBlock().getDrops();
            current.update(true, false);
            Location location = blockState.getLocation().add(0.5, 0.5, 0.5);
            World world = blockState.getWorld();
            for (ItemStack itemStack : drop)
                world.dropItemNaturally(location, itemStack);
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#replace(boolean)
     */
    @Override
    public final boolean replace(boolean shouldDrop)
    {
        if (checkForDrop())
            return true;

        if (!shouldDrop && !isSupported())
            return false;

        update();

        return true;
    }

    /**
     * Get whether blocks of a type are solid.
     *
     * @param block
     *            The type of the block.
     * @return Whether the block is solid.
     */
    public static boolean isSolid(Block block)
    {
        return block.getType().isSolid();
    }

    boolean checkForDrop()
    {
        for (Block part : getParts())
            if (checkForDrop(part))
                return true;
        return false;
    }

    /*
     * Check whether something was built where the block should come back. If
     * so, either give up on the replacement (and drop the block), or make room
     * for it.
     */
    boolean checkForDrop(Block block)
    {
        if (isEmpty(block))
            return false;
        if (!CreeperConfig.getBool(CfgVal.OVERWRITE_BLOCKS))
        {
            if (CreeperConfig.getBool(CfgVal.DROP_DESTROYED_BLOCKS))
                drop(true);
            return true;
        }
        CreeperBlock b = CreeperBlock.newBlock(block.getState());
        if (b != null)
        {
            if (CreeperConfig.getBool(CfgVal.DROP_DESTROYED_BLOCKS))
                b.drop(true);
            b.remove();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getAttachingFace()
     */
    @Override
    public BlockFace getAttachingFace()
    {
        if (!isDependent())
            return BlockFace.SELF;
        BlockData data = blockState.getBlockData();
        if (data instanceof FaceAttachable)
            switch (((FaceAttachable) data).getAttachedFace())
            {
            case CEILING:
                return BlockFace.UP;
            case FLOOR:
                return BlockFace.DOWN;
            default:
                return ((Directional) data).getFacing().getOppositeFace();
            }
        if (data instanceof Hangable && ((Hangable) data).isHanging())
            return BlockFace.UP;
        if (data instanceof Cocoa)
            return ((Cocoa) data).getFacing();
        if (data instanceof Directional && (data.getMaterial().name().contains("WALL_")
                                            || data.getMaterial() == Material.LADDER
                                            || data.getMaterial() == Material.TRIPWIRE_HOOK))
            return ((Directional) data).getFacing().getOppositeFace();
        return BlockFace.DOWN;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        return blockState.getBlockData().isSupported(getBlock());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#remove()
     */
    @Override
    public final void remove()
    {
        computeDependent();
        doRemove();
        applyConversions();
    }

    /*
     * Actually take the block out of the world.
     */
    void doRemove()
    {
        for (Block part : getParts())
            clear(part);
    }

    /*
     * Remove a block from the world, without dropping anything and without
     * notifying the neighbors (see notifyRemoved for the physics update).
     */
    static void clear(Block block)
    {
        BlockState state = block.getState(false);
        if (state instanceof Container)
            ((Container) state).getInventory().clear();
        block.setType(Material.AIR, false);
    }

    /**
     * Send the block updates for a block that was removed silently, so that
     * water flows in, redstone turns off, etc.
     *
     * @param block
     *            The block that was removed.
     */
    public static void notifyRemoved(Block block)
    {
        if (!block.isEmpty())
            return;
        block.setType(Material.STRUCTURE_VOID, false);
        block.setType(Material.AIR, true);
    }

    /**
     * Get whether a block of the world can stay where it is, or if it is going
     * to pop off because its support is gone.
     *
     * @param block
     *            The block to check.
     * @return Whether the block is still supported.
     */
    public static boolean wouldSurvive(Block block)
    {
        BlockData data = block.getBlockData();
        if (!data.isSupported(block))
            return false;
        if (data instanceof Rail)
        {
            BlockFace slope = CreeperRail.getSlope((Rail) data);
            return slope == null || !block.getRelative(slope).isEmpty();
        }
        return true;
    }

    /*
     * Find out whether the block needs support, by checking whether it would
     * survive floating in the air at the top of the world.
     */
    private void computeDependent()
    {
        if (dependent != null)
            return;
        World world = getWorld();
        Block probe = world.getBlockAt(blockState.getX(), world.getMaxHeight() - 2, blockState.getZ());
        boolean free = probe.isEmpty();
        for (BlockFace face : CARDINALS)
            free = free && probe.getRelative(face).isEmpty();
        if (free)
            dependent = !blockState.getBlockData().isSupported(probe);
        else
            dependent = !blockState.getType().isSolid();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#isDependent()
     */
    @Override
    public boolean isDependent()
    {
        computeDependent();
        return dependent;
    }

    /**
     * Get the blocks of the world this block occupies (more than one for
     * doors, beds, double chests...).
     *
     * @return The list of blocks.
     */
    public List<Block> getParts()
    {
        List<Block> parts = new ArrayList<Block>();
        parts.add(getBlock());
        return parts;
    }

    void record(Collection<ShortLocation> checked)
    {
        for (Block part : getParts())
            checked.add(new ShortLocation(part));
    }

}
