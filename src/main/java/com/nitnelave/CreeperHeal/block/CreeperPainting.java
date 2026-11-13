package com.nitnelave.CreeperHeal.block;

import com.nitnelave.CreeperHeal.config.CreeperConfig;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Painting;
import org.bukkit.inventory.ItemStack;

/**
 * Painting implementation of the CreeperHanging.
 *
 * @author nitnelave
 *
 */
class CreeperPainting extends CreeperHanging
{

    private final Location anchor;

    private final Art art;

    protected CreeperPainting(Painting painting)
    {
        super(painting);
        art = painting.getArt();
        anchor = computeAnchor(painting);
    }

    /*
     * A painting sits at the center of its canvas, but is placed from the
     * block it is anchored to. For even sizes, the center is half a block
     * away from the anchor (to the left when facing the wall, and up).
     */
    private static Location computeAnchor(Painting painting)
    {
        Art art = painting.getArt();
        double side = art.getBlockWidth() % 2 == 0 ? 0.5 : 0, up = art.getBlockHeight() % 2 == 0 ? 0.5 : 0;
        BlockFace left;
        switch (painting.getFacing())
        {
        case NORTH:
            left = BlockFace.WEST;
            break;
        case WEST:
            left = BlockFace.SOUTH;
            break;
        case SOUTH:
            left = BlockFace.EAST;
            break;
        default:
            left = BlockFace.NORTH;
        }
        return painting.getLocation().subtract(left.getModX() * side, up, left.getModZ() * side);
    }

    @Override
    protected void spawn()
    {
        getWorld().spawn(anchor, Painting.class, painting -> {
            painting.setFacingDirection(hanging.getFacing(), true);
            painting.setArt(art, true);
            copyProperties(painting);
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
            getWorld().dropItemNaturally(getLocation(), new ItemStack(Material.PAINTING, 1));
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
        return Material.PAINTING;
    }

}
