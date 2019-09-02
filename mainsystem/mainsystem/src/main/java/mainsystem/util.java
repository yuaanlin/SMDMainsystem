package mainsystem;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class util implements Listener {

    // 整理箱子
    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e) {

        if (!(e.getView().getTitle().contains("Box") || e.getView().getTitle().contains("Chest")))
            return;

        if (e.getInventory().getType() == InventoryType.CHEST || e.getInventory().getType() == InventoryType.ENDER_CHEST
                || e.getInventory().getType() == InventoryType.SHULKER_BOX) {
            Player P = (Player) e.getPlayer();
            ItemStack[] array = e.getInventory().getContents();
            List<ItemStack> list = Arrays.asList(array);
            itemcompartor cmp = new itemcompartor();
            list.sort(cmp);
            ItemStack[] newarray = list.toArray(new ItemStack[0]);
            e.getInventory().setContents(newarray);
            P.updateInventory();
        }
    }

    // 取得進度條
    public static String getProgressBar(int now, int all) {
        String ProgressBar = "";
        for (int j = 0; j < ((double) now) / ((double) all) * 20; j++)
            ProgressBar += ChatColor.BLUE + "=";
        for (int j = 0; j < 20 - ((double) now) / ((double) all) * 20; j++)
            ProgressBar += ChatColor.WHITE + "=";
        return ProgressBar;
    }

    // 發送 Action Bar Message 給某個玩家
    public static void sendActionbarMessage(Player p, String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
    }

    // 發送 Action Bar Message 給全體玩家
    public static void sendActionbarMessage(String msg) {
        for (Player p : Bukkit.getOnlinePlayers())
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
    }

    // 將 item name 轉為中文譯名
    public static String getNameInChinese(String typename) {
        String newname = "";
        for (String s : typename.split("_")) {
            newname += s.substring(0, 1).toUpperCase();
            newname += s.substring(1).toLowerCase();
            newname += "_";
        }
        newname = newname.substring(0, newname.length() - 1);
        if (App.config.contains("chinese." + newname)) {
            newname = App.config.getString("chinese." + newname);
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[輔助功能] 取得物品 " + newname + " 的中文名稱時發生錯誤。");
        }
        return newname;
    }

    // 小數點處理
    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    // 伺服器重新啟動
    public static void RestartServer() {

        // 發送記錄到 Discord 監控頻道
        EmbedBuilder MEbuilder = new EmbedBuilder();
        MEbuilder.setColor(Color.ORANGE);
        MEbuilder.setTitle("分流 " + App.servername + " 重啟");
        MEbuilder.appendDescription("分流 " + App.servername + " 正在重啟，即將發送警告至所有玩家並上傳玩家資料。");
        App.systembot.sendtoSystemChannel(MEbuilder.build());

        // 發送警告消息給分流玩家
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[主系統] 分流伺服器即將重啟，發送訊息至所有分流玩家並上傳玩家資料中 ...");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(ChatColor.GOLD + "分流定時重啟", "請移動至傳送小鎮稍後，以確保玩家資料完整", 20, 450, 20);
            App.players.get(p.getName()).upload();
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[主系統] 發送訊息至所有分流玩家且上傳玩家資料成功!");

        // 發送記錄到 Discord 監控頻道
        MEbuilder = new EmbedBuilder();
        MEbuilder.setColor(Color.GREEN);
        MEbuilder.setTitle("分流 " + App.servername + " 重啟");
        MEbuilder.appendDescription("分流 " + App.servername + " 正在重啟，發送警告訊息並上傳所有玩家資料完成。");
        App.systembot.sendtoSystemChannel(MEbuilder.build());

        // 發送記錄到 Discord 監控頻道
        MEbuilder = new EmbedBuilder();
        MEbuilder.setColor(Color.ORANGE);
        MEbuilder.setTitle("分流 " + App.servername + " 重啟");
        MEbuilder.appendDescription("分流 " + App.servername + " 正在重啟，即將開啟白名單。");
        App.systembot.sendtoSystemChannel(MEbuilder.build());

        // 開啟白名單限制玩家進入
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[主系統] 分流伺服器即將定時重啟，開啟白名單限制玩家進入 ... ");
        Bukkit.getServer().setWhitelist(true);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[主系統] 開啟白名單成功! ");

        // 發送記錄到 Discord 監控頻道
        MEbuilder = new EmbedBuilder();
        MEbuilder.setColor(Color.GREEN);
        MEbuilder.setTitle("分流 " + App.servername + " 重啟");
        MEbuilder.appendDescription("分流 " + App.servername + " 正在重啟，分流伺服器已開啟白名單。");
        App.systembot.sendtoSystemChannel(MEbuilder.build());

        // 22 秒後強制傳送剩餘玩家
        new BukkitRunnable() {
            @Override
            public void run() {

                // 發送記錄到 Discord 監控頻道
                EmbedBuilder MEbuilder = new EmbedBuilder();
                MEbuilder.setColor(Color.ORANGE);
                MEbuilder.setTitle("分流 " + App.servername + " 重啟");
                MEbuilder.appendDescription("分流 " + App.servername + " 正在重啟，即將強制傳送所有玩家至傳送小鎮。");
                App.systembot.sendtoSystemChannel(MEbuilder.build());

                // 強制傳送
                Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[主系統] 分流伺服器即將定時重啟，強制傳送所有玩家至傳送小鎮 ...");
                App.moveallplayertoserver("server_lobby");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[主系統] 分流伺服器即將定時重啟，強制傳送所有玩家至傳送小鎮成功!");

                // 發送記錄到 Discord 監控頻道
                MEbuilder = new EmbedBuilder();
                MEbuilder.setColor(Color.GREEN);
                MEbuilder.setTitle("分流 " + App.servername + " 重啟");
                MEbuilder.appendDescription("分流 " + App.servername + " 正在重啟，傳送所有玩家至傳送小鎮完成。");
                App.systembot.sendtoSystemChannel(MEbuilder.build());
            }
        }.runTaskLater(App.getPlugin(), 440);

        // 27 秒後重啟伺服器
        new BukkitRunnable() {
            @Override
            public void run() {

                // 發送記錄到 Discord 監控頻道
                EmbedBuilder MEbuilder = new EmbedBuilder();
                MEbuilder.setTitle("分流 " + App.servername + " 重啟");
                MEbuilder.setColor(Color.ORANGE);
                MEbuilder.appendDescription("分流 " + App.servername + " 正在重啟，即將關閉分流伺服器。");
                App.systembot.sendtoSystemChannel(MEbuilder.build());

                // 伺服器關閉
                Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[主系統] 分流伺服器關機中 ...");
                Bukkit.getServer().shutdown();
            }
        }.runTaskLater(App.getPlugin(), 540);
    }

    /** 取得玩家最後登入時間 */
    public static String getplayerlastlogin(String id) {

        // 預設值是剛開服的日期，若無法取得就使用該日期
        String lastlogin = "2019-06-22 18:32:35";

        // 如果玩家在線上，回傳當前時間
        if (App.allplayers.containsKey(id)) {
            return now();

            // 如果玩家在其他分流或不在線，只能得到最後一次上傳的資料
        } else {
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String datasource = "jdbc:mysql://localhost/mcserver?user=userdatadownloader&password=Ken3228009!&useSSL=false";
                conn = DriverManager.getConnection(datasource);
                Statement st = conn.createStatement();
                st.execute("SELECT * FROM userdata where `id`='" + id + "'");
                ResultSet rs = st.getResultSet();
                rs = st.getResultSet();
                while (rs.next()) {
                    lastlogin = rs.getString("lastlogindate");
                }
                conn.close();
            } catch (Exception exc) {

            }

            // 避免 lastlogin = ""
            if (lastlogin.length() > 0)
                return lastlogin;
            else
                return "2019-06-22 18:32:35";
        }

    }

    /** 不雅字詞判斷 */
    public static boolean hasDirtyWords(String str) {
        // 不雅字詞屏蔽
        if (str.contains("幹") && !str.contains("幹嘛") && !str.contains("幹啥") && !str.contains("幹部")
                && !str.contains("幹什") && !str.contains("幹甚"))
            return true;
        if (str.contains("耖") || str.contains("機掰") || str.contains("雞掰") || str.contains("雞八") || str.contains("雞巴"))
            return true;
        if (str.contains("媽的") || str.contains("馬的") || str.contains("去你的") || str.contains("fuck")
                || str.contains("bitch"))
            return true;
        if (str.contains("靠杯") || str.contains("靠北") || str.contains("靠邀") || str.contains("靠腰") || str.contains("靠 "))
            return true;
        if (str.contains("三小") && !str.contains("三小時"))
            return true;
        if (str.contains("殺小") || str.contains("沙小") || str.contains("啥小") || str.contains("洨"))
            return true;
        if (str.contains("破你娘") || str.contains("你娘") || str.contains("您娘") || str.contains("屌"))
            return true;
        return false;
    }

    /** 要求玩家回報例外錯誤 */
    public static void pleaseReport(Player P) {
        P.sendMessage(
                ChatColor.RED + " " + ChatColor.UNDERLINE + "出錯啦!" + ChatColor.WHITE + " 看到此訊息代表您剛剛找到一個系統漏洞造成的例外錯誤。");
        P.sendMessage(ChatColor.WHITE + " 請聯絡開服者並詳細敘述 \"當你做甚麼事情會看到這則訊息\"");
        P.sendMessage(ChatColor.WHITE + " 若您順利協助我們完成漏洞修復，我們將依嚴重性發送商城幣獎勵給您。");
    }

    /** 透過銀行進行跨分流轉帳 (或做金流紀錄) */
    public static void bankTransfer(String from, String to, double amt, String description, boolean justrecord) {

        int receive = 0;
        String receivetime = "not yet";
        double bal = -1;
        if (justrecord) { // 純紀錄的話不用等對方接收款項
            receive = 1; 
            receivetime = now();
            if (App.players.containsKey(to)) {
                bal = App.players.get(to).balance;
            }
        }

        try {
            Connection conn = null;
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=bankmanager&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute(
                    "INSERT INTO `bank` ( `from`, `to`,`amount`,`receive`,`bal`,`paytime`,`receivetime`,`description` ) VALUES ('"
                            + from + "', '" + to + "','" + amt + "','" + receive + "','" + bal + "','" + now() + "','" + receivetime
                            + "','" + description + "');");
            conn.close();
        } catch (Exception e) {

        }
    }

    /** 取得當前日期時間 */
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    /** 計算日期差距 */
    public static int DateDiff(String dateA, String dateB) {

        // 格式範例 2019-07-22 18:32:35

        // 格式處理
        int yearA = Integer.parseInt(dateA.split("-")[0]);
        int yearB = Integer.parseInt(dateB.split("-")[0]);
        int monthA = Integer.parseInt(dateA.split("-")[1]);
        int monthB = Integer.parseInt(dateB.split("-")[1]);
        int dayA = Integer.parseInt(dateA.split("-")[2].split(" ")[0]);
        int dayB = Integer.parseInt(dateB.split("-")[2].split(" ")[0]);

        // 分別取年月日的差值
        int yeardiff = yearA - yearB;
        int monthdiff = monthA - monthB;
        int daydiff = dayA - dayB;

        // 統一換算成日後加減
        int datediff = (yeardiff * 365) + (monthdiff * 30) + daydiff;

        // 取絕對值
        if (datediff < 0)
            datediff = -datediff;

        return datediff;
    }

}