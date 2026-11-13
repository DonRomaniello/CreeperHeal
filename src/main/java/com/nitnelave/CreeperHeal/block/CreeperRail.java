package com.nitnelave.CreeperHeal.block;

import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Rail;

/**
 * Rail implementation of CreeperBlock. Rails are put back without notifying
 * the neighbors, so that the track keeps the exact shape it had.
 *
 * @author nitnelave
 *
 */
class CreeperRail extends CreeperBlock
{

    private final Rail data;

    CreeperRail(BlockState blockState)
    {
        super(blockState);
        data = (Rail) blockState.getBlockData();
    }

    /*
     * Get the direction of the block an ascending rail is leaning on, or null
     * if the rail is flat.
     */
    static BlockFace getSlope(Rail rail)
    {
        switch (rail.getShape())
        {
        case ASCENDING_EAST:
            return BlockFace.EAST;
        case ASCENDING_WEST:
            return BlockFace.WEST;
        case ASCENDING_NORTH:
            return BlockFace.NORTH;
        case ASCENDING_SOUTH:
            return BlockFace.SOUTH;
        default:
            return null;
        }
    }

    @Override
    public void update()
    {
        getLocation().getChunk().load();
        blockState.update(true, false);
        playSound();
    }

    @Override
    public BlockFace getAttachingFace()
    {
        BlockFace slope = getSlope(data);
        return slope == null ? BlockFace.DOWN : slope;
    }

    @Override
    public boolean isSupported()
    {
        BlockFace slope = getSlope(data);
        return super.isSupported() && (slope == null || !isEmpty(getBlock().getRelative(slope)));
    }

    /**
     * Get whether the rail is ascending.
     *
     * @return Whether the rail is ascending.
     */
    public boolean isAscending()
    {
        return getSlope(data) != null;
    }
}
