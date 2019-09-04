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
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class player {

    public Player P;

    public Map<Integer, region> regionlist = new HashMap<>();

    public String id = "error!";
    public String cla = "error!";
    public String uuid = "error!";
    public int lv = -1;
    public int exp = -1;
    public ChatColor clacolor = ChatColor.DARK_GRAY;
    public List<String> tprequest = new ArrayList<>();
    public float balance = 0.1f;
    public int region = 0;
    public String regions = "";
    public String displayname;
    public int inregion = -1;
    public String displaycla = "";

    public boolean quiet = false;

    public String party = "";
    public String partyrequest = "";

    public String inventory;

    public boolean wanttoresetclass = false;
    public boolean wanttoentermission = false;

    public int maxregions;

    public int iron;
    public int sword;
    public int bow;
    public int ripe;
    public int trade;
    public int magic;
    public int fish;
    public int wood;

    public int diamond;
    public int gold;

    public int iron_master;
    public int sword_master;
    public int bow_master;
    public int ripe_master;
    public int trade_master;
    public int magic_master;
    public int fish_master;
    public int wood_master;

    public String lastlogin;

    public int baha;

    public String family;

    public int coin;
    public String nickname;
    public String particle;
    public int fly;

    public String home;

    public int putted_EMERALD = -1;
    public boolean removeingregion = false;

    public int afk = 0;
    public boolean hasAFKflag = false;

    // 一些結構單純的值 (例如 pvp 是否開啟) 用 Hash Map 統一儲存
    public Map<String, String> settings = new HashMap<>();

    // 用於自動儲存玩家資料的參數
    public Random rand = new Random();
    public int AutoSaveDelay = rand.nextInt(6000); // 讓每個玩家的自動儲存時間分散
    public int logintime = 0;

    public boolean ChatingWithNPC = false;
    public String strigns = "";

    public boolean firstlogin = false;

    public player(String p_id) {

        id = p_id;

        P = Bukkit.getPlayer(id);

        uuid = P.getUniqueId().toString();

        Gson gson = new Gson();
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=userdatadownloader&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("SELECT * FROM userdata where `id`='" + id + "'");
            ResultSet rs = st.getResultSet();

            // 查詢不到該 ID
            if (!(rs.next())) {

                // 用 UUID 查詢是否是更換遊戲 ID
                Statement st_checkuuid = conn.createStatement();
                st_checkuuid.execute("SELECT * FROM userdata where `uuid`='" + uuid + "'");
                ResultSet rs_checkuuid = st_checkuuid.getResultSet();

                if (rs_checkuuid.next()) { // 該 uuid 有註冊過，開始資料轉移程序

                    Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[玩家資料] 玩家 " + id + " 第一次登入，但他的 uuid已經註冊過。");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[玩家資料] 開始資料移轉程序。");

                    Statement st_movedata = conn.createStatement();
                    st_movedata.execute(
                            "insert into userdata (settings,lastlogin,lastlogindate,family,particle,nickname,uuid, id, class, lv, exp, balance, region, regions,displaycla) select settings,lastlogin,lastlogindate,'null',particle,nickname,uuid, '"
                                    + id
                                    + "', class, lv, exp, balance, region, 0, displaycla from userdata where uuid = '"
                                    + uuid + "' ");

                    Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[玩家資料] 資料複製完成。");

                    P.kickPlayer(ChatColor.GOLD + "系統偵測到你的 ID 變更，所以要請您重新登入。");
                    return;

                }

                // 創建新玩家帳號
                settings.put("chatcolor", "7");
                settings.put("pvp", "false");
                settings.put("mission", "0");
                String settingsdata = getsettingsinGson();
                firstlogin = true;
                st.execute(
                        "INSERT INTO `userdata`(`gold`,`diamond`,`settings`,`lastlogin`,`lastlogindate`,`family`,`particle`,`nickname`,`uuid`, `id`, `class`, `lv`, `exp`,  `balance`, `region`, `regions`,`displaycla`) VALUES (0,0,'"
                                + settingsdata + "','" + util.now() + "','" + util.now() + "','null','null','null','"
                                + Bukkit.getPlayer(id).getUniqueId().toString() + "','" + id
                                + "','無業者',1,0,0,0,'0','無業者')");
            }

            st.execute("SELECT * FROM userdata where `id`='" + id + "'");
            rs = st.getResultSet();
            while (rs.next()) {

                lastlogin = rs.getString("lastlogindate");

                family = rs.getString("family");
                if (!App.families.containsKey(family)) {
                    family = "null";
                }

                String settingsdata = rs.getString("settings");
                if (settingsdata == null || settingsdata.length() < 1) {
                    settings.put("pvp", "false");
                    settings.put("chatcolor", "7");
                    settings.put("mission", "0");
                } else {
                    java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                    }.getType();
                    settings = gson.fromJson(settingsdata, type);
                }

                sword_master = rs.getInt("sword_master");
                magic_master = rs.getInt("magic_master");
                ripe_master = rs.getInt("ripe_master");
                bow_master = rs.getInt("bow_master");
                iron_master = rs.getInt("iron_master");
                fish_master = rs.getInt("fish_master");
                trade_master = rs.getInt("trade_master");
                wood_master = rs.getInt("wood_master");

                cla = rs.getString("class");
                displaycla = rs.getString("displaycla");

                if (sword_master == 1 && !cla.contains("舞劍宗師")) {
                    cla += ",舞劍宗師";
                    displaycla = "舞劍宗師";
                    Bukkit.getPlayer(id).sendTitle(ChatColor.GOLD + "恭喜你!", "你獲得了一個新的宗師稱號", 20, 200, 20);
                } else if (sword_master == 0 && cla.contains("舞劍宗師")) {
                    cla = cla.replace(",舞劍宗師", "");
                    displaycla = cla.split(",")[0];
                    Bukkit.getPlayer(id).sendMessage("你有一個宗師稱號被奪走了，請加油!");
                }

                if (magic_master == 1 && !cla.contains("加工宗師")) {
                    cla += ",加工宗師";
                    displaycla = "加工宗師";
                    Bukkit.getPlayer(id).sendTitle(ChatColor.GOLD + "恭喜你!", "你獲得了一個新的宗師稱號", 20, 200, 20);
                } else if (magic_master == 0 && cla.contains("加工宗師")) {
                    cla = cla.replace(",加工宗師", "");
                    displaycla = cla.split(",")[0];
                    Bukkit.getPlayer(id).sendMessage("你有一個宗師稱號被奪走了，請加油!");
                }

                if (ripe_master == 1 && !cla.contains("種植宗師")) {
                    cla += ",種植宗師";
                    displaycla = "種植宗師";
                    Bukkit.getPlayer(id).sendTitle(ChatColor.GOLD + "恭喜你!", "你獲得了一個新的宗師稱號", 20, 200, 20);
                } else if (ripe_master == 0 && cla.contains("種植宗師")) {
                    cla = cla.replace(",種植宗師", "");
                    displaycla = cla.split(",")[0];
                    Bukkit.getPlayer(id).sendMessage("你有一個宗師稱號被奪走了，請加油!");
                }

                if (bow_master == 1 && !cla.contains("狙擊宗師")) {
                    cla += ",狙擊宗師";
                    displaycla = "狙擊宗師";
                    Bukkit.getPlayer(id).sendTitle(ChatColor.GOLD + "恭喜你!", "你獲得了一個新的宗師稱號", 20, 200, 20);
                } else if (bow_master == 0 && cla.contains("狙擊宗師")) {
                    cla = cla.replace(",狙擊宗師", "");
                    displaycla = cla.split(",")[0];
                    Bukkit.getPlayer(id).sendMessage("你有一個宗師稱號被奪走了，請加油!");
                }

                if (iron_master == 1 && !cla.contains("採礦宗師")) {
                    cla += ",採礦宗師";
                    displaycla = "採礦宗師";
                    Bukkit.getPlayer(id).sendTitle(ChatColor.GOLD + "恭喜你!", "你獲得了一個新的宗師稱號", 20, 200, 20);
                } else if (iron_master == 0 && cla.contains("採礦宗師")) {
                    cla = cla.replace(",採礦宗師", "");
                    displaycla = cla.split(",")[0];
                    Bukkit.getPlayer(id).sendMessage("你有一個宗師稱號被奪走了，請加油!");
                }

                if (fish_master == 1 && !cla.contains("釣魚宗師")) {
                    cla += ",釣魚宗師";
                    displaycla = "釣魚宗師";
                    Bukkit.getPlayer(id).sendTitle(ChatColor.GOLD + "恭喜你!", "你獲得了一個新的宗師稱號", 20, 200, 20);
                } else if (fish_master == 0 && cla.contains("釣魚宗師")) {
                    cla = cla.replace(",釣魚宗師", "");
                    displaycla = cla.split(",")[0];
                    Bukkit.getPlayer(id).sendMessage("你有一個宗師稱號被奪走了，請加油!");
                }

                if (trade_master == 1 && !cla.contains("貿易宗師")) {
                    cla += ",貿易宗師";
                    displaycla = "貿易宗師";
                    Bukkit.getPlayer(id).sendTitle(ChatColor.GOLD + "恭喜你!", "你獲得了一個新的宗師稱號", 20, 200, 20);
                } else if (trade_master == 0 && cla.contains("貿易宗師")) {
                    cla = cla.replace(",貿易宗師", "");
                    displaycla = cla.split(",")[0];
                    Bukkit.getPlayer(id).sendMessage("你有一個宗師稱號被奪走了，請加油!");
                }

                if (wood_master == 1 && !cla.contains("伐木宗師")) {
                    cla += ",伐木宗師";
                    displaycla = "伐木宗師";
                    Bukkit.getPlayer(id).sendTitle(ChatColor.GOLD + "恭喜你!", "你獲得了一個新的宗師稱號", 20, 200, 20);
                } else if (wood_master == 0 && cla.contains("伐木宗師")) {
                    cla = cla.replace(",伐木宗師", "");
                    displaycla = cla.split(",")[0];
                    Bukkit.getPlayer(id).sendMessage("你有一個宗師稱號被奪走了，請加油!");
                }

                nickname = rs.getString("nickname");
                particle = rs.getString("particle");
                coin = rs.getInt("coin");
                fly = rs.getInt("fly");

                exp = rs.getInt("exp");
                lv = rs.getInt("lv");
                balance = rs.getFloat("balance");
                region = rs.getInt("region");
                diamond = rs.getInt("diamond");
                gold = rs.getInt("gold");

                // 載入領地資料(字串格式) 並建構 HashMap
                regions = rs.getString("regions");
                buildRegionMap(regions);

                sword = rs.getInt("sword");
                magic = rs.getInt("magic");
                ripe = rs.getInt("ripe");
                bow = rs.getInt("bow");
                iron = rs.getInt("iron");
                fish = rs.getInt("fish");
                trade = rs.getInt("trade");
                wood = rs.getInt("wood");

                home = rs.getString("home");

                maxregions = rs.getInt("maxregions");
                baha = rs.getInt("baha");

                inventory = rs.getString("inventory");

                if (displaycla.equals("無業者"))
                    clacolor = ChatColor.GRAY;

                if (displaycla.equals("農夫"))
                    clacolor = ChatColor.DARK_GREEN;
                else if (displaycla.equals("礦工"))
                    clacolor = ChatColor.DARK_GREEN;
                else if (displaycla.equals("劍士"))
                    clacolor = ChatColor.DARK_GREEN;
                else if (displaycla.equals("弓手"))
                    clacolor = ChatColor.DARK_GREEN;
                else if (displaycla.equals("工匠"))
                    clacolor = ChatColor.DARK_GREEN;
                else if (displaycla.equals("商人"))
                    clacolor = ChatColor.DARK_GREEN;
                else if (displaycla.equals("漁夫"))
                    clacolor = ChatColor.DARK_GREEN;
                else if (displaycla.equals("木工"))
                    clacolor = ChatColor.DARK_GREEN;

                if (displaycla.equals("種植宗師"))
                    clacolor = ChatColor.YELLOW;
                else if (displaycla.equals("採礦宗師"))
                    clacolor = ChatColor.YELLOW;
                else if (displaycla.equals("舞劍宗師"))
                    clacolor = ChatColor.YELLOW;
                else if (displaycla.equals("狙擊宗師"))
                    clacolor = ChatColor.YELLOW;
                else if (displaycla.equals("加工宗師"))
                    clacolor = ChatColor.YELLOW;
                else if (displaycla.equals("貿易宗師"))
                    clacolor = ChatColor.YELLOW;
                else if (displaycla.equals("釣魚宗師"))
                    clacolor = ChatColor.YELLOW;
                else if (displaycla.equals("伐木宗師"))
                    clacolor = ChatColor.YELLOW;

                if (displaycla.equals("家族代表"))
                    clacolor = ChatColor.DARK_PURPLE;
                else if (displaycla.equals("家族幹部"))
                    clacolor = ChatColor.BLUE;

            }
            conn.close();
        } catch (Exception exc) {
            Bukkit.getPlayer(id).kickPlayer(ChatColor.RED + "載入您的資料時發生問題，請聯絡管理員");
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[Player data] When loading player data : " + exc.getMessage());
        }

        if (!family.equals("null")) {
            // 若家族資料顯示玩家是家族幹部，但玩家未擁有身分，給予身分與稱號
            if (App.families.get(family).groups.get("家族幹部").members.contains(id) && !cla.contains("家族幹部")) {
                cla += ",家族幹部";
                displaycla = "家族幹部";
                clacolor = ChatColor.BLUE;
            }
            // 若家族資料顯示玩家不是家族幹部，但玩家擁有身分，刪除身分與稱號
            if (!App.families.get(family).groups.get("家族幹部").members.contains(id) && cla.contains("家族幹部")) {
                cla = cla.replace(",家族幹部", "");
                displaycla = cla.split(",")[0];
                clacolor = ChatColor.DARK_GREEN;
            }

            if (App.families.containsKey(family)) {
                // 若玩家的家族存在，但家族成員不包含玩家，刪除該玩家資料的家族
                boolean infamily = false;
                for (Map.Entry<String, family.group> g : App.families.get(family).groups.entrySet()) {
                    if (infamily)
                        break;
                    if (App.families.get(family).president.equals(id)) {
                        infamily = true;
                    }
                    if (g.getValue().members.contains(id)) {
                        infamily = true;
                    }
                }
                if (!infamily) {
                    Bukkit.getPlayer(id).sendMessage("你已經不是你本來家族的成員了，我很遺憾!");
                    family = "null";
                }
            }
        }

        // 若沒有家族，刪除家族代表或家族幹部的身分
        if (family.equals("null")) {
            if (cla.contains("家族代表"))
                cla = cla.replace(",家族代表", "");
            if (cla.contains("家族幹部"))
                cla = cla.replace(",家族幹部", "");
        }

        // 當稱號是未擁有的身分時，設定稱號為預設身分
        if (!cla.contains(displaycla)) {
            displaycla = cla.split(",")[0];
        }

        // 設定顯示稱號色彩
        setDisplayClaColor();

        if (lv != 1 && getPlayerRegionAvailible() < 100) {
            int total = 0;

            if (!cla.contains("無業者")) {
                for (int i = 1; i <= 10; i++) {
                    total += i * 100;
                }
            }

            for (int i = 1; i < lv; i++) {
                total += i * 100;
            }

            total += exp / 10;

            addPlayerRegionAvailible(total);

            P.sendMessage(ChatColor.GOLD + "您好，為了確保貨幣的實際功能僅是用於玩家間的自由交易，");
            P.sendMessage(ChatColor.GOLD + "伺服器現在宣告領地已經改為使用\"可用領地格數\"來進行申請，");
            P.sendMessage(ChatColor.GOLD + "\"可用領地格數\"是根據您的累積在線時間獲得的，");
            P.sendMessage(ChatColor.GOLD + "由於您之前就在本伺服器遊玩，");
            P.sendMessage(ChatColor.GOLD + "因此我們將您目前的累積經驗值轉換為可用領地格數給您，");
            P.sendMessage(ChatColor.GOLD + "每 10 經驗，相當是遊玩一分鐘所得到的格數 1 格，");
            P.sendMessage(ChatColor.GOLD + "因此您目前的可用領地格數為 " + getPlayerRegionAvailible() + " 格，");
            P.sendMessage(ChatColor.GOLD + "而您目前已經使用 " + getUsingRegionSize() + " 格。");
            P.sendMessage(ChatColor.GOLD + "若您已經花費貨幣購買領地，您可以聯絡開服者協助您退回領地等值的黃金。");

            Bukkit.getConsoleSender().sendMessage("退還領地可用格數 " + total + " 給玩家 " + id);
        }

    }

    /** 同步玩家顯示稱號的顏色 */
    public void setDisplayClaColor() {
        if (displaycla.equals("無業者"))
            clacolor = ChatColor.GRAY;

        if (displaycla.equals("農夫"))
            clacolor = ChatColor.DARK_GREEN;
        else if (displaycla.equals("礦工"))
            clacolor = ChatColor.DARK_GREEN;
        else if (displaycla.equals("劍士"))
            clacolor = ChatColor.DARK_GREEN;
        else if (displaycla.equals("弓手"))
            clacolor = ChatColor.DARK_GREEN;
        else if (displaycla.equals("工匠"))
            clacolor = ChatColor.DARK_GREEN;
        else if (displaycla.equals("商人"))
            clacolor = ChatColor.DARK_GREEN;
        else if (displaycla.equals("漁夫"))
            clacolor = ChatColor.DARK_GREEN;
        else if (displaycla.equals("木工"))
            clacolor = ChatColor.DARK_GREEN;

        if (displaycla.equals("種植宗師"))
            clacolor = ChatColor.YELLOW;
        else if (displaycla.equals("採礦宗師"))
            clacolor = ChatColor.YELLOW;
        else if (displaycla.equals("舞劍宗師"))
            clacolor = ChatColor.YELLOW;
        else if (displaycla.equals("狙擊宗師"))
            clacolor = ChatColor.YELLOW;
        else if (displaycla.equals("加工宗師"))
            clacolor = ChatColor.YELLOW;
        else if (displaycla.equals("貿易宗師"))
            clacolor = ChatColor.YELLOW;
        else if (displaycla.equals("釣魚宗師"))
            clacolor = ChatColor.YELLOW;
        else if (displaycla.equals("伐木宗師"))
            clacolor = ChatColor.YELLOW;

        if (displaycla.equals("家族代表"))
            clacolor = ChatColor.DARK_PURPLE;
        else if (displaycla.equals("家族幹部"))
            clacolor = ChatColor.BLUE;
    }

    /** 建構領地 HashMap */
    public void buildRegionMap(String RegionData) {

        // 解析領地資料字串
        for (String s : RegionData.split(",")) {

            if (s.equals("0"))
                continue;

            if (App.regions.containsKey(Integer.parseInt(s))) {
                region r = App.regions.get(Integer.parseInt(s)); // 伺服器確實存在這個領地

                if (r.lordname.contains("family:")) {
                    Bukkit.getConsoleSender().sendMessage(
                            ChatColor.GOLD + "[玩家領地資料] " + id + " 的領地 " + r.name + " 現在是家族領地，因此從擁有的領地中刪除。");
                    P.sendMessage(ChatColor.RED + "你的領地 " + r.name + " 現在是家族領地，因此從你擁有的領地中刪除。");
                    continue;
                }

                if (!r.lord.equals(uuid)) { // 這個領地的 uuid 和玩家不符合
                    Bukkit.getConsoleSender().sendMessage(
                            ChatColor.GOLD + "[玩家領地資料] " + id + " 的領地 " + r.name + " 所記錄的領主並不是他，因此從你擁有的領地中刪除。");
                    P.sendMessage(ChatColor.RED + "你的領地 " + r.name + " 所記錄的領主並不是你，因此從你擁有的領地中刪除。");
                    continue;
                }

                regionlist.put(r.id, r);

            } else {
                Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.GOLD + "[玩家領地資料] " + id + " 的領地編號 " + s + " 已經不存在於伺服器，因此從擁有的領地中刪除。");
                P.sendMessage(ChatColor.RED + "你的領地編號 " + s + " 已經不存在於伺服器，因此從你擁有的領地中刪除。");
                continue;
            }
        }

        // 掃描伺服器現有領地 (確認有無遺失)
        for (Map.Entry<Integer, region> entry : App.regions.entrySet()) {

            region r = entry.getValue();

            if (regionlist.containsKey(r.id))
                continue;
            else {
                if (r.lord.equals(uuid) && !r.lordname.contains("family:")) { // 有一個領地的 uuid 和玩家相同，但不是家族領地

                    Bukkit.getConsoleSender()
                            .sendMessage(ChatColor.GOLD + "[玩家領地資料] " + id + " 的領地 " + r.name + " 遺失但現在已經被找回。");
                    P.sendMessage(ChatColor.GREEN + "你遺失的領地 " + r.name + " 已經被找回並回到你的領地清單。");
                    regionlist.put(r.id, r);
                    r.lordname = id;

                }
            }
        }
    }

    /** 取得轉為 String 格式的領地資料 (方便存於 MySQL) */
    public String getRegionsInString() {
        String regions = "0";
        for (Map.Entry<Integer, region> entry : regionlist.entrySet()) {
            regions += "," + entry.getKey();
        }
        return regions;
    }

    /** 取得 Gson 格式的 settings (方便儲存至資料庫) */
    public String getsettingsinGson() {
        Gson gson = new Gson();
        String data = gson.toJson(settings);
        return data;
    }

    /** 取得玩家最後一次的登入時間 */
    public String getLastLogin() {
        // 預設值是剛開服的日期，若無法取得就使用該日期
        String lastlogin = "2019-06-22 18:32:35";

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
        return lastlogin;

    }

    /** 更新玩家的最後一次登入時間 */
    public void updateLastLogin() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=userdatauploader&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("UPDATE `userdata` SET `lastlogindate`='" + util.now() + "' WHERE `id`='" + id + "'");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 更新玩家的最後登入時間時發生問題 : " + errors.toString());
        }
    }

    /** 檢查有沒有未領取的遊戲幣 */
    public void CheckBank() {
        Connection conn = null;
        Player P = Bukkit.getPlayer(id);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=userdatadownloader&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            ResultSet rs = st.getResultSet();
            st.execute("SELECT * FROM `bank` WHERE `to`='" + id + "' AND `receive`=0");
            rs = st.getResultSet();
            boolean empty = rs.wasNull();
            if (!empty)
                P.sendMessage(" ");
            while (rs.next()) {
                addBalance(rs.getFloat("amount"), rs.getString("description") + " (來自" + rs.getString("from") + ")");
                Statement st2 = conn.createStatement();
                st2.execute("UPDATE `bank` SET `receivetime`='" + util.now() + "', `bal`= " + util.round(balance, 2)
                        + " , `receive`=1 WHERE `to`='" + id + "' AND `paytime`='" + rs.getString("paytime") + "';");
                P.sendMessage(ChatColor.GREEN + "提款成功: " + ChatColor.WHITE + "$ " + rs.getInt("amount"));
                P.sendMessage(ChatColor.GRAY + "款項來自: " + rs.getString("from"));
                P.sendMessage(ChatColor.GRAY + "交易描述: " + rs.getString("description"));
                P.sendMessage(ChatColor.GRAY + "交易時間: " + rs.getString("paytime"));
                P.sendMessage(ChatColor.GRAY + "餘額: " + balance);
            }
            if (!empty)
                P.sendMessage(" ");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[資料庫] 自資料庫確認銀行是否有玩家 " + id + " 的未領款項時發生問題 : " + errors.toString());
        }
    }

    // 檢查有沒有贊助後尚未發送的商城幣
    public void CheckDonate() {
        Connection conn = null;
        Player P = Bukkit.getPlayer(id);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/donate?user=userdatadownloader&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            ResultSet rs = st.getResultSet();
            st.execute("SELECT * FROM `data` WHERE `player`='" + id + "' AND `givecoin`=0 AND `RtnMsg`='交易成功'");
            rs = st.getResultSet();
            boolean empty = rs.wasNull();
            if (!empty)
                P.sendMessage(" ");
            while (rs.next()) {
                Statement st2 = conn.createStatement();
                coin += rs.getInt("TradeAmt") / 10;
                st2.execute("UPDATE `data` SET `givecointime`='" + util.now() + "', `givecoin`=1 , `aftercoin`=" + coin
                        + " WHERE `player`='" + id + "' AND `givecoin`=0 AND `RtnMsg`='交易成功'");
                P.sendMessage(ChatColor.GREEN + "謝謝您贊助 SMD:Kingdoms " + ChatColor.WHITE + "NT$ " + rs.getInt("TradeAmt")
                        + ChatColor.GREEN + " 讓玩家社群更加強大!");
                P.sendMessage(ChatColor.GRAY + "訂單編號: " + rs.getString("MerchantTradeNo"));
                P.sendMessage(ChatColor.GRAY + "剩餘商城幣: " + coin);
                P.sendMessage(ChatColor.GRAY + "建議您盡快至加值商城購買物品，不要將商城幣滯留在您的帳戶中。");
            }
            if (!empty)
                P.sendMessage(" ");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.RED + "[資料庫] 自資料庫確認贊助資料庫是否有玩家 " + id + " 的未領商城幣時發生問題 : " + errors.toString());
        }
    }

    // 檢查加值商城有沒有未領取的商品
    public void CheckCoinShop() {
        Connection conn = null;
        Player P = Bukkit.getPlayer(id);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/donate?user=userdatadownloader&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            ResultSet rs = st.getResultSet();
            st.execute("SELECT * FROM `sendtoplayer` WHERE `player`='" + id + "' AND `receive`=0");
            rs = st.getResultSet();
            boolean empty = rs.wasNull();
            if (!empty)
                P.sendMessage(" ");
            while (rs.next()) {
                Statement st2 = conn.createStatement();
                String item = rs.getString("item");
                int price = rs.getInt("price");
                st2.execute("UPDATE `sendtoplayer` SET `receivetime`='" + util.now() + "', `receive`=1 WHERE `player`='" + id + "' AND `item`='" + item + "';");

                // 格式範例 [nickname]我不是服主
                if (item.contains("[nickname]")) {
                    String nick = item.replace("[nickname]", "");
                    if (coin < price) {
                        P.sendMessage(ChatColor.RED + "您在加值商城購買的 " + ChatColor.WHITE + "自訂暱稱 " + nick + ChatColor.RED
                                + " 接收失敗，商城幣餘額不足!");
                        Statement st3 = conn.createStatement();
                        st3.execute(
                                "DELETE FROM `sendtoplayer` WHERE `player`='" + id + "' AND `item`='" + item + "';");
                        continue;
                    }
                    coin -= price;
                    this.nickname = nick;

                    P.sendMessage(ChatColor.GREEN + "您在加值商城購買的 " + ChatColor.WHITE + "自訂暱稱 " + nick + ChatColor.GREEN
                            + " 已經接收，重新登入後生效");
                    P.sendMessage(ChatColor.GRAY + "剩餘商城幣: " + coin);
                }

                // 格式範例 [particle]REDSTONERINGS(5-255-255-255-1)
                if (item.contains("[particle]")) {
                    String particledata = item.replace("[particle]", "");
                    if (coin < price) {
                        P.sendMessage(ChatColor.RED + "您在加值商城購買的 " + ChatColor.WHITE + "粒子特效 " + particledata
                                + ChatColor.RED + " 接收失敗，商城幣餘額不足!");
                        Statement st3 = conn.createStatement();
                        st3.execute(
                                "DELETE FROM `sendtoplayer` WHERE `player`='" + id + "' AND `item`='" + item + "';");
                        continue;
                    }
                    coin -= price;
                    if (particle.equals("no") || particle.equals("null"))
                        this.particle = "1:" + particledata;
                    else
                        this.particle += ",1:" + particledata;

                    P.sendMessage(ChatColor.GREEN + "您在加值商城購買的 " + ChatColor.WHITE + "粒子特效 " + particledata
                            + ChatColor.GREEN + " 已經接收，開啟手機即可設定");
                    P.sendMessage(ChatColor.GRAY + "剩餘商城幣: " + coin);
                }

                // 格式範例 [chatcolor]a
                if (item.contains("[chatcolor]")) {
                    String chatcolor = item.replace("[chatcolor]", "");
                    if (coin < price) {
                        P.sendMessage(ChatColor.RED + "您在加值商城購買的 " + ChatColor.WHITE + "對話訊息色彩 " + chatcolor
                                + ChatColor.RED + " 接收失敗，商城幣餘額不足!");
                        Statement st3 = conn.createStatement();
                        st3.execute(
                                "DELETE FROM `sendtoplayer` WHERE `player`='" + id + "' AND `item`='" + item + "';");
                        continue;
                    }
                    coin -= price;
                    settings.put("chatcolor", chatcolor);

                    P.sendMessage(ChatColor.GREEN + "您在加值商城購買的 " + ChatColor.WHITE + "對話訊息色彩 " + chatcolor
                            + ChatColor.GREEN + " 已經接收並生效");
                    P.sendMessage(ChatColor.GRAY + "剩餘商城幣: " + coin);
                }

                // 格式範例 [fly]72000
                if (item.contains("[fly]")) {
                    int fly = Integer.parseInt(item.replace("[fly]", ""));
                    if (coin < price) {
                        P.sendMessage(ChatColor.RED + "您在加值商城購買的 " + ChatColor.WHITE + "領地飛行時數 " + fly / 20
                                + ChatColor.RED + "秒 接收失敗，商城幣餘額不足!");
                        Statement st3 = conn.createStatement();
                        st3.execute(
                                "DELETE FROM `sendtoplayer` WHERE `player`='" + id + "' AND `item`='" + item + "';");
                        continue;
                    }
                    coin -= price;
                    this.fly += fly;

                    P.sendMessage(ChatColor.GREEN + "您在加值商城購買的 " + ChatColor.WHITE + "領地飛行時數 " + fly / 20
                            + ChatColor.GREEN + "秒 已經接收並生效");
                    P.sendMessage(ChatColor.GRAY + "剩餘商城幣: " + coin);
                }

                // 格式範例 [region]1
                if (item.contains("[region]")) {
                    int region = Integer.parseInt(item.replace("[region]", ""));
                    if (coin < price) {
                        P.sendMessage(ChatColor.RED + "您在加值商城購買的 " + ChatColor.WHITE + "領地擴充 " + region + ChatColor.RED
                                + "塊 接收失敗，商城幣餘額不足!");
                        Statement st3 = conn.createStatement();
                        st3.execute(
                                "DELETE FROM `sendtoplayer` WHERE `player`='" + id + "' AND `item`='" + item + "';");
                        continue;
                    }
                    coin -= price;
                    this.maxregions += region;

                    P.sendMessage(ChatColor.GREEN + "您在加值商城購買的 " + ChatColor.WHITE + "領地擴充 " + region + ChatColor.GREEN
                            + "塊 已經接收並生效");
                    P.sendMessage(ChatColor.GRAY + "剩餘商城幣: " + coin);
                }
            }
            if (!empty)
                P.sendMessage(" ");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[資料庫] 自資料庫確認加值商城是否有玩家 " + id + " 的未領商品時發生問題 : " + errors.toString());
        }
    }

    /** 取得短暱稱 (僅顯示 Display cla 和 nickname) */
    public String getShortDisplayName() {
        if (nickname.equals("null"))
            return ChatColor.WHITE + "[" + clacolor + displaycla + ChatColor.WHITE + "] " + id;
        else
            return ChatColor.WHITE + "[" + clacolor + displaycla + ChatColor.WHITE + "] " + nickname;
    }

    /** 上傳玩家資料至資料庫 */
    public void upload() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=userdatauploader&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("UPDATE `userdata` SET `maxregions`=" + maxregions + ", `gold`=" + gold + ",`diamond`=" + diamond + ",`settings`='"
                    + getsettingsinGson() + "',`fly`=" + fly + ",`particle`='" + particle + "',`nickname`='" + nickname
                    + "',`coin`=" + coin + ",`home`='" + home + "', `uuid`='" + uuid + "', `family`='" + family
                    + "' ,`class`='" + cla + "',`lv`=" + lv + ",`exp`=" + exp + ",`balance`=" + util.round(balance, 0)
                    + ", `region`=" + region + ",`regions`='" + getRegionsInString() + "', `displayname`='"
                    + displayname + "' ,`trade`=" + trade + " ,`magic`=" + magic + " ,`fish`=" + fish + " ,`iron`="
                    + iron + " ,`sword`=" + sword + " ,`bow`=" + bow + " ,`ripe`=" + ripe + " ,`wood`=" + wood
                    + ",`displaycla`='" + displaycla + "' WHERE `id`='" + id + "'");
            conn.close();
        } catch (Exception exc) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[Player data] When uploading player data : " + exc.getMessage());
        }
    }

    /** 玩家收款 */
    public void addBalance(Float amt, String description) {
        balance += amt;
        util.sendActionbarMessage(P, ChatColor.GREEN + "" + ChatColor.BOLD + "獲得 " + amt + " 元！  餘額：" + balance);
        try {
            Connection conn = null;
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=bankmanager&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            st.execute("INSERT INTO `BalanceLogs` ( `id`, `uuid`,`amount`,`description`,`balance`,`time`) VALUES ('"
                    + id + "','" + uuid + "'," + amt + ",'" + description + "'," + balance + ",'" + util.now() + "');");
            conn.close();
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 紀錄玩家收款活動時發生問題: " + errors.toString());
        }

    }

    /** 玩家付款 */
    public boolean reduceBalance(float amt, String description) {
        if (balance >= amt) {
            balance -= amt;
            util.sendActionbarMessage(P, ChatColor.GOLD + "" + ChatColor.BOLD + "支付 " + amt + " 元！  餘額：" + balance);
            try {
                Connection conn = null;
                Class.forName("com.mysql.jdbc.Driver");
                String datasource = "jdbc:mysql://localhost/mcserver?user=bankmanager&password=Ken3228009!&useSSL=false";
                conn = DriverManager.getConnection(datasource);
                Statement st = conn.createStatement();
                st.execute("INSERT INTO `BalanceLogs` ( `id`, `uuid`,`amount`,`description`,`balance`,`time`) VALUES ('"
                        + id + "','" + uuid + "'," + -amt + ",'" + description + "'," + balance + ",'" + util.now()
                        + "');");
                conn.close();
            } catch (Exception exc) {
                StringWriter errors = new StringWriter();
                exc.printStackTrace(new PrintWriter(errors));
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 紀錄玩家付款活動時發生問題: " + errors.toString());
            }
            return true;
        } else // 餘額不足
            return false;
    }

    /** 設定玩家的返回位置 */
    public void setPlayerBackLocation(Location l) {
        settings.put("back", App.config.getString("server") + "," + l.getWorld().getName() + "," + l.getX() + ","
                + l.getY() + "," + l.getZ());
    }

    /** 取得玩家的返回位置 */
    public Location getPlayerBackLocation() {

        if (!settings.containsKey("back")) {
            return null; // 尚未設定
        }

        if (!settings.get("back").split(",")[0].equals(App.config.getString("server"))) {
            return null; // 不同分流
        }

        World w = Bukkit.getWorld(settings.get("back").split(",")[1]);
        double x = Double.parseDouble(settings.get("back").split(",")[2]);
        double y = Double.parseDouble(settings.get("back").split(",")[3]);
        double z = Double.parseDouble(settings.get("back").split(",")[4]);

        Location l = new Location(w, x, y, z);
        return l;
    }

    /** 取得玩家累積可用領地格數 */
    public int getPlayerRegionAvailible() {
        if (!settings.containsKey("region_ava")) {
            settings.put("region_ava", "0");
        }
        return Integer.parseInt(settings.get("region_ava"));
    }

    /** 增加玩家累積可用領地格數 */
    public void addPlayerRegionAvailible(int size) {
        if (!settings.containsKey("region_ava")) {
            settings.put("region_ava", Integer.toString(size));
        }
        int ori = Integer.parseInt(settings.get("region_ava"));
        settings.put("region_ava", Integer.toString(ori + size));
    }

    /** 取得玩家現有領地總格數 */
    public int getUsingRegionSize() {
        int total = 0;
        for (Map.Entry<Integer, region> entry : regionlist.entrySet()) {
            total += entry.getValue().size;
        }
        return total;
    }

}