package com.nitnelave.CreeperHeal.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;

import java.util.List;

/**
 * Piston implementation of the CreeperBlock. The piston always comes back
 * retracted, and the redstone extends it again if needed.
 *
 * @author nitnelave
 *
 */
class CreeperPiston extends CreeperBlock
{

    /*
     * The block of the extended arm, if any.
     */
    private Block head = null;

    CreeperPiston(BlockState blockState)
    {
        super(blockState);
        Block block = blockState.getBlock();
        BlockData data = blockState.getBlockData();
        if (data instanceof PistonHead)
        {
            Block base = block.getRelative(((PistonHead) data).getFacing().getOppositeFace());
            if (!(base.getBlockData() instanceof Piston))
                return; // A lone piston head, treat it as a regular block.
            head = block;
            this.blockState = base.getState();
        }
        Piston piston = (Piston) this.blockState.getBlockData();
        if (piston.isExtended())
        {
            Block arm = this.blockState.getBlock().getRelative(piston.getFacing());
            if (arm.getBlockData() instanceof PistonHead)
                head = arm;
            piston.setExtended(false);
            this.blockState.setBlockData(piston);
        }
    }

    @Override
    public List<Block> getParts()
    {
        List<Block> parts = super.getParts();
        if (head != null)
            parts.add(head);
        return parts;
    }

    @Override
    boolean checkForDrop()
    {
        // Only the base is replaced.
        return checkForDrop(getBlock());
    }

}
