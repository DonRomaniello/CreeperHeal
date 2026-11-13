package com.nitnelave.CreeperHeal.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;

/**
 * Represents a hanging (item frame or painting).
 *
 * @author nitnelave
 *
 */
public abstract class CreeperHanging implements Replaceable
{
    protected final Hanging hanging;
    protected final Location location;

    protected CreeperHanging(Hanging hanging)
    {
        this.hanging = hanging;
        location = hanging.getLocation();
    }

    /**
     * Create a new hanging from an in-game hanging.
     *
     * @param hanging
     *            The hanging to be represented.
     * @return The newly created hanging, or null if it cannot be recorded.
     */
    public static CreeperHanging newHanging(Hanging hanging)
    {
        if (hanging instanceof Painting)
            return new CreeperPainting((Painting) hanging);
        if (hanging instanceof ItemFrame)
            return new CreeperItemFrame((ItemFrame) hanging);
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#replace(boolean)
     */
    @Override
    public boolean replace(boolean shouldDrop)
    {
        if (shouldDrop || isSupported())
            try
            {
                spawn();
                return true;
            } catch (IllegalArgumentException e)
            {
                // Could not place the hanging.
            }
        if (shouldDrop)
            drop(true);
        return shouldDrop;
    }

    /*
     * Put the hanging back in the world. Throws an IllegalArgumentException
     * if there is no room for it.
     */
    protected abstract void spawn();

    /*
     * Copy the properties common to every entity.
     */
    protected void copyProperties(Hanging to)
    {
        to.customName(hanging.customName());
        to.setCustomNameVisible(hanging.isCustomNameVisible());
        to.setInvulnerable(hanging.isInvulnerable());
        to.setSilent(hanging.isSilent());
        to.setGlowing(hanging.isGlowing());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getWorld()
     */
    @Override
    public World getWorld()
    {
        return location.getWorld();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getLocation()
     */
    @Override
    public Location getLocation()
    {
        return location.clone();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getBlock()
     */
    @Override
    public Block getBlock()
    {
        return location.getBlock();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getAttachingFace()
     */
    @Override
    public BlockFace getAttachingFace()
    {
        return hanging.getAttachedFace();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#isDependent()
     */
    @Override
    public boolean isDependent()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#remove()
     */
    @Override
    public void remove()
    {
        hanging.remove();
    }

}
