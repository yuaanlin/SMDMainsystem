package shop;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import mainsystem.App;

public class classes {

    /** 交易所商品 */
    public static class shop_sell_item {
        public int id;
        public String owner;
        public int amount;
        public float price;
        public String itemdata_base64;
        public ItemStack item;
        public String time;

        public shop_sell_item(int id) {
            this.id = id;
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String datasource = "jdbc:mysql://localhost/mcserver?user=shopdownloader&password=Ken3228009!&useSSL=false";
                conn = DriverManager.getConnection(datasource);
                Statement st = conn.createStatement();
                st.execute("SELECT * FROM shop_sell where `id`='" + id + "'");
                ResultSet rs = st.getResultSet();
                while (rs.next()) {
                    owner = rs.getString("owner");
                    amount = rs.getInt("amount");
                    itemdata_base64 = rs.getString("data");
                    price = rs.getFloat("price");
                    time = rs.getString("time");
                }
                conn.close();
            } catch (Exception exc) {
                StringWriter errors = new StringWriter();
                exc.printStackTrace(new PrintWriter(errors));
                Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.RED + "[資料庫] 自資料庫下載商品 " + id + " 至分流建立資料時發生問題: " + errors.toString());
            }

            try {
                item = App.itemStackFromBase64(itemdata_base64);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to save item stacks.", e);
            }
        }

        // 透過資料庫取得數量，避免分流不同步而重複賣出
        public int getamount() {
            Connection conn = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String datasource = "jdbc:mysql://localhost/mcserver?user=shopdownloader&password=Ken3228009!&useSSL=false";
                conn = DriverManager.getConnection(datasource);
                Statement st = conn.createStatement();
                st.execute("SELECT * FROM shop_sell where `id`='" + id + "'");
                ResultSet rs = st.getResultSet();
                while (rs.next()) {
                    owner = rs.getString("owner");
                    amount = rs.getInt("amount");
                    itemdata_base64 = rs.getString("data");
                    price = rs.getFloat("price");
                    time = rs.getString("time");
                }
                conn.close();
            } catch (Exception exc) {
                StringWriter errors = new StringWriter();
                exc.printStackTrace(new PrintWriter(errors));
                Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.RED + "[資料庫] 自資料庫更新商品 " + id + " 的資料時發生問題: " + errors.toString());
            }
            return amount;
        }

    }

}