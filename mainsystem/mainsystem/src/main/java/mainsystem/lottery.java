package mainsystem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class lottery {

    public static void printInfo(Player P) {

        for (int j = 0; j < 20; j++) {
            P.sendMessage(" ");
        }

        P.sendMessage(" |" + ChatColor.YELLOW + " < SMD 大樂透 >");
        P.sendMessage(" |");
        P.sendMessage(" | " + ChatColor.GRAY + "玩法說明 : ");
        P.sendMessage(" | " + ChatColor.GRAY + "每個整點時依照投注金額計算機率抽出贏家贏得所有獎金！");
        P.sendMessage(" | " + ChatColor.GRAY + "最低開獎金額為 50000 元， 未湊得則往下一個整點延後。");
        P.sendMessage(" |");
        P.sendMessage(" | 累計投注金額 : " + ChatColor.YELLOW + util.round(gettotal(), 0) + " 元");
        P.sendMessage(" | 您的投注金額 : " + ChatColor.YELLOW + util.round(getamout(P) * 100, 0) + " 元");
        P.sendMessage(" | 您的中獎機率 : " + ChatColor.YELLOW + util.round(getchance(P) * 100, 0) + " %");
        P.sendMessage(" |");
        P.spigot()
                .sendMessage(App.textcomp(" | " + ChatColor.UNDERLINE + "[點我加注]", "點擊進行加注", "/mainsystem lottery buy"));
        P.sendMessage(" ");

    }

    public static void addlottery(Player P, double amount) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("INSERT INTO lotterypool (`player`,`amount`,`time`) VALUES ('" + P.getName() + "'," + amount
                    + ",'" + util.now() + "')");
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 新增樂透資料至資料庫時發生問題 : " + errors.toString());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

        player p = App.players.get(P.getName());
        // 發送投注訊息
        App.chatchannels.get("全伺服頻道").sendmessage(" ");
        App.chatchannels.get("全伺服頻道")
                .sendmessage(ChatColor.GOLD + " " + ChatColor.UNDERLINE + "加注!" + ChatColor.WHITE + " 玩家 "
                        + p.getShortDisplayName() + ChatColor.WHITE + " 加注 " + amount * 100 + " 元到樂透獎池! "
                        + ChatColor.WHITE + "累積獎金 " + util.round(gettotal(), 0) + " 元啦! ");

        // 發送投注訊息至其他分流
        App.PMH.sendAllServerBrocast(" ");
        App.PMH.sendAllServerBrocast(ChatColor.GOLD + " " + ChatColor.UNDERLINE + "加注!" + ChatColor.WHITE + " 玩家 "
                + p.getShortDisplayName() + ChatColor.WHITE + " 加注 " + amount * 100 + " 元到樂透獎池! " + ChatColor.WHITE
                + "累積獎金 " + util.round(gettotal(), 0) + " 元啦! ");
    }

    /** 開獎程序 */
    public static void getLotteryResult() {

        int i = 0;

        Double d = gettotal() / 100;

        // 記錄總張數
        int ticketAmt = d.intValue();
        int all = ticketAmt;

        // 發送計算通知
        util.sendActionbarMessage(ChatColor.GRAY + "大樂透開獎計算中 : " + util.getProgressBar(i, all));

        // 建立一個 Map<玩家, 張數> 整理每個玩家買的張數
        Map<String, Integer> tickets = new HashMap<>();

        Connection conn = null;
        ResultSet rs = null;

        Stack<String> stack = new Stack<>();

        // 整理每個玩家買的張數
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT player, SUM(amount) FROM lotterypool GROUP BY player;");
            rs = st.getResultSet();
            while (rs.next()) {
                tickets.put(rs.getString("player"), rs.getInt("SUM(amount)"));
            }
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 自票池整理每個玩家的張數時發生問題 : " + errors.toString());
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

        // 依照隨機順序將票放入 stack 直到 ticketAmt 等於 0
        int playeramt = tickets.size();
        while (ticketAmt > 0) {

            // 隨機值 random : 從 tickets 抽出一名有買彩票的玩家
            int random = (int) (Math.random() * playeramt);
            for (Map.Entry<String, Integer> entry : tickets.entrySet()) {
                if (random == 0) {
                    if (entry.getValue() == 0) // 若該玩家已經沒票了，往下一個找直到找到有彩票的玩家
                        continue;

                    stack.push(entry.getKey());
                    ticketAmt--;
                    i++;
                    tickets.put(entry.getKey(), entry.getValue() - 1);
                    break;

                } else
                    random--;
            }

            // 發送計算通知
            util.sendActionbarMessage(ChatColor.GRAY + "大樂透開獎計算中 : " + util.getProgressBar(i, all));
        }

        // 隨機值 random : 從 stack 抽出要 pop 幾層
        int random = (int) (Math.random() * stack.size());
        while (random > 0) {
            stack.pop();
            random--;
        }

        // stack 最上層為贏家
        String winner = stack.peek();

        // 發送得獎訊息
        App.chatchannels.get("全伺服頻道").sendmessage(" ");
        App.chatchannels.get("全伺服頻道").sendmessage(ChatColor.GOLD + " " + ChatColor.UNDERLINE + "開獎啦!!" + ChatColor.WHITE
                + " 恭喜玩家 " + winner + " 贏得本期大樂透獎金 " + util.round(gettotal(), 0) + " 元 OuOb!");

        // 發送得獎訊息至其他分流
        App.PMH.sendAllServerBrocast(" ");
        App.PMH.sendAllServerBrocast(ChatColor.GOLD + " " + ChatColor.UNDERLINE + "開獎啦!!" + ChatColor.WHITE + "恭喜玩家 "
                + winner + " 贏得本期大樂透獎金 " + util.round(gettotal(), 0) + " 元 OuOb!");

        // 發送獎勵
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=bankmanager&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute(
                    "INSERT INTO `bank` ( `from`, `to`,`amount`,`receive`,`bal`,`paytime`,`receivetime`,`description` ) VALUES ('樂透', '"
                            + winner + "','" + util.round(gettotal(), 0) + "','0','-1','" + util.now()
                            + "','not yet','樂透系統中獎獎金');");
            conn.close();

            // 匯款一秒後重新整理收款玩家資料
            final String winnerf = winner;
            new BukkitRunnable() {
                public void run() {
                    App.ReloadPlayerData(winnerf);
                }
            }.runTaskLater(App.getPlugin(), 20);

        } catch (Exception exc) {

        }

        // 刪除獎池
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("DELETE FROM `lotterypool` WHERE 1;");
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 清空樂透獎池時發生問題 : " + errors.toString());
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

    /** 取得累計獎金 */
    public static double gettotal() {
        double total = 0;
        Connection conn = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT SUM(amount) FROM lotterypool;");
            rs = st.getResultSet();
            while (rs.next()) {
                total = rs.getDouble("SUM(amount)");
            }
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 自資料庫取得樂透資料時發生問題 : " + errors.toString());
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
        return total * 100;
    }

    /** 取得玩家購買的彩票張數 */
    public static double getamout(Player P) {
        double amount = 0;
        Connection conn = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT SUM(amount) FROM lotterypool WHERE `player`='" + P.getName() + "';");
            rs = st.getResultSet();
            while (rs.next()) {
                amount = rs.getDouble("SUM(amount)");
            }
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 自資料庫取得樂透資料時發生問題 : " + errors.toString());
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
        return amount;
    }

    /** 計算機率 */
    public static double getchance(Player P) {
        double chance = 0;
        Connection conn = null;
        ResultSet rs = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT SUM(amount) FROM lotterypool WHERE `player`='" + P.getName() + "';");
            rs = st.getResultSet();
            while (rs.next()) {
                chance = rs.getDouble("SUM(amount)") * 100 / gettotal();
            }
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 自資料庫取得樂透資料時發生問題 : " + errors.toString());
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
        return chance;
    }

}