package career;

import java.util.Stack;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mainsystem.*;

public class wood implements Listener {

    private final App plugin;

    public wood(App plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogBreak(BlockBreakEvent e) {

        Block b = e.getBlock();
        Location l = b.getLocation();
        Player P = e.getPlayer();
        player p = App.players.get(P.getName());

        if (P.getInventory().getItemInMainHand() != null && P.getInventory().getItemInMainHand().hasItemMeta()
                && P.getInventory().getItemInMainHand().getItemMeta().getLore() != null) {

            if (P.getInventory().getItemInMainHand().getItemMeta().getLore().toString().contains("重力砍伐")) {
                return;
            }
        }

        // 砍伐經驗
        if (e.getBlock().getType().toString().contains("LOG") && e.getBlock().getMetadata("placeby").isEmpty()) {
            try {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (l.getBlock().getType() == Material.AIR) {
                            p.wood++;
                            util.sendActionbarMessage(e.getPlayer(), "伐木 : 生活經驗 +3 , 伐木熟練度 +1");
                            App.addexp(e.getPlayer().getName(), 3);
                        }
                    }
                }.runTaskLater(plugin, 1);
            } catch (Exception exc) {

            }
        }
    }

    @EventHandler
    // 重力砍伐：特殊附魔
    public void heavyFelling(BlockBreakEvent e) {
        // 取得方塊
        Block block = e.getBlock();

        // 取的玩家資訊
        Player player = e.getPlayer();

        player p = App.players.get(player.getName());
        if (!p.cla.contains("木工"))
            return;

        // player 的 cla 是玩家擁有的身分，用逗號隔開
        // 玩家放置的方塊會有一個 MetaData 叫 placeby ，值是 player id, isEmpty 代表他是天然的
        if (block.getMetadata("placeby").isEmpty()) {

            // 取的玩家手上的物品資訊
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            if (Function.JudgeHandItem(mainHandItem, "AXE", "重力砍伐")) {
                // 判斷能量
                if (Function.JudgeItemCharge(mainHandItem, 1)) {
                    // 取得附魔等級
                    short EnchantLevel = Function.GetEnchantmentLevel(mainHandItem.getItemMeta());
                    if (LogJudge(block.getType(), EnchantLevel)) {
                        // 利用 stack 來儲存樹根分支
                        Stack<Blocks> blockStack = new Stack<>();
                        blockStack.push(new Blocks(player.getWorld().getBlockAt(block.getLocation().add(0, 0, 0))));
                        // 玩家挖掘數
                        int amount = 0;
                        // 一次性最大砍伐上限(太大會造成伺服器負擔)
                        int maxAmount = 110;
                        // 取得上方的方塊
                        Blocks nextBlock = new Blocks(player.getWorld().getBlockAt(block.getLocation().add(0, 0, 0)));
                        // 判斷效率等級(減少挖掘減速的Debuff時間)
                        int effectienty = mainHandItem.getEnchantmentLevel(Enchantment.DIG_SPEED);

                        // 當堆疊為空時 也就代表附近樹根都被砍了
                        while (!blockStack.isEmpty() && amount <= maxAmount) {
                            // 總共17個方位x y z (不包括一開始砍伐樹根下方)
                            while (nextBlock.getDirect() < 17) {
                                // 判斷是否為LOG
                                if (LogJudge(nextBlock.getNearBlock().getBlock().getType(), EnchantLevel)
                                        && block.getMetadata("placeby").isEmpty()) {
                                    Function.DurabilityReduce(mainHandItem, 1);
                                    nextBlock.getBlock().breakNaturally();
                                    nextBlock.getBlock().getWorld().spawnParticle(Particle.BLOCK_CRACK,
                                            nextBlock.getBlock().getLocation(), 5, 0.25, 0.25, 0.25, 0.1,
                                            Material.OAK_LOG.createBlockData(), false);
                                    // 將新的方塊推進堆疊裡
                                    blockStack.push(nextBlock);
                                    nextBlock = new Blocks(nextBlock.getNearBlock().getBlock());
                                } else {
                                    // 方位上加
                                    nextBlock.addDirect();
                                }
                                // 當所有方位被走過後就代表他是分支的最尾端樹根
                                if (nextBlock.getDirect() >= 17) {
                                    nextBlock.getBlock().breakNaturally();
                                    Function.DurabilityReduce(mainHandItem, 1);
                                }
                            }
                            // 把堆疊裡的方塊彈出來
                            nextBlock = blockStack.pop();
                            nextBlock.addDirect();
                            amount++;
                        }
                        Function.OperaEnergy(mainHandItem, -(int) (amount * 0.5));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,
                                (int) (amount - (effectienty * 3) > 0 ? (amount - (effectienty * 3)) * 3 : 0), 10,
                                false, false));
                        p.wood += amount;
                        util.sendActionbarMessage(e.getPlayer(),
                                "伐木 : 生活經驗 +" + amount * 3 + " , 伐木熟練度 +" + amount);
                        App.addexp(e.getPlayer().getName(), amount * 3);
                    }
                }
            }
        }
    }

    public boolean LogJudge(Material material, int level) {
        switch (level) {
        case 0:
            break;
        case 3:
            if (material.equals(Material.JUNGLE_LOG))
                return true;
        case 2:
            if (material.equals(Material.ACACIA_LOG) || material.equals(Material.DARK_OAK_LOG))
                return true;
        case 1:
            if (material.equals(Material.OAK_LOG) || material.equals(Material.BIRCH_LOG))
                return true;
            break;
        default:
            break;
        }
        return false;
    }

    public boolean LogJudge(Player player, Material material, int level) {
        switch (level) {
        case 0:
            break;
        case 3:
            if (material.equals(Material.JUNGLE_LOG)) {
                player.setFoodLevel(0);
                return true;
            }
        case 2:
            if (material.equals(Material.ACACIA_LOG) || material.equals(Material.DARK_OAK_LOG)) {
                player.setFoodLevel(player.getFoodLevel() - 5);
                return true;
            }
        case 1:
            if (material.equals(Material.OAK_LOG) || material.equals(Material.BIRCH_LOG)) {
                player.setFoodLevel(player.getFoodLevel() - 2);
                return true;
            }
            break;
        default:
            break;
        }
        return false;
    }
}

class Blocks {
    final static Vector dir[] = { new Vector(1, 0, 0), new Vector(-1, 0, 0), new Vector(0, 0, 1), new Vector(0, 0, -1),
            new Vector(1, 0, 1), new Vector(-1, 0, 1), new Vector(1, 0, -1), new Vector(-1, 0, -1), new Vector(0, 1, 0),
            new Vector(1, 1, 0), new Vector(-1, 1, 0), new Vector(0, 1, 1), new Vector(0, 1, -1), new Vector(1, 1, 1),
            new Vector(-1, 1, 1), new Vector(1, 1, -1), new Vector(-1, 1, -1) };
    Block block;
    int direct = 0;

    public Blocks(Block block) {
        this.block = block;
    }

    public int getDirect() {
        return direct;
    }

    public Block getBlock() {
        return block;
    }

    public void addDirect() {
        direct++;
    }

    public Location getNearBlock() {
        return block.getLocation().add(dir[direct]);
    }
}