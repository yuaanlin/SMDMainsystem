package career;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import java.util.*;

import mainsystem.*;

public class sword implements Listener {

    private final App plugin;
    // 能量消耗的倍數(附魔等級 * 倍數 = 能量消耗量)
    // private final int multiply = 6;
    private ArrayList<ShockWave> shockWaveList = new ArrayList<>();

    public sword(App plugin) {
        this.plugin = plugin;
        effect();
    }

    @Deprecated
    @EventHandler
    // 威力打擊：特殊附魔
    public void PowerStrike(PlayerInteractEvent e) {
        // 確定玩家不是空手，且手上到具有特殊附魔(用Lore顯示)
        Player player = e.getPlayer();

        player p = App.players.get(player.getName());
        if (!p.cla.contains("劍士"))
            return;

        // 使用左鍵時 且站在地板上
        if ((e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)
                && player.isOnGround()) {
            // 取的玩家手上的物品資訊
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            // 判斷手上的物品
            if (Function.JudgeHandItem(mainHandItem, "SWORD", "威力打擊")) {
                if (player.getFoodLevel() >= 1) {
                    if (mainHandItem.getDurability() <= mainHandItem.getType().getMaxDurability()) {
                        // 取得附魔等級
                        short EnchantLevel = Function.GetEnchantmentLevel(mainHandItem.getItemMeta());
                        // 判斷玩家有沒有能量
                        if (Function.JudgeItemCharge(mainHandItem, EnchantLevel * 6)) {
                            // 技能執行
                            shockWaveList.add(new ShockWave(e.getPlayer(), (short) EnchantLevel));

                            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0f, 0.1f);
                            player.setFoodLevel((int) (player.getFoodLevel() - 1));
                            // 耐久度減少
                            Function.DurabilityReduce(mainHandItem, EnchantLevel * 3);
                            mainHandItem.setItemMeta(Function.OperaEnergy(mainHandItem, -(EnchantLevel * 6)));
                        }
                    }
                }
            }
        }
    }

    // 效果
    public void effect() {
        // 重複 1 tick 時間 (Thread)
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                // 所有的衝擊波迴圈
                try {
                    for (ShockWave shockWave : shockWaveList) {
                        shockWave.getWorld().playSound(shockWave.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.25f,
                                0.75f);
                        // 判斷衝擊波是否還存在(isAlive有計算存活時間tick) 且 是否撞到方塊
                        if (shockWave.isAlive() && shockWave.getLocation().getBlock().getType().equals(Material.AIR)) {
                            // 前兩個 tick 不做事
                            if (shockWave.getTravelTime() >= 2) {
                                // 取得衝擊波附近的實體(Entity)
                                for (Entity nearEntity : shockWave.getLocation().getWorld()
                                        .getNearbyEntities(shockWave.getLocation(), 0.75, 0.75, 0.75)) {
                                    // 判斷實體(不是使用者本人 且 有生命實體)
                                    if (!nearEntity.equals(shockWave.getPlayer())
                                            && nearEntity instanceof LivingEntity) {
                                        // 強制轉型(Casting)
                                        LivingEntity entity = (LivingEntity) nearEntity;
                                        // 判斷實體是否可以再被攻擊
                                        if (entity.getNoDamageTicks() == 0) {
                                            // 模擬實體被打中時的擊退效果
                                            entity.setVelocity(shockWave.getEyeLocation().getDirection().multiply(1.2));
                                            // 實體損失血量
                                            entity.damage(shockWave.getEnchantLevel() * 3.0);
                                            // 實體是否死亡
                                            if (entity.isDead()) {
                                                // 實體是玩家 or 生物
                                                if (entity instanceof Player) {
                                                    // 顯示擊殺
                                                    // shockWave.getPlayer().sendMessage(entity.getName() + "was killed
                                                    // by" + shockWave.getPlayer().getName());
                                                } else {
                                                    // 顯示擊殺
                                                    // shockWave.getPlayer().sendMessage(" creature killed by " +
                                                    // shockWave.getPlayer().getName());
                                                }
                                            }
                                            // 設定時體無敵時間(太短會造成連續傷害)
                                            entity.setNoDamageTicks(18);
                                        }
                                    }
                                }
                                // 顯示粒子特效
                                shockWave.getWorld().spawnParticle(Particle.SWEEP_ATTACK, shockWave.getLocation(), 4,
                                        0.5, 0.25, 0.5, 0.1, null, true);
                            }
                        } else
                            shockWaveList.remove(shockWave); // 移除衝擊波 因為生命週期已過
                    }
                } catch (Exception exc) { }
            }
        }, 0L, 1L);
    }
}

class ShockWave {
    final private Player player; // 釋放衝擊波的玩家
    private short enhantlevel = 1; // 附魔等級
    private Location loc; // 當前位置
    private Location eyeLoc; // 玩家看的方向
    private int travelTime = 0; // 生命時間
    private int maxTravelTime;

    public ShockWave(Player player, short level) {
        this.player = player;
        enhantlevel = level;
        loc = player.getLocation();
        eyeLoc = player.getEyeLocation();
        maxTravelTime = (int) (level * 4);
    }

    public short getEnchantLevel() {
        return enhantlevel;
    }

    // 回傳位置
    public Location getLocation() {
        return loc;
    }

    // 回傳玩家看的位置
    public Location getEyeLocation() {
        return eyeLoc;
    }

    // 取的當前世界
    public World getWorld() {
        return player.getWorld();
    }

    // 取得釋放玩家
    public Player getPlayer() {
        return player;
    }

    // 取的生命時間
    public int getTravelTime() {
        return travelTime;
    }

    // 判斷是否還存在(20 tick 為 1秒)
    public boolean isAlive() {
        travel();
        return (travelTime <= maxTravelTime);
    }

    // 粒子移動
    public void travel() {
        travelTime++;
        Vector temp = eyeLoc.getDirection().multiply(1.1);
        loc.add(temp.getX(), 0, temp.getZ());
    }
}