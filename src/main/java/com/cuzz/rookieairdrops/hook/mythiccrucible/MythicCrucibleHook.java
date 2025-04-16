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

package com.cuzz.rookieairdrops.hook.mythiccrucible;

import com.cuzz.rookieairdrops.RookieAirDrops;
import com.cuzz.rookieairdrops.hook.ItemHook;

import io.lumine.mythiccrucible.MythicCrucible;
import io.lumine.mythiccrucible.items.CrucibleItem;
import io.lumine.mythiccrucible.items.CrucibleItemType;
import io.lumine.mythiccrucible.items.blocks.CustomBlockItemContext;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class MythicCrucibleHook implements ItemHook {

    private final RookieAirDrops plugin;

    public MythicCrucibleHook(RookieAirDrops plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable String getId(ItemStack itemStack) {
        return MythicCrucible.inst().getItemManager()
                .getItem(itemStack)
                .map(CrucibleItem::getInternalName)
                .orElse(null);
    }

    @Override
    public @Nullable ItemStack getItem(String id,int amount) {
        ItemStack itemStack = MythicCrucible.core()
                .getItemManager()
                .getItemStack(id);
        itemStack.setAmount(amount);
        return itemStack;
    }




    @Override
    public @Nullable String getCustomBlockIdAt(Location location) {
        return MythicCrucible.inst()
                .getItemManager()
                .getCustomBlockManager()
                .getBlockFromBlock(location.getBlock())
                .map(CustomBlockItemContext::getCrucibleItem)
                .map(CrucibleItem::getInternalName)
                .orElse(null);
    }

}
