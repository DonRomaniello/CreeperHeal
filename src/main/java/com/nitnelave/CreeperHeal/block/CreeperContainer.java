package com.nitnelave.CreeperHeal.block;

import com.nitnelave.CreeperHeal.config.CreeperConfig;
import com.nitnelave.CreeperHeal.config.WCfgVal;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a block with an inventory (chest, furnace, hopper, barrel...).
 * The contents are stored in the block snapshot, and come back with the block.
 *
 * @author nitnelave
 *
 */
class CreeperContainer extends CreeperMultiblock
{

    CreeperContainer(Container container)
    {
        super(container);
        Inventory inv = container.getInventory();
        if (inv instanceof DoubleChestInventory)
        {
            DoubleChestInventory doubleChest = (DoubleChestInventory) inv;
            Location left = doubleChest.getLeftSide().getLocation(),
                            right = doubleChest.getRightSide().getLocation();
            if (left != null && right != null)
            {
                // Left side is the primary chest.
                this.blockState = left.getBlock().getState();
                addDependent(right.getBlock().getState());
            }
        }
    }

    /*
     * Drop the stored items on the ground, and forget about them.
     */
    private void dropContents()
    {
        dropContents(blockState);
        for (BlockState dependent : getDependents())
            dropContents(dependent);
    }

    private void dropContents(BlockState state)
    {
        if (!(state instanceof Container))
            return;
        Inventory snapshot = ((Container) state).getSnapshotInventory();
        Location location = state.getLocation().add(0.5, 0.5, 0.5);
        for (ItemStack itemStack : snapshot.getContents())
            if (itemStack != null && !itemStack.isEmpty())
                state.getWorld().dropItemNaturally(location, itemStack);
        snapshot.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.CreeperBlock#doRemove()
     */
    @Override
    void doRemove()
    {
        if (CreeperConfig.getWorld(getWorld()).getBool(WCfgVal.DROP_CHEST_CONTENTS))
            dropContents();
        super.doRemove();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.nitnelave.CreeperHeal.block.CreeperBlock#drop(boolean)
     */
    @Override
    public boolean drop(boolean forced)
    {
        dropContents();
        return super.drop(forced);
    }

}
