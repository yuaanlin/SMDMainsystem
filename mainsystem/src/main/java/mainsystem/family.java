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
import com.google.gson.reflect.TypeToken;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class family {

    /** 家族名稱 */
    String name;

    /** 家族代表名稱 */
    String president;

    /** 家族等級 */
    int level;

    /** 家族經驗值 */
    int exp;

    /** 家族領地資料 (String 格式) */
    String regions = "0";

    /** 家族領地列表 */
    Map<Integer, region> region = new HashMap<>();

    /** 家族是否完成創立任務 */
    boolean compelete;

    /** 家族任務列表 */
    Map<String, mission> missions = new HashMap<>();

    /** 家族成員身分組列表 */
    Map<String, group> groups = new HashMap<>();

    /** 家族成員身分組 */
    class group {
        String name;
        List<String> members = new ArrayList<>();

        public group(String name) {
            this.name = name;
        }
    }

    /** 家族任務 */
    class mission {
        String name;
        Map<Material, Integer> require = new HashMap<>();

        public mission(String name, Map<Material, Integer> require) {
            this.name = name;
            this.require = require;
        }
    }

    /** 下載現有家族 */
    public family(String name) {

        Gson gson = new Gson();
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM `families` where `name`='" + name + "'");
            ResultSet rs = st.getResultSet();
            while (rs.next()) {

                // 載入家族名稱
                this.name = name;

                // 載入家族代表名稱
                president = rs.getString("president");

                // 載入家族成員身分組資料
                String groupsdata = rs.getString("groups");
                java.lang.reflect.Type type = new TypeToken<HashMap<String, group>>() {
                }.getType();
                groups = gson.fromJson(groupsdata, type);

                // 載入家族等級
                level = rs.getInt("level");

                // 載入家族是否完成創立任務
                if (rs.getString("compelete").equals("true"))
                    compelete = true;
                else
                    compelete = false;

                // 載入家族任務資料
                String missionsdata = rs.getString("missions");
                type = new TypeToken<HashMap<String, mission>>() {
                }.getType();
                missions = gson.fromJson(missionsdata, type);

                // 載入家族經驗值
                exp = rs.getInt("exp");

                // 載入領地資料
                regions = rs.getString("regiondata");
                region = getRegionsMap(regions);

            }
            conn.close();
        } catch (Exception exc) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[Family data] When loading family data : " + exc.getMessage());
        }

        // 家族任務完成判斷
        for (Map.Entry<String, mission> mission : missions.entrySet()) {

            // 家族創立任務下修
            if (mission.getValue().name.equals("創立家族任務") && mission.getValue().require.containsKey(Material.DIAMOND)
                    && mission.getValue().require.get(Material.DIAMOND) > 4) {
                mission.getValue().require.put(Material.OAK_LOG, 54);
                mission.getValue().require.put(Material.BIRCH_LOG, 54);
                mission.getValue().require.put(Material.JUNGLE_LOG, 54);
                mission.getValue().require.put(Material.IRON_INGOT, 12);
                mission.getValue().require.put(Material.GOLD_INGOT, 6);
                mission.getValue().require.put(Material.DIAMOND, 3);
            }

            if (mission.getValue().require.isEmpty()) {

                // 創立家族任務 - 完成後啟用家族
                if (mission.getValue().name.equals("創立家族任務")) {
                    compelete = true;
                }

                return;
            }
        }

    }

    /** 創立新家族 */
    public family(String name, List<Player> members, Player president) {

        this.name = name;
        this.president = president.getName();
        this.groups.put("一般成員", new group("一般成員"));
        this.groups.put("家族幹部", new group("家族幹部"));
        this.exp = 0;
        compelete = false;
        this.level = 1;
        this.regions = "0";
        HashMap<Material, Integer> require = new HashMap<>();
        require.put(Material.OAK_LOG, 54);
        require.put(Material.BIRCH_LOG, 54);
        require.put(Material.JUNGLE_LOG, 54);
        require.put(Material.IRON_INGOT, 12);
        require.put(Material.GOLD_INGOT, 6);
        require.put(Material.DIAMOND, 3);
        missions.put("創立家族任務", new mission("創立家族任務", require));
        for (Player P : members) {
            if (P == president)
                continue;
            this.groups.get("一般成員").members.add(P.getName());
        }
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute(
                    "INSERT INTO `families`(`name`,`president`,`regiondata`,`groups`,`missions`,`compelete`,`level`,`exp`,`regions`,`MemberNumber`,`ActiveMemberNumber`) VALUES ('"
                            + name + "','" + this.president + "','" + regions + "','" + getGroupsinGson() + "','"
                            + getmissionsinGson() + "','" + compelete + "'," + level + "," + exp + "," + getregions()
                            + "," + getmembernumber() + "," + getactivemembernumber() + ")");
            conn.close();
        } catch (Exception exc) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[Family data] When inserting family data : " + exc.getMessage());
        }

    }

    /** 解析家族領地資料字串並建立家族領地列表 */
    public HashMap<Integer, region> getRegionsMap(String data) {
        HashMap<Integer, region> region = new HashMap<>();
        for (String s : regions.split(",")) {

            // 確定無效情形 : 領地編號 0 或是未擁有領地
            if (s.equals("0") || s.length() <= 0) {
                continue;
            }

            int i = 0;

            // 將 s 試圖轉換為整數
            try {
                i = Integer.parseInt(s);
            } catch (Exception exc) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[家族資料] 解析家族 " + name + " 的領地資料時發生例外錯誤：");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[家族資料] 無法將字串 \"" + s + "\" 轉換成整數。");
                continue;
            }

            // 試圖從伺服器取得領地
            region r;
            if (App.regions.containsKey(i)) {
                r = App.regions.get(i);
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[家族資料] 解析家族 " + name + " 的領地資料時發生例外錯誤：");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[家族資料] 伺服器領地列表不包含編號 " + i + " 這個領地。");
                continue;
            }

            // 確認該領地是不是該家族的家族領地
            if (r.lordname.equals("family:" + name)) {
                region.put(i, r);
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[家族資料] 解析家族 " + name + " 的領地資料時發生例外錯誤：");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[家族資料] 伺服器領地編號 " + i + " 的領主是 " + r.lordname
                        + " ，不是 " + "family:" + name + " 。");
                continue;
            }

        }

        // 檢查家族有沒有遺失的領地 
        for (Map.Entry<Integer, region> entry : App.regions.entrySet()) {
            region r = entry.getValue();
            if (r.lordname.equals("family:" + this.name) && !region.containsKey(r.id)) {
                Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.YELLOW + "[家族資料] 家族 " + name + " 遺失的領地 " + r.name + " 已被找回。");
                region.put(r.id, r);
            }
        }
        
        return region;
    }

    /** 取得 Gson 格式的 groups (方便儲存至資料庫) */
    public String getGroupsinGson() {
        Gson gson = new Gson();
        String data = gson.toJson(groups);
        return data;
    }

    /** 取得 Gson 格式的 missions (方便儲存至資料庫) */
    public String getmissionsinGson() {
        Gson gson = new Gson();
        String data = gson.toJson(missions);
        return data;
    }

    /** 上傳該家族資料至資料庫 */
    public void upload() {

        // 建立家族領地列表 String 格式
        String regionsdata = "";
        for (Map.Entry<Integer, region> entry : region.entrySet()) {
            region r = entry.getValue();
            regionsdata += r.id + ",";
        }
        if (regionsdata.length() > 0)
            regionsdata = regionsdata.substring(0, regionsdata.length() - 1);

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Yuanlin1207!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();

            // 上傳家族資料 (依欄位)
            st.execute("UPDATE `families` SET `regiondata`='" + regionsdata + "',`groups`='" + getGroupsinGson()
                    + "',`missions`='" + getmissionsinGson() + "',`compelete`='" + compelete + "',`level`=" + level
                    + ",`exp`=" + exp + ",`regions`=" + getregions() + ",`MemberNumber`=" + getmembernumber()
                    + ",`ActiveMemberNumber`=" + getactivemembernumber() + " WHERE `name`='" + name + "'");

            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[資料庫] 上傳家族資料 " + name + " 時發生問題 : " + errors.toString());
        }
    }

    /** 取得家族領地總面積 */
    public int getregions() {
        int regions = 0;
        for (String s : this.regions.split(",")) {

            int i = 0;

            try {
                i = Integer.parseInt(s);
            } catch (Exception exc) {
                continue;
            }

            if (s.equals("0"))
                continue;

            if (!App.regions.containsKey(i))
                continue;

            regions += App.regions.get(i).size;
        }
        return regions;
    }

    /** 取得家族成員總數 */
    public int getmembernumber() {
        int num = 1;
        for (Map.Entry<String, group> g : groups.entrySet()) {
            num += g.getValue().members.size();
        }
        return num;
    }

    /** 取得活躍家族成員總數 */
    public int getactivemembernumber() {
        int num = 1;
        for (Map.Entry<String, group> g : groups.entrySet()) {
            for (String P : g.getValue().members) {
                if (App.allplayers.containsKey(P)) {
                    num++;
                } else {
                    if (util.DateDiff(util.getplayerlastlogin(P), util.now()) < 7)
                        num++;
                }
            }
        }
        return num;
    }

    /** 新增家族任務 */
    public void addMission(String name, Map<Material, Integer> require) {
        mission mission = new mission(name, require);
        missions.put(name, mission);
    }

    /** 新增家族任務 */
    public void addMission(mission mission) {
        missions.put(mission.name, mission);
    }

}