package com.nitnelave.CreeperHeal.block;

import com.nitnelave.CreeperHeal.config.CreeperConfig;
import org.bukkit.Material;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

/**
 * ItemFrame implementation of the CreeperHanging (regular and glowing).
 *
 * @author nitnelave
 *
 */
class CreeperItemFrame extends CreeperHanging
{

    private final ItemStack item;

    protected CreeperItemFrame(ItemFrame frame)
    {
        super(frame);
        item = frame.getItem().clone();
    }

    @Override
    protected void spawn()
    {
        if (hanging instanceof GlowItemFrame)
            spawn(GlowItemFrame.class);
        else
            spawn(ItemFrame.class);
    }

    private <T extends ItemFrame> void spawn(Class<T> type)
    {
        final ItemFrame old = (ItemFrame) hanging;
        getWorld().spawn(location, type, frame -> {
            frame.setFacingDirection(old.getFacing(), true);
            frame.setItem(item, false);
            frame.setRotation(old.getRotation());
            frame.setItemDropChance(old.getItemDropChance());
            frame.setVisible(old.isVisible());
            frame.setFixed(old.isFixed());
            copyProperties(frame);
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#drop(boolean)
     */
    @Override
    public boolean drop(boolean forced)
    {
        if (forced || CreeperConfig.shouldDrop())
        {
            if (!item.isEmpty())
                getWorld().dropItemNaturally(getLocation(), item);
            getWorld().dropItemNaturally(getLocation(), new ItemStack(getType(), 1));
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.Replaceable#getType()
     */
    @Override
    public Material getType()
    {
        return hanging instanceof GlowItemFrame ? Material.GLOW_ITEM_FRAME : Material.ITEM_FRAME;
    }

}
