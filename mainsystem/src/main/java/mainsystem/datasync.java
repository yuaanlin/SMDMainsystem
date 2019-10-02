package mainsystem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.nametagedit.plugin.NametagEdit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import shop.classes.shop_sell_item;

public class datasync {

    // 上傳單一交易所物品資料至資料庫
    public static void upload_shop(int id) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            shop_sell_item item = App.shop_sell.get(id);
            st.execute("UPDATE `shop_sell` SET `amount`=" + item.amount + " WHERE `id`='" + id + "'");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[資料庫] 上傳單一交易所物品 (" + id + ") 時發生問題: " + errors.toString());
        }
    }

    // 下載單一交易所物品資料至資料庫
    public static void download_shop(int id) {

        App.shop_sell.remove(id);

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM `shop_sell` WHERE `id`=" + id + ";");
            ResultSet rs = st.getResultSet();
            while (rs.next()) {

                if (rs.getInt("amount") <= 0)
                    continue;

                if (rs.getInt("id") == 0)
                    continue;

                App.shop_sell.put(rs.getInt("id"), new shop_sell_item(rs.getInt("id")));
            }
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[資料庫] 下載單一交易所物品 (" + id + ") 時發生問題: " + errors.toString());
        }
    }

    // 上傳所有交易所物品資料至資料庫
    public static void upload_shop() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            int i = 0;
            int all = App.shop_sell.size();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[資料庫] 準備上傳所有交易所物品至資料庫 ...");
            for (Map.Entry<Integer, shop_sell_item> r : App.shop_sell.entrySet()) {
                st.execute("UPDATE `shop_sell` SET `amount`=" + r.getValue().amount + " WHERE `id`='" + r.getValue().id
                        + "'");
                i++;
                util.sendActionbarMessage(ChatColor.GRAY + "將交易所資料同步至資料庫中 : " + util.getProgressBar(i, all));
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[資料庫] " + i + " 個交易所商品已經上傳至資料庫");
            util.sendActionbarMessage(ChatColor.GRAY + "將交易所同步至資料庫完成 owo!");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 上傳所有交易所物品時發生問題 : " + errors.toString());
        }
    }

    // 下載所有交易所物品資料至資料庫
    public static void download_shop() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM `shop_sell` ORDER BY `id` DESC;");
            ResultSet rs = st.getResultSet();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[資料庫] 準備從資料庫下載所有交易所物品 ...");
            int all = 0;
            if (rs != null) {
                rs.last(); // moves cursor to the last row
                all = rs.getRow(); // get row id
                rs.beforeFirst();
            }
            int i = 0;
            while (rs.next()) {
                if (rs.getInt("amount") <= 0)
                    continue;
                if (rs.getInt("id") == 0)
                    continue;
                App.shop_sell.put(rs.getInt("id"), new shop_sell_item(rs.getInt("id")));
                i++;
                util.sendActionbarMessage(ChatColor.GRAY + "從資料庫同步交易所中 : " + util.getProgressBar(i, all));
            }
            util.sendActionbarMessage(ChatColor.GRAY + "從資料庫同步交易所完成 owo!");
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.GREEN + "[資料庫] " + App.shop_sell.size() + " 個交易所物品已經順利下載自資料庫");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 下載所有交易所物品時發生問題 : " + errors.toString());
        }
    }

    // 上傳單一領地資料至資料庫
    public static void upload_region(int id) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            region r = App.regions.get(id);
            st.execute("UPDATE `regions` SET `tp_x`=" + r.tpx + ", `tp_y`=" + r.tpy + ", `tp_z`=" + r.tpz + ", `name`='"
                    + r.name + "',`lord`='" + r.lord + "' ,`lordname`='" + r.lordname + "',`permission`='"
                    + r.getnewjson() + "' WHERE `id`='" + id + "'");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[資料庫] 上傳本分流的領地 " + id + " 至資料庫時發生問題: " + errors.toString());
        }
    }

    // 下載單一領地資料至資料庫
    public static void download_region(int id) {

        App.regions.remove(id);

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM regions WHERE `id`=" + id);
            ResultSet rs = st.getResultSet();
            while (rs.next()) {
                if (App.getPlugin().getConfig().getInt("server") == rs.getInt("server")) {
                    if (!Bukkit.getWorld(rs.getString("world"))
                            .getBlockAt(rs.getInt("centerx"), rs.getInt("flagy") + 1, rs.getInt("centerz")).getType()
                            .toString().contains("BANNER")) {
                        Bukkit.getWorld(rs.getString("world"))
                                .getBlockAt(rs.getInt("centerx"), rs.getInt("flagy") + 1, rs.getInt("centerz"))
                                .setType(Material.WHITE_BANNER, false);
                    }
                }
                App.regions.put(rs.getInt("id"), new region(rs.getInt("id")));
                region r = App.regions.get(rs.getInt("id"));

                for (Hologram pe : HologramsAPI.getHolograms(App.getPlugin()))
                    if (pe.getLine(0).toString().contains("領地旗幟") && pe.getLine(0).toString().contains(r.name))
                        pe.delete();

                if (r.server == App.getPlugin().getConfig().getInt("server")) {
                    Hologram hologram = HologramsAPI.createHologram(App.getPlugin(),
                            new Location(Bukkit.getWorld(r.world), r.centerx + 0.5, r.flagy + 3.5, r.centerz + 0.5));
                    hologram.appendTextLine("[領地旗幟] " + ChatColor.GREEN + r.name);
                    if (r.lordname.contains("family:"))
                        hologram.appendTextLine(ChatColor.GRAY + "家族 " + r.lordname.split("family:")[1] + " 的家族領地");
                    else
                        hologram.appendTextLine(ChatColor.GRAY + "領主 " + r.lordname + " 的領地");
                }

            }
            conn.close();

        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[資料庫] 自資料庫同步領地 " + id + " 資料時發生問題 : " + errors.toString());
        }
    }

    // 上傳所有領地資料至資料庫
    public static void upload_region() {

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            int i = 0;
            int all = App.regions.size();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[資料庫] 準備上傳本分流所有領地資料至資料庫 ...");
            for (Map.Entry<Integer, region> r : App.regions.entrySet()) {

                if (r.getValue().server != App.config.getInt("server"))
                    continue;

                st.execute("UPDATE `regions` SET `tp_x`=" + r.getValue().tpx + ", `tp_y`=" + r.getValue().tpy
                        + ", `tp_z`=" + r.getValue().tpz + ", `name`='" + r.getValue().name + "',`lord`='"
                        + r.getValue().lord + "' ,`lordname`='" + r.getValue().lordname + "',`permission`='"
                        + r.getValue().getnewjson() + "' WHERE `id`='" + r.getValue().id + "'");
                i++;
                util.sendActionbarMessage(ChatColor.GRAY + "將本分流的領地資料同步至資料庫中 : " + util.getProgressBar(i, all));
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[資料庫] 已經將 " + i + " 個本分流的領地上傳至資料庫");
            util.sendActionbarMessage(ChatColor.GRAY + "將本分流的領地同步至資料庫完成 owo!");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 上傳所有本分流的領地至資料庫時發生問題: " + errors.toString());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    // 下載所有領地資料至資料庫
    public static void download_region() {

        App.regions.clear();

        for (Hologram pe : HologramsAPI.getHolograms(App.getPlugin()))
            if (pe.getLine(0).toString().contains("領地旗幟"))
                pe.delete();

        Connection conn = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[資料庫] 準備自資料庫下載所有領地資料 ...");
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM regions");
            rs = st.getResultSet();
            int all = 0;
            if (rs != null) {
                rs.last(); // moves cursor to the last row
                all = rs.getRow(); // get row id
                rs.beforeFirst();
            }
            int i = 0;
            while (rs.next()) {

                if (rs.getInt("id") == 0)
                    continue;

                if (App.getPlugin().getConfig().getInt("server") == rs.getInt("server")) {
                    if (!Bukkit.getWorld(rs.getString("world"))
                            .getBlockAt(rs.getInt("centerx"), rs.getInt("flagy") + 1, rs.getInt("centerz")).getType()
                            .toString().contains("BANNER")) {
                        Bukkit.getWorld(rs.getString("world"))
                                .getBlockAt(rs.getInt("centerx"), rs.getInt("flagy") + 1, rs.getInt("centerz"))
                                .setType(Material.WHITE_BANNER, false);
                    }
                }

                App.regions.put(rs.getInt("id"), new region(rs.getInt("id")));
                region r = App.regions.get(rs.getInt("id"));
                if (r.server == App.getPlugin().getConfig().getInt("server")) {
                    Hologram hologram = HologramsAPI.createHologram(App.getPlugin(),
                            new Location(Bukkit.getWorld(r.world), r.centerx + 0.5, r.flagy + 3.5, r.centerz + 0.5));
                    hologram.appendTextLine("[領地旗幟] " + ChatColor.GREEN + r.name);
                    if (r.lordname.contains("family:"))
                        hologram.appendTextLine(ChatColor.GRAY + "家族 " + r.lordname.split("family:")[1] + " 的家族領地");
                    else
                        hologram.appendTextLine(ChatColor.GRAY + "領主 " + r.lordname + " 的領地");
                }

                i++;
                util.sendActionbarMessage(ChatColor.GRAY + "自資料庫同步所有領地資料中 : " + util.getProgressBar(i, all));
            }
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.GREEN + "[資料庫] 已經自資料庫下載所有 " + App.regions.size() + " 個領地資料至分流伺服器");
            util.sendActionbarMessage(ChatColor.GRAY + "自資料庫同步所有領地資料完成 owo!");

        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 自資料庫同步所有領地資料時發生問題 : " + errors.toString());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    // 上傳單一玩家資料至資料庫
    public static void upload_player(String id) {
        App.players.get(id).upload();
    }

    // 自資料庫下載單一玩家資料
    public static void download_player(String id) {

        App.players.get(id).upload();

        new BukkitRunnable() {
            @Override
            public void run() {
                Player P = Bukkit.getPlayer(id);
                try {
                    player p = new player(P.getName());
                    App.players.put(P.getName(), p);
                    p.CheckBank();
                    p.CheckCoinShop();
                    p.CheckDonate();

                    if (p.regions.split(",").length - 1 > p.maxregions) {
                        P.sendMessage(" ");
                        P.sendMessage(ChatColor.RED + "提醒您，你的領地數量 " + (p.regions.split(",").length - 1)
                                + " 已超過您可擁有的最大領地 " + p.maxregions);
                        P.sendMessage(ChatColor.RED + "建議您盡速刪除不必要的領地，或至加值商城購買領地擴充卷。");
                    }

                    String family = "";
                    if (!p.family.equals("null") && App.families.get(p.family).compelete) {
                        family = " " + p.family;
                    }

                    if (p.nickname.equals("null")) {
                        P.setDisplayName(family + " [" + p.clacolor + p.displaycla + ChatColor.WHITE + "] " + p.id
                                + ChatColor.GREEN + " Lv. " + p.lv);
                    } else {
                        P.setDisplayName(family + " [" + p.clacolor + p.displaycla + ChatColor.WHITE + "] " + p.nickname
                                + ChatColor.GREEN + " Lv. " + p.lv);
                    }
                    P.setPlayerListName(P.getDisplayName());
                    App.players.get(P.getName()).displayname = P.getDisplayName();
                    NametagEdit.getApi().setPrefix(P, "§f[" + p.clacolor + p.displaycla + "§f] ");
                    NametagEdit.getApi().setSuffix(P, " §aLv." + p.lv);
                } catch (Exception exc) {
                    StringWriter errors = new StringWriter();
                    exc.printStackTrace(new PrintWriter(errors));
                    Bukkit.getConsoleSender()
                            .sendMessage(ChatColor.RED + "[資料庫] 自資料庫下載玩家 " + id + " 資料時發生問題 : " + errors.toString());
                }
            }
        }.runTaskLater(App.getPlugin(), 20);

    }

    // 上傳所有玩家資料至資料庫
    public static void upload_player() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            int all = Bukkit.getOnlinePlayers().size();
            int i = 0;
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[資料庫] 準備上傳所有玩家資料至資料庫 ... ");
            for (Player theplayer : Bukkit.getOnlinePlayers()) {
                player p = App.players.get(theplayer.getName());
                st.execute("UPDATE `userdata` SET `gold`=" + p.gold + ",`diamond`=" + p.diamond + ",`settings`='"
                + p.getsettingsinGson() + "',`fly`=" + p.fly + ",`particle`='" + p.particle + "',`nickname`='" + p.nickname
                + "',`coin`=" + p.coin + ",`home`='" + p.home + "', `uuid`='" + p.uuid + "', `family`='" + p.family
                + "' ,`class`='" + p.cla + "',`lv`=" + p.lv + ",`exp`=" + p.exp + ",`balance`=" + p.balance + ", `region`="
                + p.region + ",`regions`='" + p.getRegionsInString() + "', `displayname`='" + p.displayname
                + "' ,`trade`=" + p.trade + " ,`magic`=" + p.magic + " ,`fish`=" + p.fish + " ,`iron`=" + p.iron
                + " ,`sword`=" + p.sword + " ,`bow`=" + p.bow + " ,`ripe`=" + p.ripe + " ,`wood`=" + p.wood
                + ",`displaycla`='" + p.displaycla + "' WHERE `id`='" + p.id + "'");
                i++;
                util.sendActionbarMessage(ChatColor.GRAY + "將所有玩家資料同步至資料庫中 : " + util.getProgressBar(i, all));
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[資料庫] 已經將所有 " + i + " 位玩家的資料上傳至資料庫");
            util.sendActionbarMessage(ChatColor.GRAY + "將所有玩家資料同步至資料庫完成 owo!");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 在上傳所有玩家資料至資料庫時發生問題 : " + errors.toString());
        }
    }

    // 自資料庫下載所有玩家資料
    public static void download_player() {
        try {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[資料庫] 準備自資料庫下載所有玩家資料 ... ");
            int i = 0;
            int all = Bukkit.getOnlinePlayers().size();
            for (Player theplayer : Bukkit.getOnlinePlayers()) {
                String name = theplayer.getName();
                player p = new player(name);
                Player P = Bukkit.getPlayer(name);
                for (String s : p.regions.split(",")) {
                    if (!s.equals("0") && !App.regions.containsKey(Integer.parseInt(s))) {
                        Bukkit.getPlayer(p.id).sendMessage(ChatColor.RED + "你有一個領地因為某種原因而被移除。");
                        p.regions = p.regions.replace("," + s, "");
                    }
                }
                // 領地數量警示
                if (p.regions.split(",").length - 1 > p.maxregions) {
                    P.sendMessage(" ");
                    P.sendMessage(ChatColor.RED + "提醒您，你的領地數量 " + (p.regions.split(",").length - 1) + " 已超過您可擁有的最大領地 "
                            + p.maxregions);
                    P.sendMessage(ChatColor.RED + "建議您盡速刪除不必要的領地，或至加值商城購買領地擴充卷。");
                }

                App.players.put(name, p);

                // 設定 Display Name
                if (p.nickname.equals("null"))
                    P.setDisplayName(App.getChat().getPlayerPrefix(P) + p.id + App.getChat().getPlayerSuffix(P));
                else
                    P.setDisplayName(App.getChat().getPlayerPrefix(P) + p.nickname + App.getChat().getPlayerSuffix(P));
                P.setPlayerListName(P.getDisplayName());
                p.displayname = P.getDisplayName();
                NametagEdit.getApi().setPrefix(P, "§f[" + p.clacolor + p.displaycla + "§f] ");
                NametagEdit.getApi().setSuffix(P, " §aLv." + p.lv);
                i++;
                util.sendActionbarMessage(ChatColor.GRAY + "將所有玩家資料同步至資料庫中 : " + util.getProgressBar(i, all));
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[資料庫] 已經自資料庫下載所有 " + i + " 位玩家資料");
            util.sendActionbarMessage(ChatColor.GRAY + "自資料庫同步所有玩家資料完成 owo!");
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 下載所有玩家資料時發生問題 : " + errors.toString());
        }
    }

    // 上傳單一家族資料至資料庫
    public static void upload_family(String name) {
        App.families.get(name).upload();
    }

    // 自資料庫下載單一家族資料
    public static void download_family(String name) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM families WHERE `name`='" + name + "'");
            ResultSet rs = st.getResultSet();
            while (rs.next()) {
                App.families.put(rs.getString("name"), new family(rs.getString("name")));
            }
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 自資料庫下載單一家族資料時發生問題 : " + errors.toString());
        }
    }

    // 上傳所有家族資料至資料庫
    public static void upload_family() {
        try {
            int i = 0;
            int all = App.families.size();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[資料庫] 準備上傳所有家族資料至資料庫 ... ");
            for (Map.Entry<String, family> f : App.families.entrySet()) {
                f.getValue().upload();
                i++;
                util.sendActionbarMessage(ChatColor.GRAY + "將所有家族資料同步至資料庫中 : " + util.getProgressBar(i, all));
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[資料庫] 已經上傳 " + i + " 個家族資料至資料庫");
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 上傳所有家族資料至資料庫時發生問題 : " + errors.toString());
        }
    }

    // 自資料庫下載所有家族資料
    public static void download_family() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[資料庫] 準備自資料庫下載所有家族資料至分流伺服器 ...");
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM families");
            ResultSet rs = st.getResultSet();
            int all = 0;
            if (rs != null) {
                rs.last(); // moves cursor to the last row
                all = rs.getRow(); // get row id
                rs.beforeFirst();
            }
            int i = 0;
            while (rs.next()) {

                App.families.put(rs.getString("name"), new family(rs.getString("name")));
                i++;

                String loading = "";
                for (int j = 0; j < ((double) i) / ((double) all) * 20; j++) {
                    loading += ChatColor.BLUE + "=";
                }
                for (int j = 0; j < 20 - ((double) i) / ((double) all) * 20; j++) {
                    loading += ChatColor.WHITE + "=";
                }
                for (Player P : Bukkit.getOnlinePlayers()) {
                    util.sendActionbarMessage(P, ChatColor.YELLOW + "下載所有家族資料中" + ChatColor.BLUE + " ["
                            + ChatColor.WHITE + loading + ChatColor.BLUE + "] " + ChatColor.YELLOW + "伺服器將暫時卡頓");
                }
            }
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.GREEN + "[資料庫] 已經自資料庫下載 " + App.families.size() + " 個家族資料至分流伺服器");

        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 自資料庫下載所有家族資料時發生問題 : " + errors.toString());
        }
    }

}