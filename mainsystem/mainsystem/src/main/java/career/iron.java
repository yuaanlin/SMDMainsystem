package career;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mainsystem.App;
import mainsystem.player;
import mainsystem.region;
import mainsystem.util;

public class iron implements Listener {

    // 最大挖掘數 (看要不要用挖礦值來增加)
    final int maxAmount = 5;
    // 能量消耗的倍數(附魔等級 * 倍數 = 能量消耗量)
    private final int multiply = 1;

    @EventHandler
    public void getIronExp(BlockBreakEvent e) {

        Player P = e.getPlayer();
        player p = App.players.get(P.getName());
        Block b = e.getBlock();
        if (e.isCancelled())
            return;

        if (e.getBlock().getMetadata("placeby").isEmpty()) {
            if (b.getType().toString().contains("ORE")) {

                if (b.getType().toString().contains("IRON")) {
                    p.iron += 6;
                    util.sendActionbarMessage(P, "鐵礦 : 生活經驗 +10 , 採礦熟練度 +6");
                    App.addexp(P.getName(), 10);
                } else if (b.getType().toString().contains("DIAMOND")) {
                    if (P.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
                        e.setCancelled(true);
                        ItemStack diamond = new ItemStack(Material.DIAMOND);
                        b.setType(Material.AIR);
                        int r = (int) (Math.random() * 3);
                        if (r == 0) {
                            diamond.setAmount(2);
                            p.diamond += 2;
                        } else {
                            diamond.setAmount(1);
                            p.diamond += 1;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                b.getWorld().dropItemNaturally(b.getLocation(), diamond);
                            }
                        }.runTaskLater(App.getPlugin(), 1L);
                    } else if (P.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
                        e.setCancelled(true);
                        ItemStack diamond = new ItemStack(Material.DIAMOND);
                        b.setType(Material.AIR);
                        int r = (int) (Math.random() * 4);
                        if (r == 0) {
                            diamond.setAmount(2);
                            p.diamond += 2;
                        } else if (r == 1) {
                            diamond.setAmount(3);
                            p.diamond += 3;
                        } else {
                            diamond.setAmount(1);
                            p.diamond += 1;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                b.getWorld().dropItemNaturally(b.getLocation(), diamond);
                            }
                        }.runTaskLater(App.getPlugin(), 1L);
                    } else if (P.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
                        e.setCancelled(true);
                        ItemStack diamond = new ItemStack(Material.DIAMOND);
                        b.setType(Material.AIR);
                        int r = (int) (Math.random() * 5);
                        if (r == 0) {
                            diamond.setAmount(2);
                            p.diamond += 2;
                        } else if (r == 1) {
                            diamond.setAmount(3);
                            p.diamond += 3;
                        } else if (r == 2) {
                            diamond.setAmount(4);
                            p.diamond += 4;
                        } else {
                            diamond.setAmount(1);
                            p.diamond += 1;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                b.getWorld().dropItemNaturally(b.getLocation(), diamond);
                            }
                        }.runTaskLater(App.getPlugin(), 1L);
                    }

                    p.iron += 20;
                    util.sendActionbarMessage(P, "鑽石礦 : 生活經驗 +40 , 採礦熟練度 +20");
                    App.addexp(P.getName(), 40);
                } else if (b.getType().toString().contains("EMERALD")) {
                    p.iron += 30;
                    util.sendActionbarMessage(P, "綠寶石礦 : 生活經驗 +30 , 採礦熟練度 +30");
                    App.addexp(P.getName(), 30);
                } else if (b.getType().toString().contains("GOLD")) {

                    if (P.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 1) {
                        e.setCancelled(true);
                        ItemStack gold = new ItemStack(Material.GOLD_ORE);
                        b.setType(Material.AIR);
                        int r = (int) (Math.random() * 3);
                        if (r == 0) {
                            gold.setAmount(2);
                            p.gold += 2;
                        } else {
                            gold.setAmount(1);
                            p.gold += 1;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                b.getWorld().dropItemNaturally(b.getLocation(), gold);
                            }
                        }.runTaskLater(App.getPlugin(), 1L);
                    } else if (P.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 2) {
                        e.setCancelled(true);
                        ItemStack gold = new ItemStack(Material.GOLD_ORE);
                        b.setType(Material.AIR);
                        int r = (int) (Math.random() * 4);
                        if (r == 0) {
                            gold.setAmount(2);
                            p.gold += 2;
                        } else if (r == 1) {
                            gold.setAmount(3);
                            p.gold += 3;
                        } else {
                            gold.setAmount(1);
                            p.gold += 1;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                b.getWorld().dropItemNaturally(b.getLocation(), gold);
                            }
                        }.runTaskLater(App.getPlugin(), 1L);
                    } else if (P.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) == 3) {
                        e.setCancelled(true);
                        ItemStack gold = new ItemStack(Material.GOLD_ORE);
                        b.setType(Material.AIR);
                        int r = (int) (Math.random() * 5);
                        if (r == 0) {
                            gold.setAmount(2);
                            p.gold += 2;
                        } else if (r == 1) {
                            gold.setAmount(3);
                            p.gold += 3;
                        } else if (r == 2) {
                            gold.setAmount(4);
                            p.gold += 4;
                        } else {
                            gold.setAmount(1);
                            p.gold += 1;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                b.getWorld().dropItemNaturally(b.getLocation(), gold);
                            }
                        }.runTaskLater(App.getPlugin(), 1L);
                    }
                    p.iron += 10;
                    util.sendActionbarMessage(P, "金礦 : 生活經驗 +20 , 採礦熟練度 +10");
                    App.addexp(P.getName(), 20);
                } else if (b.getType().toString().contains("LAPIS")) {
                    p.iron += 4;
                    util.sendActionbarMessage(P, "青金石礦 : 生活經驗 +8 , 採礦熟練度 +4");
                    App.addexp(P.getName(), 8);
                } else if (b.getType().toString().contains("REDSTONE")) {
                    p.iron += 4;
                    util.sendActionbarMessage(P, "紅石礦 : 生活經驗 +4 , 採礦熟練度 +4");
                    App.addexp(P.getName(), 4);
                } else if (b.getType().toString().contains("COAL")) {
                    p.iron += 2;
                    util.sendActionbarMessage(P, "煤礦 : 生活經驗 +4 , 採礦熟練度 +2");
                    App.addexp(P.getName(), 4);
                }
            }
        }
    }

    // ===================================================================================
    @EventHandler
    // 爆破挖掘：特殊附魔? 暫定名子
    @SuppressWarnings("deprecation")
    public void MiningBlast(BlockBreakEvent e) {
        // 取得方塊
        Block block = e.getBlock();

        // 取的玩家資訊
        Player player = e.getPlayer();
        player p = App.players.get(player.getName());

        if (!p.cla.contains("礦工"))
            return;

        // 玩家面向的單位向量 (X, Y, Z)
        Vector unitFacing = player.getFacing().getDirection();

        // player 的 cla 是玩家擁有的身分，用逗號隔開
        // 玩家放置的方塊會有一個 MetaData 叫 placeby ，值是 player id, isEmpty 代表他是天然的
        if (block.getType().equals(Material.STONE) && block.getMetadata("placeby").isEmpty()) {

            // 取的玩家手上的物品資訊
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            // 確定玩家不是空手，且手上到具有特殊附魔(用Lore顯示)
            try {
                if (Function.JudgeHandItem(mainHandItem, "PICKAXE", "深度挖掘")) {

                    // 取得附魔等級
                    short EnchantLevel = Function.GetEnchantmentLevel(mainHandItem.getItemMeta());

                    if (player.getFoodLevel() >= (EnchantLevel * 2)) {
                        if (Function.JudgeItemCharge(mainHandItem, EnchantLevel * multiply)) {
                            int amount = 0;
                            // 用附魔等級增加高度
                            for (int y = 0; y < EnchantLevel; y++) {
                                // 暫存石頭向量位置
                                Vector vetorStone = new Vector(0.0, y, 0.0);
                                // 取得下個要破壞的方塊
                                Block nextBlock = player.getWorld().getBlockAt(block.getLocation().add(vetorStone));

                                // 判斷是否石頭
                                for (int i = 0; (i < maxAmount) && (nextBlock.getType().equals(Material.STONE)); i++) {

                                    if (App.inwhichregion(nextBlock.getLocation()) != null) {

                                        region r = App.inwhichregion(nextBlock.getLocation());

                                        if (!r.hasPermission(p, "break")) {
                                            return;
                                        }

                                    }

                                    // 判斷耐久值(可以在這判斷是否石頭在別人領地)
                                    if (mainHandItem.getDurability() <= mainHandItem.getType().getMaxDurability()) {

                                        // 鎬子耐久度減少 根據原版公式 (100/(耐久等級 + 1))%
                                        Function.DurabilityReduce(mainHandItem, 1);
                                        // 破壞方塊(絲綢之觸效果)
                                        if (mainHandItem.containsEnchantment(Enchantment.SILK_TOUCH)) {
                                            nextBlock.setType(Material.AIR);
                                            nextBlock.getWorld().dropItem(nextBlock.getLocation(),
                                                    new ItemStack(Material.STONE));
                                        } else
                                            nextBlock.breakNaturally(mainHandItem);
                                        // 播放爆炸特效(第一次不觸發特效)
                                        if (amount > 0)
                                            nextBlock.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,
                                                    nextBlock.getLocation().add(0.5, 0, 0.5), 2, 0, 0, 0, 0.5, null,
                                                    true);
                                        // 向量繼續上加
                                        vetorStone.add(unitFacing);
                                        // 再次取得上方的方塊
                                        nextBlock = player.getWorld().getBlockAt(block.getLocation().add(vetorStone));
                                        // 計算挖石頭的量
                                        amount++;
                                    } else
                                        break;
                                }
                            }
                            // 減少飢餓度 (有挖掉2個以上才使用技能)
                            if (amount >= 2) {
                                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f,
                                        1.0f);
                                player.getWorld().playSound(block.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f,
                                        0.5f);
                                mainHandItem
                                        .setItemMeta(Function.OperaEnergy(mainHandItem, -(EnchantLevel * multiply)));
                            }
                        }
                    }
                }
            } catch (Exception exc) {
            }
        }
    }
}