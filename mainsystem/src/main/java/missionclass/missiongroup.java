package missionclass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class missiongroup {

    List<String> players = new ArrayList<>();
    String starttime;
    int id;

    public missiongroup(int id) {
        this.id = id;
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=regionuploader&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM `mission` WHERE `endtime`='進行中' AND `id`=" + id);
            ResultSet rs = st.getResultSet();
            while (rs.next()) {
                starttime = rs.getString("starttime");
                for (String s : rs.getString("players").split(",")) {
                    players.add(s);
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
    }

}