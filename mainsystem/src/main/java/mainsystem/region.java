package mainsystem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

public class region {

    static class group {
        String name;
        Map<String, Integer> permission = new HashMap<>();
        List<String> player = new ArrayList<>();

        public group(String na) {
            name = na;
            permission.put("enter", 1);
            permission.put("break", 0);
            permission.put("chest", 0);
            permission.put("place", 0);
            permission.put("tp", 1);
            permission.put("fly", 0);
        }
    }

    public class groups {
        List<group> groups = new ArrayList<>();
    }

    groups groups = new groups();

    String name;
    String lord;
    int id;
    int centerx;
    int centerz;
    int width;
    int size;
    String lordname;
    int maxx;
    int minx;
    int maxz;
    int minz;
    int tpx;
    int tpy;
    int server;
    int tpz;
    Location tp;
    Location flag;
    String permission;
    int num_groups;

    String world;
    int flagy;

    public region(int id_) {
        id = id_;
        Connection conn = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM regions where `id`='" + id + "'");
            ResultSet rs = st.getResultSet();
            while (rs.next()) {
                name = rs.getString("name");
                lord = rs.getString("lord");
                lordname = rs.getString("lordname");
                centerx = rs.getInt("centerx");
                centerz = rs.getInt("centerz");
                width = rs.getInt("width");
                size = rs.getInt("size");
                maxx = rs.getInt("maxx");
                minx = rs.getInt("minx");
                maxz = rs.getInt("maxz");
                minz = rs.getInt("minz");
                tpx = rs.getInt("tp_x");
                tpy = rs.getInt("tp_y");
                tpz = rs.getInt("tp_z");
                server = rs.getInt("server");
                world = rs.getString("world");
                flagy = rs.getInt("flagy");
                permission = rs.getString("permission");
            }
            conn.close();
        } catch (Exception exc) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "" + exc.getStackTrace());
        }

        tp = new Location(Bukkit.getWorld(world), tpx, tpy, tpz);

        Gson gson = new Gson();
        String employ_json = permission;
        groups = gson.fromJson(employ_json, groups.getClass());
        for (group g : groups.groups) {
            if (!g.permission.containsKey("place")) {
                g.permission.put("place", 0);
            }
            if (!g.permission.containsKey("tp")) {
                g.permission.put("tp", 0);
            }
            if (!g.permission.containsKey("fly")) {
                g.permission.put("fly", 0);
            }
        }
    }

    public String getnewjson() {
        Gson gson = new Gson();
        permission = gson.toJson(groups);
        return permission;
    }

    /** 印出該領地資訊 */
    public void showRegionInfo(Player P) {

        player p = App.players.get(P.getName());

        for (int j = 0; j < 20; j++) {
            P.sendMessage(" ");
        }
        P.sendMessage(ChatColor.WHITE + "| 這是領地 " + ChatColor.GREEN + name + ChatColor.WHITE + " 的領地旗幟 : \n");
        P.sendMessage(ChatColor.WHITE + "| 該領地是隸屬於 " + ChatColor.GREEN + lordname.replace("family:", "家族 ") + ChatColor.WHITE + " 的領地 ");

        P.sendMessage("| \n| 該領地宣告的身分組如下: ");

        int i = 0;
        for (group g : groups.groups) {
            TextComponent gr = new TextComponent(
                    ChatColor.GREEN + "| -- 身分組 " + i + " " + g.name + " ");
            gr.toPlainText();
            TextComponent edit = App.textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[點擊編輯權限]",
                    "點擊編輯身分組 " + g.name + " 的權限", "/region " + id + " groups set " + i);

            if (hasPermission(p, "manage"))
                gr.addExtra(edit);

            P.spigot().sendMessage(gr);
            i++;
        }

        if (hasPermission(p, "manage")) {
            TextComponent gr = new TextComponent(ChatColor.WHITE + "| \n| 或是你也可以點擊 ");
            gr.toPlainText();
            gr.addExtra(
                    App.textcomp(ChatColor.UNDERLINE + "這裡", "點擊增加身分組", "/region " + id + " groups add"));
            gr.addExtra(ChatColor.RESET + " 來增加新的身分組");
            P.spigot().sendMessage(gr);
        }

        if (hasPermission(p, "manage") && p.family != null && !lordname.contains("family:")) {
            TextComponent gr = new TextComponent(ChatColor.WHITE + "| \n| 點擊 ");
            gr.toPlainText();
            gr.addExtra(
                    App.textcomp(ChatColor.UNDERLINE + "這裡", "點擊將領地給予家族", "/region " + id + " tofamily"));
            gr.addExtra(ChatColor.RESET + " 將此領地歸入家族所有");
            P.spigot().sendMessage(gr);
        }
    }
    
    /** 上傳該領地資料至資料庫 */
    public void upload() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("UPDATE `regions` SET `tp_x`=" + tpx + ", `tp_y`=" + tpy + ",`tp_z`=" + tpz + ", `name`='" + name
                    + "',`lord`='" + lord + "' ,`lordname`='" + lordname + "',`permission`='" + permission
                    + "' WHERE `id`='" + id + "'");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.RED + "[資料庫] 單獨同步領地 " + name + "( " + id + " ) 時發生問題 : " + errors.toString());
        }
    }

    /** 判斷玩家是否擁有領地的某項權限 */
    public boolean hasPermission(player p, String permission) {

        // 伺服器管理者 擁有所有權限 (傳送權除外)
        if (p.id.equals("ken20001207") && !permission.equals("tp")) {
            return true;
        }

        // 私人領地擁有者 擁有所有權限
        if (p.regionlist.containsKey(id))
            return true;

        // 家族領地的家族代表 擁有所有權限
        if (lordname.contains("family:") && lordname.split(":")[1].equals(p.family) && p.cla.contains("家族代表"))
            return true;

        // 領地管理權限
        if (permission.equals("manage")) {
            // 家族領地，家族幹部有管理權限
            if (lordname.contains("family:") && lordname.split(":")[1].equals(p.family) && p.cla.contains("家族幹部"))
                return true;
            return false;
        }

        // 其他權限
        for (group g : groups.groups) {
            // 玩家在身分組內，根據身分組權限設定
            if (g.player.contains(p.id)) {
                if (g.permission.containsKey(permission) && g.permission.get(permission) == 1)
                    return true;
                else
                    return false;
            }
            // 玩家是家族成員，根據家族成員身分組權限設定
            if(g.name.equals("家族成員") && p.family.equals(lordname.replace("family:",""))){
                if (g.permission.containsKey(permission) && g.permission.get(permission) == 1)
                    return true;
                else
                    return false;
            }
        }
        // 玩家不在身分組內，檢查預設身分組權限
        if (groups.groups.get(0).permission.containsKey(permission)
                && groups.groups.get(0).permission.get(permission) == 1)
            return true;
        else
            return false;
    }
}