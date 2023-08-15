package ch.toyaxo.inventoryswap;

import ch.toyaxo.inventoryswap.commands.SwapCMD;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class InventorySwap extends JavaPlugin {

    public static BukkitTask task;

    public void onEnable() {
        saveDefaultConfig();
        getCommand("swap").setExecutor((CommandExecutor)new SwapCMD(this));
        getCommand("swap-stop").setExecutor((CommandExecutor)new SwapCMD(this));
    }

    public void onDisable() {
        System.out.println();
    }
}
