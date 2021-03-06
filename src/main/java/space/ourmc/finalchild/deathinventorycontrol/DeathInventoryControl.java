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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DeathInventoryControl extends JavaPlugin {

    Random randomGenerator;
    private DeathListener listener;

    public void onEnable() {
        randomGenerator = new Random();
        listener = new DeathListener(this);
        if (getConfig().isConfigurationSection("items")) {
            ConfigurationSection itemSection = getConfig().getConfigurationSection("items");
            listener.items = itemSection.getKeys(false).stream().collect(Collectors.toMap(UUID::fromString, e -> (List<ItemStack>) itemSection.getList(e)));
        }
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void onDisable() {
        getConfig().set("items", null);
        listener.items.entrySet().stream().forEach(e -> getConfig().set("items." + e.getKey().toString(), e.getValue()));
        saveConfig();
    }
}
