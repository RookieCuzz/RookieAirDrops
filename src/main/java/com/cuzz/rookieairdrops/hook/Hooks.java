/*
 *
 *  *     HMCLeaves
 *  *     Copyright (C) 2022  Hibiscus Creative Studios
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License, or
 *  *     (at your option) any later version.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU General Public License
 *  *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.cuzz.rookieairdrops.hook;

import com.cuzz.rookieairdrops.RookieAirDrops;

import com.cuzz.rookieairdrops.hook.mythiccrucible.MythicCrucibleHook;
import com.cuzz.rookieairdrops.hook.oraxen.OraxenHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Hooks {

    @Nullable
    private static Map<String,ItemHook> itemHook=new HashMap<>();


    public static void load(RookieAirDrops plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") != null) {
            plugin.getLogger().info("Oraxen found, loading hook");
            itemHook.put("oa",new OraxenHook(plugin));
        }
        if (plugin.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            plugin.getLogger().info("ItemsAdder found, loading hook");
        }
        if (plugin.getServer().getPluginManager().getPlugin("MythicCrucible") != null) {
            plugin.getLogger().info("MythicCrucible found, loading hook");
            itemHook.put("mmc", new MythicCrucibleHook(plugin));
        }
    }

    public ItemHook getItemHook(String name){

        return itemHook.get(name);

    }

}
