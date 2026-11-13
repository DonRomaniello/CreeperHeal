package com.nitnelave.CreeperHeal.block;

import com.nitnelave.CreeperHeal.config.CreeperConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an armor stand. The entity is stored as a full snapshot
 * (equipment, pose, name...), and comes back exactly as it was.
 */
public class CreeperArmorStand implements Replaceable
{
    private final ArmorStand stand;
    private final Location location;
    private final EntitySnapshot snapshot;
    private final List<ItemStack> contents = new ArrayList<ItemStack>();
    private boolean wasRemoved = false;

    /**
     * Record an armor stand, and remove it from the world.
     *
     * @param stand
     *            The armor stand to record.
     * @return The recorded armor stand, or null if it cannot be recorded.
     */
    public static CreeperArmorStand newArmorStand(ArmorStand stand)
    {
        EntitySnapshot snapshot = stand.createSnapshot();
        return snapshot == null ? null : new CreeperArmorStand(stand, snapshot);
    }

    private CreeperArmorStand(ArmorStand stand, EntitySnapshot snapshot)
    {
        this.stand = stand;
        this.snapshot = snapshot;
        this.location = stand.getLocation();
        EntityEquipment equipment = stand.getEquipment();
        for (EquipmentSlot slot : EquipmentSlot.values())
            if (stand.canUseEquipmentSlot(slot))
            {
                ItemStack item = equipment.getItem(slot);
                if (!item.isEmpty())
                    contents.add(item.clone());
            }
        remove();
    }

    @Override
    public boolean replace(boolean shouldDrop)
    {
        snapshot.createEntity(getLocation());
        return true;
    }

    @Override
    public Block getBlock()
    {
        return location.getBlock();
    }

    @Override
    public World getWorld()
    {
        return location.getWorld();
    }

    @Override
    public Material getType()
    {
        return Material.ARMOR_STAND;
    }

    @Override
    public BlockFace getAttachingFace()
    {
        return BlockFace.DOWN;
    }

    @Override
    public Location getLocation()
    {
        return location.clone();
    }

    @Override
    public boolean isDependent()
    {
        return true;
    }

    @Override
    public boolean drop(boolean forced)
    {
        if (forced || CreeperConfig.shouldDrop())
        {
            getWorld().dropItemNaturally(getLocation(), new ItemStack(Material.ARMOR_STAND, 1));
            for (ItemStack itemStack : contents)
                getWorld().dropItemNaturally(getLocation(), itemStack);
            return true;
        }
        return false;
    }

    @Override
    public void remove()
    {
        if (!wasRemoved)
        {
            wasRemoved = true;
            stand.getEquipment().clear();
            stand.remove();
        }
    }
}
