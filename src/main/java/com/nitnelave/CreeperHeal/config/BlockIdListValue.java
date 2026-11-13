package com.nitnelave.CreeperHeal.config;

import com.nitnelave.CreeperHeal.block.BlockId;
import com.nitnelave.CreeperHeal.utils.CreeperLog;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashSet;

class BlockIdListValue extends ConfigValue<HashSet<BlockId>>
{

    protected BlockIdListValue(CfgValEnumMember v, YamlConfiguration file)
    {
        //noinspection unchecked
        super((HashSet<BlockId>) v.getDefaultValue(), file, v.getKey());
    }

    @Override
    protected void load()
    {
        HashSet<BlockId> set = new HashSet<BlockId>();
        String tmp_str1 = config.getString(getKey(), "").trim();
        String[] split = tmp_str1.split(",");
        for (String elem : split)
        {
            elem = elem.trim();
            if (elem.isEmpty() || elem.equals("0"))
                continue;
            Material material = Material.matchMaterial(elem);
            if (material == null)
                CreeperLog.warning("Invalid material in " + getKey() + ": " + elem);
            else
                set.add(new BlockId(material));
        }
        setValue(set);
    }

    @Override
    protected void write()
    {
        config.set(getKey(), formatList());
    }

    private String formatList()
    {
        if (value.isEmpty())
            return "0";
        StringBuilder b = new StringBuilder();
        for (BlockId block : value)
        {
            b.append(block.toString());
            b.append(", ");
        }

        String blocklist = b.toString();
        return blocklist.substring(0, blocklist.length() - 2);

    }

}
