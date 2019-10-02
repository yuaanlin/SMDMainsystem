package missionclass;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import mainsystem.App;
import mainsystem.player;
import mainsystem.util;

public class main implements Listener {

    @EventHandler
    public void missionPlayerJoin(PlayerJoinEvent e) {

        Player P = e.getPlayer();
        player p = App.players.get(P.getName());

        if (!p.settings.containsKey("mission") || p.settings.get("mission").equals("0")) {
            p.settings.put("mission", "0");
            P.kickPlayer(ChatColor.RED + "你沒有進行中的挑戰，無法進入副本分流");
            return;
        }

        missiongroup mg;
        if (!App.missiongroups.containsKey(Integer.parseInt(p.settings.get("mission")))) {
            App.missiongroups.put(Integer.parseInt(p.settings.get("mission")),
                    new missiongroup(Integer.parseInt(p.settings.get("mission"))));
        }
        mg = App.missiongroups.get(Integer.parseInt(p.settings.get("mission")));

        P.sendMessage("world-" + p.settings.get("mission"));

        // 該隊伍第一次登入 -> 創建隊伍世界
        if (Bukkit.getServer().getWorld("world-" + p.settings.get("mission")) == null) {

            P.sendMessage("null");
            Bukkit.getConsoleSender().sendMessage("[副本分流] 隊伍 " + p.settings.get("mission") + " 的隊員第一次進入，創建主世界 ... ");
            Bukkit.getServer().createWorld(new WorldCreator("world-" + p.settings.get("mission")));
            Bukkit.getConsoleSender().sendMessage("[副本分流] 隊伍 " + p.settings.get("mission") + " 的隊員第一次進入，創建地域 ... ");
            Bukkit.getServer().createWorld(
                    new WorldCreator("world-" + p.settings.get("mission") + "_nether").environment(Environment.NETHER));
            Bukkit.getConsoleSender().sendMessage("[副本分流] 隊伍 " + p.settings.get("mission") + " 的隊員第一次進入，創建終界 ... ");
            Bukkit.getServer().createWorld(new WorldCreator("world-" + p.settings.get("mission") + "_the_end")
                    .environment(Environment.THE_END));
        } else {
            P.sendMessage("not null");
        }

        Location l = Bukkit.getWorld("world-" + p.settings.get("mission")).getSpawnLocation();

        // 傳送玩家到該世界
        P.teleport(l);

        // 發送訊息
        P.sendMessage(ChatColor.DARK_AQUA + " " + ChatColor.UNDERLINE + "副本挑戰" + ChatColor.WHITE
                + " 歡迎回到副本挑戰分流! 這裡是你和你的組員的專屬世界 (編號 " + mg.id + ")");
        P.sendMessage(ChatColor.WHITE + " 別忘記，副本挑戰的時間限制是一個星期唷!");
        P.sendMessage(ChatColor.WHITE + " 你是 " + mg.starttime + " 開始挑戰的 ~");
        P.sendMessage(ChatColor.WHITE + " 礙於技術限制，有些部分要請您注意喔：");
        P.sendMessage(ChatColor.WHITE + " 每次世界傳送的時候 (例如使用地獄門) 都會移動到另一個世界的同一個點，");
        P.sendMessage(ChatColor.WHITE + " 且另外一個世界不會產生傳送門喔! 首次前往地域前請記得攜帶黑曜石和打火機 !");
    }

    @EventHandler
    public void portalredirect(PlayerPortalEvent e) {

        Player P = e.getPlayer();
        player p = App.players.get(P.getName());
        missiongroup mg = App.missiongroups.get(Integer.parseInt(p.settings.get("mission")));

        if (e.getCause() == TeleportCause.NETHER_PORTAL) {
            String worldname = "";
            if (e.getFrom().getWorld().getName().contains("_nether")) {
                worldname = e.getFrom().getWorld().getName().replace("_nether", "");
                World w = Bukkit.getWorld(worldname);
                Location loc = w.getSpawnLocation();
                e.setTo(loc);
            } else {
                worldname = e.getFrom().getWorld().getName() + "_nether";
                World w = Bukkit.getWorld(worldname);
                Location loc = w.getSpawnLocation();
                loc.setY(1);
                while (!(loc.getBlock().getType().equals(Material.NETHERRACK)
                        && loc.add(0, 1, 0).getBlock().getType().equals(Material.AIR))) {
                    loc = loc.add(0, 1, 0);
                    if (loc.getY() > 150) {
                        loc.add(1, 0, 0);
                        loc.setY(0);
                    }
                }
                e.setTo(loc);
            }
        }
        if (e.getCause() == TeleportCause.END_PORTAL) {
            String worldname = "";
            if (e.getFrom().getWorld().getName().contains("_the_end")) {

                worldname = e.getFrom().getWorld().getName().replace("_the_end", "");
                World w = Bukkit.getWorld(worldname);
                Location loc = w.getSpawnLocation();
                e.setTo(loc);

                Connection conn = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
                    conn = DriverManager.getConnection(datasource);
                    Statement st = conn.createStatement();
                    st.execute("UPDATE `mission` SET `endtime`='" + util.now() + "' WHERE id=" + mg.id + ";");
                    conn.close();
                } catch (Exception exc) {
                    StringWriter errors = new StringWriter();
                    exc.printStackTrace(new PrintWriter(errors));
                    Bukkit.getConsoleSender()
                            .sendMessage(ChatColor.RED + "[資料庫] 副本完成時更新資料庫發生問題 : " + errors.toString());
                } finally {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException exc) {
                        }
                    }
                }

                // 更新玩家的 mission 值
                p.settings.put("mission", "0");

                // 10 秒後傳送玩家回主分流
                P.sendMessage(ChatColor.DARK_AQUA + " " + ChatColor.UNDERLINE + "副本挑戰" + ChatColor.WHITE
                        + " 恭喜你完成挑戰! 我們將傳送你回傳送小鎮。");
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        P.performCommand("connect server_lobby");
                    }

                }.runTaskLater(App.getPlugin(), 200);

                final int gid = mg.id;
                // 20秒後刪除世界
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        Bukkit.getServer().unloadWorld("world-" + gid, true);
                        deleteWorld(new File("world-" + gid));
                        Bukkit.getServer().unloadWorld("world-" + gid + "_nether", true);
                        deleteWorld(new File("world-" + gid + "_nether"));
                        Bukkit.getServer().unloadWorld("world-" + gid + "_the_end", true);
                        deleteWorld(new File("world-" + gid + "_the_end"));
                        Bukkit.getConsoleSender().sendMessage("[副本分流] 隊伍 " + gid + " 的檔案已經全數刪除。");
                    }

                }.runTaskLater(App.getPlugin(), 400);

                // 發送完成公告
                App.PMH.sendAllServerBrocast(" ");
                App.PMH.sendAllServerBrocast(ChatColor.DARK_AQUA + " " + ChatColor.UNDERLINE + "副本挑戰" + ChatColor.WHITE
                        + " 玩家 " + P.getName() + " 完成一周屠龍挑戰了! ");

            } else {
                worldname = e.getFrom().getWorld().getName() + "_the_end";
                World w = Bukkit.getWorld(worldname);
                Location loc = w.getSpawnLocation();

                loc.setY(1);
                while (!(loc.getBlock().getType().equals(Material.BEDROCK)
                        && loc.add(0, 1, 0).getBlock().getType().equals(Material.AIR))) {
                    loc = loc.add(0, 1, 0);
                    if (loc.getY() > 150) {
                        loc.add(1, 0, 0);
                        loc.setY(0);
                    }
                }
                e.setTo(loc);
            }
        }
    }

    public static Map<Integer, missiongroup> missiongroup_initial() {
        Bukkit.getConsoleSender().sendMessage("[副本分流] 副本挑戰隊伍初始化中 ... ");
        Map<Integer, missiongroup> map = new HashMap<>();
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM `mission` WHERE `endtime`='進行中'");
            ResultSet rs = st.getResultSet();
            while (rs.next()) {
                missiongroup mg = new missiongroup(rs.getInt("id"));
                if (util.DateDiff(mg.starttime, util.now()) > 7) {
                    Statement st_overtime = conn.createStatement();
                    st_overtime.execute("UPDATE mission SET `endtime`='挑戰失敗' , `quittime`='" + util.now() + "'");
                    Bukkit.getConsoleSender().sendMessage("[副本分流] 隊伍 " + mg.id + " 挑戰失敗");
                } else {
                    map.put(rs.getInt("id"), mg);
                    Bukkit.getConsoleSender().sendMessage("[副本分流] 隊伍 " + mg.id + " 載入完成 : " + " 挑戰者 "
                            + rs.getString("players") + " 開始時間 " + mg.starttime);
                }
            }
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 載入副本玩家時發生問題 : " + errors.toString());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException exc) {
                }
            }
        }
        return map;
    }

    private static boolean deleteWorld(File path) {
        if (path.exists()) {
            File files[] = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteWorld(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

}