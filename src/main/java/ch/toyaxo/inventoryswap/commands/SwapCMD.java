package ch.toyaxo.inventoryswap.commands;

import ch.toyaxo.inventoryswap.InventorySwap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SwapCMD implements CommandExecutor {
    private final InventorySwap pluginMain;

    public SwapCMD(InventorySwap inventorySwap) {
        this.pluginMain = inventorySwap;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (label.equals("swap")) {
            if (InventorySwap.task != null) {
                sender.sendMessage(this.pluginMain.getConfig().getString("messages.cannot-start-two"));
                return true;
            }
            int timer = Integer.parseInt(args[0]);
            List<Player> players = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                Player p = Bukkit.getPlayer(args[i]);
                if (p == null) {
                    sender.sendMessage(this.pluginMain.getConfig().getString("messages.player-name-invalid"));
                    return true;
                }
                players.add(p);
            }
            if (players.size() < 2) {
                sender.sendMessage("Need at least 2 players !");
                return true;
            }
            StringBuilder pB = new StringBuilder();
            for (int j = 0; j < players.size(); j++)
                pB.append(((Player)players.get(j)).getDisplayName()).append((j != players.size() - 1) ? "," : "");
            startSwapTask(timer, players);
            Bukkit.broadcastMessage("§4"+String.format(this.pluginMain.getConfig().getString("messages.started-msg"), new Object[] { pB.toString() }));
            return true;
        }
        if (label.equalsIgnoreCase("swap-stop")) {
            stopSwapTask();
            Bukkit.broadcastMessage(this.pluginMain.getConfig().getString("messages.stopped-msg"));
        }
        return true;
    }

    private void startSwapTask(final int timer, final List<Player> players) {
        InventorySwap.task = Bukkit.getScheduler().runTaskTimer((Plugin)this.pluginMain, new Runnable() {
            int time = timer;

            public void run() {
                if (this.time == 15) {
                    Bukkit.broadcastMessage("§e"+this.time + " " + SwapCMD.this.pluginMain.getConfig().getString("messages.seconds-before-swapping-msg"));
                } else if (this.time == 0) {
                    if(!checkPlayers(players)){} else {
                        Bukkit.broadcastMessage("§c§l"+SwapCMD.this.pluginMain.getConfig().getString("messages.started-swapping-msg"));
                        List<ItemStack[]> contents = (List)new ArrayList<>();
                        List<ItemStack> offHands = new ArrayList<>();
                        List<ItemStack[]> armors = (List)new ArrayList<>();
                        players.forEach(p -> {
                            contents.add(p.getInventory().getContents());
                            offHands.add(p.getInventory().getItemInOffHand());
                            armors.add(new ItemStack[] { p.getInventory().getItem(EquipmentSlot.HEAD), p.getInventory().getItem(EquipmentSlot.CHEST), p.getInventory().getItem(EquipmentSlot.LEGS), p.getInventory().getItem(EquipmentSlot.FEET)});
                        });
                        for (int i = 0; i < players.size(); i++) {
                            Player p = players.get(i);
                            if (i == 0) {
                                p.getInventory().setContents(contents.get(players.size() - 1));
                                p.getInventory().setItemInOffHand(offHands.get(players.size() - 1));
                                p.getInventory().setHelmet(((ItemStack[])armors.get(players.size() - 1))[0]);
                                p.getInventory().setChestplate(((ItemStack[])armors.get(players.size() - 1))[1]);
                                p.getInventory().setLeggings(((ItemStack[])armors.get(players.size() - 1))[2]);
                                p.getInventory().setBoots(((ItemStack[])armors.get(players.size() - 1))[3]);
                            } else {
                                p.getInventory().setContents(contents.get(i - 1));
                                p.getInventory().setItemInOffHand(offHands.get(i - 1));
                                p.getInventory().setHelmet(((ItemStack[])armors.get(i - 1))[0]);
                                p.getInventory().setChestplate(((ItemStack[])armors.get(i - 1))[1]);
                                p.getInventory().setLeggings(((ItemStack[])armors.get(i - 1))[2]);
                                p.getInventory().setBoots(((ItemStack[])armors.get(i - 1))[3]);
                            }
                        }
                    }
                    this.time = timer;
                } else if (this.time <= 5) {
                    if (this.time == 1) {
                        Bukkit.broadcastMessage("§c§l"+this.time + " " + ((String)Objects.<String>requireNonNull(SwapCMD.this.pluginMain.getConfig().getString("messages.seconds-before-swapping-msg"))).toUpperCase());
                    } else {
                        Bukkit.broadcastMessage("§c"+this.time + " "+ SwapCMD.this.pluginMain.getConfig().getString("messages.seconds-before-swapping-msg"));
                    }
                }
                checkPlayers(players);
                players.forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(this.time + " " + SwapCMD.this.pluginMain.getConfig().getString("messages.seconds-before-swapping-msg"), ChatColor.BLUE)));
                this.time--;
            }
        },0L, 20L);
    }

    private void stopSwapTask() {
        InventorySwap.task.cancel();
        InventorySwap.task = null;
    }

    private boolean checkPlayers(final List<Player> players) {
        boolean allPlayersOnline = true;

        for (Player player : players) {
            if (!player.isOnline()) {
                stopSwapTask();
                Bukkit.broadcastMessage("§4§l" + this.pluginMain.getConfig().getString("messages.stopped-disconnect"));
                allPlayersOnline = false;
                break;
            }
        }
        return allPlayersOnline;
    }
}