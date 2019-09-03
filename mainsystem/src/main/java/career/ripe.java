package career;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mainsystem.*;

public class ripe implements Listener {

    private final App plugin;

    public ripe(App plugin) {
        this.plugin = plugin;
    }

    // ===================================================================================
    @EventHandler
    public void onCropBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Location l = b.getLocation();
        Player P = e.getPlayer();
        player p = App.players.get(P.getName());

        if (b.getType() == Material.WHEAT || b.getType().toString().contains("CARROTS")
                || b.getType().toString().contains("POTATOES") || b.getType().toString().contains("BEETROOTS")) {
            BlockData data = e.getBlock().getBlockData();
            Ageable ag = (Ageable) data;
            if (ag.getAge() == ag.getMaximumAge()) {
                try {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (l.getBlock().getType() == Material.AIR) {
                                p.ripe++;
                                util.sendActionbarMessage(e.getPlayer(), "收成 : 生活經驗 +2 , 採收熟練度 +1");
                                App.addexp(e.getPlayer().getName(), 2);
                            }
                        }
                    }.runTaskLater(plugin, 1);
                } catch (Exception exc) {
                }
            }
        }

        if (b.getType() == Material.MELON || b.getType() == Material.PUMPKIN || b.getType() == Material.SUGAR_CANE) {
            if (b.getMetadata("placeby").isEmpty()) {

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (l.getBlock().getType() == Material.AIR) {
                            p.ripe++;
                            util.sendActionbarMessage(e.getPlayer(), "收成 : 生活經驗 +2 , 採收熟練度 +1");
                            App.addexp(e.getPlayer().getName(), 2);
                        }
                    }
                }.runTaskLater(plugin, 1);
            }
        }
    }

    // ===================================================================================
    @EventHandler
    @SuppressWarnings("deprecation")
    // 自動種植：特殊附魔
    public void autoPlanting(BlockBreakEvent e) {
        // 取得方塊
        Block block = e.getBlock();
        // 取的玩家資訊
        Player player = e.getPlayer();

        player p = App.players.get(player.getName());
        if (!p.cla.contains("農夫"))
            return;

        // 方塊暫存
        final Block tempBlock[] = new Block[3];
        tempBlock[0] = player.getWorld().getBlockAt(block.getLocation().add(0, 0, 0));
        // 判斷為哪個作物
        Material mateBlock = block.getType();
        final short cropsType = (short) (mateBlock.equals(Material.WHEAT) ? 0
                : (mateBlock.equals(Material.CARROTS) ? 1
                        : (mateBlock.equals(Material.POTATOES) ? 2 : (mateBlock.equals(Material.BEETROOTS) ? 3 : 9))));
        // player 的 cla 是玩家擁有的身分，用逗號隔開
        // 玩家放置的方塊會有一個 MetaData 叫 placeby ，值是 player id, isEmpty 代表他是天然的
        if (cropsType != 9) {

            BlockData blockData = block.getBlockData();
            Ageable age = (Ageable) blockData;
            // 必須是小麥且生長期(age)為 7 只有甜菜根為成熟期為 3
            if (age.getAge() == age.getMaximumAge()) {
                // 取的玩家手上的物品資訊
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();

                // 自訂義涵式 (判斷玩家手上的工具種類以及是否包含附魔)
                if (Function.JudgeHandItem(mainHandItem, "HOE", "自動種植")) {
                    // 判斷耐久附魔等級
                    int durability = mainHandItem.getEnchantmentLevel(Enchantment.DURABILITY);
                    // 判斷幸運附魔等級
                    int luck = mainHandItem.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                    // 取得附魔等級
                    short EnchantLevel = Function.GetEnchantmentLevel(mainHandItem.getItemMeta());

                    // 種子的 slot
                    int slot = 0;
                    // seed 的 ItemStack
                    ItemStack seed = null;
                    // 尋找 seed 並且取得 seed 所在的 slot 並且根據附魔等級來消耗對應的種子量
                    if ((cropsType == 0))
                        slot = Function.SearchItem(player, new ItemStack(Material.WHEAT_SEEDS), EnchantLevel);
                    else if ((cropsType == 1))
                        slot = Function.SearchItem(player, new ItemStack(Material.CARROT), EnchantLevel);
                    else if ((cropsType == 2))
                        slot = Function.SearchItem(player, new ItemStack(Material.POTATO), EnchantLevel);
                    else if ((cropsType == 3))
                        slot = Function.SearchItem(player, new ItemStack(Material.BEETROOT_SEEDS), EnchantLevel);

                    if (Function.JudgeItemCharge(mainHandItem, 1)) {
                        // 耐久度必須大於等於 最大耐久度 slot 小於 36 代表有找到種子(不包含左手空間)
                        if (mainHandItem.getDurability() <= mainHandItem.getType().getMaxDurability() && slot < 36) {
                            // 體力消耗
                            player.setExhaustion(player.getExhaustion() + ((float) EnchantLevel / 2));
                            if (player.getFacing().getModX() != 0) {
                                // 右邊方塊
                                tempBlock[1] = player.getWorld().getBlockAt(block.getLocation().add(0, 0, -1));
                                // 左邊方塊
                                tempBlock[2] = player.getWorld().getBlockAt(block.getLocation().add(0, 0, 1));
                            } else {
                                tempBlock[1] = player.getWorld().getBlockAt(block.getLocation().add(1, 0, 0));
                                tempBlock[2] = player.getWorld().getBlockAt(block.getLocation().add(-1, 0, 0));
                            }
                            // 左右邊方塊破壞(根據附魔等級來判斷要破壞那些方塊)
                            for (int i = 1; i < EnchantLevel; i++) {
                                // 因為小麥與甜菜都是單產量的作物
                                if ((cropsType == 0 && tempBlock[i].getData() == (byte) 7)
                                        || ((cropsType == 3 && tempBlock[i].getData() == (byte) 3))) {

                                    p.ripe++;
                                    util.sendActionbarMessage(e.getPlayer(), "收成 : 生活經驗 +2 , 採收熟練度 +1");
                                    App.addexp(e.getPlayer().getName(), 2);

                                    tempBlock[i].breakNaturally();
                                } else {
                                    // 旁邊必須要生長期滿的作物才會掉東西
                                    if (tempBlock[i].getData() == (byte) 7) {

                                        p.ripe++;
                                        util.sendActionbarMessage(e.getPlayer(), "收成 : 生活經驗 +2 , 採收熟練度 +1");
                                        App.addexp(e.getPlayer().getName(), 2);

                                        tempBlock[i].setType(Material.AIR);
                                        // 隨機掉落 1~4個(再根據幸運附魔而增加掉落)
                                        int itemStack = (int) (Math.random() * (luck + 4)) + 1;
                                        // 掉落馬鈴薯或胡蘿蔔
                                        player.getWorld().dropItem(tempBlock[i].getLocation(), new ItemStack(
                                                ((cropsType == 1) ? Material.CARROT : Material.POTATO), itemStack));
                                    }
                                }
                            }

                            // 取的種子的 itemStack
                            seed = player.getInventory().getItem(slot);
                            /// 鋤頭耐久度減少 根據原版公式 (100/(耐久等?+1))%
                            for (int i = 0; i < EnchantLevel; i++) {
                                if ((int) (Math.random() * 100) < (int) (100 / (durability + 1))) {
                                    // 假如兩邊的方塊下非耕地就不消耗耐久
                                    if (player.getWorld().getBlockAt(tempBlock[i].getLocation().add(0, -1, 0)).getType()
                                            .equals(Material.FARMLAND)) {
                                        player.getInventory().getItemInMainHand()
                                                .setDurability((short) (mainHandItem.getDurability() + (short) 1));
                                    }
                                }
                            }
                            // 種子減少(根據附魔等級)
                            player.getInventory().setItem(slot,
                                    new ItemStack(seed.getType(), seed.getAmount() - EnchantLevel));
                            // 能量減少
                            Function.OperaEnergy(mainHandItem, -1);
                            // 延遲 4 tick 時間，由於破壞方塊馬上放置新方塊會導致新方塊被破壞
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    // 附魔等級幾等就判斷幾次
                                    for (int i = 0; i < EnchantLevel; i++) {
                                        // 判斷旁邊是否為耕地
                                        if (player.getWorld().getBlockAt(tempBlock[i].getLocation().add(0, -1, 0))
                                                .getType().equals(Material.FARMLAND)) {
                                            // 種下不同的作物
                                            switch (cropsType) {
                                            case 0:
                                                tempBlock[i].setType(Material.WHEAT);
                                                break;
                                            case 1:
                                                tempBlock[i].setType(Material.CARROTS);
                                                break;
                                            case 2:
                                                tempBlock[i].setType(Material.POTATOES);
                                                break;
                                            case 3:
                                                tempBlock[i].setType(Material.BEETROOTS);
                                                break;
                                            default:
                                                break;
                                            }
                                            // 粒子效果
                                            if (cropsType <= 3) {
                                                tempBlock[i].getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                                                        tempBlock[i].getLocation().add(0.5, 0, 0.5), 10, 0.25, 0.125,
                                                        0.25, 1.0, null, false);
                                            }
                                        } else {
                                            // 種植失敗就歸還種子
                                            switch (cropsType) {
                                            case 0:
                                                player.getInventory().addItem(new ItemStack(Material.WHEAT_SEEDS));
                                                break;
                                            case 1:
                                                player.getInventory().addItem(new ItemStack(Material.CARROT));
                                                break;
                                            case 2:
                                                player.getInventory().addItem(new ItemStack(Material.POTATO));
                                                break;
                                            case 3:
                                                player.getInventory().addItem(new ItemStack(Material.BEETROOT_SEEDS));
                                                break;
                                            default:
                                                break;
                                            }
                                        }
                                    }
                                }
                            }.runTaskLater(this.plugin, 4L);
                        }
                    }
                }
            }
        }
    }
}
