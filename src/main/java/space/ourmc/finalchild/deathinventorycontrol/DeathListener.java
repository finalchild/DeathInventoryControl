/*
 * This file is part of DeathInventoryControl, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Final Child
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package space.ourmc.finalchild.deathinventorycontrol;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;

final class DeathListener implements Listener {

    private DeathInventoryControl plugin;
    Map<UUID, List<ItemStack>> items = new HashMap<UUID, List<ItemStack>>();

    DeathListener(DeathInventoryControl plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        int defaultChance;
        try {
            defaultChance = getPermissionInt(event.getEntity(), "deathinventorycontrol.keepinventory");
        } catch (Exception e) {
            defaultChance = event.getKeepInventory() ? 100 : 0;
        }

        if (event.getEntity().hasPermission("deathinventorycontrol.keeplevel.true")) {
            event.setKeepLevel(true);
        } else if (event.getEntity().hasPermission("deathinventorycontrol.keeplevel.false")) {
            event.setKeepLevel(false);
        }

        event.setKeepInventory(false);

        Map<Material, Integer> permissionMaterialIntegerMap = getPermissionMaterialIntegerMap(event.getEntity(), "deathinventorycontrol.item");
        List<ItemStack> keptItems = new ArrayList<ItemStack>();
        for (Iterator<ItemStack> it = event.getDrops().iterator(); it.hasNext();) {
            ItemStack item = it.next();
            if (permissionMaterialIntegerMap.containsKey(item.getType())) {
                if (plugin.randomGenerator.nextInt(100) < permissionMaterialIntegerMap.get(item.getType())) {
                    keptItems.add(item);
                    it.remove();
                }
            } else {
                if (plugin.randomGenerator.nextInt(100) < defaultChance) {
                    keptItems.add(item);
                    it.remove();
                }
            }
        }
        if (!keptItems.isEmpty()) {
            items.put(event.getEntity().getUniqueId(), keptItems);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (items.containsKey(event.getPlayer().getUniqueId())) {
            for (ItemStack item : items.get(event.getPlayer().getUniqueId()))
                event.getPlayer().getInventory().addItem(item);
            items.remove(event.getPlayer().getUniqueId());
        }
    }

    public int getPermissionInt(Permissible permissible, String permission) throws Exception {
        return Integer.parseInt(getPermissionStringDeep(permissible, permission));
    }

    public Map<Material, Integer> getPermissionMaterialIntegerMap(Permissible permissible, String permission) {
        List<String> permissionStringListDeep = getPermissionStringListDeep(permissible, permission);
        Map<Material, Integer> result = new HashMap<Material, Integer>();
        for (String text : permissionStringListDeep) {
            String[] splitted = text.split("\\.");
            if (splitted.length == 2) {
                result.put(Material.getMaterial(splitted[0].toUpperCase()), Integer.parseInt(splitted[1]));
            }
        }
        return result;
    }

    public String getPermissionStringDeep(Permissible permissible, String permission) {
        List<String> list = new ArrayList<String>();
        for (PermissionAttachmentInfo info : permissible.getEffectivePermissions()) {
            if (info.getPermission().startsWith(permission + ".")) {
                list.add(info.getPermission());
            }
        }
        return list.get(0).replaceFirst(permission + ".", "");
    }

    public List<String> getPermissionStringListDeep(Permissible permissible, String permission) {
        List<String> result = new ArrayList<String>();
        for (PermissionAttachmentInfo info : permissible.getEffectivePermissions()) {
            if (info.getPermission().startsWith(permission + ".")) {
                result.add(info.getPermission().replaceFirst(permission + ".", ""));
            }
        }
        return result;
    }

}
