package com.nitnelave.CreeperHeal.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;

/**
 * Bed implementation of CreeperBlock.
 *
 * @author nitnelave
 *
 */
class CreeperBed extends CreeperMultiblock
{

    CreeperBed(BlockState blockState)
    {
        super(blockState);
        Bed bed = (Bed) blockState.getBlockData();
        boolean foot = bed.getPart() == Bed.Part.FOOT;
        Block other = blockState.getBlock().getRelative(foot ? bed.getFacing() : bed.getFacing().getOppositeFace());
        if (other.getType() != blockState.getType() || ((Bed) other.getBlockData()).getPart() == bed.getPart())
            return; // Half a bed.
        if (foot)
        {
            // The head is the main block.
            this.blockState = other.getState();
            addDependent(blockState);
        }
        else
            addDependent(other.getState());
    }

}
