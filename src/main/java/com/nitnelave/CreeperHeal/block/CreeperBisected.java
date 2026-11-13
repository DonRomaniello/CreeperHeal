package com.nitnelave.CreeperHeal.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;

/**
 * Two-block-high blocks: doors, tall flowers, tall grass...
 *
 * @author nitnelave
 *
 */
class CreeperBisected extends CreeperMultiblock
{

    CreeperBisected(BlockState blockState, Bisected bisected)
    {
        super(blockState);
        boolean top = bisected.getHalf() == Bisected.Half.TOP;
        Block other = blockState.getBlock().getRelative(top ? BlockFace.DOWN : BlockFace.UP);
        if (other.getType() != blockState.getType()
            || ((Bisected) other.getBlockData()).getHalf() == bisected.getHalf())
            return; // A lone half, nothing else to take care of.
        if (top)
        {
            // The bottom half is the main block, it is the one that needs support.
            this.blockState = other.getState();
            addDependent(blockState);
        }
        else
            addDependent(other.getState());
    }

}
