package com.nitnelave.CreeperHeal.block;

import com.nitnelave.CreeperHeal.config.CreeperConfig;
import com.nitnelave.CreeperHeal.config.WCfgVal;
import org.bukkit.Location;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Shulker box implementation of CreeperBlock. The contents are stored in the
 * block snapshot, and come back with the block.
 */
public class CreeperShulkerBox extends CreeperBlock
{

    CreeperShulkerBox(ShulkerBox blockState)
    {
        super(blockState);
    }

    private void dropContents()
    {
        Inventory snapshot = ((ShulkerBox) blockState).getSnapshotInventory();
        Location location = blockState.getLocation().add(0.5, 0.5, 0.5);
        for (ItemStack itemStack : snapshot.getContents())
            if (itemStack != null && !itemStack.isEmpty())
                blockState.getWorld().dropItemNaturally(location, itemStack);
        snapshot.clear();
    }

    @Override
    void doRemove()
    {
        if (CreeperConfig.getWorld(getWorld()).getBool(WCfgVal.DROP_CHEST_CONTENTS))
            dropContents();
        super.doRemove();
    }

    @Override
    public boolean drop(boolean forced)
    {
        // Always drop the contents, the box itself according to the drop chance.
        dropContents();
        if (forced || CreeperConfig.shouldDrop())
        {
            Location location = blockState.getLocation().add(0.5, 0.5, 0.5);
            blockState.getWorld().dropItemNaturally(location, new ItemStack(blockState.getType()));
            return true;
        }
        return false;
    }

}
