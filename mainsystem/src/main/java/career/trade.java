package career;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import mainsystem.*;

public class trade implements Listener {

    private final App plugin;

    public trade(App plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTrade(InventoryClickEvent e) {
        Player P = (Player) e.getWhoClicked();
        player p = App.players.get(P.getName());

        if (e.getInventory() != null && e.getInventory().getType() == InventoryType.MERCHANT) {
            int amo0 = 0;

            if (e.getInventory().getItem(0) != null)
                amo0 = e.getInventory().getItem(0).getAmount();

            if (e.getRawSlot() == 2) {

                if (e.getInventory().getItem(2) != null) {

                    if (e.getInventory().getItem(0) != null
                            && e.getInventory().getItem(0).getType() == Material.EMERALD) {
                        p.putted_EMERALD = e.getInventory().getItem(0).getAmount();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (e.getInventory().getItem(0) != null)
                                    p.putted_EMERALD -= e.getInventory().getItem(0).getAmount();
                                p.trade += p.putted_EMERALD * 5;

                                App.addexp(P.getName(), p.putted_EMERALD * 5);
                                util.sendActionbarMessage(P,
                                        "交易 : 生活經驗 + " + p.putted_EMERALD * 5 + " , 貿易熟練度 +" + p.putted_EMERALD * 5);
                                p.putted_EMERALD = -1;

                                // 給予機率性特殊道具
                                if (p.cla.contains("商人")) {
                                    plugin.giveRandomItem(P);
                                }

                            }
                        }.runTaskLater(plugin, 1);
                    } else if (e.getInventory().getItem(1) != null
                            && e.getInventory().getItem(1).getType() == Material.EMERALD) {
                        p.putted_EMERALD = e.getInventory().getItem(1).getAmount();
                        new BukkitRunnable() {
                            @Override
                            public void run() {

                                if (e.getInventory().getItem(1) != null)
                                    p.putted_EMERALD -= e.getInventory().getItem(1).getAmount();
                                p.trade += p.putted_EMERALD * 5;

                                App.addexp(P.getName(), p.putted_EMERALD * 5);
                                util.sendActionbarMessage(P,
                                        "交易 : 生活經驗 + " + p.putted_EMERALD * 5 + " , 貿易熟練度 +" + p.putted_EMERALD * 5);
                                p.putted_EMERALD = -1;

                                // 給予機率性特殊道具
                                if (p.cla.contains("商人")) {
                                    plugin.giveRandomItem(P);
                                }

                            }
                        }.runTaskLater(plugin, 1);
                    } else if (e.getInventory().getItem(2) != null
                            && e.getInventory().getItem(2).getType() == Material.EMERALD) {
                        p.putted_EMERALD = e.getInventory().getItem(2).getAmount();
                        final int amo00 = amo0;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (e.getInventory().getItem(0) != null
                                        && e.getInventory().getItem(0).getAmount() == amo00)
                                    return;
                                p.trade += p.putted_EMERALD * 5;

                                App.addexp(P.getName(), p.putted_EMERALD * 5);
                                util.sendActionbarMessage(P,
                                        "交易 : 生活經驗 + " + p.putted_EMERALD * 5 + " , 貿易熟練度 +" + p.putted_EMERALD * 5);
                                p.putted_EMERALD = -1;

                                // 給予機率性特殊道具
                                if (p.cla.contains("商人")) {
                                    plugin.giveRandomItem(P);
                                }
                            }
                        }.runTaskLater(plugin, 1);

                    }
                }
            }
        }
    }

}