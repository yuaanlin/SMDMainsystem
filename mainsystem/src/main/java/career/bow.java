package career;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;

import mainsystem.App;
import mainsystem.player;
import mainsystem.util;
import net.dv8tion.jda.api.EmbedBuilder;

public class bow implements Listener {

    private final App plugin;

    public bow(App plugin) {
        this.plugin = plugin;
        effect();
    }

    // ==================================================================================================
    public ArrayList<Arrow> arrowList = new ArrayList<>();

    @EventHandler
    // 爆破箭矢：特殊附魔? 暫定名子
    public void onPlayerShoot(EntityShootBowEvent e) {

        if (e.getProjectile() instanceof Arrow && ((Arrow) e.getProjectile()).getShooter() instanceof Player) {
            // 取得弓箭
            Arrow arrow = (Arrow) e.getProjectile();
            // 取得射手
            Player player = (Player) arrow.getShooter();

            player p = App.players.get(player.getName());
            if (!p.cla.contains("弓手"))
                return;

            // 取的玩家手上的物品資訊
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();

            // 確定玩家不是空手，且手上到具有特殊附魔(用Lore顯示)
            if (Function.JudgeHandItem(mainHandItem, "BOW", "爆破箭矢")) {
                // 必須要拉滿弦
                if (e.getForce() == 1.0) {
                    // 取得附魔等級
                    short EnchantLevel = Function.GetEnchantmentLevel(mainHandItem.getItemMeta());
                    if (Function.JudgeItemCharge(mainHandItem, EnchantLevel)) {
                        // 射擊的玩家
                        arrow.setMetadata("ShootBy", new FixedMetadataValue(plugin, player.getName()));
                        // 附魔等級
                        arrow.setMetadata("Explosion", new FixedMetadataValue(plugin, Short.toString(EnchantLevel)));
                        arrowList.add(arrow);
                        Function.OperaEnergy(mainHandItem, -EnchantLevel);
                    }
                }
            }
        }
    }

    @EventHandler
    public void arrowExplosion(ProjectileHitEvent e) {

        try {

            if (!(e.getEntity() instanceof Arrow))
                return;

            // 箭矢
            Arrow arrow = (Arrow) e.getEntity();
            // 判斷是 MetaData 的 ShootBy 是否有值
            if (!arrow.getMetadata("ShootBy").isEmpty()) {

                float power = arrow.getMetadata("Explosion").get(0).asFloat() * 1.5f;

                Location loc = arrow.getLocation();

                Boolean explode = true;
                player da = App.players.get(arrow.getMetadata("ShootBy").get(0).asString());
                for (Player P : util.getPlayerNear(loc, 8)) {
                    player en = App.players.get(P.getName());
                    if (!util.judgePVP(da, en)) {
                        explode = false;
                    }
                }

                if (explode) {
                    BlockFace face = arrow.getFacing();
                    double x = loc.getX(), y = loc.getY(), z = loc.getZ();
                    arrow.getWorld().createExplosion(x + face.getModX(), y + 1, z - face.getModZ(), power, false,
                            false);
                    arrow.getWorld().spawnParticle(Particle.FLAME, loc, 300, 1.0, 1.0, 1.0, (power / 15), null, true);
                    arrow.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 150, 1.0, 1.0, 1.0, (power / 15), null,
                            true);
                    arrow.getWorld().spawnParticle(Particle.LAVA, loc, 50, 1.5, 1.0, 1.5, (power / 15), null, true);
                }

                arrowList.remove(arrow);
                arrow.remove();
            }

        } catch (Exception exc) {

            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));

            // 發送記錄到 Discord 監控頻道
            EmbedBuilder MEbuilder = new EmbedBuilder();
            MEbuilder.setColor(Color.RED);
            MEbuilder.setTitle("分流 " + App.servername + " 發生錯誤");
            MEbuilder.appendDescription(errors.toString());
            App.systembot.sendtoSystemChannel(MEbuilder.build());

        }

    }

    public void effect() {
        // 延遲 2 tick 時間
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Projectile arrow : arrowList) {
                    arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 8, 0.0, 0.0, 0.0, 0.05, null,
                            true);
                }
            }
        }, 0L, 1L);
    }
}