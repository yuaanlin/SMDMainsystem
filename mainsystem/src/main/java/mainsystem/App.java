package mainsystem;

import static org.bukkit.Material.LEGACY_STANDING_BANNER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.nametagedit.plugin.NametagEdit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import career.bow;
import career.iron;
import career.magic;
import career.ripe;
import career.sword;
import career.trade;
import career.wood;
import mainsystem.family.mission;
import mainsystem.region.group;
import missionclass.missiongroup;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import shop.classes.shop_sell_item;
import smd.discordbot.bot;
import smd.discordbot.main;

public class App extends JavaPlugin implements Listener {

    // 紀錄上一次該分流執行 CoreProtect Purge 的時間 ( 避免繁複讀取資料庫)
    private static String LastCoPurge = "";

    // Protocolib API
    private ProtocolManager protocolManager;

    // 同步所有分流的在線玩家列表
    public static Map<String, Integer> allplayers = new HashMap<>();

    // 儲存資料庫資料到分流伺服器，避免繁複讀取資料庫造成負擔
    public static Map<String, player> players = new HashMap<>();
    public static Map<Integer, region> regions = new HashMap<>();
    public static Map<String, String> serverdatas = new HashMap<>();
    public static Map<Integer, shop_sell_item> shop_sell = new HashMap<>();
    public static Map<String, family> families = new HashMap<>();
    public static Map<Integer, missiongroup> missiongroups = new HashMap<>();

    // 儲存自訂義物品的 class
    public items items = new items();

    public items getitems() {
        return items;
    }

    // 儲存需要隨著玩家登出而關閉的 Tasks
    public static Map<Player, List<BukkitTask>> tasks = new HashMap<>();

    // 儲存隊伍列表的 Hash Map
    public static Map<String, party> partys = new HashMap<>();

    // 儲存商店所有最便宜物品的數量
    public static int ShopDisplayNumber = 1;

    // 儲存對話頻道的 Hash Map
    public static Map<String, chatchannel> chatchannels = new HashMap<>();

    // Vault API 參數
    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    // Discord 機器人
    public static bot chatbot;
    public static bot shopbot;
    public static bot systembot;

    // 宣告 Plugin Message Channel 處理類別
    public static PluginMessageHandler PMH = new PluginMessageHandler();

    // 儲存該系統運行於哪個分流伺服器 (中文名稱)
    static String servername = "";

    // 載入設定檔
    static FileConfiguration config;

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            log.severe(String.format("[%s] - getServer().getPluginManager().getPlugin(\"Vault\") == null",
                    getDescription().getName()));
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            log.severe(String.format("[%s] - rsp == null", getDescription().getName()));
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

    /** TODO: 系統載入完成前禁止玩家操作的機制 */

    @Override
    public void onEnable() {

        // 註冊寵物系統需要的監聽封包
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
                            if (event.getPlayer().getName().equals("ken20001207")) {
                                PacketContainer packet = event.getPacket();
                                float left = -packet.getFloat().read(0);
                                float forward = packet.getFloat().read(1);
                                boolean jump = packet.getBooleans().read(0);
                                Player P = event.getPlayer();
                                // 垂直方向
                                Vector vec = new Vector(-P.getLocation().getDirection().getZ(), 0,
                                        P.getLocation().getDirection().getX()).multiply(left);
                                if (P.getVehicle() != null && P.getVehicle().isOnGround()
                                        && !(P.getVehicle() instanceof Player)) {
                                    P.getVehicle().setRotation(P.getLocation().getYaw(), P.getLocation().getPitch());
                                    if (jump) {
                                        P.getVehicle()
                                                .setVelocity(P.getVehicle().getVelocity().add(new Vector(0, 0.5, 0)));
                                    } else {
                                        P.getVehicle().setVelocity(P.getVehicle().getVelocity().add(P.getLocation()
                                                .getDirection().multiply(forward).add(vec).setY(0).multiply(0.1)));
                                    }

                                }
                            }
                        }
                    }
                });

        // 分流名稱對照 ---------------------------------------
        if (getConfig().getInt("server") == 0)
            servername = "傳送小鎮";
        if (getConfig().getInt("server") == 1)
            servername = "北境";
        if (getConfig().getInt("server") == 2)
            servername = "南境";
        if (getConfig().getInt("server") == 9453)
            servername = "測試";
        if (getConfig().getInt("server") == 11)
            servername = "屠龍者挑戰副本";

        // 初始化 Discord Bot ---------------------------------------
        main discordplugin = JavaPlugin.getPlugin(main.class);
        chatbot = discordplugin.getChatBot();
        shopbot = discordplugin.getShopBot();
        systembot = discordplugin.getsystembot();

        // 關閉白名單 (避免自動重啟後玩家無法進入) ---------------------------------------
        getServer().setWhitelist(false);

        // Vault 註冊 ---------------------------------------
        ServicesManager sm = getServer().getServicesManager();
        sm.register(Economy.class, new economy(), this, ServicePriority.Highest);
        sm.register(Chat.class, new chat(perms), this, ServicePriority.Highest);
        sm.register(Permission.class, new permission(), this, ServicePriority.Highest);
        setupPermissions();
        setupChat();

        // Event Listener 註冊 ---------------------------------------
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new magic(this), this);
        getServer().getPluginManager().registerEvents(new trade(this), this);
        getServer().getPluginManager().registerEvents(new wood(this), this);
        getServer().getPluginManager().registerEvents(new ripe(this), this);
        getServer().getPluginManager().registerEvents(new bow(this), this);
        getServer().getPluginManager().registerEvents(new sword(this), this);
        getServer().getPluginManager().registerEvents(new iron(), this);
        getServer().getPluginManager().registerEvents(new util(), this);
        getServer().getPluginManager().registerEvents(new pet(), this);

        // Plugin Message Channel 註冊 ---------------------------------------
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", PMH);

        // Dependences 檢查 ---------------------------------------
        if (!setupEconomy()) {
            log.severe(String.format("[%s] - 未偵測到 Vault 插件!", getDescription().getName()));
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("*** 未偵測到 Holographic Display 插件! ***");
        }

        // Config 處理 ---------------------------------------
        config = getConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // 副本分流初始化
        if (config.getInt("server") == 11) {
            missiongroups = missionclass.main.missiongroup_initial();
            getServer().getPluginManager().registerEvents(new missionclass.main(), this);
            Bukkit.getConsoleSender().sendMessage("[副本分流] 副本分流初始化完成!");
        }

        // 移除現有的領地 hologram ---------------------------------------
        for (Hologram pe : HologramsAPI.getHolograms(getPlugin()))
            if (pe.getLine(0).toString().contains("領地旗幟"))
                pe.delete();

        // 下載伺服器參數 ---------------------------------------
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=serverdatamanager&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st3 = conn.createStatement();
            st3.execute("SELECT * FROM serverdatas");
            ResultSet rs3 = st3.getResultSet();
            while (rs3.next()) {
                serverdatas.put(rs3.getString("name"), rs3.getString("value"));
            }
            conn.close();
        } catch (Exception exc) {
            Bukkit.getConsoleSender()
                    .sendMessage(ChatColor.RED + "[MySQL Error] when loading server datas : " + exc.getStackTrace());
        }

        // 下載資料自資料庫 ---------------------------------------
        datasync.download_region();
        datasync.download_family();
        datasync.download_player();
        datasync.download_shop();

        // 設定玩家 Display Name
        for (Player P : Bukkit.getOnlinePlayers()) {
            player p = players.get(P.getName());
            if (p.nickname.equals("null"))
                P.setDisplayName(chat.getPlayerPrefix(P) + ChatColor.GRAY + p.id + chat.getPlayerSuffix(P));
            else
                P.setDisplayName(chat.getPlayerPrefix(P) + ChatColor.WHITE + p.nickname + chat.getPlayerSuffix(P));
        }

        // 同步分流在線玩家列表
        try {
            PMH.SendPluginMessage("mainsystem_askplayerlist", "");
        } catch (Exception exc) {

        }
        for (Player P : Bukkit.getOnlinePlayers()) {
            // 通知其他分流加入該玩家到在線玩家列表
            try {
                PMH.SendPluginMessage("mainsystem_playerjoin",
                        "%p%" + P.getName() + "%p%%s%" + config.getString("server") + "%s%");
            } catch (Exception exc) {

            }

            // 加入該分流的在線玩家列表
            allplayers.put(P.getName(), config.getInt("server"));
        }

        // 建立聊天頻道 ---------------------------------------
        chatchannel cc;

        cc = new chatchannel("全伺服頻道");
        chatchannels.put("全伺服頻道", cc);
        for (Player P : Bukkit.getOnlinePlayers())
            cc.addplayer(P);

        cc = new chatchannel(servername + "分流頻道");
        chatchannels.put(servername + "分流頻道", cc);
        for (Player P : Bukkit.getOnlinePlayers())
            cc.addplayer(P);

        for (Map.Entry<String, family> entry : families.entrySet()) {
            family f = entry.getValue();
            cc = new chatchannel(f.name + "家族頻道");
            chatchannels.put(f.name + "家族頻道", cc);
            for (Player P : Bukkit.getOnlinePlayers())
                if (players.get(P.getName()).family.equals(f.name))
                    cc.addplayer(P);
        }

        // 玩家暫離計時功能 ---------------------------------------
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Map.Entry<String, player> entry : players.entrySet()) {
                    player p = entry.getValue();
                    p.afk++;

                    if (p.afk >= 12000 && !p.hasAFKflag) {
                        final Hologram hologram = HologramsAPI.createHologram(App.this,
                                Bukkit.getPlayer(p.id).getLocation().add(0.0, 3.0, 0.0));
                        p.hasAFKflag = true;
                        hologram.appendTextLine(ChatColor.GRAY + p.id + " 玩家暫離");
                        hologram.appendItemLine(new ItemStack(Material.BARRIER));
                    }

                    if (p.afk >= 24000 && getConfig().getInt("server") != 0) {
                        Bukkit.getPlayer(p.id).performCommand("connect server_lobby");
                    }

                    if (p.afk >= 24000 && getConfig().getInt("server") == 0) {
                        Bukkit.getPlayer(p.id).teleport(new Location(Bukkit.getWorld("spawn"), 0, 64, 142));
                        p.afk = 0;
                    }
                }

            }
        }.runTaskTimer(this, 1L, 1L);

        // 領地飛行計時功能 ---------------------------------------
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player P : Bukkit.getOnlinePlayers()) {

                    if (P.getName().equals("ken20001207"))
                        continue;

                    player p = players.get(P.getName());

                    // 不在領地內卻可以飛行
                    if (p.inregion == -1 && P.getAllowFlight()) {
                        P.setAllowFlight(false);
                    }

                    // 在領地內飛行
                    if (P.isFlying()) {
                        p.fly--;

                        // 剩餘秒數是 100 的倍數時發送警告
                        if ((p.fly / 20) % 100 == 0) {
                            util.sendActionbarMessage(P, "剩餘飛行時數 " + (p.fly / 20) + " 秒");
                        }

                        // 時數用完了
                        if (p.fly <= 0) {
                            P.setAllowFlight(false);
                        }
                    }

                }

            }
        }.runTaskTimer(this, 1L, 1L);

        // Discord 開機公告 ---------------------------------------ㄋ
        EmbedBuilder MEbuilder = new EmbedBuilder();
        MEbuilder.setTitle("分流 " + servername + " 重啟");
        MEbuilder.setColor(java.awt.Color.GREEN);
        MEbuilder.appendDescription("分流 " + servername + " 現已上線。");
        App.systembot.sendtoSystemChannel(MEbuilder.build());

        // 玩家自動上傳資料功能 ---------------------------------------
        new BukkitRunnable() {

            @Override
            public void run() {

                for (Map.Entry<String, player> entry : players.entrySet()) {
                    player p = entry.getValue();
                    p.logintime++;
                    if ((p.logintime + p.AutoSaveDelay) % 12000 == 0) {
                        util.sendActionbarMessage(Bukkit.getPlayer(p.id), ChatColor.GRAY + "定時儲存玩家資料到資料庫中 ...");
                        p.upload();
                        util.sendActionbarMessage(Bukkit.getPlayer(p.id), ChatColor.GRAY + "定時儲存玩家資料到資料庫完畢 OuOb");
                    }
                }

            }
        }.runTaskTimer(this, 1L, 1L);

        // 樂透定時開獎功能 ---------------------------------------
        if (config.getInt("server") == 0)
            new BukkitRunnable() {
                @Override
                public void run() {
                    Connection conn = null;
                    ResultSet rs = null;
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Ken3228009!&useSSL=false";
                        conn = DriverManager.getConnection(datasource);
                        Statement st = conn.createStatement();
                        st.execute("SELECT * FROM lotterylog WHERE `time`='" + util.now().substring(0, 13) + "';");
                        rs = st.getResultSet();
                        if (!(rs.next())) {
                            Statement st2 = conn.createStatement();
                            st2.execute("INSERT lotterylog (`time`,`total`) VALUES ('" + util.now().substring(0, 13)
                                    + "','" + lottery.gettotal() + "');");
                            Bukkit.getConsoleSender().sendMessage("[樂透] 定時檢查樂透是否需要開獎 : " + util.now().substring(0, 13)
                                    + " 累積獎金 " + lottery.gettotal());
                            if (lottery.gettotal() < 50000) {
                                // 發送不須開獎訊息
                                App.chatchannels.get("全伺服頻道").sendmessage(" ");
                                App.chatchannels.get("全伺服頻道").sendmessage(ChatColor.GOLD + " " + ChatColor.UNDERLINE
                                        + "獎池沒錢QQ" + ChatColor.WHITE + " 樂透獎池的錢不足五萬元，延後到下個整點開獎。 ");

                                // 發送不須開獎訊息
                                App.PMH.sendAllServerBrocast(" ");
                                App.PMH.sendAllServerBrocast(ChatColor.GOLD + " " + ChatColor.UNDERLINE + "獎池沒錢QQ"
                                        + ChatColor.WHITE + " 樂透獎池的錢不足五萬元，延後到下個整點開獎。 ");
                            } else if (lottery.gettotal() > 5000000) {
                                // 發送不須開獎訊息
                                App.chatchannels.get("全伺服頻道").sendmessage(" ");
                                App.chatchannels.get("全伺服頻道").sendmessage(ChatColor.GOLD + " " + ChatColor.UNDERLINE
                                        + "獎池塞爆" + ChatColor.WHITE + " 樂透獎池的票數太多了，開獎程序可能會導致伺服器崩潰，我們會盡快處理。 ");

                                // 發送不須開獎訊息
                                App.PMH.sendAllServerBrocast(" ");
                                App.PMH.sendAllServerBrocast(ChatColor.GOLD + " " + ChatColor.UNDERLINE + "獎池沒錢QQ"
                                        + ChatColor.WHITE + " 樂透獎池的錢不足五萬元，延後到下個整點開獎。 ");
                            } else {
                                lottery.getLotteryResult();

                            }
                        }
                    } catch (Exception exc) {
                        StringWriter errors = new StringWriter();
                        exc.printStackTrace(new PrintWriter(errors));
                        Bukkit.getConsoleSender()
                                .sendMessage(ChatColor.RED + "[資料庫] 定時檢查樂透是否需要開獎時發生問題 : " + errors.toString());
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
            }.runTaskTimer(this, 1, 1200);

        // 每天凌晨四點自動清除 CoreProtect 資料庫內超過一個月的資料 -------------------------------
        new BukkitRunnable() {
            @Override
            public void run() {

                if (!util.now().substring(11, 13).equals("04")) { // 現在不是凌晨四點
                    return;
                }

                if (LastCoPurge.equals(util.now().substring(0, 10))) { // 已經檢查過資料庫 確定今天執行過 Purge 了
                    return;
                }

                Connection conn = null;
                ResultSet rs = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    String datasource = "jdbc:mysql://localhost/mcserver?user=root&password=Ken3228009!&useSSL=false";
                    conn = DriverManager.getConnection(datasource);
                    Statement st = conn.createStatement();
                    st.execute("SELECT * FROM AutoCoPurgeLogs WHERE `date`='" + util.now().substring(0, 10)
                            + "' AND `server`=" + config.getString("server") + ";");
                    rs = st.getResultSet();
                    if (!(rs.next())) {
                        Statement st2 = conn.createStatement();
                        st2.execute("INSERT AutoCoPurgeLogs (`server`,`date`) VALUES (" + config.getString("server")
                                + ",'" + util.now().substring(0, 10) + "');");
                        Bukkit.getConsoleSender()
                                .sendMessage("[定時清理] 定時清除 CoreProtect 紀錄 : " + util.now().substring(0, 13));
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "co purge t:30d");
                    }
                } catch (Exception exc) {
                    StringWriter errors = new StringWriter();
                    exc.printStackTrace(new PrintWriter(errors));
                    Bukkit.getConsoleSender()
                            .sendMessage(ChatColor.RED + "[定時清理] 定時清除 CoreProtect 紀錄時發生問題 : " + errors.toString());
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
        }.runTaskTimer(this, 1, 1200);

        // 分流定時重啟功能 ---------------------------------------
        if (config.getInt("server") != 0)
            new BukkitRunnable() {

                @Override
                public void run() {
                    util.RestartServer();
                }
            }.runTaskLater(App.getPlugin(), 576000);

        // 每分鐘發送領定可用格數
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, player> entry : players.entrySet()) {
                    entry.getValue().addPlayerRegionAvailible(1);
                }
            }
        }.runTaskTimer(this, 1200L, 1200L);

        // 巴哈文章廣告宣傳功能 ---------------------------------------
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player P : Bukkit.getOnlinePlayers()) {
                    if (players.get(P.getName()).baha == 0) {
                        P.sendMessage(" ");
                        P.sendMessage(ChatColor.GRAY + "喜歡 SMD:Kingdoms 嗎? 到巴哈給我們 GP 作為鼓勵吧!");
                        P.sendMessage(ChatColor.GRAY + "按了以後可以和伺服主兌換酷炫的足跡粒子特效唷 OwO ");
                        TextComponent message = new TextComponent(ChatColor.GRAY + "[喜歡，給個GP!]");
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                "https://forum.gamer.com.tw/C.php?bsn=18673&snA=176229&tnum=9&subbsn=18"));
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("點擊前往巴哈招生文幫忙按GP，領取酷炫粒子特效").create()));
                        P.spigot().sendMessage(message);
                        P.spigot().sendMessage(
                                textcomp(ChatColor.GRAY + "[我已經按過GP了]", "點擊後將不會收到此通知", "/mainsystem baha 0"));
                        P.sendMessage(" ");
                    }
                }

            }
        }.runTaskTimer(this, 1L, 12000L);

        for (Player theplayer : Bukkit.getOnlinePlayers()) {
            util.sendActionbarMessage(theplayer, ChatColor.GREEN + "伺服器主系統重新啟動完畢，造成不便請見諒 OuOb");
        }

        //

    }

    /** 怪物傷害 1.5倍 (提升遊戲難度) */
    @EventHandler
    public void MobDamageAdd(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && !(e.getDamager() instanceof Player)) {
            e.setDamage(e.getDamage() * 1.5);
        }
    }

    /** PVP 模式處理 (未開啟PVP的玩家不會受到傷害) */
    @EventHandler
    public void PVPMode(EntityDamageByEntityEvent e) {

        try {

            if (config.getInt("server") == 0)
                return;

            // 近戰處理
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                player da = players.get(((Player) e.getDamager()).getName());
                player en = players.get(((Player) e.getEntity()).getName());

                if (e.getEntity().hasMetadata("NPC")) {
                    return;
                }

                if(!util.judgePVP(da,en)) {
                    e.setCancelled(true);
                }
            }

            // 弓箭處理
            if (e.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) e.getDamager();
                ProjectileSource shooter = arrow.getShooter();

                if (shooter instanceof Player && e.getEntity() instanceof Player) {

                    player da = players.get(((Player) shooter).getName());
                    player en = players.get(((Player) e.getEntity()).getName());

                    if(!util.judgePVP(da,en)) {
                        e.setCancelled(true);
                    }

                }
            }
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            if (e.getDamager() instanceof Player) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[例外錯誤] 處理玩家 "
                        + ((Player) e.getDamager()).getName() + " PVP 事件時發生問題 : " + errors.toString());
                util.pleaseReport((Player) e.getDamager());
            }
        }
    }

    // 同隊伍間玩家無法互相傷害
    @EventHandler
    public void samepartydamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            player damager = players.get(((Player) e.getDamager()).getName());
            player entity = players.get(((Player) e.getEntity()).getName());
            if (damager.party.length() != 0 && entity.party.length() != 0 && damager.party.equals(entity.party)) {
                e.setCancelled(true);
                return;
            }
        }

        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            ProjectileSource shooter = arrow.getShooter();

            if (shooter instanceof Player && e.getEntity() instanceof Player) // checking if the shooter is a player.
            {
                player damager = players.get(((Player) shooter).getName());
                player entity = players.get(((Player) e.getEntity()).getName());
                if (damager.party.length() != 0 && entity.party.length() != 0 && damager.party.equals(entity.party)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    // 飛行狀態下無法攻擊
    @EventHandler
    public void attackwhenflying(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player P = (Player) e.getDamager();
            if (P.isFlying()) {
                e.setCancelled(true);
                util.sendActionbarMessage(P, "飛行模式下無法攻擊");
            }
        }
    }

    public static App getPlugin() {
        return JavaPlugin.getPlugin(App.class);
    }

    private final DecimalFormat format = new DecimalFormat("##.##");

    private Object serverInstance;
    private Field tpsField;

    public String getTPS(int time) {
        try {
            double[] tps = ((double[]) tpsField.get(serverInstance));
            return format.format(tps[time]);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {

        // 上傳分流資料
        datasync.upload_family();
        datasync.upload_region();
        datasync.upload_player();
        datasync.upload_shop();

        // 移除所有 hologram
        for (Hologram pe : HologramsAPI.getHolograms(this)) {
            pe.delete();
        }

        // 上傳伺服器參數
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String datasource = "jdbc:mysql://localhost/mcserver?user=serverdatamanager&password=Ken3228009!&useSSL=false";
            conn = DriverManager.getConnection(datasource);
            Statement st = conn.createStatement();
            for (Map.Entry<String, String> entry : serverdatas.entrySet()) {
                st.execute("UPDATE `serverdatas` SET `value`='" + entry.getValue() + "' WHERE `name`='" + entry.getKey()
                        + "'");
            }
            conn.close();
        } catch (Exception exc) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MySQL Error] errord2: " + exc.getMessage());
        }

        // 發送記錄到 Discord 監控頻道
        EmbedBuilder MEbuilder = new EmbedBuilder();
        MEbuilder.setColor(java.awt.Color.PINK);
        MEbuilder.setTitle("分流 " + App.servername + " 系統關閉");
        MEbuilder.appendDescription("分流 " + App.servername + " 的系統已經關閉");
        MEbuilder.appendDescription("可能是定時重啟、手動重啟或伺服器關機?");
        App.systembot.sendtoSystemChannel(MEbuilder.build());

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        e.setJoinMessage("");
        Player P = e.getPlayer();
        player p = new player(P.getName());

        // 給予玩家一些其他插件的權限
        PermissionAttachment attachment = P.addAttachment(getPlugin());
        attachment.setPermission("coreprotect.inspect", true);

        // 先確定玩家資料是完整的，避免 null 例外
        try {
            if (p.nickname.equals("null")) {
            }
        } catch (Exception exc) {
            return;
        }

        // 加入在線玩家列表 (延遲一秒避免加入後又收到其他分流剛剛發送的刪除要求)
        new BukkitRunnable() {
            @Override
            public void run() {

                // 通知其他分流加入該玩家到在線玩家列表
                try {
                    PMH.SendPluginMessage("mainsystem_playerjoin",
                            "%p%" + p.id + "%p%%s%" + config.getString("server") + "%s%");
                } catch (Exception exc) {

                }

                // 加入該分流的在線玩家列表
                allplayers.put(p.id, config.getInt("server"));
            }
        }.runTaskLater(this, 20);

        try {

            // 傳送小鎮 title
            if (getConfig().getInt("server") == 0) {
                P.sendTitle(ChatColor.YELLOW + "SMD:Kingdom 傳送小鎮", ChatColor.GRAY + "世界的中心點，各地往來的必經之地", 20, 200, 20);
                P.teleport(new Location(Bukkit.getWorld("spawn"), 0, 64, 142));
                Bukkit.getWorld("spawn").spawnParticle(Particle.PORTAL,
                        new Location(Bukkit.getWorld("spawn"), 0, 64, 142).add(0, 1, 0), 100);
            }

            // 下載玩家資料
            players.put(P.getName(), p);

            // 領地數量警示
            if (p.regionlist.size() > p.maxregions) {
                P.sendMessage(" ");
                P.sendMessage(ChatColor.RED + "提醒您，你的領地數量 " + p.regionlist.size() + " 已超過您可擁有的最大領地 " + p.maxregions);
                P.sendMessage(ChatColor.RED + "建議您盡速刪除不必要的領地，或至加值商城購買領地擴充卷。");
            }

            // 設定 Display Name
            if (p.nickname.equals("null"))
                P.setDisplayName(chat.getPlayerPrefix(P) + ChatColor.GRAY + p.id + chat.getPlayerSuffix(P));
            else
                P.setDisplayName(chat.getPlayerPrefix(P) + ChatColor.WHITE + p.nickname + chat.getPlayerSuffix(P));
            P.setPlayerListName(P.getDisplayName());
            players.get(P.getName()).displayname = P.getDisplayName();
            NametagEdit.getApi().setPrefix(P, "§f[" + p.clacolor + p.displaycla + "§f]");
            NametagEdit.getApi().setSuffix(P, " §aLv." + p.lv);

            Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[連線] 玩家 " + p.id + ChatColor.GREEN + " 進入 "
                    + ChatColor.BLUE + servername + " 分流");

            p.CheckBank();
            p.CheckDonate();
            p.CheckCoinShop();
        } catch (Exception exc) {
            e.getPlayer().kickPlayer(ChatColor.RED + "玩家資料錯誤導致無法登入，請至 Discord 通知管理員");
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.RED + "[連線] 玩家 " + P.getName() + " 連入" + servername + " 分流時發生問題 : " + errors.toString());
        }

        // 加入聊天頻道
        chatchannels.get("全伺服頻道").addplayer(P);
        chatchannels.get(servername + "分流頻道").addplayer(P);
        if (families.containsKey(p.family)) {
            if (chatchannels.containsKey(p.family + "家族頻道"))
                chatchannels.get(p.family + "家族頻道").addplayer(P);
            else
                chatchannels.put(p.family + "家族頻道", new chatchannel(p.family + "家族頻道"));
        }

        // 贊助玩家粒子特效處理
        if (!p.particle.equals("no") && !p.particle.equals("null"))
            setupPlayerParticle(P);

        // 每日登入發送飛行秒數
        if (p.lastlogin.length() == 0 || (p.lastlogin.length() > 0 && util.DateDiff(p.lastlogin, util.now()) > 0)) {
            P.sendMessage("這是你今天第一次登入! 送你領地飛行時數 20 分鐘，希望你可以邀請更多朋友來玩 OuOb!");
            p.fly += 24000;
        }

        // 新玩家加入
        if (p.firstlogin) {

            // 發送歡迎消息至當前分流
            chatchannels.get("全伺服頻道").sendmessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.UNDERLINE + "OuOb!!"
                    + ChatColor.WHITE + " 新玩家 " + p.getShortDisplayName() + ChatColor.WHITE + " 加入伺服器啦 !!");

            // 發送訊息至其他分流
            PMH.sendAllServerBrocast(ChatColor.LIGHT_PURPLE + " " + ChatColor.UNDERLINE + "OuOb!!" + ChatColor.WHITE
                    + " 新玩家 " + p.getShortDisplayName() + ChatColor.WHITE + " 加入伺服器啦 !!");

            ItemStack is = new ItemStack(Material.WOODEN_SWORD);
            is.setAmount(1);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(ChatColor.GRAY + "無業者木劍");
            im.setLore(Arrays.asList(" ", ChatColor.GRAY + "謝謝你選擇 SMD : Kingdoms ! ",
                    ChatColor.GRAY + "希望這把木劍可以幫上忙 OwO "));
            im.addEnchant(Enchantment.DIG_SPEED, 3, false);
            im.addEnchant(Enchantment.DURABILITY, 3, false);
            is.setItemMeta(im);
            P.getInventory().addItem(is);

            is = new ItemStack(Material.WOODEN_AXE);
            is.setAmount(1);
            im = is.getItemMeta();
            im.setDisplayName(ChatColor.GRAY + "無業者木斧");
            im.setLore(Arrays.asList(" ", ChatColor.GRAY + "謝謝你選擇 SMD : Kingdoms ! ",
                    ChatColor.GRAY + "希望這把木斧可以幫上忙 OwO "));
            im.addEnchant(Enchantment.DIG_SPEED, 3, false);
            im.addEnchant(Enchantment.DURABILITY, 3, false);
            is.setItemMeta(im);
            P.getInventory().addItem(is);

            is = new ItemStack(Material.WOODEN_PICKAXE);
            is.setAmount(1);
            im = is.getItemMeta();
            im.setDisplayName(ChatColor.GRAY + "無業者木鎬");
            im.setLore(Arrays.asList(" ", ChatColor.GRAY + "謝謝你選擇 SMD : Kingdoms ! ",
                    ChatColor.GRAY + "希望這把木鎬可以幫上忙 OwO "));
            im.addEnchant(Enchantment.DIG_SPEED, 3, false);
            im.addEnchant(Enchantment.DURABILITY, 3, false);
            is.setItemMeta(im);
            P.getInventory().addItem(is);

            is = new ItemStack(Material.WOODEN_SHOVEL);
            is.setAmount(1);
            im = is.getItemMeta();
            im.setDisplayName(ChatColor.GRAY + "無業者木鏟");
            im.setLore(Arrays.asList(" ", ChatColor.GRAY + "謝謝你選擇 SMD : Kingdoms ! ",
                    ChatColor.GRAY + "希望這把鏟子可以幫上忙 OwO "));
            im.addEnchant(Enchantment.DIG_SPEED, 3, false);
            im.addEnchant(Enchantment.DURABILITY, 3, false);
            is.setItemMeta(im);
            P.getInventory().addItem(is);

            TextComponent web = new TextComponent(ChatColor.GRAY + "客服機器人 悄悄對您說 : 歡迎加入我們伺服器， " + ChatColor.GRAY
                    + "希望你可以先去官網的\"新手教學\"頁面查看基本的操作教學， " + ChatColor.GRAY + "讓你更快的融入伺服器唷 OuOb (點擊本訊息前往新手教學頁面)");
            web.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/help.php?page=1"));
            web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("點擊前往 SMD:Kingdoms 新手教學網頁").create()));
            P.spigot().sendMessage(web);

            p.firstlogin = false;
        }

        // 更新玩家最後一次的登入時間
        p.updateLastLogin();

        // 還給老玩家累積領地格數
        if (p.lv != 1 && p.getPlayerRegionAvailible() < 100) {
            int total = 0;

            if (!p.cla.contains("無業者")) {
                for (int i = 1; i <= 10; i++) {
                    total += i * 100;
                }
            }

            for (int i = 1; i < p.lv; i++) {
                total += i * 100;
            }

            total += p.exp / 10;

            p.addPlayerRegionAvailible(total);

            P.sendMessage(ChatColor.GOLD + "您好，為了確保貨幣的實際功能僅是用於玩家間的自由交易，");
            P.sendMessage(ChatColor.GOLD + "伺服器現在宣告領地已經改為使用\"可用領地格數\"來進行申請，");
            P.sendMessage(ChatColor.GOLD + "\"可用領地格數\"是根據您的累積在線時間獲得的，");
            P.sendMessage(ChatColor.GOLD + "由於您之前就在本伺服器遊玩，");
            P.sendMessage(ChatColor.GOLD + "因此我們將您目前的累積經驗值轉換為可用領地格數給您，");
            P.sendMessage(ChatColor.GOLD + "每 10 經驗，相當是遊玩一分鐘所得到的格數 1 格，");
            P.sendMessage(ChatColor.GOLD + "因此您目前的可用領地格數為 " + p.getPlayerRegionAvailible() + " 格，");
            P.sendMessage(ChatColor.GOLD + "而您目前已經使用 " + p.getUsingRegionSize() + " 格。");
            P.sendMessage(ChatColor.GOLD + "若您已經花費貨幣購買領地，您可以聯絡開服者協助您退回領地等值的黃金。");

            Bukkit.getConsoleSender().sendMessage("退還領地可用格數 " + total + " 給玩家 " + p.id);

        }

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {

        Player P = e.getPlayer();

        if (config.getInt("server") == 11) {
            if (e.getPlayer().getBedSpawnLocation() != null)
                e.setRespawnLocation(e.getPlayer().getBedSpawnLocation());
            else
                e.setRespawnLocation(e.getPlayer().getWorld().getSpawnLocation());
            return;
        }

        if (config.getInt("server") == 0) {
            P.sendTitle(ChatColor.YELLOW + "SMD:Kingdom 傳送小鎮", ChatColor.GRAY + "世界的中心點，各地往來的必經之地", 20, 200, 20);
            P.teleport(new Location(Bukkit.getWorld("spawn"), 0, 64, 142));
            Bukkit.getWorld("spawn").spawnParticle(Particle.PORTAL,
                    new Location(Bukkit.getWorld("spawn"), 0, 64, 142).add(0, 1, 0), 100);
            return;
        }

        player p = players.get(e.getPlayer().getName());

        if (p.home != null && p.home.split(",").length == 5) {
            if (p.home.split(",")[4].equals(config.getString("server"))) {
                Location l = new Location(Bukkit.getWorld(p.home.split(",")[0]), Integer.parseInt(p.home.split(",")[1]),
                        Integer.parseInt(p.home.split(",")[2]), Integer.parseInt(p.home.split(",")[3]));
                e.setRespawnLocation(l);
                Bukkit.getWorld(p.home.split(",")[0]).spawnParticle(Particle.PORTAL, l.add(0, 1, 0), 100);
            } else {
                util.sendActionbarMessage(e.getPlayer(), ChatColor.GRAY + "你離家太遠了，無法傳送回家");
            }
        } else {
            e.setRespawnLocation(new Location(Bukkit.getWorld(serverdatas.get("spawn_world")),
                    Integer.parseInt(serverdatas.get("spawn_x")), Integer.parseInt(serverdatas.get("spawn_y")),
                    Integer.parseInt(serverdatas.get("spawn_z"))));
            Bukkit.getWorld(serverdatas.get("spawn_world")).spawnParticle(Particle.PORTAL,
                    new Location(Bukkit.getWorld(serverdatas.get("spawn_world")),
                            Integer.parseInt(serverdatas.get("spawn_x")), Integer.parseInt(serverdatas.get("spawn_y")),
                            Integer.parseInt(serverdatas.get("spawn_z"))).add(0, 1, 0),
                    100);
        }
    }

    @EventHandler
    public void onNPCClicked(NPCRightClickEvent e) {

        try {

            Player P = e.getClicker();
            player p = players.get(P.getName());
            NPC npc = e.getNPC();

            if (npc.getName().contains("領地大臣")) {
                P.performCommand("chat 3 1");
            } else if (npc.getName().contains("農夫大臣")) {
                P.performCommand("chat 農夫 1");
            } else if (npc.getName().contains("工匠大臣")) {
                P.performCommand("chat 工匠 1");
            } else if (npc.getName().contains("漁夫大臣")) {
                P.performCommand("chat 漁夫 1");
            } else if (npc.getName().contains("劍士大臣")) {
                P.performCommand("chat 劍士 1");
            } else if (npc.getName().contains("弓手大臣")) {
                P.performCommand("chat 弓手 1");
            } else if (npc.getName().contains("礦工大臣")) {
                P.performCommand("chat 礦工 1");
            } else if (npc.getName().contains("商人大臣")) {
                P.performCommand("chat 商人 1");
            } else if (npc.getName().contains("木工大臣")) {
                P.performCommand("chat 木工 1");

            } else if (npc.getName().contains("交易所大臣")) {
                if (P.getInventory().getItemInMainHand().getType().toString().contains("AIR")) {
                    P.performCommand("shop buy 1");
                } else {
                    P.performCommand("shop sell");
                }
            } else if (npc.getName().contains("財政大臣")) {
                if (P.getInventory().getItemInMainHand() != null
                        && P.getInventory().getItemInMainHand().getType() == Material.GOLD_INGOT) {

                    if (P.getInventory().getItemInMainHand().getAmount() != 64) {
                        P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 財政大臣 " + ChatColor.DARK_GRAY
                                + ">>  " + ChatColor.WHITE + " 我們黃金是一組一組收購的喔，不然我手會痠 XD ");
                        return;
                    }

                    P.getInventory().getItemInMainHand().setAmount(0);
                    P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 財政大臣 " + ChatColor.DARK_GRAY
                            + ">>  " + ChatColor.WHITE + " 嗯... 是組好黃金呢! 辛苦啦XD ");

                    // 發送錢給玩家
                    p.addBalance(450f, "販賣一組黃金給財政大臣");

                } else {
                    if (P.getInventory().firstEmpty() == -1) {
                        P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 財政大臣 " + ChatColor.DARK_GRAY
                                + ">>  " + ChatColor.WHITE + "  你包包已經滿了 -.- 不能跟我買黃金");
                        return;
                    }
                    if (p.reduceBalance(450f, "和財政大臣購買一組黃金")) {
                        P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 財政大臣 " + ChatColor.DARK_GRAY
                                + ">>  " + ChatColor.WHITE + " 熱騰騰的金條來囉 OuOb ");
                        ItemStack gold = new ItemStack(Material.GOLD_INGOT);
                        gold.setAmount(64);
                        P.getInventory().addItem(gold);
                        return;
                    } else {
                        P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 財政大臣 " + ChatColor.DARK_GRAY
                                + ">>  " + ChatColor.WHITE + "  你錢不夠囉 一組黃金要 450 塊 ~");
                        return;
                    }

                }
            } else if (npc.getName().contains("傳送官")) {
                P.sendMessage(" [" + ChatColor.AQUA + "船長" + ChatColor.WHITE + "] 傳送官 " + ChatColor.DARK_GRAY + ">>  "
                        + ChatColor.WHITE + "早阿 , 要回傳送小鎮嗎?");
                P.spigot().sendMessage(
                        textcomp(ChatColor.UNDERLINE + " [我要前往傳送小鎮] ", "點擊傳送至傳送小鎮", "/connect server_lobby"));
            } else if (npc.getName().contains("格鬥教練")) {
                P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 格鬥教練 " + ChatColor.DARK_GRAY + ">>  "
                        + ChatColor.WHITE + "小子，你還太嫩了! 欠缺訓練呢 ...");
                P.spigot().sendMessage(
                        textcomp(ChatColor.UNDERLINE + " [我要前往競技場] ", "點擊傳送至 PVP 競技場 (會噴裝!)", "/mainsystem FFA"));
            } else if (npc.getName().contains("家族總管")) {
                if (p.family.equals("null") || P.getInventory().getItemInMainHand().getAmount() == 0) {
                    P.performCommand("chat 家族總管 1");
                } else {
                    family f = families.get(p.family);
                    Material m = P.getInventory().getItemInMainHand().getType();
                    for (Map.Entry<String, mission> mission : f.missions.entrySet()) {
                        for (Map.Entry<Material, Integer> req : mission.getValue().require.entrySet()) {
                            if (req.getKey() == m) {
                                if (P.getInventory().getItemInMainHand().getAmount() == 64) {
                                    util.sendActionbarMessage(P,
                                            "你為家族任務 " + mission.getKey() + " 貢獻了一組 "
                                                    + util.getNameInChinese(req.getKey().toString()) + " 還需要 "
                                                    + (req.getValue() - 1) + " 組");

                                    if (req.getValue() == 1)
                                        mission.getValue().require.remove(req.getKey());
                                    else
                                        mission.getValue().require.put(req.getKey(), req.getValue() - 1);

                                    P.getInventory().getItemInMainHand().setAmount(0);
                                    f.upload();
                                    families.put(f.name, new family(f.name));
                                    P.updateInventory();
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                PMH.SendPluginMessage("mainsystem_downloadfamily",
                                                        "%family%" + p.family + "%family%");
                                            } catch (Exception e) {

                                            }
                                        }
                                    }.runTaskLater(this, 3);

                                    return;
                                } else {
                                    util.sendActionbarMessage(P, "家族任務物品繳交是以一組為單位的!");
                                    return;
                                }
                            }
                        }
                    }
                    P.performCommand("family");
                    return;
                }
            }

        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + ex.getMessage() + " at "
                    + ex.getStackTrace()[0].getLineNumber());
        }

    }

    public static TextComponent textcomp(String text, String hover, String cmd) {
        TextComponent a = new TextComponent(text);
        a.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        a.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
        return a;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        event.setCancelled(true);

        try {

            Player player = event.getPlayer();
            Player P = event.getPlayer();
            player p = players.get(player.getName());
            final String message = event.getMessage();
            String message2 = message;

            // 玩家與 NPC 對話的事件處理
            if (players.get(player.getName()).ChatingWithNPC) {

                // 正在等待玩家輸入要購買多少錢的樂透
                if (p.strigns.equals("lottery,buy")) {

                    Bukkit.getScheduler()
                            .callSyncMethod(this, () -> player.performCommand("mainsystem lottery buy " + message))
                            .get();
                    p.ChatingWithNPC = false;
                    p.strigns = "";
                    return;

                    // 正在等待玩家輸入要邀請至隊伍的玩家名稱
                } else if (p.strigns.equals("party,invite")) {

                    Bukkit.getScheduler().callSyncMethod(this, () -> player.performCommand("party invite " + message))
                            .get();
                    p.ChatingWithNPC = false;
                    p.strigns = "";
                    return;

                    // 正在等待玩家輸入要創立的隊伍名稱
                } else if (p.strigns.equals("party,create")) {

                    Bukkit.getScheduler().callSyncMethod(this, () -> player.performCommand("party create " + message))
                            .get();
                    p.ChatingWithNPC = false;
                    p.strigns = "";
                    return;

                    // 正在等玩家輸入要販售的物品價格
                } else if (players.get(player.getName()).strigns.contains("shop,sell,")) {

                    Double price = 0.0;
                    try {
                        price = Double.parseDouble(message);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "你必須輸入一個數字!");
                        return;
                    }

                    if (price > 0) {
                        Bukkit.getScheduler().callSyncMethod(this,
                                () -> player.performCommand("shop sell " + p.strigns.split(",")[2] + " " + message))
                                .get();
                        p.ChatingWithNPC = false;
                        p.strigns = "";
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "你這樣會不會賣太便宜 0.0?");
                    }

                    // 正在等玩家輸入要販售的物品數量
                } else if (players.get(player.getName()).strigns.equals("shop,sell")) {
                    if (isInteger(message)) {
                        Bukkit.getScheduler().callSyncMethod(this, () -> player.performCommand("shop sell " + message))
                                .get();
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "你必須輸入一個數字!");
                    }

                    // 正在等玩家輸入要購買的商品數量
                } else if (players.get(player.getName()).strigns.contains("shop,buy")) {
                    if (isInteger(message)) {
                        Bukkit.getScheduler().callSyncMethod(this,
                                () -> player.performCommand("shop buy " + p.strigns.split(",")[2] + " " + message))
                                .get();
                        p.ChatingWithNPC = false;
                        p.strigns = "";
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "你必須輸入一個數字!");
                    }

                    // 正在等待玩家輸入要購買的領地名稱
                } else if (players.get(player.getName()).strigns.contains("buyregion")) {
                    String NPCname = CitizensAPI.getNPCRegistry().getById(1).getName();
                    TextComponent cancel = new TextComponent(" " + ChatColor.UNDERLINE + "[算了,下次再申請]");
                    cancel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("和 " + NPCname + " 說 \" 算了,下次再申請\"").create()));
                    cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chat cancel"));
                    if (players.get(player.getName()).strigns.split(",").length == 1) {
                        if (message.length() > 8) {
                            TextComponent head = new TextComponent("");
                            head.toPlainText();
                            TextComponent msg = new TextComponent(
                                    "" + NPCname + " §8>> " + ChatColor.GRAY + "這個名子太長了呢，最多只能8個字哦 \n\n");
                            head.addExtra(msg);
                            player.spigot().sendMessage(head);
                            player.sendMessage(" ");
                            player.spigot().sendMessage(cancel);
                            player.sendMessage(" ");
                        } else {
                            players.get(player.getName()).strigns += message + ",";
                            TextComponent head = new TextComponent("");
                            head.toPlainText();
                            TextComponent msg = new TextComponent("" + NPCname + " §8>> " + ChatColor.GRAY + "嗯... "
                                    + ChatColor.YELLOW + message + ChatColor.GRAY + " , 好名子! 這塊領地有多大呢? (請輸入領地邊長，您剩餘"
                                    + (p.getPlayerRegionAvailible() - p.getUsingRegionSize()) + "格) \n\n");
                            head.addExtra(msg);
                            player.sendMessage(" ");
                            head.addExtra(cancel);
                            player.sendMessage(" ");
                            player.spigot().sendMessage(head);

                        }

                        // 正在等玩家輸入要購買的領地邊長
                    } else if (players.get(player.getName()).strigns.split(",").length == 2) {

                        if (!(isInteger(message))) {
                            TextComponent head = new TextComponent("");
                            head.toPlainText();
                            TextComponent msg = new TextComponent(
                                    "" + NPCname + " §8>> " + ChatColor.GRAY + "你必須給我一個純數字，我才聽得懂呀QQ  \n\n");
                            head.addExtra(msg);
                            player.spigot().sendMessage(head);
                            player.sendMessage(" ");
                            player.spigot().sendMessage(cancel);
                            player.sendMessage(" ");
                        } else if (Integer.parseInt(message) < 10) {
                            TextComponent head = new TextComponent("");
                            head.toPlainText();
                            TextComponent msg = new TextComponent(
                                    "" + NPCname + " §8>> " + ChatColor.GRAY + "最小邊長也要 10 格啦! 你是要買廁所喔XD \n\n");
                            head.addExtra(msg);
                            player.spigot().sendMessage(head);
                            player.sendMessage(" ");
                            player.spigot().sendMessage(cancel);
                            player.sendMessage(" ");
                        } else if (Integer.parseInt(message) > 4000) {
                            TextComponent head = new TextComponent("");
                            head.toPlainText();
                            TextComponent msg = new TextComponent(
                                    "" + NPCname + " §8>> " + ChatColor.GRAY + "哇... 現在最多只能買邊長小於 4000 格耶 \n\n");
                            head.addExtra(msg);
                            player.spigot().sendMessage(head);
                            player.sendMessage(" ");
                            player.spigot().sendMessage(cancel);
                            player.sendMessage(" ");

                        } else {

                            players.get(player.getName()).strigns += message + ",";

                            int i = 0;
                            String name = "";
                            int region_width = 0;
                            for (String s : p.strigns.split(",")) {
                                if (s.equals("buyregion"))
                                    i++;
                                else if (i == 1) {
                                    name = s;
                                    i++;
                                } else if (i == 2) {
                                    region_width = Integer.parseInt(s);
                                }
                            }

                            players.get(player.getName()).strigns += " " + message;
                            TextComponent head = new TextComponent("");
                            head.toPlainText();
                            TextComponent msg = new TextComponent(
                                    "" + NPCname + " §8>> " + ChatColor.GRAY + "好的, 這塊領地的邊長將會是" + ChatColor.YELLOW
                                            + message + ChatColor.GRAY + " ,面積合計 " + ChatColor.YELLOW
                                            + Integer.parseInt(message) * Integer.parseInt(message) + " 格。 \n");
                            TextComponent msg2 = new TextComponent(
                                    "" + NPCname + " §8>> " + ChatColor.GRAY + "申請領地需要花費您的剩餘領地格數，目前剩下 "
                                            + (p.getPlayerRegionAvailible() - p.getUsingRegionSize()) + " 確定申請嗎? \n");
                            TextComponent ok = new TextComponent(" " + ChatColor.UNDERLINE + "[確定申請]");
                            ok.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ComponentBuilder("按下後確定申請領地").create()));
                            ok.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/buyregion " + name + " " + region_width));
                            head.addExtra(msg);
                            head.addExtra(msg2);
                            player.sendMessage(" ");
                            player.spigot().sendMessage(head);
                            player.sendMessage(" ");
                            player.spigot().sendMessage(ok);
                            player.spigot().sendMessage(cancel);
                            player.sendMessage(" ");

                        }
                    }

                    // 正在等待玩家輸入要加入領地身分組的玩家名稱
                } else if (players.get(player.getName()).strigns.contains("region")
                        && players.get(player.getName()).strigns.contains("groups")
                        && players.get(player.getName()).strigns.contains("addplayer")) {
                    regions.get(Integer.parseInt(p.strigns.split(" ")[1])).groups.groups
                            .get(Integer.parseInt(p.strigns.split(" ")[4])).player.add(message);
                    util.sendActionbarMessage(event.getPlayer(),
                            ChatColor.GREEN + "已經將玩家 " + message + " 加入到身分組 "
                                    + regions.get(Integer.parseInt(p.strigns.split(" ")[1])).groups.groups
                                            .get(Integer.parseInt(p.strigns.split(" ")[4])).name);
                    Bukkit.getScheduler()
                            .callSyncMethod(this,
                                    () -> player.performCommand("region " + Integer.parseInt(p.strigns.split(" ")[1])
                                            + " groups set " + Integer.parseInt(p.strigns.split(" ")[4])))
                            .get();
                    p.ChatingWithNPC = false;
                    p.strigns = "";

                    // 正在等待玩家輸入要新增領地身分組的名稱
                } else if (players.get(player.getName()).strigns.contains("region")
                        && players.get(player.getName()).strigns.contains("groups")
                        && players.get(player.getName()).strigns.contains("add")) {
                    if (message.length() >= 8) {
                        event.getPlayer().sendMessage("最多只能輸入八個字");
                        return;
                    } else {
                        region r = regions.get(Integer.parseInt(p.strigns.split(" ")[1]));
                        r.groups.groups.add(new group(message));
                        util.sendActionbarMessage(event.getPlayer(), ChatColor.GREEN + "新身分組已經建立。");
                        r.showRegionInfo(P);
                        p.ChatingWithNPC = false;
                        p.strigns = "";
                    }
                }
                return;
            } else {

                // 不雅字詞屏蔽
                if (util.hasDirtyWords(message2)) {
                    P.sendMessage(" ");
                    P.sendMessage(
                            ChatColor.RED + " " + ChatColor.UNDERLINE + "沒禮貌!" + ChatColor.WHITE + " 系統偵測到您試圖發送不雅字詞。");
                    P.sendMessage(ChatColor.GRAY + " 請和我們一起維護伺服器玩家社群的素質水準，感謝您的配合!");
                    P.sendMessage(ChatColor.GRAY + " 被冤枉了嗎? 至 Discord 向開服者回報髒話誤判內容 OuOb");
                    return;
                }

                // 訊息顏色設定
                String colored_msg = "";
                ChatColor chatcolor = ChatColor.GRAY;
                if (!p.settings.containsKey("chatcolor")) { // 若玩家沒有文字顏色設定，設為預設值灰色
                    p.settings.put("chatcolor", "7");
                }

                if (p.settings.get("chatcolor").equals("7")) { // 灰色
                    chatcolor = ChatColor.GRAY;
                } else if (p.settings.get("chatcolor").equals("f")) { // 白色
                    chatcolor = ChatColor.WHITE;
                } else if (p.settings.get("chatcolor").equals("a")) { // 淺綠色
                    chatcolor = ChatColor.GREEN;
                } else if (p.settings.get("chatcolor").equals("2")) { // 深綠色
                    chatcolor = ChatColor.DARK_GREEN;
                } else if (p.settings.get("chatcolor").equals("e")) { // 黃色
                    chatcolor = ChatColor.YELLOW;
                } else if (p.settings.get("chatcolor").equals("6")) { // 橘色
                    chatcolor = ChatColor.GOLD;
                } else if (p.settings.get("chatcolor").equals("d")) { // 粉色
                    chatcolor = ChatColor.LIGHT_PURPLE;
                } else if (p.settings.get("chatcolor").equals("9")) { // 淺藍
                    chatcolor = ChatColor.BLUE;
                } else if (p.settings.get("chatcolor").equals("1")) { // 深藍
                    chatcolor = ChatColor.DARK_BLUE;
                } else if (p.settings.get("chatcolor").equals("b")) { // 淺天藍
                    chatcolor = ChatColor.AQUA;
                } else if (p.settings.get("chatcolor").equals("3")) { // 深天藍
                    chatcolor = ChatColor.DARK_AQUA;
                } else if (p.settings.get("chatcolor").equals("c")) { // 紅色
                    chatcolor = ChatColor.RED;
                }

                char[] c = message2.toCharArray();
                for (char cc : c) {
                    colored_msg += chatcolor + "" + cc;
                }

                // 取得聊天頻道
                chatchannel cc = chatchannels.get("全伺服頻道");
                if (message2.substring(0, 1).equals("#")) {
                    if (chatchannels.containsKey(p.family + "家族頻道")) {
                        colored_msg = colored_msg.replaceFirst("#", "");
                        colored_msg = ChatColor.BLUE + "(家族頻道) " + colored_msg;
                        cc = chatchannels.get(p.family + "家族頻道");
                    } else {
                        util.sendActionbarMessage(P, "無效的頻道: " + p.family + "家族頻道");
                        return;
                    }
                } else if (message2.substring(0, 1).equals("!")) {
                    colored_msg = colored_msg.replaceFirst("!", "");
                    colored_msg = ChatColor.DARK_GREEN + "(分流頻道) " + colored_msg;
                    cc = chatchannels.get(servername + "分流頻道");
                } else if (message2.substring(0, 1).equals("%")) {

                    if (p.party.length() == 0) {
                        util.sendActionbarMessage(P, "不在隊伍內無法使用隊伍頻道");
                        return;
                    }

                    if (chatchannels.containsKey(p.party + "隊伍頻道")) {
                        colored_msg = colored_msg.replaceFirst("%", "");
                        colored_msg = ChatColor.GOLD + "(隊伍頻道) " + colored_msg;
                        cc = chatchannels.get(p.party + "隊伍頻道");
                    } else {
                        util.sendActionbarMessage(P, "無效的頻道: " + p.party + "隊伍頻道");
                        return;
                    }
                }

                // 檢查玩家有無在該頻道
                if (!cc.member.contains(P)) {
                    util.sendActionbarMessage(P, "您不在頻道 " + cc.channelname);
                    return;
                }

                // 訊息格式建立
                TextComponent head = new TextComponent("");
                head.toPlainText();
                TextComponent icon = new TextComponent(" §8>> ");
                head.addExtra(getnamehover(player.getName(), true, false));
                head.addExtra(icon);
                head.addExtra(ChatColor.RESET + colored_msg);

                // 傳訊息至聊天頻道
                cc.sendmessage(head);

                // 發送訊息至 Discord
                if (cc.channelname.equals("全伺服頻道"))
                    chatbot.sendtoChatChannel("` " + ChatColor.stripColor(player.getDisplayName()) + " : ` "
                            + ChatColor.stripColor(colored_msg));

                // 傳訊息至其他分流
                PluginMessageHandler.sendAllServerChat(player.getName(), colored_msg, cc.channelname);

                // 官網引導訊息
                String msg_stripcolor = ChatColor.stripColor(colored_msg);
                if (msg_stripcolor.contains("官網") || msg_stripcolor.contains("官方網站")) {
                    TextComponent web = new TextComponent(ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服"
                            + ChatColor.WHITE + "] Bot9453 §8>> " + ChatColor.GRAY + " 有人提到官方網站嗎? " + ChatColor.GRAY
                            + "按我這條訊息可以前往喔 OuOb");
                    web.setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/index.php"));
                    web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("點擊前往 SMD:Kingdoms 官方網站").create()));
                    cc.sendmessage(web);
                } else if (msg_stripcolor.contains("商城") || msg_stripcolor.contains("加值商城")) {
                    TextComponent web = new TextComponent(ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服"
                            + ChatColor.WHITE + "] Bot9453 §8>> " + ChatColor.GRAY + " 有人提到加值商城嗎? " + ChatColor.GRAY
                            + "按我這條訊息可以前往喔 OuOb");
                    web.setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/donateshop.php"));
                    web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("點擊前往 SMD:Kingdoms 加值商城").create()));
                    cc.sendmessage(web);
                } else if (msg_stripcolor.contains("贊助")) {
                    TextComponent web = new TextComponent(ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服"
                            + ChatColor.WHITE + "] Bot9453 §8>> " + ChatColor.GRAY + " 有人提到贊助伺服嗎? " + ChatColor.GRAY
                            + "按我這條訊息可以前往喔 OuOb");
                    web.setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/donate.php"));
                    web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("點擊前往 SMD:Kingdoms 贊助頁面").create()));
                    cc.sendmessage(web);
                } else if (msg_stripcolor.contains("新手教學") || msg_stripcolor.contains("教學")
                        || msg_stripcolor.contains("怎麼玩") || msg_stripcolor.contains("怎玩")) {
                    TextComponent web = new TextComponent(ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服"
                            + ChatColor.WHITE + "] Bot9453 §8>> " + ChatColor.GRAY + " 有人需要新手教學嗎? " + ChatColor.GRAY
                            + "按我這條訊息可以前往喔 OuOb");
                    web.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                            "http://yuanyuan.cloud/smd/help.php?page=1.php"));
                    web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("點擊前往 SMD:Kingdoms 新手教學").create()));
                    cc.sendmessage(web);
                } else if (msg_stripcolor.contains("手機") || msg_stripcolor.contains("選單")
                        || msg_stripcolor.contains("菜單")) {
                    TextComponent web = new TextComponent(ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服"
                            + ChatColor.WHITE + "] Bot9453 §8>> " + ChatColor.GRAY + " 有人提到手機選單嗎? " + ChatColor.GRAY
                            + "按\"背包合成表\"的右邊那一格就可以開啟喔! " + ChatColor.GRAY + "(按這條訊息也可以)");
                    web.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mainsystem phone"));
                    web.setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊開啟手機選單").create()));
                    cc.sendmessage(web);
                }

                // 傳訊息至控制台
                Bukkit.getConsoleSender().sendMessage("(" + cc.channelname + ") " + head.toPlainText());

                // 顯示對話泡泡
                final String mess = colored_msg;
                Bukkit.getScheduler().callSyncMethod(this, () -> this.setholo(player, mess, 1)).get();

            }

        } catch (Exception exception) {
            Player Player = event.getPlayer();
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            exception.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            util.pleaseReport(Player);
        }

    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        e.setQuitMessage("");

        Player P = e.getPlayer();
        String name = P.getName();
        player p = players.get(P.getName());

        // 通知其他分流從在線玩家列表刪除玩家
        try {
            PMH.SendPluginMessage("mainsystem_playerquit", "%p%" + name + "%p%");
        } catch (Exception exc) {

        }

        // 用 afk flag 確保玩家的資料已經載入完畢，避免 null 例外
        try {
            if (p.hasAFKflag) {
            }
        } catch (Exception exc) {
            return;
        }

        try {

            if (p.hasAFKflag) {
                for (Hologram pe : HologramsAPI.getHolograms(this)) {

                    if (pe.getLine(0).toString().contains(p.id) && pe.getLine(0).toString().contains("玩家暫離")) {
                        pe.delete();
                        break;
                    }
                }
            }

            if (p.party.length() != 0) {
                P.performCommand("party quit");
            }

            players.get(name).upload();

            players.remove(p.id);

            Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[連線] 玩家 " + P.getName() + ChatColor.WHITE + " 離開 "
                    + ChatColor.BLUE + servername + " 分流");

        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.RED + "[連線] 玩家 " + P.getName() + " 離開 " + servername + " 分流時發生問題 : " + errors.toString());
        }

        // 關閉屬於該玩家的 Tasks
        if (tasks.containsKey(P)) {
            List<BukkitTask> tasklist = tasks.get(P);
            for (BukkitTask task : tasklist) {
                task.cancel();
            }
            tasks.remove(P);
        }

        // 將玩家所在的所有頻道退出
        for (Map.Entry<String, chatchannel> entry : chatchannels.entrySet()) {
            chatchannel cc = entry.getValue();
            if (cc.member.contains(P))
                cc.removeplayer(P);
        }

    }

    public static String itemStackToBase64(ItemStack item) throws IllegalStateException {
        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());

        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static ItemStack itemStackFromBase64(String data) throws IOException {
        try {

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;

        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    /** 重新載入玩家資料 (上傳資料，一秒後下載回來) */
    public static void ReloadPlayerData(String id) {

        if (players.containsKey(id)) {

            player p = players.get(id);

            // 上傳資料
            p.upload();

            // 一秒後下載回來
            new BukkitRunnable() {
                @Override
                public void run() {
                    player p = new player(id);
                    players.put(id, p);
                    p.CheckBank();
                    p.CheckCoinShop();
                    p.CheckDonate();
                }
            }.runTaskLater(App.getPlugin(), 20);

        } else {
            // 若不再目前分流，發送 Plugin Message 要求其他分流下載
            try {
                PMH.SendPluginMessage("mainsystem_downloadplayer", "%player%" + id + "%player%");
            } catch (Exception e) {

            }
        }
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String cmdlable, String[] args) {

        try {

            if (sender instanceof ConsoleCommandSender) {
                if (cmdlable.equals("mainsystem")) {
                    if (args.length == 1 && args[0].equals("restart")) {

                        if (getConfig().getInt("server") == 0) {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Mainsystem] this is lobby server");
                            return false;
                        }

                        util.RestartServer();
                        return false;
                    }
                    return false;
                } else if (cmdlable.equals("forceconnect")) {
                    if (args.length == 2 && Bukkit.getPlayer(args[0]) != null) {
                        Bukkit.getPlayer(args[0]).performCommand("connect " + args[1]);
                        Bukkit.getConsoleSender()
                                .sendMessage(ChatColor.GREEN + "已經強迫玩家 " + args[0] + " 執行連線指令 /connect " + args[1]);
                    } else {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "該玩家不在線上!");
                    }
                }
                return false;
            }

            Player senderp = (Player) sender;
            Player P = senderp;
            player p = players.get(P.getName());
            String name = senderp.getName();
            if (cmdlable.equals("family")) {
                if (args.length == 0) {

                    if (p.family.equals("null")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又沒有家族 0.0");
                        return false;
                    }

                    family f = families.get(p.family);
                    for (int j = 0; j < 20; j++) {
                        P.sendMessage(" ");
                    }
                    P.sendMessage(ChatColor.GRAY + " < 您的所屬家族: " + ChatColor.YELLOW + f.name + ChatColor.GRAY + " >");
                    P.sendMessage(ChatColor.GRAY + " | ");
                    if (!f.compelete)
                        P.sendMessage(ChatColor.GRAY + " | 家族狀態: " + ChatColor.YELLOW + " 創立任務尚未完成");
                    P.sendMessage(ChatColor.GRAY + " | 家族成員數: " + ChatColor.YELLOW + f.getmembernumber()
                            + ChatColor.GRAY + " 人");
                    P.sendMessage(ChatColor.GRAY + " | 等級: " + ChatColor.YELLOW + f.level + ChatColor.GRAY + " 等");
                    P.sendMessage(ChatColor.GRAY + " | 經驗: " + ChatColor.YELLOW + f.exp + ChatColor.GRAY + " / "
                            + f.level * 1000);
                    P.sendMessage(ChatColor.GRAY + " | 代表: " + ChatColor.YELLOW + f.president);
                    P.sendMessage(ChatColor.GRAY + " | 領地面積: " + ChatColor.YELLOW + f.getregions());
                    TextComponent head = new TextComponent("");
                    P.sendMessage(" ");
                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[家族成員列表]", "點擊查看家族成員列表",
                            "/family member"));
                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[家族任務清單]", "點擊查看家族任務清單",
                            "/family mission"));
                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[家族領地清單]", "點擊查看家族領地清單",
                            "/family regions"));
                    P.spigot().sendMessage(head);

                } else if (args.length == 1 && args[0].equals("mission")) {

                    if (p.family.equals("null")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又沒有家族 0.0");
                        return false;
                    }
                    family f = families.get(p.family);
                    for (int j = 0; j < 20; j++) {
                        P.sendMessage(" ");
                    }
                    P.sendMessage(ChatColor.GRAY + " < " + p.family + " 的家族任務 >");
                    P.sendMessage(ChatColor.GRAY + " |");
                    for (Map.Entry<String, mission> m : f.missions.entrySet()) {
                        P.sendMessage(ChatColor.GRAY + " | 任務 " + ChatColor.YELLOW + m.getValue().name);
                        P.sendMessage(ChatColor.GRAY + " |");
                        for (Map.Entry<Material, Integer> req : m.getValue().require.entrySet()) {
                            P.sendMessage(ChatColor.GRAY + " | --> 物品 " + ChatColor.YELLOW
                                    + util.getNameInChinese(req.getKey().name()) + ChatColor.GRAY + " 還需要 "
                                    + ChatColor.YELLOW + req.getValue() + " 組");
                        }
                        P.sendMessage(" ");
                        TextComponent head = new TextComponent("");
                        head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[回到家族選單首頁]", "點擊回到家族選單首頁",
                                "/family"));
                        P.spigot().sendMessage(head);
                    }
                } else if (args.length == 1 && args[0].equals("member")) {

                    if (p.family.equals("null")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又沒有家族 0.0");
                        return false;
                    }
                    family f = families.get(p.family);
                    for (int j = 0; j < 20; j++) {
                        P.sendMessage(" ");
                    }
                    P.sendMessage(ChatColor.GRAY + " < " + p.family + " 的家族成員 >");
                    P.sendMessage(ChatColor.GRAY + " |");
                    for (Map.Entry<String, mainsystem.family.group> g : f.groups.entrySet()) {

                        P.sendMessage(ChatColor.GRAY + " | 身分組 " + g.getValue().name);
                        P.sendMessage(ChatColor.GRAY + " | ");
                        for (String m : g.getValue().members) {
                            if (g.getValue().members.size() == 0)
                                P.sendMessage(ChatColor.GRAY + " | --> 尚無成員");

                            if (f.groups.get("家族幹部").members.contains(p.id) || f.president.equals(p.id)) {

                                ChatColor c = ChatColor.GRAY;

                                if (allplayers.containsKey(m)) {
                                    c = ChatColor.GREEN;
                                } else {
                                    if (util.DateDiff(util.getplayerlastlogin(m), util.now()) < 7)
                                        c = ChatColor.WHITE;
                                }

                                TextComponent head = new TextComponent(ChatColor.GRAY + " | --> " + c + m);
                                head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[踢出家族]",
                                        "將 " + m + " 從家族踢出", "/family kick " + m));

                                if (g.getValue().name.equals("一般成員"))
                                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[升級為家族幹部]",
                                            "將 " + m + " 升級為家族幹部", "/family upgrade " + m));

                                if (g.getValue().name.equals("家族幹部"))
                                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[降級為一般成員]",
                                            "將 " + m + " 降級為一般成員", "/family downgrade " + m));

                                if (m.equals(p.id))
                                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[退出家族]",
                                            "退出家族", "/family quit"));

                                P.spigot().sendMessage(head);

                            } else {
                                TextComponent head = new TextComponent(ChatColor.GRAY + " | --> " + m);

                                if (m.equals(p.id))
                                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[退出家族]",
                                            "退出家族", "/family quit"));

                                P.spigot().sendMessage(head);
                            }
                        }
                    }
                    P.sendMessage(" ");
                    TextComponent head = new TextComponent("");
                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[回到家族選單首頁]", "點擊回到家族選單首頁",
                            "/family"));
                    P.spigot().sendMessage(head);
                } else if (args.length == 1 && args[0].equals("quit")) {

                    if (p.family.equals("null")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又沒有家族 0.0");
                        return false;
                    }

                    family f = families.get(p.family);

                    if (p.cla.contains("家族幹部")) {
                        f.groups.get("家族幹部").members.remove(p.id);
                        p.cla = p.cla.replace(",家族幹部", "");
                    } else {
                        f.groups.get("一般成員").members.remove(p.id);
                    }

                    f.upload();

                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            try {
                                PMH.SendPluginMessage("mainsystem_downloadfamily", "%family%" + f.name + "%family%");
                            } catch (Exception e) {

                            }
                        }
                    }.runTaskLater(this, 10);

                    // 一秒後重新載入該玩家
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ReloadPlayerData(P.getName());
                        }
                    }.runTaskLater(this, 20);

                    p.family = "null";

                } else if (args.length == 1 && args[0].equals("regions")) {

                    if (p.family.equals("null")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又沒有家族 0.0");
                        return false;
                    }
                    family f = families.get(p.family);
                    for (int j = 0; j < 20; j++) {
                        P.sendMessage(" ");
                    }
                    P.sendMessage(ChatColor.GRAY + " < " + p.family + " 的家族領地 >");
                    P.sendMessage(ChatColor.GRAY + " |");
                    for (Map.Entry<Integer, region> entry : f.region.entrySet()) {
                        region r = entry.getValue();
                        TextComponent head = new TextComponent(ChatColor.GRAY + " | --> " + ChatColor.YELLOW + r.name
                                + ChatColor.GRAY + " 面積 " + ChatColor.YELLOW + r.size + ChatColor.GRAY + " 平方公尺");
                        head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[前往]",
                                " 傳送至家族領地 " + r.name, "/region tp " + r.id));
                        P.spigot().sendMessage(head);
                    }
                    P.sendMessage(" ");
                    TextComponent head = new TextComponent("");
                    head.addExtra(textcomp(ChatColor.RESET + " " + ChatColor.UNDERLINE + "[回到家族選單首頁]", "點擊回到家族選單首頁",
                            "/family"));
                    P.spigot().sendMessage(head);

                } else if (args.length == 2 && args[0].equals("kick")) {
                    if (p.family.equals("null")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又沒有家族 0.0");
                        return false;
                    }
                    family f = families.get(p.family);
                    if (f.groups.get("家族幹部").members.contains(p.id) || f.president.equals(p.id)) {
                        for (Map.Entry<String, mainsystem.family.group> g : f.groups.entrySet()) {
                            if (g.getValue().members.contains(args[1])) {
                                g.getValue().members.remove(args[1]);
                                util.sendActionbarMessage(P, ChatColor.GREEN + "已經將該玩家踢出家族");
                                f.upload();
                                P.performCommand("family member");
                                new BukkitRunnable() {

                                    @Override
                                    public void run() {
                                        try {
                                            PMH.SendPluginMessage("mainsystem_downloadfamily",
                                                    "%family%" + f.name + "%family%");
                                        } catch (Exception e) {

                                        }
                                    }
                                }.runTaskLater(this, 10);

                                // 重新載入該玩家
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        ReloadPlayerData(args[1]);
                                    }
                                }.runTaskLater(this, 20);
                                return false;
                            }
                        }
                        util.sendActionbarMessage(P, ChatColor.RED + "你們家族沒有這個人耶 0.0");
                        return false;
                    } else {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又不是家族幹部 0.0");
                        return false;
                    }
                } else if (args.length == 2 && args[0].equals("upgrade")) {
                    if (p.family.equals("null")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又沒有家族 0.0");
                        return false;
                    }

                    family f = families.get(p.family);
                    if (f.groups.get("家族幹部").members.contains(p.id) || f.president.equals(p.id)) {
                        if (f.groups.get("一般成員").members.contains(args[1])) {
                            f.groups.get("一般成員").members.remove(args[1]);
                            f.groups.get("家族幹部").members.add(args[1]);
                            util.sendActionbarMessage(P, ChatColor.GREEN + "已經將玩家 " + args[1] + " 升為家族幹部");
                            f.upload();
                            P.performCommand("family member");
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    try {
                                        PMH.SendPluginMessage("mainsystem_downloadfamily",
                                                "%family%" + f.name + "%family%");
                                    } catch (Exception e) {

                                    }
                                }
                            }.runTaskLater(this, 10);

                            // 一秒後重新載入該玩家
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    ReloadPlayerData(args[1]);
                                }
                            }.runTaskLater(this, 20);

                            return false;
                        }
                        util.sendActionbarMessage(P, ChatColor.RED + "一般成員裡面沒有這個人耶 0.0");
                        return false;
                    } else {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又不是家族幹部 0.0");
                        return false;
                    }
                } else if (args.length == 2 && args[0].equals("downgrade")) {
                    if (p.family.equals("null")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又沒有家族 0.0");
                        return false;
                    }
                    family f = families.get(p.family);
                    if (f.groups.get("家族幹部").members.contains(p.id) || f.president.equals(p.id)) {
                        if (f.groups.get("家族幹部").members.contains(args[1])) {
                            f.groups.get("家族幹部").members.remove(args[1]);
                            f.groups.get("一般成員").members.add(args[1]);
                            util.sendActionbarMessage(P, ChatColor.GREEN + "已經將該玩家 " + args[1] + " 降為家族成員");
                            f.upload();
                            P.performCommand("family member");
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    try {
                                        PMH.SendPluginMessage("mainsystem_downloadfamily",
                                                "%family%" + f.name + "%family%");
                                    } catch (Exception e) {

                                    }
                                }
                            }.runTaskLater(this, 10);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    ReloadPlayerData(args[1]);
                                }
                            }.runTaskLater(this, 20);
                            return false;
                        }
                        util.sendActionbarMessage(P, ChatColor.RED + "家族幹部裡面沒有這個人耶 0.0");
                        return false;
                    } else {
                        util.sendActionbarMessage(P, ChatColor.RED + "你又不是家族幹部 0.0");
                        return false;
                    }
                }

                // 跨分流密語
            } else if (cmdlable.equals("m")) {

                if (args.length < 2) {
                    P.sendMessage(ChatColor.RED + " 使用方式 : /m <玩家ID> <訊息> ");
                    return false;
                }

                if (!allplayers.containsKey(args[0])) {
                    P.sendMessage(ChatColor.RED + " 玩家 " + args[0] + " 不在伺服器的任何一個分流內 QAQ");
                    return false;
                }

                String msg = "";
                boolean first = true;
                for (String s : args) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    msg += " " + s;
                }

                char[] c = msg.toCharArray();
                msg = "";
                for (char cc : c) {
                    msg += ChatColor.GRAY + "" + cc;
                }

                P.sendMessage(ChatColor.GRAY + " " + P.getName() + " >> " + args[0] + " (悄悄話) " + msg);
                if (Bukkit.getPlayer(args[0]) != null) {
                    TextComponent m = new TextComponent(ChatColor.GRAY + " " + P.getName() + " >> (悄悄話) " + msg);
                    m.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + P.getName()));
                    m.setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊回覆悄悄話").create()));
                    Bukkit.getPlayer(args[0]).spigot().sendMessage(m);
                } else {
                    PMH.SendPluginMessage("mainsystem_mcommand",
                            "%f%" + P.getName() + "%f%%t%" + args[0] + "%t%%m%" + msg + "%m%");
                }

            } else if (cmdlable.equals("setparticle") && args.length == 1) {
                String origin = "";
                boolean first = true;
                for (String s : p.particle.split(",")) {
                    if (s.contains(args[0])) {
                        if (s.split(":")[0].equals("1")) {
                            if (!first)
                                origin += ",";
                            origin += "0:" + s.split(":")[1];
                        } else if (s.split(":")[0].equals("0")) {
                            if (!first)
                                origin += ",";
                            origin += "1:" + s.split(":")[1];
                        }
                    } else {
                        if (!first)
                            origin += ",";
                        origin += s;
                    }

                    first = false;
                }
                p.particle = origin;

                setupPlayerParticle(P);
                for (int j = 0; j < 20; j++) {
                    P.sendMessage(" ");
                }
                for (String s : p.particle.split(",")) {
                    ChatColor color = ChatColor.GRAY;
                    boolean enable = false;
                    if (s.split(":")[0].equals("1")) {
                        color = ChatColor.GREEN;
                        enable = true;
                    }
                    String particledata = s.split(":")[1];
                    String particlename = "";
                    if (particledata.split("\\(")[0].equals("REDSTONE"))
                        particlename = "紅石粉塵粒子尾流" + particledata.replace("REDSTONE", "");
                    else if (particledata.split("\\(")[0].equals("REDSTONERINGS"))
                        particlename = "紅石粉塵粒子環繞" + particledata.replace("REDSTONERINGS", "");
                    else if (particledata.split("\\(")[0].equals("FLAME"))
                        particlename = "火焰粒子尾流" + particledata.replace("FLAME", "");
                    else if (particledata.split("\\(")[0].equals("FLAMERINGS"))
                        particlename = "火焰粒子環繞" + particledata.replace("FLAMERINGS", "");

                    TextComponent head = new TextComponent(color + particlename + " ");
                    if (enable) {
                        head.addExtra(textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[關閉粒子特效]",
                                "點擊關閉 " + particlename, "/setparticle " + particledata));
                    } else {
                        head.addExtra(textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[開啟粒子特效]",
                                "點擊開啟 " + particlename, "/setparticle " + particledata));
                    }

                    P.spigot().sendMessage(head);

                }
                P.sendMessage("");
            } else if (cmdlable.equals("connect")) {
                util.sendActionbarMessage(P, ChatColor.GOLD + "即將傳送分流，上傳資料中");
                p.upload();
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("ConnectOther");
                        out.writeUTF(p.id);
                        out.writeUTF(args[0]);
                        P.sendPluginMessage(App.this, "BungeeCord", out.toByteArray());
                    }
                }.runTaskLater(this, 20);
            } else if (cmdlable.equals("mainsystem")) {

                // pvp 切換指令
                if (args.length >= 1 && args[0].equals("pvp")) {

                    if (!p.settings.containsKey("pvp")) {
                        p.settings.put("pvp", "true");
                    }

                    if (p.settings.get("pvp").equals("true")) {
                        p.settings.put("pvp", "false");
                        util.sendActionbarMessage(P, ChatColor.GREEN + "PVP 模式 : 關閉");
                        return false;
                    } else {
                        p.settings.put("pvp", "true");
                        util.sendActionbarMessage(P, ChatColor.GREEN + "PVP 模式 : " + ChatColor.RED + "開啟");
                        return false;
                    }

                    // 傳送至競技場
                } else if (args.length == 1 && args[0].equals("FFA")) {
                    P.teleport(new Location(Bukkit.getWorld("FFA"), 206, 7, 1341));

                    // 購買充能熔爐
                } else if (args.length == 1 && args[0].equals("buyPF")) {

                    if (P.getInventory().firstEmpty() == -1) {
                        P.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 工匠大臣 "
                                + ChatColor.DARK_GRAY + " >> " + ChatColor.GRAY + "包包滿了還想領充能熔爐 ? 很六喔");
                    }

                    P.getInventory().addItem(items.PowerFurnace);

                    // 購買充能熔爐
                } else if (args.length == 1 && args[0].equals("buyRTPP")) {

                    if (P.getInventory().firstEmpty() == -1) {
                        P.sendMessage(ChatColor.WHITE + "[" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 領地大臣 "
                                + ChatColor.DARK_GRAY + " >> " + ChatColor.GRAY + "包包滿了還想拿領地傳送點設置卷 ? 很六喔");
                    }
                    P.getInventory().addItem(items.RegionTPPaper);

                    // 樂透指令處理
                } else if (args.length >= 1 && args[0].equals("lottery")) {

                    if (args.length == 1) {
                        lottery.printInfo(P);
                    } else if (args.length == 2 && args[1].equals("buy")) {
                        for (int j = 0; j < 20; j++) {
                            P.sendMessage(" ");
                        }
                        P.sendMessage(ChatColor.WHITE + "樂透一張是 " + ChatColor.YELLOW + "100" + ChatColor.WHITE
                                + " 元，請輸入你要購買的" + ChatColor.YELLOW + "張數" + ChatColor.WHITE + " : ");
                        p.strigns = "lottery,buy";
                        p.ChatingWithNPC = true;
                    } else if (args.length == 3 && args[1].equals("buy")) {

                        if (!isInteger(args[2])) {
                            P.sendMessage(ChatColor.RED + "你必須輸入一個整數!");
                            return false;
                        }

                        float amount = 0.0f;
                        try {
                            amount = Float.parseFloat(args[2]);
                        } catch (NumberFormatException e) {
                            P.sendMessage(ChatColor.RED + "你必須輸入一個數字!");
                            return false;
                        }

                        if (p.reduceBalance((float) amount * 100, "購買大樂透")) {
                            lottery.addlottery(P, amount);
                        } else {
                            P.sendMessage(ChatColor.RED + "你好像沒那麼多錢了");
                            return false;
                        }
                    } else if (args.length == 2 && args[1].equals("getresult")) {
                        if (p.id.equals("ken20001207")) {
                            lottery.getLotteryResult();
                        }
                    }

                } else if (args.length == 1 && args[0].equals("phone")) {
                    open_phone(P);
                } else if (args.length == 2 && args[0].equals("baha") && args[1].equals("0")) {
                    Connection conn = null;
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        String datasource = "jdbc:mysql://localhost/mcserver?user=userdatauploader&password=Ken3228009!&useSSL=false";
                        conn = DriverManager.getConnection(datasource);
                        Statement st = conn.createStatement();
                        st.execute("UPDATE `userdata` SET `baha`=1 WHERE `id`='" + senderp.getName() + "'");
                        util.sendActionbarMessage(senderp, "謝謝你的支持!");
                        conn.close();
                        return false;
                    } catch (Exception exc) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + exc.getMessage());
                    }
                }
                if (name.equals("ken20001207")) {
                    if (args[0].equals("getmissionpaper")) {
                        senderp.getInventory().addItem(items.MissionPaper);
                    } else if (args[0].equals("moveallplayertoserver")) {

                        if (!args[1].contains("server")) {
                            senderp.sendMessage("/mainsystem moveallplayertoserver <server> <reason>");
                            return false;
                        }

                        for (Player pa : Bukkit.getOnlinePlayers()) {
                            pa.sendTitle(ChatColor.GOLD + "注意", "即將進行分流傳送，原因 : " + args[2], 20, 300, 20);
                            players.get(pa.getName()).upload();
                        }
                    } else if (args[0].equals("getafk")) {
                        senderp.sendMessage(Integer.toString(players.get("ken20001207").afk));
                    } else if (args[0].equals("getbook")) {
                        senderp.getInventory().addItem(items.booklist.get(args[1]));
                    } else if (args[0].equals("get充能熔爐")) {
                        ItemStack stack = new ItemStack(Material.FURNACE, 1);
                        ItemMeta im = stack.getItemMeta();
                        im.setDisplayName(ChatColor.WHITE + "充能熔爐");
                        im.setLore(Arrays.asList("", ChatColor.GRAY + "能夠幫特殊附魔道具充能的熔爐，",
                                ChatColor.GRAY + "每位稱職的工匠家中都必須有一個。", ""));
                        stack.setItemMeta(im);
                        stack.setAmount(64);
                        senderp.getInventory().addItem(new ItemStack(stack));
                    } else if (args[0].equals("getfamilypaper")) {
                        ItemStack stack = new ItemStack(Material.PAPER, 1);
                        ItemMeta im = stack.getItemMeta();
                        im.setDisplayName(ChatColor.DARK_PURPLE + "家族創立卷");
                        im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵即可創立家族，",
                                ChatColor.WHITE + "使用本卷的玩家將成為家族長，且該玩家必須是當前隊伍的隊長",
                                ChatColor.WHITE + "在該玩家的隊伍中的其他成員將成為家族成員，且人數必須為10人。"));
                        stack.setItemMeta(im);

                        senderp.getInventory().addItem(new ItemStack(stack));
                    } else if (args[0].equals("getfamilyinvite")) {
                        ItemStack stack = new ItemStack(Material.PAPER, 1);
                        ItemMeta im = stack.getItemMeta();
                        im.setDisplayName(ChatColor.DARK_PURPLE + "家族邀請函 : " + args[1]);
                        im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵加入家族 " + args[1],
                                ChatColor.WHITE + "(無法在傳送小鎮使用)"));
                        stack.setItemMeta(im);

                        senderp.getInventory().addItem(new ItemStack(stack));
                    }

                } else {
                    util.sendActionbarMessage(senderp, ChatColor.RED + "你不能用這個指令");
                }
            } else if (cmdlable.equals("setdata")) {

                if (name.equals("ken20001207")) {

                    if (args.length == 1 && args[0].equals("setspawn")) {

                        serverdatas.put("spawn_world", senderp.getLocation().getWorld().getName());
                        serverdatas.put("spawn_x", Integer.toString(senderp.getLocation().getBlockX()));
                        serverdatas.put("spawn_y", Integer.toString(senderp.getLocation().getBlockY()));
                        serverdatas.put("spawn_z", Integer.toString(senderp.getLocation().getBlockZ()));
                        util.sendActionbarMessage(senderp, ChatColor.GREEN + "伺服重生點設為 " + senderp.getLocation());
                        return false;
                    }

                    // Debug 指令 : 直接完成創立家族任務
                    if (args.length == 2 && args[0].equals("compelete")) {

                        if (!families.containsKey(args[1])) {
                            util.sendActionbarMessage(senderp, ChatColor.RED + "未知的家族");
                            return false;
                        }

                        family f = families.get(args[1]);

                        if (families.get(args[1]).compelete) {
                            util.sendActionbarMessage(senderp, ChatColor.RED + "該家族已經啟用");
                            return false;
                        }

                        // 啟用家族
                        f.compelete = true;

                        // 刪除創立家族任務 (如果有)
                        if (f.missions.containsKey("創立家族任務")) {
                            f.missions.remove("創立家族任務");
                        }

                        // 上傳家族資料
                        f.upload();

                        // 一秒後同步其他分流
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    PMH.SendPluginMessage("mainsystem_downloadfamily",
                                            "%family%" + f.name + "%family%");
                                } catch (Exception e) {

                                }
                            }
                        }.runTaskLater(this, 20);

                        util.sendActionbarMessage(senderp, ChatColor.GREEN + "設定成功");

                    }

                    if (args.length == 3) {
                        Player targetplayer = Bukkit.getPlayerExact(args[0]);
                        if (targetplayer == null) {
                            util.sendActionbarMessage(senderp, ChatColor.RED + ">> 該玩家不在線上");
                            return false;
                        } else {
                            player player = players.get(args[0]);
                            if (args[1].equals("lv"))
                                player.lv = Integer.parseInt(args[2]);
                            else if (args[1].equals("class"))
                                player.cla = args[2];
                            else if (args[1].equals("exp"))
                                player.exp = Integer.parseInt(args[2]);
                            else if (args[1].equals("balance"))
                                player.balance = Float.parseFloat(args[2]);
                            else if (args[1].equals("bow"))
                                player.bow = Integer.parseInt(args[2]);
                            else if (args[1].equals("iron"))
                                player.iron = Integer.parseInt(args[2]);
                            else if (args[1].equals("sword"))
                                player.sword = Integer.parseInt(args[2]);
                            else if (args[1].equals("magic"))
                                player.magic = Integer.parseInt(args[2]);
                            else if (args[1].equals("ripe"))
                                player.ripe = Integer.parseInt(args[2]);
                            else if (args[1].equals("fish"))
                                player.fish = Integer.parseInt(args[2]);
                            else if (args[1].equals("wood"))
                                player.wood = Integer.parseInt(args[2]);
                            else if (args[1].equals("trade"))
                                player.trade = Integer.parseInt(args[2]);
                        }

                        util.sendActionbarMessage(senderp, ChatColor.GREEN + " 已將 " + targetplayer.getDisplayName()
                                + " 的 " + args[1] + " 設為 " + args[2]);

                        ReloadPlayerData(targetplayer.getName());
                    }
                } else
                    util.sendActionbarMessage(senderp, ChatColor.RED + "本指令為系統工程師測試使用。");

                // 交易所
            } else if (cmdlable.equals("shop")) {

                if (args.length == 0) {
                    senderp.sendMessage(ChatColor.RED + "這個指令不是給你這樣用的，去峽谷找交易所大臣吧?");
                    return false;
                }

                if (args[0].equals("sell")) {

                    if (args.length == 1) {
                        P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 交易大臣" + ChatColor.DARK_GRAY
                                + " >> " + ChatColor.WHITE + " 你要賣掉幾個你手上的東西呢?");
                        p.ChatingWithNPC = true;
                        p.strigns = "shop,sell";
                        P.spigot().sendMessage(textcomp("[取消對話]", "點擊取消對話", "/chat cancel"));
                        return false;
                    }

                    if (args.length == 2) {
                        P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 交易大臣" + ChatColor.DARK_GRAY
                                + " >> " + ChatColor.WHITE + " 一個你手上的東西要多少錢呢?");
                        p.ChatingWithNPC = true;
                        p.strigns = "shop,sell," + args[1];
                        P.spigot().sendMessage(textcomp("[取消對話]", "點擊取消對話", "/chat cancel"));
                        return false;
                    }
                    Double price = 0.0;
                    try {
                        price = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        return false;
                    }

                    if (!isInteger(args[1]) || price <= 0) {
                        return false;
                    }

                    Connection conn = null;
                    int id = -1;

                    ItemStack stackinhand = P.getInventory().getItemInMainHand();
                    ItemStack thestack = new ItemStack(stackinhand);

                    if (stackinhand.getType().equals(Material.AIR)) {
                        P.sendMessage(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 交易大臣" + ChatColor.DARK_GRAY
                                + " >> " + ChatColor.WHITE + "嗯? 你手上是空的呀 @_@ 把你要賣的東西放在手上!");
                        return false;
                    }

                    int amount = Integer.parseInt(args[1]);

                    for (int i = 0; i <= 35; i++) {
                        ItemStack temp = P.getInventory().getItem(i);
                        if (temp != null && temp.getItemMeta().toString().equals(thestack.getItemMeta().toString())
                                && temp.getType().toString().equals(thestack.getType().toString())) {
                            while (P.getInventory().getItem(i) != null && amount > 0) {
                                P.getInventory().getItem(i).setAmount(P.getInventory().getItem(i).getAmount() - 1);
                                amount--;
                            }
                        }
                    }

                    if (amount == Integer.parseInt(args[1])) {
                        P.sendMessage("你的身上沒有這個商品");
                        return false;
                    }

                    if (amount > 0) {
                        P.sendMessage("你的身上沒有那麼多該物品，因此只賣出了 " + (Integer.parseInt(args[1]) - amount) + " 個");
                    }

                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        String datasource = "jdbc:mysql://localhost/mcserver?user=shopuploader&password=Ken3228009!&useSSL=false";
                        conn = DriverManager.getConnection(datasource);
                        Statement st = conn.createStatement();
                        st.execute("SELECT * FROM `shop_sell` ORDER BY `id` DESC LIMIT 1");
                        ResultSet rs = st.getResultSet();
                        while (rs.next()) {
                            id = rs.getInt("id") + 1;
                        }
                        Statement st2 = conn.createStatement();
                        st2.execute("INSERT INTO `shop_sell` (id, owner, data, amount, price, time) VALUES ( '" + id
                                + "', '" + senderp.getName() + "', '" + itemStackToBase64(thestack) + "',"
                                + (Integer.parseInt(args[1]) - amount) + " , " + args[2] + ",'" + util.now() + "' );");
                        shop_sell.put(id, new shop_sell_item(id));
                        PMH.SendPluginMessage("mainsystem_downloadshop", "%shop%" + id + "%shop%");
                        conn.close();
                        Bukkit.getConsoleSender().sendMessage(
                                ChatColor.GREEN + "[交易所] 玩家 " + senderp.getName() + " 上架了一個商品 編號 " + id + " 到交易所販售了");
                        TextComponent msg;

                        // 設定商品的顯示名稱 (有自訂義名稱的道具顯示自訂義名稱)
                        String itemname = "";
                        if (thestack.getItemMeta().getDisplayName().length() > 1) {
                            itemname = thestack.getItemMeta().getDisplayName();
                        } else {
                            itemname = util.getNameInChinese(thestack.getType().toString());
                        }

                        // 發送訊息給賣家
                        util.sendActionbarMessage(P,
                                ChatColor.GREEN + "成功將 " + args[1] + " 個 " + itemname + ChatColor.GREEN + " 上架至交易所販售");

                        // 發送訊息給全伺服頻道
                        msg = new TextComponent(
                                " [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] 交易大臣" + ChatColor.DARK_GRAY
                                        + " >>  " + ChatColor.GRAY + senderp.getDisplayName() + ChatColor.GRAY + " 剛剛把 "
                                        + (Integer.parseInt(args[1]) - amount) + " 個 " + itemname + ChatColor.GRAY + " "
                                        + ChatColor.GRAY + "上架到販賣交易所囉," + ChatColor.GRAY + " 一個只賣 " + args[2] + " 元!");
                        chatchannels.get("全伺服頻道").sendmessage(msg);

                        // 發送訊息到 Discord
                        shopbot.selltoShopChannel(itemname, P.getName(), (Integer.parseInt(args[1]) - amount),
                                Double.parseDouble(args[2]));

                    } catch (Exception exc) {
                        Bukkit.getConsoleSender()
                                .sendMessage(ChatColor.RED + "[MySQL Error] when sell item : " + exc.getMessage());
                    }

                    // 自交易所購買物品
                } else if (args[0].equals("buy")) {

                    if (args.length == 2) {
                        openshop_sell(P, Integer.parseInt(args[1]));
                        return false;
                    }

                    if (!shop_sell.containsKey(Integer.parseInt(args[1]))) {
                        util.sendActionbarMessage(P, ChatColor.GOLD + "" + ChatColor.BOLD + "找不到這個商品");
                        return false;
                    }

                    shop_sell_item item = shop_sell.get(Integer.parseInt(args[1]));

                    if (Integer.parseInt(args[2]) > item.getamount()) {
                        util.sendActionbarMessage(P, ChatColor.GOLD + "" + ChatColor.BOLD + "這個商品現在沒有那麼多庫存囉");
                        return false;
                    }

                    if (Integer.parseInt(args[2]) <= 0) {
                        util.sendActionbarMessage(P, ChatColor.GOLD + "" + ChatColor.BOLD + "無效的數量");
                        return false;
                    }

                    if (P.getInventory().firstEmpty() == -1) {
                        util.sendActionbarMessage(P, ChatColor.GOLD + "" + ChatColor.BOLD + "您的背包已經滿了");
                        return false;
                    }

                    if (p.reduceBalance(item.price * Float.parseFloat(args[2]),
                            "以單價 " + item.price + " 元向 " + item.owner + " 購買 " + Integer.parseInt(args[2]) + " 個 "
                                    + util.getNameInChinese(item.item.getType().toString()))) {

                        // 透過銀行系統轉帳
                        util.bankTransfer(P.getName(), item.owner, item.price * Double.parseDouble(args[2]),
                                "以單價 " + item.price + " 元購買 " + Integer.parseInt(args[2]) + " 個 "
                                        + util.getNameInChinese(item.item.getType().toString()),
                                false);

                        // 匯款一秒後重新整理收款玩家資料
                        new BukkitRunnable() {
                            public void run() {
                                ReloadPlayerData(item.owner);
                            }
                        }.runTaskLater(this, 20);

                        String newstack64 = itemStackToBase64(item.item);
                        ItemStack newstack = itemStackFromBase64(newstack64);
                        newstack.setAmount(Integer.parseInt(args[2]));
                        senderp.getInventory().addItem(new ItemStack(newstack));
                        P.updateInventory();
                        item.amount = item.amount - Integer.parseInt(args[2]);
                        int id = item.id;

                        // 上傳更新後的商品資料， 5 ticks 之後同步其他分流
                        datasync.upload_shop(id);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    PMH.SendPluginMessage("mainsystem_downloadshop",
                                            "%shop%" + shop_sell.get(id).id + "%shop%");
                                } catch (Exception exc) {

                                }
                            }
                        }.runTaskLater(this, 5);

                        // 設定商品的顯示名稱 (有自訂義名稱的道具顯示自訂義名稱)
                        String itemname = "";
                        if (item.item.getItemMeta().getDisplayName().length() > 1) {
                            itemname = item.item.getItemMeta().getDisplayName();
                        } else {
                            itemname = util.getNameInChinese(item.item.getType().toString());
                        }

                        // 發送訊息到 discord
                        shopbot.shoppinginShopChannel(itemname, item.owner, P.getName(), Integer.parseInt(args[2]),
                                item.price, item.amount);

                        // 賣完的話從資料庫刪除
                        if (item.amount <= 0) {
                            try {
                                Connection conn = null;
                                Class.forName("com.mysql.jdbc.Driver");
                                String datasource = "jdbc:mysql://localhost/mcserver?user=shopuploader&password=Ken3228009!&useSSL=false";
                                conn = DriverManager.getConnection(datasource);
                                Statement st2 = conn.createStatement();
                                st2.execute("DELETE FROM `shop_sell` WHERE `id`=" + item.id);
                                shop_sell.remove(item.id);
                                conn.close();
                                Bukkit.getConsoleSender()
                                        .sendMessage(ChatColor.GREEN + "[交易所] 交易所商品編號 " + item.id + " 已經賣完了，從資料庫刪除");
                            } catch (Exception exc) {
                                Bukkit.getConsoleSender().sendMessage(
                                        ChatColor.RED + "[MySQL Error] when sell item : " + exc.getMessage());
                            }

                            // 20 ticks 之後同步其他分流
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    try {
                                        PMH.SendPluginMessage("mainsystem_downloadshop", "%shop%" + id + "%shop%");
                                    } catch (Exception exc) {

                                    }
                                }
                            }.runTaskLater(this, 20);
                        }

                    }

                }

            } else if (cmdlable.equals("pay")) {

                if (args.length != 2)
                    return false;

                if (isInteger(args[1])) {
                    if (Integer.parseInt(args[1]) <= 0)
                        return false;
                    if (p.reduceBalance(Float.parseFloat(args[1]), "pay 指令轉帳 (來自" + senderp.getName() + ")")) {

                        // 透過銀行跨分流轉帳
                        util.bankTransfer(senderp.getName(), args[0], Double.parseDouble(args[1]), "pay 指令轉帳", false);

                        // 匯款一秒後重新整理收款玩家資料
                        new BukkitRunnable() {
                            public void run() {
                                ReloadPlayerData(args[0]);
                            }
                        }.runTaskLater(this, 20);

                    }
                }

            } else if (cmdlable.equals("setdisplayname")) {
                if (args.length == 0) {
                    senderp.sendMessage("請選擇你要顯示的稱號: ");
                    for (String s : players.get(senderp.getName()).cla.split(",")) {
                        senderp.spigot()
                                .sendMessage(textcomp("[" + s + "]", "點擊將自己的稱號設為 " + s, "/setdisplayname " + s));
                    }
                } else if (args.length == 1) {
                    for (String s : players.get(senderp.getName()).cla.split(",")) {
                        if (s.contains(args[0])) {
                            players.get(senderp.getName()).displaycla = s;
                            reloaddata(senderp.getName());
                            return false;
                        }
                    }

                    util.sendActionbarMessage(senderp, ChatColor.RED + "原來你有這個稱號喔?! 你很厲害捏");
                    return false;
                }
            } else if (cmdlable.equals("spawn")) {

                p.setPlayerBackLocation(P.getLocation());

                if (getConfig().getInt("server") == 0) {

                    P.sendTitle(ChatColor.YELLOW + "SMD:Kingdom 傳送小鎮", ChatColor.GRAY + "世界的中心點，各地往來的必經之地", 20, 200,
                            20);
                    P.teleport(new Location(Bukkit.getWorld("spawn"), 0, 64, 142));
                    Bukkit.getWorld("spawn").spawnParticle(Particle.PORTAL,
                            new Location(Bukkit.getWorld("spawn"), 0, 64, 142).add(0, 1, 0), 100);
                    return false;

                }

                senderp.teleport(new Location(Bukkit.getWorld(serverdatas.get("spawn_world")),
                        Integer.parseInt(serverdatas.get("spawn_x")), Integer.parseInt(serverdatas.get("spawn_y")),
                        Integer.parseInt(serverdatas.get("spawn_z"))));
                Bukkit.getWorld(serverdatas.get("spawn_world")).spawnParticle(Particle.PORTAL,
                        new Location(Bukkit.getWorld(serverdatas.get("spawn_world")), 0, 64, 142).add(0, 1, 0), 100);
            } else if (cmdlable.equals("tpa")) {

                if (args.length == 0) {
                    senderp.sendMessage(ChatColor.RED + "偵測到非法存取: 請不要用輸入方式使用該指令。");
                    return false;
                }

                Player targetplayer = Bukkit.getPlayerExact(args[0]);
                if (targetplayer == null) {
                    senderp.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColor.RED + ">> 該玩家不在線上"));
                    return false;
                } else if (players.get(targetplayer.getName()).tprequest.contains(senderp.getName())) {
                    senderp.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColor.RED + ">> 該玩家已經有你的傳送請求尚未回復。"));
                    return false;
                } else {
                    TextComponent head = new TextComponent(">> ");
                    head.toPlainText();
                    TextComponent msg = new TextComponent(ChatColor.WHITE + " 請求傳送至你的身邊 , " + ChatColor.WHITE);
                    TextComponent accept = new TextComponent(ChatColor.UNDERLINE + "[ 點擊接受 ]" + ChatColor.RESET + " ");
                    accept.setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊接受傳送請求").create()));
                    accept.setClickEvent(
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + senderp.getName()));
                    TextComponent deny = new TextComponent(ChatColor.UNDERLINE + "[ 點擊拒絕 ]");
                    deny.setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊拒絕傳送請求").create()));
                    deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + senderp.getName()));
                    head.addExtra(getnamehover(senderp.getName(), true, false));
                    head.addExtra(msg);
                    head.addExtra(accept);
                    head.addExtra(deny);
                    targetplayer.spigot().sendMessage(head);
                    players.get(targetplayer.getName()).tprequest.add(senderp.getName());
                    senderp.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent
                            .fromLegacyText(ChatColor.GREEN + "已經發送傳送請求至 " + targetplayer.getDisplayName()));
                }
            } else if (cmdlable.equals("tpaccept")) {

                if (args.length != 1) {
                    senderp.sendMessage(ChatColor.RED + "偵測到非法存取: 請不要用輸入方式使用該指令。");
                    return false;
                }

                Player targetplayer = Bukkit.getPlayerExact(args[0]);
                if (p.tprequest.contains(args[0])) {
                    if (targetplayer == null) {
                        senderp.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(ChatColor.RED + ">> 該玩家不在線上"));
                        return false;
                    } else {
                        targetplayer.teleport(senderp.getLocation());
                        senderp.getLocation().getWorld().spawnParticle(Particle.PORTAL,
                                senderp.getLocation().add(0, 1, 0), 100);
                        p.tprequest.remove(args[0]);
                        util.sendActionbarMessage(senderp, ChatColor.GREEN + ">> 已經接受 " + targetplayer.getDisplayName()
                                + ChatColor.GREEN + " 傳送請求");
                        util.sendActionbarMessage(targetplayer,
                                ChatColor.GREEN + ">> 你對 " + senderp.getDisplayName() + ChatColor.GREEN + " 的傳送請求已被接受");
                        return false;
                    }
                } else {
                    senderp.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColor.RED + ">> 本傳送請求已經過期"));
                }
            } else if (cmdlable.equals("tpdeny")) {

                if (args.length != 1) {
                    P.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "請不要用輸入的方式使用指令喔 OuOb");
                    return false;
                }

                Player targetplayer = Bukkit.getPlayer(args[0]);
                if (p.tprequest.contains(args[0])) {
                    if (targetplayer == null) {
                        senderp.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(ChatColor.RED + ">> 該玩家不在線上"));
                        return false;
                    } else {
                        util.sendActionbarMessage(senderp, ChatColor.GREEN + ">> 已經拒絕 " + targetplayer.getDisplayName()
                                + ChatColor.GREEN + " 傳送請求");
                        util.sendActionbarMessage(targetplayer,
                                ChatColor.RED + ">> 你對 " + senderp.getDisplayName() + ChatColor.RED + " 的傳送請求已被拒絕");
                        p.tprequest.remove(args[0]);
                    }
                } else {
                    senderp.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColor.RED + ">> 本傳送請求已經過期"));
                }
            } else if (cmdlable.equals("getflag")) {

                if (name.equals("ken20001207")) {
                    ItemStack stack = new ItemStack(LEGACY_STANDING_BANNER, 1);
                    ItemMeta im = stack.getItemMeta();
                    im.setDisplayName(ChatColor.DARK_PURPLE + "領地旗幟: " + ChatColor.WHITE + args[0]);
                    im.setLore(Arrays.asList("", ChatColor.WHITE + "將本旗幟插入地面來宣告領地。", "",
                            ChatColor.GRAY + "領地名稱: " + ChatColor.WHITE + args[0],
                            ChatColor.GRAY + "領主名稱: " + ChatColor.WHITE + args[1],
                            ChatColor.GRAY + "領地邊長: " + ChatColor.WHITE + args[2], ChatColor.GRAY + "領地大小: "
                                    + ChatColor.WHITE + (Integer.parseInt(args[2]) * Integer.parseInt(args[2]) + "")));
                    stack.setItemMeta(im);

                    senderp.getInventory().addItem(new ItemStack(stack));
                } else {
                    util.sendActionbarMessage(senderp, ChatColor.RED + "本指令為系統工程師測試使用。");
                }
            } else if (cmdlable.equals("tpalist")) {
                senderp.sendMessage("目前未處理的傳送請求: ");
                for (String id : players.get(senderp.getName()).tprequest) {
                    senderp.sendMessage(Bukkit.getPlayer(id).getDisplayName());
                }
            } else if (cmdlable.equals("chat")) {
                if (args.length == 0) {
                    return false;
                } else if (args.length == 1) {
                    if (args[0].equals("cancel")) {
                        players.get(senderp.getName()).strigns = "";
                        players.get(senderp.getName()).ChatingWithNPC = false;
                        util.sendActionbarMessage(senderp, "與 NPC 的對話已經結束");
                    }
                } else if (args.length == 2) {

                    // NPC 進階對話
                    if (args[0].equals("農夫")) {
                        if (args[1].equals("5")) {
                            senderp.performCommand("class 農夫");
                            return false;
                        }
                    } else if (args[0].equals("礦工")) {
                        if (args[1].equals("5")) {
                            senderp.performCommand("class 礦工");
                            return false;
                        }
                    } else if (args[0].equals("工匠")) {
                        if (args[1].equals("5")) {
                            senderp.performCommand("class 工匠");
                            return false;
                        } else if (args[1].equals("9")) {
                            P.performCommand("mainsystem buyPF");
                        }
                    } else if (args[0].equals("商人")) {
                        if (args[1].equals("5")) {
                            senderp.performCommand("class 商人");
                            return false;
                        }
                    } else if (args[0].equals("劍士")) {
                        if (args[1].equals("5")) {
                            senderp.performCommand("class 劍士");
                            return false;
                        }
                    } else if (args[0].equals("弓手")) {
                        if (args[1].equals("5")) {
                            senderp.performCommand("class 弓手");
                            return false;
                        }
                    } else if (args[0].equals("木工")) {
                        if (args[1].equals("5")) {
                            senderp.performCommand("class 木工");
                            return false;
                        }
                    } else if (args[0].equals("漁夫")) {
                        if (args[1].equals("5")) {
                            senderp.performCommand("class 漁夫");
                            return false;
                        }
                    } else if (args[0].equals("家族總管")) {
                        if (args[1].equals("8")) {
                            senderp.performCommand("family");
                            return false;
                        } else if (args[1].equals("4")) {
                            Inventory inv = Bukkit.createInventory(null, 9, "可用家族道具");

                            List<ItemStack> list = new ArrayList<>();
                            ItemStack stack1 = new ItemStack(Material.PAPER, 1);
                            ItemMeta im = stack1.getItemMeta();
                            im.setDisplayName(ChatColor.DARK_PURPLE + "家族創立卷");
                            im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵即可創立家族，",
                                    ChatColor.WHITE + "使用本卷的玩家將成為家族長，且該玩家必須是當前隊伍的隊長",
                                    ChatColor.WHITE + "在該玩家的隊伍中的其他成員將成為家族成員，且人數必須為10人。", "", "",
                                    ChatColor.AQUA + "商品定價: " + ChatColor.WHITE + "$ 50000"));
                            stack1.setItemMeta(im);
                            if (p.family.equals("null"))
                                list.add(stack1);

                            ItemStack stack2 = new ItemStack(Material.PAPER, 1);
                            im = stack2.getItemMeta();
                            im.setDisplayName(ChatColor.DARK_PURPLE + "家族邀請函 : " + p.family);
                            im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵加入家族 " + p.family,
                                    ChatColor.WHITE + "(無法在傳送小鎮使用)", "",
                                    ChatColor.AQUA + "商品需求 " + ChatColor.WHITE + "一組小麥"));
                            stack2.setItemMeta(im);
                            if (!p.family.equals("null") && (p.cla.contains("家族代表") || p.cla.contains("家族幹部")))
                                list.add(stack2);

                            int i = 0;
                            for (ItemStack item : list) {
                                inv.setItem(i, item);
                                i++;
                            }

                            P.openInventory(inv);
                            return false;
                        }
                    }

                    for (int j = 0; j < 20; j++) {
                        P.sendMessage(" ");
                    }

                    TextComponent head = new TextComponent("");
                    head.toPlainText();
                    TextComponent msg = new TextComponent(" [" + ChatColor.AQUA + "大臣" + ChatColor.WHITE + "] "
                            + getConfig().getString("NPCchats." + args[0] + ".name") + " §8>> " + ChatColor.GRAY
                            + getConfig().getString("NPCchats." + args[0] + "." + args[1] + ".msg"));
                    head.addExtra(msg);
                    senderp.spigot().sendMessage(head);

                    senderp.sendMessage("\n");

                    TextComponent action = new TextComponent("");
                    action.toPlainText();

                    for (int i = 1; i <=

                            getConfig().getInt("NPCchats." + args[0] + "." + args[1] + ".next.number"); i++) {

                        TextComponent actiontemp = new TextComponent(" " + ChatColor.UNDERLINE + "["
                                + getConfig().getString("NPCchats." + args[0] + "." + args[1] + ".next." + i + ".msg")
                                + "]" + ChatColor.RESET + " ");
                        actiontemp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chat " + args[0] + " "
                                + getConfig().getString("NPCchats." + args[0] + "." + args[1] + ".next." + i + ".to")));
                        actiontemp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("和 " + getConfig().getString("NPCchats." + args[0] + ".name")
                                        + ChatColor.WHITE + " 說 \""
                                        + getConfig().getString(
                                                "NPCchats." + args[0] + "." + args[1] + ".next." + i + ".msg")
                                        + "\"").create()));
                        action.addExtra(actiontemp);
                    }

                    senderp.spigot().sendMessage(action);
                    senderp.sendMessage("\n");

                    if (args[0].equals("3")) {
                        if (args[1].equals("4")) {
                            players.get(senderp.getName()).ChatingWithNPC = true;
                            players.get(senderp.getName()).strigns += "buyregion,";
                        }
                        if (args[1].equals("1")) {
                            players.get(senderp.getName()).strigns = "";
                            players.get(senderp.getName()).ChatingWithNPC = false;
                        }
                        if (args[1].equals("8")) {
                            P.performCommand("mainsystem buyRTPP");
                        }
                    }

                }

            } else if (cmdlable.equals("buyregion")) {

                if (args.length != 2 || !isInteger(args[1])) {
                    senderp.sendMessage(ChatColor.RED + "偵測到非法存取: 請不要用輸入方式使用該指令。");
                    return false;
                }

                if (senderp.getInventory().firstEmpty() == -1) {
                    TextComponent head = new TextComponent("");
                    head.toPlainText();
                    TextComponent msg = new TextComponent(" " + ChatColor.WHITE + "[" + ChatColor.AQUA + "大臣"
                            + ChatColor.WHITE + "] 領地大臣 §8>> " + ChatColor.GRAY + "你的包包已經滿了= = 我要怎麼給你領地旗幟? \n\n");
                    head.addExtra(msg);
                    senderp.spigot().sendMessage(head);
                    senderp.performCommand("chat cancel");
                    return false;
                }

                if (p.getPlayerRegionAvailible() - p.getUsingRegionSize() >= Integer.parseInt(args[1])
                        * Integer.parseInt(args[1])) {
                    TextComponent head = new TextComponent("");
                    head.toPlainText();
                    TextComponent msg = new TextComponent(" " + ChatColor.WHITE + "[" + ChatColor.AQUA + "大臣"
                            + ChatColor.WHITE + "] 領地大臣 §8>> " + ChatColor.GRAY + "申請成功! 快拿你的旗幟去宣告領地吧~ \n\n");
                    head.addExtra(msg);
                    senderp.spigot().sendMessage(head);
                    senderp.performCommand("chat cancel");

                    ItemStack stack = new ItemStack(Material.LEGACY_STANDING_BANNER, 1);
                    ItemMeta im = stack.getItemMeta();
                    im.setDisplayName(ChatColor.DARK_PURPLE + "領地旗幟: " + ChatColor.WHITE + args[0]);
                    im.setLore(Arrays.asList("", ChatColor.WHITE + "將本旗幟插入地面來宣告領地。", "",
                            ChatColor.GRAY + "領地名稱: " + ChatColor.WHITE + args[0],
                            ChatColor.GRAY + "領主名稱: " + ChatColor.WHITE + senderp.getName(),
                            ChatColor.GRAY + "領地邊長: " + ChatColor.WHITE + args[1], ChatColor.GRAY + "領地大小: "
                                    + ChatColor.WHITE + (Integer.parseInt(args[1]) * Integer.parseInt(args[1]) + "")));
                    stack.setItemMeta(im);

                    senderp.getInventory().addItem(new ItemStack(stack));

                } else {
                    TextComponent head = new TextComponent("");
                    head.toPlainText();
                    TextComponent msg = new TextComponent(" " + ChatColor.WHITE + "[" + ChatColor.AQUA + "大臣"
                            + ChatColor.WHITE + "] 領地大臣 §8>> " + ChatColor.GRAY + "太大塊啦 OAO! 你現在只剩下 "
                            + (p.getPlayerRegionAvailible() - p.getUsingRegionSize()) + " 格的可用領地格數了，玩越久格數越多喔!\n\n");
                    head.addExtra(msg);
                    senderp.spigot().sendMessage(head);
                    senderp.performCommand("chat cancel");
                }

            } else if (cmdlable.equals("class")) {

                player player = players.get(senderp.getName());
                Player Player = senderp;

                if (args.length == 1) {

                    if (args[0].equals("農夫")) {
                        String header = "農夫大臣 " + ChatColor.DARK_GRAY + ">> " + ChatColor.WHITE + " ";
                        if (player.cla.contains("無業者")) {
                            if (player.lv >= 10) {
                                if (player.ripe >= 1000) {
                                    Player.sendMessage(header + "歡迎加入農夫的行列， " + Player.getName());
                                    player.cla = player.cla.replace("無業者", "農夫");
                                    player.lv = 1;
                                    player.exp = 0;
                                    if (player.displaycla.equals("無業者")) {
                                        player.displaycla = "農夫";
                                        player.clacolor = ChatColor.DARK_GREEN;
                                    }
                                    Player.sendTitle(ChatColor.GREEN + "轉職成功!", "你已經成為農夫了!", 20, 100, 20);
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[Player active] "
                                            + player.id + " has been change class to 農夫");
                                    serverdatas.put("num_農夫",
                                            Integer.toString(Integer.parseInt(serverdatas.get("num_農夫")) + 1));

                                    reloaddata(player.id);

                                } else
                                    Player.sendMessage(header + "小夥子，你真的已經知道怎麼種菜了嗎? (種植熟練度不足)");
                            } else
                                Player.sendMessage(header + "你現在想成為農夫還太急了呢，再多生活一陣子吧。 (等級不足)");
                        } else
                            Player.sendMessage(header + "你已經有工作了呀。");
                    }

                    else if (args[0].equals("礦工")) {

                        String header = "礦工大臣 " + ChatColor.DARK_GRAY + ">> " + ChatColor.WHITE + " ";
                        if (player.cla.contains("無業者")) {
                            if (player.lv >= 10) {
                                if (player.iron >= 10000) {
                                    Player.sendMessage(header + "歡迎加入礦工的行列， " + Player.getName());
                                    player.cla = player.cla.replace("無業者", "礦工");
                                    player.lv = 1;
                                    player.exp = 0;
                                    if (player.displaycla.equals("無業者")) {
                                        player.displaycla = "礦工";
                                        player.clacolor = ChatColor.DARK_GREEN;
                                    }
                                    Player.sendTitle(ChatColor.GREEN + "轉職成功!", "你已經成為礦工了!", 20, 100, 20);
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[Player active] "
                                            + player.id + " has been change class to 礦工");
                                    serverdatas.put("num_礦工",
                                            Integer.toString(Integer.parseInt(serverdatas.get("num_礦工")) + 1));
                                    reloaddata(player.id);

                                } else
                                    Player.sendMessage(header + "小夥子，你真的已經知道怎麼挖礦了嗎? (挖礦熟練度不足)");
                            } else
                                Player.sendMessage(header + "你現在想成為礦工還太急了呢，再多生活一陣子吧。 (等級不足)");
                        } else
                            Player.sendMessage(header + "你已經有工作了呀。");
                    }

                    else if (args[0].equals("工匠")) {
                        String header = "工匠大臣 " + ChatColor.DARK_GRAY + ">> " + ChatColor.WHITE + " ";
                        if (player.cla.contains("無業者")) {
                            if (player.lv >= 10) {
                                if (player.magic >= 1000) {
                                    Player.sendMessage(header + "歡迎加入工匠的行列， " + Player.getName());
                                    player.cla = player.cla.replace("無業者", "工匠");
                                    player.lv = 1;
                                    player.exp = 0;
                                    if (player.displaycla.equals("無業者")) {
                                        player.displaycla = "工匠";
                                        player.clacolor = ChatColor.DARK_GREEN;
                                    }
                                    Player.sendTitle(ChatColor.GREEN + "轉職成功!", "你已經成為工匠了!", 20, 100, 20);
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[Player active] "
                                            + player.id + " has been change class to 工匠");
                                    serverdatas.put("num_工匠",
                                            Integer.toString(Integer.parseInt(serverdatas.get("num_工匠")) + 1));
                                    reloaddata(player.id);

                                } else
                                    Player.sendMessage(header + "小夥子，你真的已經知道怎麼加工道具了嗎? (加工熟練度不足)");
                            } else
                                Player.sendMessage(header + "你現在想成為工匠還太急了呢，再多生活一陣子吧。 (等級不足)");
                        } else
                            Player.sendMessage(header + "你已經有工作了呀。");
                    }

                    else if (args[0].equals("弓手")) {
                        String header = "弓手大臣 " + ChatColor.DARK_GRAY + ">> " + ChatColor.WHITE + " ";
                        if (player.cla.contains("無業者")) {
                            if (player.lv >= 10) {
                                if (player.bow >= 1000) {
                                    Player.sendMessage(header + "歡迎加入弓手的行列， " + Player.getName());
                                    player.cla = player.cla.replace("無業者", "弓手");
                                    player.lv = 1;
                                    player.exp = 0;
                                    if (player.displaycla.equals("無業者"))
                                        player.displaycla = "弓手";
                                    player.clacolor = ChatColor.DARK_GREEN;
                                    Player.sendTitle(ChatColor.GREEN + "轉職成功!", "你已經成為弓手了!", 20, 100, 20);
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[Player active] "
                                            + player.id + " has been change class to 弓手");
                                    serverdatas.put("num_弓手",
                                            Integer.toString(Integer.parseInt(serverdatas.get("num_弓手")) + 1));
                                    reloaddata(player.id);

                                } else
                                    Player.sendMessage(header + "小夥子，你真的已經知道怎麼射箭了嗎? (射箭熟練度不足)");
                            } else
                                Player.sendMessage(header + "你現在想成為弓手還太急了呢，再多生活一陣子吧。 (等級不足)");
                        } else
                            Player.sendMessage(header + "你已經有工作了呀。");
                    }

                    else if (args[0].equals("劍士")) {
                        String header = "劍士大臣 " + ChatColor.DARK_GRAY + ">> " + ChatColor.WHITE + " ";
                        if (player.cla.contains("無業者")) {
                            if (player.lv >= 10) {
                                if (player.sword >= 1000) {
                                    Player.sendMessage(header + "歡迎加入劍士的行列， " + Player.getName());
                                    player.cla = player.cla.replace("無業者", "劍士");
                                    player.lv = 1;
                                    player.exp = 0;
                                    if (player.displaycla.equals("無業者")) {
                                        player.displaycla = "劍士";
                                        player.clacolor = ChatColor.DARK_GREEN;
                                    }
                                    Player.sendTitle(ChatColor.GREEN + "轉職成功!", "你已經成為劍士了!", 20, 100, 20);
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[Player active] "
                                            + player.id + " has been change class to 劍士");
                                    serverdatas.put("num_劍士",
                                            Integer.toString(Integer.parseInt(serverdatas.get("num_劍士")) + 1));
                                    reloaddata(player.id);

                                } else
                                    Player.sendMessage(header + "小夥子，你真的已經知道怎麼用劍了嗎? (用劍熟練度不足)");
                            } else
                                Player.sendMessage(header + "你現在想成為劍士還太急了呢，再多生活一陣子吧。 (等級不足)");
                        } else
                            Player.sendMessage(header + "你已經有工作了呀。");
                    }

                    else if (args[0].equals("木工")) {
                        String header = "木工大臣 " + ChatColor.DARK_GRAY + ">> " + ChatColor.WHITE + " ";
                        if (player.cla.contains("無業者")) {
                            if (player.lv >= 10) {
                                if (player.wood >= 5000) {
                                    Player.sendMessage(header + "歡迎加入木工的行列， " + Player.getName());
                                    player.cla = player.cla.replace("無業者", "木工");
                                    player.lv = 1;
                                    player.exp = 0;
                                    if (player.displaycla.equals("無業者")) {
                                        player.displaycla = "木工";
                                        player.clacolor = ChatColor.DARK_GREEN;
                                    }
                                    Player.sendTitle(ChatColor.GREEN + "轉職成功!", "你已經成為木工了!", 20, 100, 20);
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[Player active] "
                                            + player.id + " has been change class to 木工");
                                    serverdatas.put("num_木工",
                                            Integer.toString(Integer.parseInt(serverdatas.get("num_木工")) + 1));
                                    reloaddata(player.id);

                                } else
                                    Player.sendMessage(header + "小夥子，你真的已經知道怎麼伐木了嗎? (伐木熟練度不足)");
                            } else
                                Player.sendMessage(header + "你現在想成為木工還太急了呢，再多生活一陣子吧。 (等級不足)");
                        } else
                            Player.sendMessage(header + "你已經有工作了呀。");
                    }

                    else if (args[0].equals("商人")) {
                        String header = "商人大臣 " + ChatColor.DARK_GRAY + ">> " + ChatColor.WHITE + " ";
                        if (player.cla.contains("無業者")) {
                            if (player.lv >= 10) {
                                if (player.trade >= 1000) {
                                    Player.sendMessage(header + "歡迎加入商人的行列， " + Player.getName());
                                    player.cla = player.cla.replace("無業者", "商人");
                                    player.lv = 1;
                                    player.exp = 0;
                                    if (player.displaycla.equals("無業者")) {
                                        player.displaycla = "商人";
                                        player.clacolor = ChatColor.DARK_GREEN;
                                    }
                                    Player.sendTitle(ChatColor.GREEN + "轉職成功!", "你已經成為商人了!", 20, 100, 20);
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[Player active] "
                                            + player.id + " has been change class to 商人");
                                    serverdatas.put("num_商人",
                                            Integer.toString(Integer.parseInt(serverdatas.get("num_商人")) + 1));
                                    reloaddata(player.id);
                                } else
                                    Player.sendMessage(header + "小夥子，你真的已經知道怎麼貿易了嗎? (貿易熟練度不足)");
                            } else
                                Player.sendMessage(header + "你現在想成為商人還太急了呢，再多生活一陣子吧。 (等級不足)");
                        } else
                            Player.sendMessage(header + "你已經有工作了呀。");
                    }

                    else if (args[0].equals("漁夫")) {
                        String header = "漁夫大臣 " + ChatColor.DARK_GRAY + ">> " + ChatColor.WHITE + " ";
                        if (player.cla.contains("無業者")) {
                            if (player.lv >= 10) {
                                if (player.fish >= 1000) {
                                    Player.sendMessage(header + "歡迎加入漁夫的行列， " + Player.getName());
                                    player.cla = player.cla.replace("無業者", "漁夫");
                                    player.lv = 1;
                                    player.exp = 0;
                                    if (player.displaycla.equals("無業者")) {
                                        player.displaycla = "漁夫";
                                        player.clacolor = ChatColor.DARK_GREEN;
                                    }
                                    Player.sendTitle(ChatColor.GREEN + "轉職成功!", "你已經成為漁夫了!", 20, 100, 20);
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[Player active] "
                                            + player.id + " has been change class to 漁夫");
                                    serverdatas.put("num_漁夫",
                                            Integer.toString(Integer.parseInt(serverdatas.get("num_漁夫")) + 1));
                                    reloaddata(player.id);
                                } else
                                    Player.sendMessage(header + "小夥子，你真的已經知道怎麼貿易了嗎? (貿易熟練度不足)");
                            } else
                                Player.sendMessage(header + "你現在想成為漁夫還太急了呢，再多生活一陣子吧。 (等級不足)");
                        } else
                            Player.sendMessage(header + "你已經有工作了呀。");
                    }

                }
            } else if (cmdlable.equals("party")) {

                player player = players.get(senderp.getName());
                Player Player = senderp;

                if (args.length == 0) {

                    if (p.party.length() == 0) {
                        for (int j = 0; j < 20; j++) {
                            P.sendMessage(" ");
                        }
                        P.sendMessage("您還沒有隊伍，請輸入想要創立的隊伍名稱: ");
                        p.ChatingWithNPC = true;
                        p.strigns = "party,create";
                        P.spigot().sendMessage(textcomp(ChatColor.UNDERLINE + "[點擊取消]", "點擊取消創立隊伍", "/chat cancel"));
                    } else {
                        for (int j = 0; j < 20; j++) {
                            P.sendMessage(" ");
                        }
                        party party = partys.get(p.party);
                        P.sendMessage(
                                ChatColor.WHITE + " < 隊伍資訊 : " + ChatColor.YELLOW + p.party + ChatColor.WHITE + " >");
                        P.sendMessage(ChatColor.WHITE + " | ");
                        P.sendMessage(ChatColor.WHITE + " | 隊長 " + party.cap.getDisplayName());
                        P.sendMessage(ChatColor.WHITE + " | ");
                        P.sendMessage(ChatColor.WHITE + " | 隊伍人數 " + ChatColor.YELLOW + party.member.size()
                                + ChatColor.WHITE + " / 10");
                        P.sendMessage(ChatColor.WHITE + " | ");
                        P.sendMessage(ChatColor.WHITE + " | 隊員列表 : ");
                        P.sendMessage(ChatColor.WHITE + " | ");
                        for (Player PP : party.member) {
                            TextComponent textcomp = new TextComponent(ChatColor.WHITE + " | -> ");
                            textcomp.addExtra(getnamehover(PP.getName(), true, false));
                            P.spigot().sendMessage(textcomp);
                        }
                        P.sendMessage(ChatColor.WHITE + " | ");
                        P.spigot().sendMessage(textcomp(ChatColor.WHITE + " | " + ChatColor.UNDERLINE + "[點擊退出隊伍]",
                                "點擊退出隊伍", "/party quit"));
                        P.spigot().sendMessage(textcomp(ChatColor.WHITE + " | " + ChatColor.UNDERLINE + "[點擊邀請玩家]",
                                "點擊邀請玩家", "/party invite"));
                    }

                }

                if (args.length == 1) {

                    if (args[0].equals("accept")) {

                        if (player.partyrequest.length() == 0) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "你沒有收到隊伍邀請。");
                            return false;
                        }

                        partys.get(players.get(player.partyrequest).party).addmember(Player);
                        player.party = players.get(player.partyrequest).party;
                        player.partyrequest = "";
                        util.sendActionbarMessage(Player, ChatColor.GREEN + "成功加入隊伍。");
                        chatchannels.get(player.party + "隊伍頻道").member.add(P);

                        for (Player pa : partys.get(player.party).member) {
                            util.sendActionbarMessage(pa,
                                    ChatColor.GREEN + "玩家 " + Player.getDisplayName() + ChatColor.GREEN + " 加入隊伍。");
                        }

                    } else if (args[0].equals("deny")) {

                        if (player.partyrequest.length() == 0) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "你沒有收到隊伍邀請。");
                            return false;
                        }

                        util.sendActionbarMessage(Bukkit.getPlayer(player.partyrequest),
                                Player.getDisplayName() + ChatColor.RED + " 拒絕加入隊伍。");

                        player.partyrequest = "";
                        util.sendActionbarMessage(Player, ChatColor.GREEN + "已經拒絕加入隊伍");

                    } else if (args[0].equals("quit")) {

                        if (player.party.length() == 0) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "你沒有在隊伍裡。");
                            return false;
                        }

                        if (Player.getName().equals(partys.get(player.party).cap.getName())) {
                            String partyname = player.party;
                            for (Player pa : partys.get(partyname).member) {
                                util.sendActionbarMessage(pa, ChatColor.RED + "隊伍解散");
                                players.get(pa.getName()).party = "";
                            }
                            partys.remove(partyname);
                            chatchannels.remove(partyname + "隊伍頻道");
                        } else {

                            for (Player pa : partys.get(player.party).member) {
                                util.sendActionbarMessage(pa, ChatColor.GREEN + senderp.getDisplayName() + " 退出隊伍");
                            }
                            partys.get(player.party).member.remove(senderp);
                            chatchannels.get(player.party + "隊伍頻道").member.remove(P);
                            player.party = "";
                            util.sendActionbarMessage(Player, ChatColor.GREEN + "成功退出隊伍");
                        }

                    } else if (args[0].equals("invite")) {

                        P.sendMessage("請輸入想要邀請至隊伍的玩家名稱: ");
                        p.ChatingWithNPC = true;
                        p.strigns = "party,invite";
                        P.spigot().sendMessage(textcomp(ChatColor.UNDERLINE + "[點擊取消]", "點擊取消邀請玩家", "/chat cancel"));

                    }

                } else if (args.length == 2) {
                    if (args[0].equals("create")) {

                        if (args[1].length() > 8) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "隊伍名稱最多八個字。");
                            return false;
                        }

                        if (player.party.length() != 0) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "你已經有隊伍了。");
                            return false;
                        }

                        partys.put(args[1], new party(Player, args[1]));
                        chatchannels.put(args[1] + "隊伍頻道", new chatchannel(args[1] + "隊伍頻道"));
                        chatchannels.get(args[1] + "隊伍頻道").member.add(P);
                        player.party = args[1];
                        util.sendActionbarMessage(Player, ChatColor.GREEN + "隊伍創立成功 OuOb");

                    } else if (args[0].equals("invite")) {

                        if (player.party.length() == 0) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "你沒有在隊伍裡 QuQp");
                            return false;
                        }

                        if (partys.get(player.party).number() >= 10) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "隊伍人數已滿 QuQp");
                            return false;
                        }

                        if (Bukkit.getPlayer(args[1]) == null) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "該玩家不在當前分流 QuQp");
                            return false;
                        }

                        if (players.get(args[1]).partyrequest.length() != 0) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "該玩家已有尚未處理的隊伍邀請 QuQp");
                            return false;
                        }

                        if (players.get(args[1]).party.length() != 0) {
                            util.sendActionbarMessage(Player, ChatColor.RED + "該玩家已有隊伍 QuQp");
                            return false;
                        }

                        TextComponent head = new TextComponent(">> ");
                        head.toPlainText();
                        TextComponent msg = new TextComponent(ChatColor.WHITE + " 邀請你加入他的隊伍 , " + ChatColor.WHITE);
                        TextComponent accept = new TextComponent(
                                ChatColor.UNDERLINE + "[ 點擊接受 ]" + ChatColor.RESET + " ");
                        accept.setHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊加入隊伍").create()));
                        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept"));
                        TextComponent deny = new TextComponent(ChatColor.UNDERLINE + "[ 點擊拒絕 ]");
                        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("點擊婉拒加入隊伍邀請").create()));
                        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party deny"));
                        head.addExtra(getnamehover(senderp.getName(), true, false));
                        head.addExtra(msg);
                        head.addExtra(accept);
                        head.addExtra(deny);
                        Bukkit.getPlayer(args[1]).spigot().sendMessage(head);
                        players.get(args[1]).partyrequest = senderp.getName();

                        util.sendActionbarMessage(Player, ChatColor.GREEN + "已經送出隊伍加入邀請。");

                    }
                }
            } else if (cmdlable.equals("region")) {

                player player = players.get(senderp.getName());
                Player Player = senderp;

                if (args.length == 0) {
                    Player.sendMessage("\n你擁有的領地如下: ");
                    for (Map.Entry<Integer, region> entry : p.regionlist.entrySet()) {
                        region r = entry.getValue();
                        P.sendMessage(ChatColor.YELLOW + r.name + ChatColor.WHITE + " (" + r.size + " 平方公尺) "
                                + ChatColor.GRAY + "位在 " + r.centerx + "," + r.flagy + "," + r.centerz);
                    }
                    return false;
                } else if (args.length == 1 && args[0].equals("tp")) {
                    Player.sendMessage("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n請選擇要傳送的領地: \n");
                    for (Map.Entry<Integer, region> entry : regions.entrySet()) {
                        region r = entry.getValue();

                        if (r.hasPermission(p, "tp")) {
                            ChatColor cc = ChatColor.GRAY;
                            String mine = ChatColor.GREEN + "" + ChatColor.BOLD + "(您的領地) ";
                            String family = ChatColor.BLUE + "" + ChatColor.BOLD + "(家族領地) ";

                            if (r.server == config.getInt("server")) {
                                cc = ChatColor.GREEN;
                            }

                            if (!r.lordname.replace("family:", "").equals(p.family)) {
                                family = "";
                            }

                            if (!r.lordname.equals(p.id)) {
                                mine = "";
                            }

                            Player.spigot().sendMessage(textcomp(cc + "[" + r.name + "]" + mine + family,
                                    "點擊傳送到領地 " + r.name, "/region tp " + r.id));
                            continue;
                        }

                    }
                    return false;
                } else if (args.length == 1 && args[0].equals("buytp")) {
                    if (p.id.equals("ken20001207")) {
                        ItemStack stack = new ItemStack(Material.PAPER, 1);
                        ItemMeta im = stack.getItemMeta();
                        im.setDisplayName(ChatColor.DARK_PURPLE + "領地傳送點設置卷");
                        im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵即可宣告領地之傳送位置。"));
                        stack.setItemMeta(im);

                        senderp.getInventory().addItem(new ItemStack(stack));
                    }
                    return false;

                } else if (args.length >= 2 && args[0].equals("tp")) {

                    if (args.length > 2 || !isInteger(args[1])) {
                        Player.sendMessage(ChatColor.RED + "未知的指令使用方式，或許你是指 [/region tp] - 列出可傳送的領地");
                        return false;
                    }

                    region r = regions.get(Integer.parseInt(args[1]));
                    if (r != null) {

                        if (!r.hasPermission(p, "tp")) {
                            util.sendActionbarMessage(P, ChatColor.RED + "" + ChatColor.BOLD + "你沒有該領地的傳送權限");
                            return false;
                        }

                        if (r.server != getConfig().getInt("server")) {
                            util.sendActionbarMessage(Player, ChatColor.GOLD + "" + ChatColor.BOLD + "跨分流目前還未開放傳送");
                            return false;
                        }

                        if (r.lordname.contains("family:")) {
                            Player.teleport(new Location(Bukkit.getWorld(r.world), r.tpx, r.tpy, r.tpz));
                            Bukkit.getWorld(r.world).spawnParticle(Particle.PORTAL,
                                    new Location(Bukkit.getWorld(r.world), r.tpx, r.tpy, r.tpz).add(0, 1, 0), 100);
                            return false;
                        }

                        Player.teleport(new Location(Bukkit.getWorld(r.world), r.tpx, r.tpy, r.tpz));
                        Bukkit.getWorld(r.world).spawnParticle(Particle.PORTAL,
                                new Location(Bukkit.getWorld(r.world), r.tpx, r.tpy, r.tpz).add(0, 1, 0), 100);
                        return false;

                    } else {
                        Player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "該領地不存在");
                        return false;
                    }
                }

                if (!isInteger(args[0]))
                    return false;

                // 取得玩家目前想要設定的領地
                region r;
                if (regions.containsKey(Integer.parseInt(args[0]))) {
                    r = regions.get(Integer.parseInt(args[0]));
                } else {
                    util.sendActionbarMessage(Player, ChatColor.RED + "這個領地不存在 OuOb");
                    return false;
                }

                String enters = "";
                String breaks = "";
                String chest = "";
                String place = "";
                String tp = "";
                String fly = "";
                if (r.hasPermission(p, "manage")) {

                    if (args.length == 2 && args[1].equals("groups")) {
                        Player.sendMessage("\n你的領地 " + ChatColor.YELLOW + r.name + ChatColor.WHITE + " 共有 "
                                + r.groups.groups.size() + " 組身分組: ");
                        for (int j = 0; j < r.groups.groups.size(); j++) {
                            if (r.groups.groups.get(j).permission.get("enter") == 1)
                                enters = "可以";
                            else
                                enters = "不行";
                            if (r.groups.groups.get(j).permission.get("break") == 1)
                                breaks = "可以";
                            else
                                breaks = "不行";
                            if (r.groups.groups.get(j).permission.get("chest") == 1)
                                chest = "可以";
                            else
                                chest = "不行";
                            if (r.groups.groups.get(j).permission.get("place") == 1)
                                place = "可以";
                            else
                                place = "不行";
                            if (r.groups.groups.get(j).permission.get("tp") == 1)
                                tp = "可以";
                            else
                                tp = "不行";
                            if (r.groups.groups.get(j).permission.get("fly") == 1)
                                fly = "可以";
                            else
                                fly = "不行";
                            Player.sendMessage(">> " + ChatColor.GREEN + j + " " + r.groups.groups.get(j).name
                                    + ChatColor.WHITE + " : " + enters + "進入領地, " + breaks + "破壞方塊, " + chest + "使用箱子, "
                                    + place + "放置方塊, " + tp + "領地傳送," + fly + "領地飛行");

                        }
                        return false;
                    } else if (args.length == 6 && args[1].equals("groups") && args[2].equals("set")
                            && !args[4].equals("removeplayer") && !args[4].equals("addplayer")) {
                        r.groups.groups.get(Integer.parseInt(args[3])).permission.put(args[4],
                                Integer.parseInt(args[5]));
                        util.sendActionbarMessage(Player, ChatColor.GREEN + "已經把領地 " + r.name + " 的 "
                                + r.groups.groups.get(Integer.parseInt(args[3])).name + " 身分組的權限更新");
                        Player.performCommand("region " + args[0] + " groups set " + args[3]);
                        return false;
                    } else if (args.length == 4 && args[1].equals("groups") && args[2].equals("set")) {

                        group g = regions.get(Integer.parseInt(args[0])).groups.groups.get(Integer.parseInt(args[3]));

                        for (int j = 0; j < 20; j++) {
                            Player.sendMessage(" ");
                        }

                        Player.sendMessage("| 領地 " + ChatColor.GREEN + r.name + ChatColor.WHITE + " 的身分組 "
                                + ChatColor.GREEN + g.name + ChatColor.WHITE + " :");

                        if (g.permission.get("enter") == 0) {
                            TextComponent head = new TextComponent("| \n| -- 該身分組 不能 進入領地 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為可以]", "點擊設為 \"可以\" 進入領地",
                                    "/region " + args[0] + " groups set " + args[3] + " enter 1"));
                            Player.spigot().sendMessage(head);
                        } else {
                            TextComponent head = new TextComponent("| \n| -- 該身分組 可以 進入領地 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為不能]", "點擊設為 \"不能\" 進入領地",
                                    "/region " + args[0] + " groups set " + args[3] + " enter 0"));
                            Player.spigot().sendMessage(head);
                        }

                        if (g.permission.get("break") == 0) {
                            TextComponent head = new TextComponent("| -- 該身分組 不能 破壞領地內的方塊 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為可以]", "點擊設為 \"可以\" 破壞領地內的方塊",
                                    "/region " + args[0] + " groups set " + args[3] + " break 1"));
                            Player.spigot().sendMessage(head);
                        } else {
                            TextComponent head = new TextComponent("| -- 該身分組 可以 破壞領地內的方塊 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為不能]", "點擊設為 \"不能\" 破壞領地內的方塊",
                                    "/region " + args[0] + " groups set " + args[3] + " break 0"));
                            Player.spigot().sendMessage(head);
                        }

                        if (g.permission.get("chest") == 0) {
                            TextComponent head = new TextComponent("| -- 該身分組 不能 使用領地內的箱子 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為可以]", "點擊設為 \"可以\" 使用領地內的箱子",
                                    "/region " + args[0] + " groups set " + args[3] + " chest 1"));
                            Player.spigot().sendMessage(head);
                        } else {
                            TextComponent head = new TextComponent("| -- 該身分組 可以 使用領地內的箱子 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為不能]", "點擊設為 \"不能\" 使用領地內的箱子",
                                    "/region " + args[0] + " groups set " + args[3] + " chest 0"));
                            Player.spigot().sendMessage(head);
                        }

                        if (g.permission.get("place") == 0) {
                            TextComponent head = new TextComponent("| -- 該身分組 不能 在領地內放置方塊 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為可以]", "點擊設為 \"可以\" 在領地內放置方塊",
                                    "/region " + args[0] + " groups set " + args[3] + " place 1"));
                            Player.spigot().sendMessage(head);
                        } else {
                            TextComponent head = new TextComponent("| -- 該身分組 可以 在領地內放置方塊 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為不能]", "點擊設為 \"不能\" 在領地內放置方塊",
                                    "/region " + args[0] + " groups set " + args[3] + " place 0"));
                            Player.spigot().sendMessage(head);
                        }

                        if (g.permission.get("fly") == 0) {
                            TextComponent head = new TextComponent("| -- 該身分組 不能 在領地內飛行 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為可以]", "點擊設為 \"可以\" 在領地內飛行",
                                    "/region " + args[0] + " groups set " + args[3] + " fly 1"));
                            Player.spigot().sendMessage(head);
                        } else {
                            TextComponent head = new TextComponent("| -- 該身分組 可以 在領地內飛行 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為不能]", "點擊設為 \"不能\" 在領地內飛行",
                                    "/region " + args[0] + " groups set " + args[3] + " fly 0"));
                            Player.spigot().sendMessage(head);
                        }

                        if (g.permission.get("tp") == 0) {
                            TextComponent head = new TextComponent("| -- 該身分組 不能 傳送至該領地 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為可以]\n", "點擊設為 \"可以\" 傳送至該領地",
                                    "/region " + args[0] + " groups set " + args[3] + " tp 1"));
                            Player.spigot().sendMessage(head);
                        } else {
                            TextComponent head = new TextComponent("| -- 該身分組 可以 傳送至該領地 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "[設為不能]\n", "點擊設為 \"不能\" 傳送至該領地",
                                    "/region " + args[0] + " groups set " + args[3] + " tp 0"));
                            Player.spigot().sendMessage(head);
                        }

                        if (!args[3].equals("0")) {
                            Player.spigot().sendMessage(new TextComponent("| 以下玩家在此身分組內: "));
                            for (String pa : r.groups.groups.get(Integer.parseInt(args[3])).player) {
                                TextComponent head = new TextComponent("|-- " + pa + " ");
                                head.addExtra(textcomp(ChatColor.UNDERLINE + "[從此身分組踢出]", "將 " + pa + " 從此身分組踢出",
                                        "/region " + args[0] + " groups set " + args[3] + " removeplayer " + pa));
                                Player.spigot().sendMessage(head);
                            }
                            TextComponent head = new TextComponent("| \n| 點擊 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "這裡", "將玩家加入身分組",
                                    "/region " + args[0] + " groups set " + args[3] + " addplayer"));
                            head.addExtra(ChatColor.RESET + " 將玩家加入身分組");
                            Player.spigot().sendMessage(head);
                            head = new TextComponent("| \n| 點擊 ");
                            head.addExtra(textcomp(ChatColor.UNDERLINE + "這裡", "刪除身分組 " + g.name,
                                    "/region " + args[0] + " groups set " + args[3] + " removegroup"));
                            head.addExtra(ChatColor.RESET + " 刪除該身分組");
                            Player.spigot().sendMessage(head);
                        }

                    } else if (args.length == 3 && args[1].equals("groups") && args[2].equals("add")) {
                        player.ChatingWithNPC = true;
                        player.strigns = "region " + args[0] + " groups add";
                        for (int j = 0; j < 20; j++) {
                            P.sendMessage(" ");
                        }
                        Player.sendMessage(">> 請輸入新的身分組名稱");
                        return false;
                    } else if (args.length == 6 && args[1].equals("groups") && args[4].equals("removeplayer")) {
                        r.groups.groups.get(Integer.parseInt(args[3])).player.remove(args[5]);
                        util.sendActionbarMessage(Player, ChatColor.GREEN + "已經把玩家 " + args[5] + " 從 "
                                + r.groups.groups.get(Integer.parseInt(args[3])).name + " 身分組的踢出");
                        P.performCommand("region " + args[0] + " groups set " + args[3]);
                        return false;
                    } else if (args.length == 5 && args[1].equals("groups") && args[4].equals("removegroup")) {
                        r.groups.groups.remove(Integer.parseInt(args[3]));
                        util.sendActionbarMessage(Player, ChatColor.GREEN + "已經把該身分組移除");
                        r.showRegionInfo(P);
                        return false;
                    } else if (args.length == 5 && args[1].equals("groups") && args[4].equals("addplayer")) {
                        for (int j = 0; j < 20; j++) {
                            P.sendMessage(" ");
                        }
                        Player.sendMessage(">> 請輸入要加入該身分組的玩家名稱");
                        player.ChatingWithNPC = true;
                        player.strigns = "regions " + args[0] + " groups set " + args[3] + " addplayer";
                        return false;
                    } else if (args.length == 6 && args[1].equals("groups") && args[4].equals("addplayer")) {
                        r.groups.groups.get(Integer.parseInt(args[3])).player.add(args[5]);

                        util.sendActionbarMessage(Player, ChatColor.GREEN + "已經把玩家 " + args[5] + " 加入 "
                                + r.groups.groups.get(Integer.parseInt(args[3])).name + " 身分組");
                        P.performCommand("region " + args[0] + " groups set " + args[3]);
                        player.ChatingWithNPC = false;
                        player.strigns = "";
                        return false;
                    } else if (args.length == 2 && args[1].equals("tofamily")) {

                        if (p.family.equals("null")) {
                            util.sendActionbarMessage(P, ChatColor.RED + "你沒有家族欸");
                            return false;
                        }

                        for (String s : p.regions.split(",")) {
                            if (s.equals(args[0])) {
                                r.lordname = "family:" + p.family;
                                family f = families.get(p.family);
                                f.regions = f.regions + "," + args[0];
                                r.groups.groups.add(new group("家族成員"));
                                p.regionlist.remove(r.id);
                                r.upload();
                                f.upload();
                                p.upload();
                                for (Hologram pe : HologramsAPI.getHolograms(this)) {

                                    if (pe.getLine(0).toString().contains(r.name)
                                            && pe.getLine(1).toString().contains(P.getName())) {
                                        pe.removeLine(1);
                                        pe.appendTextLine(ChatColor.GRAY + "家族 " + f.name + " 的家族領地");
                                        break;
                                    }
                                }
                                r.showRegionInfo(P);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            PMH.SendPluginMessage("mainsystem_downloadregion",
                                                    "%region%" + r.id + "%region%");
                                            PMH.SendPluginMessage("mainsystem_downloadfamily",
                                                    "%family%" + f.name + "%family%");
                                        } catch (Exception e) {

                                        }
                                    }
                                }.runTaskLater(this, 20);
                                return false;
                            }
                        }

                        util.sendActionbarMessage(P, ChatColor.RED + "這個領地不是你的欸");

                        return false;
                    }
                    return false;

                }

                util.sendActionbarMessage(Player, ChatColor.RED + "這個領地不是你的!");
            } else if (cmdlable.equals("chatchannel")) {

                if (args.length != 1)
                    return false;

                if (!chatchannels.containsKey(args[0])) {
                    util.sendActionbarMessage(P, ChatColor.RED + "該聊天頻道不存在");
                    return false;
                }

                chatchannel cc = chatchannels.get(args[0]);

                if (cc.channelname.contains("家族頻道") && !cc.channelname.replace("家族頻道", "").equals(p.family)) {
                    util.sendActionbarMessage(P, ChatColor.RED + "家族聊天頻道只有家族成員可以進入");
                    return false;
                }

                if (cc.channelname.contains("隊伍頻道") && !cc.channelname.replace("隊伍頻道", "").equals(p.party)) {
                    util.sendActionbarMessage(P, ChatColor.RED + "隊伍聊天頻道只有該隊伍的隊員可以進入");
                    return false;
                }

                if (!cc.member.contains(P))
                    cc.addplayer(P);
                else
                    cc.removeplayer(P);

                P.sendMessage("");
                for (Map.Entry<String, chatchannel> entry : chatchannels.entrySet()) {
                    cc = entry.getValue();
                    if (cc.member.contains(P)) {
                        TextComponent head = new TextComponent(ChatColor.GREEN + cc.channelname);
                        head.toPlainText();
                        head.addExtra(textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[退出頻道]",
                                "點擊退出 " + cc.channelname, "/chatchannel " + cc.channelname));
                        P.spigot().sendMessage(head);
                    } else {
                        TextComponent head = new TextComponent(ChatColor.GRAY + cc.channelname);
                        head.toPlainText();
                        head.addExtra(textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[加入頻道]",
                                "點擊加入 " + cc.channelname, "/chatchannel " + cc.channelname));
                        P.spigot().sendMessage(head);
                    }
                }
                P.sendMessage("");

            }

        } catch (Exception exception) {
            Player Player = (Player) sender;
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            exception.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            util.pleaseReport(Player);
        }

        return false;
    }

    @EventHandler
    public void familyshop(InventoryClickEvent e) {

        Player P = (Player) e.getWhoClicked();
        player p = players.get(P.getName());

        if (e.getView().getTitle().contains("可用家族道具")) {
            e.setCancelled(true);
        } else
            return;

        if (e.getRawSlot() < 0 || e.getRawSlot() > 10)
            return;

        if (e.getClickedInventory().getItem(e.getRawSlot()) == null)
            return;

        ItemStack item = e.getClickedInventory().getItem(e.getRawSlot());

        if (item.getItemMeta().getDisplayName().contains("家族創立卷")) {

            if (P.getInventory().firstEmpty() == -1) {
                P.closeInventory();
                P.sendMessage(ChatColor.RED + "包包空間不足");
                return;
            }

            ItemStack stack = new ItemStack(Material.PAPER, 1);
            ItemMeta im = stack.getItemMeta();
            im.setDisplayName(ChatColor.DARK_PURPLE + "家族創立卷");
            im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵即可創立家族，",
                    ChatColor.WHITE + "使用本卷的玩家將成為家族長，且該玩家必須是當前隊伍的隊長",
                    ChatColor.WHITE + "在該玩家的隊伍中的其他成員將成為家族成員，且人數必須為10人。"));
            stack.setItemMeta(im);
            P.getInventory().addItem(new ItemStack(stack));

        } else if (item.getItemMeta().getDisplayName().contains("家族邀請函")) {

            if (P.getInventory().firstEmpty() == -1) {
                P.closeInventory();
                P.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "包包空間不足");
                return;
            }

            boolean wheat_cost = false;
            for (int i = 0; i <= 35; i++) {
                ItemStack temp = P.getInventory().getItem(i);
                if (temp.getType().equals(Material.WHEAT) && temp.getAmount() == 64) {
                    P.getInventory().getItem(i).setAmount(0);
                    P.updateInventory();
                    wheat_cost = true;
                    break;
                }
            }

            if (!wheat_cost) {
                P.closeInventory();
                P.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "需要一組小麥");
                return;
            }

            ItemStack stack = new ItemStack(Material.PAPER, 1);
            ItemMeta im = stack.getItemMeta();
            im.setDisplayName(ChatColor.DARK_PURPLE + "家族邀請函 : " + p.family);
            im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵加入家族 " + p.family, ChatColor.WHITE + "(無法在傳送小鎮使用)"));
            stack.setItemMeta(im);
            P.getInventory().addItem(new ItemStack(stack));

        }
    }

    /** 防止苦力怕破壞方塊 */
    @EventHandler
    public void AntiCrepperBreakBlock(EntityExplodeEvent e) {
        if (e.getEntity() instanceof Creeper) {
            e.setCancelled(true);
            e.getLocation().getWorld().createExplosion(e.getLocation().getX(), e.getLocation().getY(),
                    e.getLocation().getZ(), 4.0F, false, false);
        }
    }

    /** 對話時產生 hologram 在玩家頭上 */
    public boolean setholo(Player Player, String msg, int second) {

        final Hologram hologram = HologramsAPI.createHologram(this, Player.getLocation().add(0.0, 2.0, 0.0));
        hologram.appendTextLine(msg);

        new BukkitRunnable() {
            int ticksRun;

            @Override
            public void run() {
                ticksRun++;
                hologram.teleport(Player.getLocation().add(0.0, 2.7, 0.0));

                if (ticksRun > second * 20) {
                    hologram.teleport(Player.getLocation().add(0.0, 5, 0.0));
                }

                if (ticksRun > second * 20 + 0.2) {
                    hologram.delete();
                    cancel();
                }
            }
        }.runTaskTimer(this, 1L, 1L);

        return true;

    }

    @EventHandler
    public void getCoin1(EntityDeathEvent e) {

        if (e.getEntity() instanceof Player) { // 擊殺玩家不給經驗
            return;
        }

        if (e.getEntity().getType().equals(EntityType.PIG_ZOMBIE)) {
            for (ItemStack im : e.getDrops()) {
                if (im.getType().equals(Material.GOLD_NUGGET)) {
                    im.setType(Material.IRON_NUGGET);
                }
                if (im.getType().equals(Material.GOLDEN_SWORD)) {
                    im.setType(Material.IRON_SWORD);
                }
                if (im.getType().equals(Material.GOLD_INGOT)) {
                    im.setAmount(0);
                    ;
                }
            }
        }

        Entity killer = e.getEntity().getKiller();
        if (killer instanceof Player) {
            Player p = (Player) killer;
            if (((Player) killer).getInventory().getItemInMainHand().getType().toString().contains("SWORD")) {
                e.setDroppedExp(e.getDroppedExp() / 2);
                util.sendActionbarMessage(p, "砍殺 : 生活經驗 +10, 舞劍熟練度 +1");
                players.get(p.getName()).sword++;
                addexp(p.getName(), 10);
            } else if (((Player) killer).getInventory().getItemInMainHand().getType().toString().contains("BOW")) {
                double dist = p.getLocation().distance(e.getEntity().getLocation());

                if (dist > 200) { // 距離太遠，可能是鑽系統漏洞
                    return;
                }

                int exp = 20;
                int bow = 1 + (int) dist / 3;
                util.sendActionbarMessage(p, "狙殺 : 生活經驗 +" + exp + ", 狙擊熟練度 +" + bow);
                players.get(p.getName()).bow += bow;
                addexp(p.getName(), exp);
            }

        }
    }

    @EventHandler
    public void setBackforDeathPlayer(PlayerDeathEvent e) {
        Player P = e.getEntity();
        player p = players.get(P.getName());
        p.setPlayerBackLocation(P.getLocation());
    }

    // 跨領地液體流動事件處理
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {

        try {

            if (e.getBlock().getType() != Material.LAVA && e.getBlock().getType() != Material.WATER)
                return;
            if (inwhichregion(e.getBlock().getLocation()) != inwhichregion(e.getToBlock().getLocation())) {
                e.setCancelled(true);
            }

        } catch (Exception exception) {
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            exception.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
        }
    }

    public static void reloaddata(String id) {

        if (!Bukkit.getOnlinePlayers().toString().contains(id))
            return;

        Player P = Bukkit.getPlayerExact(id);
        player p = players.get(id);
        util.sendActionbarMessage(P, ChatColor.GRAY + "重新整理玩家資料 ...");
        player newp = players.get(id);

        // 設定顯示稱號色彩
        p.setDisplayClaColor();

        if (p.nickname.equals("null"))
            P.setDisplayName(chat.getPlayerPrefix(P) + ChatColor.GRAY + p.id + chat.getPlayerSuffix(P));
        else
            P.setDisplayName(chat.getPlayerPrefix(P) + ChatColor.WHITE + p.nickname + chat.getPlayerSuffix(P));

        if (!p.family.equals("null")) {
            if (App.families.get(p.family).groups.get("家族幹部").members.contains(id) && !p.cla.contains("家族幹部")) {
                p.cla += ",家族幹部";
                p.displaycla = "家族幹部";
                App.reloaddata(id);
                return;
            }
            if (!App.families.get(p.family).groups.get("家族幹部").members.contains(id) && p.cla.contains("家族幹部")) {
                p.cla = p.cla.replace(",家族幹部", "");
                p.displaycla = p.cla.split(",")[0];
                App.reloaddata(id);
                return;
            }
            if (!App.families.containsKey(p.family)) {
                Bukkit.getPlayer(p.id).sendMessage("你本來的家族已經不存在了，我很遺憾!");
                p.family = "null";
                App.reloaddata(p.id);
                return;
            } else {
                for (Map.Entry<String, family.group> g : App.families.get(p.family).groups.entrySet()) {
                    if (App.families.get(p.family).president.equals(p.id)) {
                        return;
                    }
                    if (g.getValue().members.contains(p.id)) {
                        return;
                    }
                }
                Bukkit.getPlayer(p.id).sendMessage("你已經不是你本來家族的成員了，我很遺憾!");
                p.family = "null";
                App.reloaddata(id);
                return;
            }
        }

        if (p.family.equals("null")) {
            if (p.cla.contains("家族代表"))
                p.cla = p.cla.replace(",家族代表", "");
            if (p.cla.contains("家族幹部"))
                p.cla = p.cla.replace(",家族幹部", "");
        }

        players.get(id).displayname = P.getDisplayName();
        P.setPlayerListName(P.getDisplayName());

        NametagEdit.getApi().setPrefix(P, "§f[" + newp.clacolor + newp.displaycla + "§f] ");
        NametagEdit.getApi().setSuffix(P, " §aLv." + newp.lv);

        p.CheckBank();

        util.sendActionbarMessage(P, ChatColor.GREEN + "重新整理玩家資料完畢 owo");
    }

    // 開啟交易所選單介面
    private static void openshop_sell(Player P, int page) {
        try {
            // 先過濾交易所，選出每種商品最低價的 item 就好
            Map<ItemStack, shop_sell_item> cheapest = new HashMap<>();
            for (Map.Entry<Integer, shop_sell_item> entry : shop_sell.entrySet()) {
                shop_sell_item item = entry.getValue();
                ItemStack stackin1 = item.item;
                stackin1.setAmount(1);
                if (cheapest.containsKey(stackin1)) {
                    if (cheapest.get(stackin1).price > item.price)
                        cheapest.put(stackin1, item);
                    else
                        continue;
                } else {
                    cheapest.put(stackin1, item);
                }
            }

            // 更新顯示商品數量
            ShopDisplayNumber = cheapest.size();

            // 將選出的最低價商品加上商品描述，添加到待上架清單
            List<ItemStack> menu = new ArrayList<>();
            for (Map.Entry<ItemStack, shop_sell_item> item : cheapest.entrySet()) {

                ItemStack stack = new ItemStack(item.getValue().item);
                stack.setAmount(1);
                ItemMeta im = stack.getItemMeta();
                if (item.getValue().item.getItemMeta().getDisplayName() != null)
                    im.setDisplayName(item.getValue().item.getItemMeta().getDisplayName());
                if (item.getValue().item.getItemMeta().getLore() != null) {
                    List<String> lore = item.getValue().item.getItemMeta().getLore();
                    lore.add(" ");
                    lore.add(" ");
                    lore.add(ChatColor.GRAY + "商品編號: " + ChatColor.WHITE + item.getValue().id);
                    lore.add(ChatColor.GRAY + "賣家: " + ChatColor.WHITE + item.getValue().owner);
                    lore.add(ChatColor.GRAY + "上架時間: " + ChatColor.WHITE + item.getValue().time);
                    lore.add(ChatColor.AQUA + "商品定價: " + ChatColor.WHITE + "$ " + item.getValue().price);
                    lore.add(ChatColor.AQUA + "剩餘數量: " + ChatColor.WHITE + item.getValue().amount);

                    im.setLore(lore);
                } else {
                    im.setLore(Arrays.asList(" ", ChatColor.GRAY + "商品編號: " + ChatColor.WHITE + item.getValue().id,
                            ChatColor.GRAY + "賣家: " + ChatColor.WHITE + item.getValue().owner,
                            ChatColor.GRAY + "上架時間: " + ChatColor.WHITE + item.getValue().time,
                            ChatColor.AQUA + "商品定價: " + ChatColor.WHITE + "$ " + item.getValue().price,
                            ChatColor.AQUA + "剩餘數量: " + ChatColor.WHITE + item.getValue().amount));
                }
                stack.setItemMeta(im);

                menu.add(stack);
            }

            // 對待上架清單進行排序及選取需要的部分
            itemcompartor cmp = new itemcompartor();
            menu.sort(cmp);
            if (menu.size() >= page * 52 + 1) {
                menu = menu.subList((page - 1) * 52, page * 52 + 1);
            } else {
                menu = menu.subList((page - 1) * 52, menu.size());
            }

            // 建立交易所介面
            Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BLACK + "販賣交易所:" + page);
            int j = 0;
            for (ItemStack item : menu) {
                if (j == 45 || j == 53) {
                    j++;
                    continue;
                }
                inv.setItem(j, item);
                j++;
            }

            int pages = (ShopDisplayNumber / 52) + 1;

            // 45 上一頁 53 下一頁
            ItemStack stack;
            ItemMeta im;
            if (page != 1) {
                stack = new ItemStack(Material.RED_WOOL);
                im = stack.getItemMeta();
                im.setDisplayName(ChatColor.GRAY + "上一頁");
                stack.setItemMeta(im);
                inv.setItem(45, stack);
            } else {
                stack = new ItemStack(Material.CHEST);
                im = stack.getItemMeta();
                im.setDisplayName(ChatColor.GRAY + "查看我上架的商品");
                stack.setItemMeta(im);
                inv.setItem(45, stack);
            }
            if (page != pages) {
                stack = new ItemStack(Material.RED_WOOL);
                im = stack.getItemMeta();
                im.setDisplayName(ChatColor.GRAY + "下一頁");
                stack.setItemMeta(im);
                inv.setItem(53, stack);
            }

            P.openInventory(inv);
        } catch (Exception exc) {
            StringWriter errors = new StringWriter();
            exc.printStackTrace(new PrintWriter(errors));
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[交易所] 在開啟交易所介面時發生錯誤 : " + errors.toString());
        }
    }

    // 啟動玩家的粒子特效
    public static void setupPlayerParticle(Player P) {
        player p = players.get(P.getName());

        // 如果玩家當前已經有執行中的 Particle Tasks 先關閉
        if (tasks.containsKey(P)) {
            for (BukkitTask task : tasks.get(P)) {
                task.cancel();
            }
        }

        // 重新建立 Tasks
        for (String s : p.particle.split(",")) {
            if (s.split(":")[0].equals("1")) {
                String particledata = s.split(":")[1];
                createPlayerParticles(P, particledata);
            }
        }
    }

    public void open_phone(Player P) {
        player p = players.get(P.getName());
        P.playSound(P.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, SoundCategory.BLOCKS, 10, 5);
        Inventory inv = Bukkit.createInventory(null, 54, p.id + " 的手機");

        ItemStack stack0 = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta im0 = stack0.getItemMeta();
        im0.setDisplayName(ChatColor.GRAY + "<-- 類型 | 按鈕 -->");
        stack0.setItemMeta(im0);
        inv.setItem(1, stack0);
        inv.setItem(10, stack0);
        inv.setItem(19, stack0);
        inv.setItem(28, stack0);
        inv.setItem(37, stack0);
        inv.setItem(46, stack0);

        ItemStack type1 = new ItemStack(Material.RED_WOOL);
        ItemMeta typeim1 = type1.getItemMeta();
        typeim1.setDisplayName(ChatColor.WHITE + "玩家個人資訊 " + ChatColor.YELLOW + "-->");
        type1.setItemMeta(typeim1);
        inv.setItem(0, type1);

        ItemStack type2 = new ItemStack(Material.ORANGE_WOOL);
        ItemMeta typeim2 = type2.getItemMeta();
        typeim2.setDisplayName(ChatColor.WHITE + "系統功能選單 " + ChatColor.YELLOW + "-->");
        type2.setItemMeta(typeim2);
        inv.setItem(9, type2);

        ItemStack type3 = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta typeim3 = type3.getItemMeta();
        typeim3.setDisplayName(ChatColor.WHITE + "玩家傳送選單 " + ChatColor.YELLOW + "-->");
        type3.setItemMeta(typeim3);
        inv.setItem(18, type3);

        ItemStack type5 = new ItemStack(Material.GREEN_WOOL);
        ItemMeta typeim5 = type5.getItemMeta();
        typeim5.setDisplayName(ChatColor.WHITE + "分流移動選單 " + ChatColor.YELLOW + "-->");
        type5.setItemMeta(typeim5);
        inv.setItem(27, type5);

        ItemStack type4 = new ItemStack(Material.BLUE_WOOL);
        ItemMeta typeim4 = type4.getItemMeta();
        typeim4.setDisplayName(ChatColor.WHITE + "顯示設定選單 " + ChatColor.YELLOW + "-->");
        type4.setItemMeta(typeim4);
        inv.setItem(36, type4);

        ItemStack type6 = new ItemStack(Material.PURPLE_WOOL);
        ItemMeta typeim6 = type6.getItemMeta();
        typeim6.setDisplayName(ChatColor.WHITE + "遊戲相關資訊 " + ChatColor.YELLOW + "-->");
        type6.setItemMeta(typeim6);
        inv.setItem(45, type6);

        ItemStack info1 = new ItemStack(Material.SNOWBALL);
        ItemMeta info1im = info1.getItemMeta();
        info1im.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "伺服器官方網站");
        info1im.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊取得前往官方網站的按鈕。"));
        info1.setItemMeta(info1im);
        inv.setItem(47, info1);

        ItemStack info2 = new ItemStack(Material.SNOWBALL);
        ItemMeta info2im = info2.getItemMeta();
        info2im.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "伺服器社群規範");
        info2im.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊取得前往伺服器社群規範網頁的按鈕。"));
        info2.setItemMeta(info2im);
        inv.setItem(48, info2);

        ItemStack info3 = new ItemStack(Material.SNOWBALL);
        ItemMeta info3im = info3.getItemMeta();
        info3im.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "贊助伺服器");
        info3im.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊取得前往贊助伺服器網頁的按鈕。"));
        info3.setItemMeta(info3im);
        inv.setItem(49, info3);

        ItemStack info4 = new ItemStack(Material.SNOWBALL);
        ItemMeta info4im = info4.getItemMeta();
        info4im.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "新手教學");
        info4im.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊取得前往新手教學網頁的按鈕。"));
        info4.setItemMeta(info4im);
        inv.setItem(50, info4);

        ItemStack info5 = new ItemStack(Material.SNOWBALL);
        ItemMeta info5im = info5.getItemMeta();
        info5im.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "Discord 玩家社群");
        info5im.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊取得前往伺服器 Discord 玩家社群的按鈕。"));
        info5.setItemMeta(info5im);
        inv.setItem(51, info5);

        ItemStack stack1 = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta im1 = stack1.getItemMeta();
        im1.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "我的資料");
        im1.setLore(Arrays.asList(" ", ChatColor.WHITE + "玩家名稱 " + ChatColor.GRAY + p.id,
                ChatColor.WHITE + "職業 " + ChatColor.GRAY + p.clacolor + p.cla,
                ChatColor.WHITE + "等級 " + ChatColor.GRAY + p.lv + " 等",
                ChatColor.WHITE + "經驗 " + ChatColor.GRAY + p.exp + " 點 / " + (p.lv * 1000) + " 點",
                ChatColor.WHITE + "交易貨幣 " + ChatColor.GRAY + util.round(p.balance, 0) + " 元",
                ChatColor.WHITE + "最大領地格數 " + ChatColor.GRAY + p.getPlayerRegionAvailible() + " 格", " ",
                ChatColor.WHITE + "商城幣 " + ChatColor.GRAY + p.coin + " 元",
                ChatColor.WHITE + "領地飛行時數 " + ChatColor.GRAY + p.fly / 20 + " 秒", " ",
                ChatColor.WHITE + "伐木熟練度 " + ChatColor.GRAY + p.wood,
                ChatColor.WHITE + "舞劍熟練度 " + ChatColor.GRAY + p.sword,
                ChatColor.WHITE + "弓箭熟練度 " + ChatColor.GRAY + p.bow,
                ChatColor.WHITE + "採礦熟練度 " + ChatColor.GRAY + p.iron,
                ChatColor.WHITE + "加工熟練度 " + ChatColor.GRAY + p.magic,
                ChatColor.WHITE + "釣魚熟練度 " + ChatColor.GRAY + p.fish,
                ChatColor.WHITE + "貿易熟練度 " + ChatColor.GRAY + p.trade,
                ChatColor.WHITE + "採收熟練度 " + ChatColor.GRAY + p.ripe, "", ChatColor.GRAY + "(點擊可關閉手機)"));
        stack1.setItemMeta(im1);
        inv.setItem(2, stack1);

        ItemStack stack2 = new ItemStack(Material.CHEST);
        ItemMeta im2 = stack2.getItemMeta();
        im2.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "行動交易所");
        im2.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊開啟交易所線上商城"));
        stack2.setItemMeta(im2);
        inv.setItem(11, stack2);

        ItemStack stack3 = new ItemStack(Material.SHIELD);
        ItemMeta im3 = stack3.getItemMeta();
        im3.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "組隊系統");
        im3.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊開啟組隊系統選單", ""));
        stack3.setItemMeta(im3);
        inv.setItem(12, stack3);

        ItemStack stack4 = new ItemStack(Material.COMPASS);
        ItemMeta im4 = stack4.getItemMeta();
        im4.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "回到分流傳送區");
        im4.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊回到該分流的傳送區域"));
        stack4.setItemMeta(im4);
        inv.setItem(20, stack4);

        ItemStack stack5 = new ItemStack(Material.ENDER_PEARL);
        ItemMeta im5 = stack5.getItemMeta();
        im5.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "領地傳送");
        im5.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊查看可傳送的領地"));
        stack5.setItemMeta(im5);
        inv.setItem(21, stack5);

        ItemStack stack6 = new ItemStack(Material.NETHER_STAR);
        ItemMeta im6 = stack6.getItemMeta();
        im6.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "聊天頻道設定");
        im6.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊設定聊天頻道設定"));
        stack6.setItemMeta(im6);
        inv.setItem(13, stack6);

        ItemStack stack7 = new ItemStack(Material.BARRIER);
        ItemMeta im7 = stack7.getItemMeta();
        im7.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "安靜模式");
        im7.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊退出所有文字聊天頻道"));
        stack7.setItemMeta(im7);
        inv.setItem(14, stack7);

        ItemStack stack8 = new ItemStack(Material.RED_BED);
        ItemMeta im8 = stack8.getItemMeta();
        im8.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "回家");
        im8.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊回家"));
        stack8.setItemMeta(im8);
        inv.setItem(22, stack8);

        ItemStack stack9 = new ItemStack(Material.MINECART);
        ItemMeta im9 = stack9.getItemMeta();
        im9.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "回到傳送前的地點");
        im9.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊回到傳送前的地點"));
        stack9.setItemMeta(im9);
        inv.setItem(23, stack9);

        ItemStack stack24 = new ItemStack(Material.OAK_PLANKS);
        ItemMeta im24 = stack24.getItemMeta();
        im24.setDisplayName(ChatColor.WHITE + "[分流傳送] " + ChatColor.GOLD + "傳送小鎮 分流");
        im24.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊傳送至 \"傳送小鎮\" 分流，你會回到離開該分流時的最後位置。 "));
        stack24.setItemMeta(im24);
        inv.setItem(29, stack24);

        ItemStack stack25 = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta im25 = stack25.getItemMeta();
        im25.setDisplayName(ChatColor.WHITE + "[分流傳送] " + ChatColor.GOLD + "北境 分流");
        im25.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊傳送至 \"北境\" 分流，你會回到離開該分流時的最後位置。 "));
        stack25.setItemMeta(im25);
        inv.setItem(30, stack25);

        ItemStack stack26 = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta im26 = stack26.getItemMeta();
        im26.setDisplayName(ChatColor.WHITE + "[分流傳送] " + ChatColor.GOLD + "南境 分流");
        im26.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊傳送至 \"南境\" 分流，你會回到離開該分流時的最後位置。 "));
        stack26.setItemMeta(im26);
        inv.setItem(31, stack26);

        ItemStack stack27 = new ItemStack(Material.IRON_BLOCK);
        ItemMeta im27 = stack27.getItemMeta();
        im27.setDisplayName(ChatColor.WHITE + "[分流傳送] " + ChatColor.GOLD + "屠龍者挑戰副本 分流");
        im27.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊傳送至 \"屠龍者挑戰副本\" 分流，你會回到您任務副本的世界重生點。 "));
        stack27.setItemMeta(im27);
        if (p.settings.containsKey("mission") && !p.settings.get("mission").equals("0"))
            inv.setItem(32, stack27);

        ItemStack stack10 = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta im10 = stack10.getItemMeta();
        im10.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "設定顯示粒子特效");
        im10.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊設定顯示粒子特效"));
        stack10.setItemMeta(im10);
        inv.setItem(38, stack10);

        ItemStack stack39 = new ItemStack(Material.PAPER);
        ItemMeta im39 = stack39.getItemMeta();
        im39.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "設定顯示暱稱");
        im39.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊設定你要把甚麼身分作為稱號"));
        stack39.setItemMeta(im39);
        inv.setItem(39, stack39);

        ItemStack stack11 = new ItemStack(Material.GOLD_INGOT);
        ItemMeta im11 = stack11.getItemMeta();
        im11.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "大樂透選單");
        im11.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊開啟大樂透選單"));
        stack11.setItemMeta(im11);
        inv.setItem(15, stack11);

        ItemStack stack16 = new ItemStack(Material.APPLE);
        ItemMeta im16 = stack16.getItemMeta();
        im16.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "家族選單");
        im16.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊開啟家族選單"));
        stack16.setItemMeta(im16);
        inv.setItem(16, stack16);

        ItemStack stack17 = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta im17 = stack17.getItemMeta();
        im17.setDisplayName(ChatColor.WHITE + "[手機功能] " + ChatColor.GOLD + "切換 PVP 模式");
        im17.setLore(Arrays.asList(" ", ChatColor.GRAY + "點擊切換 開啟/關閉 PVP 模式，", ChatColor.GRAY + "開啟後無法攻擊他人，也不會被他人攻擊。"));
        stack17.setItemMeta(im17);
        inv.setItem(3, stack17);

        P.openInventory(inv);
    }

    @EventHandler
    public void usephone(InventoryClickEvent e) {
        Player P = (Player) e.getWhoClicked();
        player p = players.get(P.getName());
        if (e.getRawSlot() == 0 && e.getView().getTitle().equals("Crafting") && e.getInventory().getItem(0) == null) {
            open_phone(P);
        } else if (e.getView().getTitle().contains("手機")) {
            e.setCancelled(true);

            if (e.getRawSlot() < 0 || e.getRawSlot() > 54)
                return;

            if (e.getInventory().getItem(e.getRawSlot()) != null
                    && e.getInventory().getItem(e.getRawSlot()).hasItemMeta()
                    && (e.getInventory().getItem(e.getRawSlot()).getItemMeta().getDisplayName().contains("手機功能") || e
                            .getInventory().getItem(e.getRawSlot()).getItemMeta().getDisplayName().contains("分流傳送"))) {

                String ItemDisplayName = e.getInventory().getItem(e.getRawSlot()).getItemMeta().getDisplayName();

                if (ItemDisplayName.contains("領地傳送")) {
                    P.performCommand("region tp");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("組隊系統")) {
                    P.performCommand("party");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("官方網站")) {
                    TextComponent comp = new TextComponent(
                            ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "[點我前往伺服器官方網站]");
                    comp.setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/index.php"));
                    comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("點擊前往 SMD:Kingdoms 官方網站").create()));
                    P.sendMessage(" ");
                    P.spigot().sendMessage(comp);
                    P.closeInventory();
                } else if (ItemDisplayName.contains("社群規範")) {
                    TextComponent comp = new TextComponent(
                            ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "[點我前往伺服器社群規範網頁]");
                    comp.setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/law.php?"));
                    comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("點擊前往伺服器社群規範網頁").create()));
                    P.sendMessage(" ");
                    P.spigot().sendMessage(comp);
                    P.closeInventory();
                } else if (ItemDisplayName.contains("新手教學")) {
                    TextComponent comp = new TextComponent(
                            ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "[點我前往新手教學網頁]");
                    comp.setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/help.php?page=1"));
                    comp.setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊前往新手教學網頁").create()));
                    P.sendMessage(" ");
                    P.spigot().sendMessage(comp);
                    P.closeInventory();
                } else if (ItemDisplayName.contains("Discord")) {
                    TextComponent comp = new TextComponent(
                            ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "[點我前往伺服器官方 Discord]");
                    comp.setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discordapp.com/invite/m4REaMa"));
                    comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("點擊前往伺服器官方 Discord").create()));
                    P.sendMessage(" ");
                    P.spigot().sendMessage(comp);
                    P.closeInventory();
                } else if (ItemDisplayName.contains("贊助伺服")) {
                    TextComponent comp = new TextComponent(
                            ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "[點我前往贊助伺服網頁]");
                    comp.setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/donate.php"));
                    comp.setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊前往贊助伺服網頁").create()));
                    P.sendMessage(" ");
                    P.spigot().sendMessage(comp);
                    P.closeInventory();
                } else if (ItemDisplayName.contains("PVP")) {
                    P.performCommand("mainsystem pvp");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("大樂透選單")) {
                    P.performCommand("mainsystem lottery");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("分流傳送區")) {
                    P.performCommand("spawn");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("家族")) {
                    P.performCommand("family");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("顯示暱稱")) {
                    P.performCommand("setdisplayname");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("北境")) {
                    P.performCommand("connect server_1");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("南境")) {
                    P.performCommand("connect server_2");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("傳送小鎮")) {
                    P.performCommand("connect server_lobby");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("屠龍者")) {
                    P.performCommand("connect server_11");
                    P.closeInventory();
                } else if (ItemDisplayName.contains("行動交易所")) {
                    P.closeInventory();
                    P.performCommand("shop buy 1");
                } else if (ItemDisplayName.contains("回家")) {
                    P.closeInventory();
                    boolean nohome = true;
                    if (p.home != null && p.home.split(",").length == 5) {
                        if (p.home.split(",")[4].equals(config.getString("server"))) {
                            nohome = false;
                            Location l = new Location(Bukkit.getWorld(p.home.split(",")[0]),
                                    Integer.parseInt(p.home.split(",")[1]), Integer.parseInt(p.home.split(",")[2]),
                                    Integer.parseInt(p.home.split(",")[3]));
                            p.setPlayerBackLocation(P.getLocation());
                            P.teleport(l);
                            Bukkit.getWorld(p.home.split(",")[0]).spawnParticle(Particle.PORTAL, l.add(0, 1, 0), 100);
                        } else {
                            nohome = false;
                            util.sendActionbarMessage(P, ChatColor.GRAY + "你離家太遠了，無法傳送回家");
                        }
                    }

                    if (nohome) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你還沒有家喔! 找張床睡吧 OWO");
                    }
                } else if (ItemDisplayName.contains("回到傳送前")) {
                    P.closeInventory();

                    if (p.getPlayerBackLocation() == null) {
                        util.sendActionbarMessage(P, ChatColor.GOLD + "" + ChatColor.BOLD + "您的上一個位置不存在或不在該分流");
                        P.playSound(P.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, SoundCategory.BLOCKS, 10, 1);
                        return;
                    }
                    Location now = P.getLocation();
                    P.teleport(p.getPlayerBackLocation());
                    p.setPlayerBackLocation(now);
                    p.getPlayerBackLocation().getWorld().spawnParticle(Particle.PORTAL,
                            p.getPlayerBackLocation().add(0, 1, 0), 100);

                } else if (ItemDisplayName.contains("安靜模式")) {
                    P.closeInventory();
                    for (Map.Entry<String, chatchannel> entry : chatchannels.entrySet()) {
                        chatchannel cc = entry.getValue();
                        if (cc.member.contains(P)) {
                            cc.member.remove(P);
                            P.sendMessage(ChatColor.GRAY + "已經從文字聊天頻道 " + ChatColor.WHITE + cc.channelname
                                    + ChatColor.GRAY + " 離開。");
                        }
                    }
                } else if (ItemDisplayName.contains("聊天頻道設定")) {
                    P.closeInventory();
                    for (int j = 0; j < 20; j++) {
                        P.sendMessage(" ");
                    }
                    for (Map.Entry<String, chatchannel> entry : chatchannels.entrySet()) {
                        chatchannel cc = entry.getValue();
                        if (cc.member.contains(P)) {
                            TextComponent head = new TextComponent(ChatColor.GREEN + cc.channelname);
                            head.toPlainText();
                            head.addExtra(textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[退出頻道]",
                                    "點擊退出 " + cc.channelname, "/chatchannel " + cc.channelname));
                            P.spigot().sendMessage(head);
                        } else {
                            TextComponent head = new TextComponent(ChatColor.GRAY + cc.channelname);
                            head.toPlainText();
                            head.addExtra(textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[加入頻道]",
                                    "點擊加入 " + cc.channelname, "/chatchannel " + cc.channelname));
                            P.spigot().sendMessage(head);
                        }
                    }
                    P.sendMessage("");
                } else if (ItemDisplayName.contains("設定顯示粒子特效")) {
                    P.closeInventory();
                    for (int j = 0; j < 20; j++) {
                        P.sendMessage(" ");
                    }

                    if (p.particle.equals("no") || p.particle.equals("null")) {
                        P.sendMessage(ChatColor.RED + "你沒有粒子特效，去加值商城挑選喜歡的吧!");
                        return;
                    }

                    for (String s : p.particle.split(",")) {
                        ChatColor color = ChatColor.GRAY;
                        boolean enable = false;
                        if (s.split(":")[0].equals("1")) {
                            color = ChatColor.GREEN;
                            enable = true;
                        }
                        String particledata = s.split(":")[1];
                        String particlename = "";
                        if (particledata.split("\\(")[0].equals("REDSTONE"))
                            particlename = "紅石粉塵粒子尾流" + particledata.replace("REDSTONE", "");
                        else if (particledata.split("\\(")[0].equals("REDSTONERINGS"))
                            particlename = "紅石粉塵粒子環繞" + particledata.replace("REDSTONERINGS", "");
                        else if (particledata.split("\\(")[0].equals("FLAME"))
                            particlename = "火焰粒子尾流" + particledata.replace("FLAME", "");
                        else if (particledata.split("\\(")[0].equals("FLAMERINGS"))
                            particlename = "火焰粒子環繞" + particledata.replace("FLAMERINGS", "");

                        TextComponent head = new TextComponent(color + particlename + " ");
                        if (enable) {
                            head.addExtra(textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[關閉粒子特效]",
                                    "點擊關閉 " + particlename, "/setparticle " + particledata));
                        } else {
                            head.addExtra(textcomp(ChatColor.WHITE + "" + ChatColor.UNDERLINE + "[開啟粒子特效]",
                                    "點擊開啟 " + particlename, "/setparticle " + particledata));
                        }

                        P.spigot().sendMessage(head);

                    }
                    P.sendMessage("");
                }
            }
        }
    }

    /** 開啟玩家上架的商品介面 */
    public void openshop_my_item(Player P) {

        player p = players.get(P.getName());

        // 列出自己的商品
        List<shop_sell_item> itemlist = new ArrayList<>();
        for (Map.Entry<Integer, shop_sell_item> entry : shop_sell.entrySet()) {
            shop_sell_item item = entry.getValue();
            if (item.owner.equals(p.id))
                itemlist.add(item);
        }

        // 增加商品敘述並加入到介面
        int slot = 0;
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BLACK + "我上架的商品");
        for (shop_sell_item item : itemlist) {

            if (slot == 53 && itemlist.size() > 54) {
                ItemStack stack = new ItemStack(Material.RED_WOOL);
                ItemMeta im = stack.getItemMeta();
                im.setDisplayName(ChatColor.GRAY + "還有 " + (itemlist.size() - 53) + " 項商品 ...");
                im.setLore(Arrays.asList(ChatColor.GRAY + "你上架太多商品啦!", ChatColor.GRAY + "請把一些賣不出去的商品下架"));
                inv.setItem(53, stack);
                break;
            }

            ItemStack stack = new ItemStack(item.item);
            stack.setAmount(1);
            ItemMeta im = stack.getItemMeta();
            if (item.item.getItemMeta().getDisplayName() != null)
                im.setDisplayName(item.item.getItemMeta().getDisplayName());
            if (item.item.getItemMeta().getLore() != null) {
                List<String> lore = item.item.getItemMeta().getLore();
                lore.add(" ");
                lore.add(" ");
                lore.add(ChatColor.GRAY + "商品編號: " + ChatColor.WHITE + item.id);
                lore.add(ChatColor.GRAY + "上架時間: " + ChatColor.WHITE + item.time);
                lore.add(ChatColor.AQUA + "商品定價: " + ChatColor.WHITE + "$ " + item.price);
                lore.add(ChatColor.AQUA + "剩餘數量: " + ChatColor.WHITE + item.amount);

                im.setLore(lore);
            } else {
                im.setLore(Arrays.asList(" ", ChatColor.GRAY + "商品編號: " + ChatColor.WHITE + item.id,
                        ChatColor.GRAY + "上架時間: " + ChatColor.WHITE + item.time,
                        ChatColor.AQUA + "商品定價: " + ChatColor.WHITE + "$ " + item.price,
                        ChatColor.AQUA + "剩餘數量: " + ChatColor.WHITE + item.amount));
            }
            stack.setItemMeta(im);
            inv.setItem(slot, stack);
            slot++;
        }
        P.openInventory(inv);
    }

    /** 交易所介面點擊事件 */
    @EventHandler
    public void click_on_shop(InventoryClickEvent e) {
        Player P = (Player) e.getWhoClicked();
        player p = players.get(P.getName());
        String title = e.getView().getTitle();

        if (title.contains("交易所") || title.contains("我上架的商品"))
            e.setCancelled(true);
        else
            return;

        int page = 0;
        if (title.contains("交易所"))
            page = Integer.parseInt(title.split("交易所:")[1]);

        if (e.getRawSlot() >= 0 && e.getRawSlot() < 54 && e.getInventory().getItem(e.getRawSlot()) != null
                && e.getInventory().getItem(e.getRawSlot()).hasItemMeta()) {

            if (e.getSlot() == 45 && title.contains("交易所") && e.getInventory().getItem(e.getRawSlot()).getItemMeta()
                    .getDisplayName().equals(ChatColor.GRAY + "查看我上架的商品")) {
                P.closeInventory();
                openshop_my_item(P);
            } else if (e.getSlot() == 45 && title.contains("交易所") && e.getInventory().getItem(e.getRawSlot())
                    .getItemMeta().getDisplayName().equals(ChatColor.GRAY + "上一頁")) {
                P.closeInventory();
                P.performCommand("shop buy " + (page - 1));
            } else if (e.getSlot() == 53 && title.contains("交易所") && e.getInventory().getItem(e.getRawSlot())
                    .getItemMeta().getDisplayName().equals(ChatColor.GRAY + "下一頁")) {
                P.closeInventory();
                P.performCommand("shop buy " + (page + 1));
            }
        }

        if (e.getSlot() >= 0 && e.getClickedInventory().getItem(e.getSlot()) != null
                && e.getClickedInventory().getItem(e.getSlot()).getItemMeta().getLore() != null) {
            int id = 0;
            for (String s : e.getClickedInventory().getItem(e.getSlot()).getItemMeta().getLore()) {
                if (s.contains("商品編號")) {
                    id = Integer.parseInt(s.split("f")[1]);
                }
            }

            if (shop_sell.get(id).owner.equals(p.id)) {

                if (P.getInventory().firstEmpty() == -1) {
                    P.sendMessage("你的包包已經滿了，下架商品失敗");
                    return;
                }

                ItemStack item = shop_sell.get(id).item;
                if (shop_sell.get(id).getamount() > 64) {
                    item.setAmount(64);
                    P.getInventory().addItem(item);
                    P.updateInventory();
                    P.sendMessage("已經將該商品下架一組");
                    P.closeInventory();
                    shop_sell.get(id).amount -= 64;
                    // 上傳更新後的商品資料， 5 ticks 之後同步其他分流
                    datasync.upload_shop(id);
                    final int final_id = id;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                PMH.SendPluginMessage("mainsystem_downloadshop", "%shop%" + final_id + "%shop%");
                            } catch (Exception exc) {

                            }
                        }
                    }.runTaskLater(this, 5);
                    return;
                } else {
                    item.setAmount(shop_sell.get(id).amount);
                    P.getInventory().addItem(item);
                    P.updateInventory();
                    P.closeInventory();
                    P.sendMessage("已經將該商品下架");
                    try {
                        Connection conn = null;
                        Class.forName("com.mysql.jdbc.Driver");
                        String datasource = "jdbc:mysql://localhost/mcserver?user=shopuploader&password=Ken3228009!&useSSL=false";
                        conn = DriverManager.getConnection(datasource);
                        Statement st2 = conn.createStatement();
                        st2.execute("DELETE FROM `shop_sell` WHERE `id`=" + id);
                        shop_sell.remove(id);
                        // 5 ticks 之後同步其他分流
                        final int final_id = id;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    PMH.SendPluginMessage("mainsystem_downloadshop", "%shop%" + final_id + "%shop%");
                                } catch (Exception exc) {

                                }
                            }
                        }.runTaskLater(this, 5);
                        conn.close();
                        Bukkit.getConsoleSender()
                                .sendMessage(ChatColor.GREEN + "[交易所] 商品編號 " + id + " 已經被賣家 " + p.id + " 下架。");
                    } catch (Exception exc) {
                        Bukkit.getConsoleSender()
                                .sendMessage(ChatColor.RED + "[MySQL Error] when sell item : " + exc.getMessage());
                    }
                    return;
                }

            }

            p.ChatingWithNPC = true;
            p.strigns = "shop,buy," + Integer.toString(id);
            P.closeInventory();
            P.sendMessage("請輸入購買數量: ");
            P.spigot().sendMessage(textcomp("[取消對話]", "點擊取消對話", "/chat cancel"));
        }
    }

    /** Tab Compelete 回傳所有分流玩家列表 (對話時) */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (cmd.getName().equalsIgnoreCase("m") || cmd.getName().equalsIgnoreCase("pay")) {
            if (args.length == 1) {
                ArrayList<String> list = new ArrayList<String>();

                if (!args[0].equals("")) {
                    for (Map.Entry<String, Integer> entry : allplayers.entrySet()) {
                        String s = entry.getKey();
                        if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                            list.add(s);
                        }
                    }
                } else {
                    for (Map.Entry<String, Integer> entry : allplayers.entrySet()) {
                        String s = entry.getKey();
                        list.add(s);
                    }
                }

                Collections.sort(list);

                return list;
            }
        }

        return null;
    }

    /** 隨機給予機率性特殊道具 */
    public void giveRandomItem(Player P) {

        // 背包滿了直接取消
        if (P.getInventory().firstEmpty() == -1) {
            return;
        }

        // 第一個隨機值 0~999 : 判定要給予第幾階的物品
        int random = (int) (Math.random() * 1000);

        // 997 ~ 999 : 第三階物品 - 0.3%
        if (random >= 997) {

            // 第二個隨機值 0~5 判斷要給第幾個第三階物品
            random = (int) (Math.random() * 6);

            // 五本 III 特殊附魔
            for (Map.Entry<String, ItemStack> entry : items.booklist.entrySet()) {
                if (entry.getKey().contains("III")) {
                    if (random <= 0) {
                        P.getInventory().addItem(entry.getValue());
                        Bukkit.getConsoleSender()
                                .sendMessage(ChatColor.GREEN + "[機率給予] " + ChatColor.WHITE + P.getDisplayName()
                                        + ChatColor.WHITE + " 得到了 " + entry.getValue().getItemMeta().getDisplayName());
                        return;
                    }
                    random--;
                }
            }

            // 轉職卷
            P.getInventory().addItem(items.ResetClassPaper);
            P.sendMessage("你釣到了一張 " + items.ResetClassPaper.getItemMeta().getDisplayName());
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[機率給予] " + ChatColor.WHITE + P.getDisplayName()
                    + ChatColor.WHITE + " 得到了 " + items.ResetClassPaper.getItemMeta().getDisplayName());

            // 986 ~ 996 : 第二階物品 - 1.1%
        } else if (random > 985) {

            // 第二個隨機值 0~5 判斷要給第幾個第二階物品
            random = (int) (Math.random() * 6);

            // 五本 II 特殊附魔
            for (Map.Entry<String, ItemStack> entry : items.booklist.entrySet()) {
                if (entry.getKey().contains("II") && !entry.getKey().contains("III")) {
                    if (random <= 0) {
                        P.getInventory().addItem(entry.getValue());
                        Bukkit.getConsoleSender()
                                .sendMessage(ChatColor.GREEN + "[機率給予] " + ChatColor.WHITE + P.getDisplayName()
                                        + ChatColor.WHITE + " 得到了 " + entry.getValue().getItemMeta().getDisplayName());
                        return;
                    }
                    random--;
                }
            }

            // 任務卷
            P.getInventory().addItem(items.MissionPaper);
            P.sendMessage("你釣到了一張 " + items.MissionPaper.getItemMeta().getDisplayName());
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[機率給予] " + ChatColor.WHITE + P.getDisplayName()
                    + ChatColor.WHITE + " 得到了 " + items.MissionPaper.getItemMeta().getDisplayName());

            // 971 ~ 985 : 第一階物品 - 1.5%
        } else if (random > 970) {

            // 第二個隨機值 0~4 判斷要給第幾個第一階物品
            random = (int) (Math.random() * 5);

            // 五本 I 特殊附魔
            for (Map.Entry<String, ItemStack> entry : items.booklist.entrySet()) {
                if (entry.getKey().contains("I") && !entry.getKey().contains("II")) {
                    if (random <= 0) {
                        P.getInventory().addItem(entry.getValue());
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[機率給予] " + P.getDisplayName() + " 得到了 "
                                + entry.getValue().getItemMeta().getDisplayName());
                        return;
                    }
                    random--;
                }
            }
        }

    }

    public static void sendMessageToAllPlyaer(TextComponent... msgs) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < msgs.length; i++) {
                if (!players.get(player.getName()).quiet)
                    player.spigot().sendMessage(msgs[i]);
            }
        }
    }

    /** 防止新玩家跑到原本世界而不是傳送小鎮 */
    @EventHandler
    public void avoidPlayerInWrongWorld(PlayerMoveEvent e) {
        if (config.getInt("server") == 0 && e.getTo().getWorld().getName().equals("world")) {
            e.getPlayer().performCommand("spawn");
        }
        if (config.getInt("server") == 0 && e.getTo().getWorld().getName().equals("FFA") && e.getTo().getX() < 130) {
            e.getPlayer().performCommand("spawn");
        }
    }

    /** 右鍵點擊使用特殊物品 */
    @EventHandler
    public void useItem(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.HAND)
            return;
        Action act = e.getAction();
        Player P = e.getPlayer();
        player p = players.get(P.getName());

        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

        // 使用職業重置卷
        if ((act == Action.RIGHT_CLICK_BLOCK || act == Action.RIGHT_CLICK_AIR)
                && item.getType().toString().contains("PAPER") && item.getItemMeta().getDisplayName().contains("職業重置卷")
                && item.getItemMeta().hasLore()) {

            // 無業者不可使用
            if (p.cla.contains("無業者")) {
                P.sendMessage(ChatColor.RED + "無業者不用重置職業!");
                return;
            }

            // 第一次按將發送警告標語
            if (!p.wanttoresetclass) {
                P.sendMessage(" ");
                P.sendMessage(ChatColor.GOLD + "確定要進行角色職業重置嗎?");
                P.sendMessage(ChatColor.GOLD + "角色將回歸無業者身分，等級回歸一等");
                P.sendMessage(ChatColor.GOLD + "角色職業重置不會影響熟練度累計");
                P.sendMessage(ChatColor.GOLD + "十秒內再次使用本卷將進行重置。");
                P.sendMessage(" ");
                p.wanttoresetclass = true;

                new BukkitRunnable() {

                    @Override
                    public void run() {

                        if (p.wanttoresetclass) {
                            P.sendMessage(ChatColor.GOLD + "10秒到了");
                            p.wanttoresetclass = false;
                        }

                    }
                }.runTaskLater(this, 200);
                return;
            }

            // 發送訊息
            P.sendTitle("角色重置!", ChatColor.GRAY + "你現在是 無業者 Lv.1", 10, 100, 10);

            // 消耗道具
            P.getInventory().getItemInMainHand().setAmount(0);

            // 修改 cla 欄位
            if (p.cla.contains(",")) {
                String oricla = p.cla.split(",")[0];
                p.cla = p.cla.replace(oricla + ",", "無業者,");
            } else {
                p.cla = "無業者";
            }

            // 經驗等級重置
            p.lv = 1;
            p.exp = 0;

            // 上傳玩家資料
            p.upload();

            // 重新整理顯示資訊
            reloaddata(p.id);

            return;
        }

        // 使用任務卷
        if ((act == Action.RIGHT_CLICK_BLOCK || act == Action.RIGHT_CLICK_AIR)
                && item.getType().toString().contains("PAPER") && item.getItemMeta().getDisplayName().contains("屠龍者挑戰")
                && item.getItemMeta().hasLore()) {

            if (!p.wanttoentermission) {

                // 判斷隊員有沒有人有正在進行的副本
                if (p.party.length() > 0) {
                    for (Player PP : partys.get(p.party).member) {
                        player pp = players.get(PP.getName());

                        if (!pp.settings.containsKey("mission")) {
                            pp.settings.put("mission", "0");
                        }

                        if (!pp.settings.get("mission").equals("0")) {
                            util.sendActionbarMessage(P, ChatColor.RED + "有隊員正在其他挑戰副本中");
                            return;
                        }

                    }
                } else {

                    if (!p.settings.containsKey("mission")) {
                        p.settings.put("mission", "0");
                    }

                    if (!p.settings.get("mission").equals("0")) {
                        util.sendActionbarMessage(P, ChatColor.RED + "你正在其他的副本中");
                        return;
                    }
                }

                if (p.party.length() == 0 || partys.get(p.party).member.size() == 1) {
                    P.sendMessage(ChatColor.GOLD + "你確定要單人前往屠龍者挑戰副本嗎?");
                    P.sendMessage(ChatColor.GOLD + "你將會被傳送至背包資料不同步的副本分流");
                    P.sendMessage(ChatColor.GOLD + "開始期限為一個禮拜的屠龍者挑戰");
                    P.sendMessage(ChatColor.GOLD + "期限內可以透過手機自由切換分流");
                    P.sendMessage(ChatColor.GOLD + "十秒內再次使用本卷即出發前往屠龍者挑戰副本");
                    p.wanttoentermission = true;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (p.wanttoentermission) {
                                P.sendMessage(ChatColor.GOLD + "10秒到了");
                                p.wanttoentermission = false;
                            }
                        }
                    }.runTaskLater(this, 200);
                    return;
                } else if (p.party.length() > 0) {
                    if (partys.get(p.party).cap == P) {

                        if (partys.get(p.party).member.size() > 4) {
                            util.sendActionbarMessage(P, ChatColor.RED + "本副本的人數限制為 4 人");
                            return;
                        }

                        P.sendMessage(ChatColor.GOLD + "你確定要和當前隊伍的玩家前往屠龍者挑戰副本嗎?");
                        P.sendMessage(ChatColor.GOLD + "你將會被傳送至背包資料不同步的副本分流");
                        P.sendMessage(ChatColor.GOLD + "開始期限為一個禮拜的屠龍者挑戰");
                        P.sendMessage(ChatColor.GOLD + "期限內可以透過手機自由切換分流");
                        P.sendMessage(ChatColor.GOLD + "十秒內再次使用本卷即出發前往屠龍者挑戰副本");
                        p.wanttoentermission = true;
                        new BukkitRunnable() {
                            @Override
                            public void run() {

                                if (p.wanttoentermission) {
                                    P.sendMessage(ChatColor.GOLD + "10秒到了");
                                    p.wanttoentermission = false;
                                }

                            }
                        }.runTaskLater(this, 200);
                        return;
                    } else {
                        util.sendActionbarMessage(P, ChatColor.RED + "只有隊長可以出發前往任務副本");
                        return;
                    }
                }

            }

            P.getInventory().getItemInMainHand().setAmount(P.getInventory().getItemInMainHand().getAmount() - 1);

            // 上傳到資料庫
            Connection conn = null;
            int id = 0;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String datasource = "jdbc:mysql://localhost/mcserver?user=regionuploader&password=Ken3228009!&useSSL=false";
                conn = DriverManager.getConnection(datasource);
                Statement st = conn.createStatement();
                st.execute("SELECT * FROM `mission` ORDER BY `id` DESC LIMIT 1;");
                ResultSet rs = st.getResultSet();
                while (rs.next()) {
                    id = rs.getInt("id") + 1;
                }

                // 建構挑戰者玩家清單
                String players_s = "";
                if (p.party.length() > 0) {
                    for (Player PP : partys.get(p.party).member) {
                        player pp = players.get(PP.getName());
                        pp.settings.put("mission", Integer.toString(id));
                        players_s += pp.id + ",";
                    }
                    players_s = players_s.substring(0, players_s.length() - 1);
                } else {
                    players_s = p.id;
                    p.settings.put("mission", Integer.toString(id));
                }

                st.execute("INSERT INTO mission (id,players,starttime,endtime) VALUES (" + id + ",'" + players_s + "','"
                        + util.now() + "','進行中');");
                conn.close();
            } catch (Exception exc) {
                StringWriter errors = new StringWriter();
                exc.printStackTrace(new PrintWriter(errors));
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[資料庫] 開始副本任務時發生問題 : " + errors.toString());
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException exc) {
                    }
                }
            }

            // 3 秒後將玩家移動分流
            util.sendActionbarMessage(P, "副本資料生成中 ...");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (p.party.length() > 0) {
                        for (Player PP : partys.get(p.party).member) {
                            PP.performCommand("connect server_11");
                            App.chatchannels.get("全伺服頻道").sendmessage(ChatColor.DARK_AQUA + " " + ChatColor.UNDERLINE
                                    + "副本挑戰" + ChatColor.WHITE + " 玩家 " + PP.getName() + " 出發前往屠龍者挑戰副本了，為他們加油吧 !");
                            App.PMH.sendAllServerBrocast(ChatColor.DARK_AQUA + " " + ChatColor.UNDERLINE + "副本挑戰 "
                                    + ChatColor.WHITE + " 玩家 " + PP.getName() + " 出發前往屠龍者挑戰副本了，為他們加油吧 !");
                        }
                    } else {
                        App.chatchannels.get("全伺服頻道").sendmessage(ChatColor.DARK_AQUA + " " + ChatColor.UNDERLINE
                                + "副本挑戰" + ChatColor.WHITE + " 玩家 " + p.id + " 出發前往屠龍者挑戰副本了，為他們加油吧 !");
                        App.PMH.sendAllServerBrocast(ChatColor.DARK_AQUA + " " + ChatColor.UNDERLINE + "副本挑戰 "
                                + ChatColor.WHITE + " 玩家 " + p.id + " 出發前往屠龍者挑戰副本了，為他們加油吧 !");
                        P.performCommand("connect server_11");
                    }
                }
            }.runTaskLater(this, 60);

        }

        if ((act == Action.RIGHT_CLICK_BLOCK || act == Action.RIGHT_CLICK_AIR)
                && item.getType().toString().contains("PAPER") && item.getItemMeta().getDisplayName().contains("領地傳送點")
                && item.getItemMeta().hasLore()) {

            region r = inwhichregion(P.getLocation());

            if (r == null) {
                util.sendActionbarMessage(P, ChatColor.RED + "你不在領地內");
                return;
            }

            if (!r.hasPermission(p, "manage")) {
                util.sendActionbarMessage(P, ChatColor.RED + "你沒有該領地的管理權限");
                return;
            }

            r.tpx = P.getLocation().getBlockX();
            r.tpy = P.getLocation().getBlockY() + 1;
            r.tpz = P.getLocation().getBlockZ();
            util.sendActionbarMessage(P, ChatColor.GREEN + "領地 " + r.name + " 的傳送點設置成功!");
            P.getInventory().getItemInMainHand().setAmount(P.getInventory().getItemInMainHand().getAmount() - 1);
            return;

        }

        if ((act == Action.RIGHT_CLICK_BLOCK || act == Action.RIGHT_CLICK_AIR)
                && item.getType().toString().contains("PAPER") && item.getItemMeta().getDisplayName().contains("家族邀請函")
                && item.getItemMeta().hasLore()) {

            if (!p.family.equals("null")) {
                util.sendActionbarMessage(P, ChatColor.RED + "你已經有家族了，請先退出當前家族。");
                return;
            }

            String name = item.getItemMeta().getDisplayName().split(" : ")[1];

            if (!families.containsKey(name)) {
                util.sendActionbarMessage(P, ChatColor.RED + "該家族已經不存在了。");
                return;
            }

            if (p.cla.contains("無業者") && p.lv < 5) {
                util.sendActionbarMessage(P, ChatColor.RED + "" + ChatColor.BOLD + "無業者需要 5 等後才能加入家族");
                return;
            }

            families.get(name).groups.get("一般成員").members.add(p.id);
            families.get(name).upload();
            new BukkitRunnable() {

                @Override
                public void run() {
                    try {
                        PMH.SendPluginMessage("mainsystem_downloadfamily", "%family%" + name + "%family%");
                    } catch (Exception e) {

                    }
                }
            }.runTaskLater(this, 20);
            p.family = name;
            p.upload();
            reloaddata(p.id);

            P.getInventory().getItemInMainHand().setAmount(0);
            P.updateInventory();

        }

        if ((act == Action.RIGHT_CLICK_BLOCK || act == Action.RIGHT_CLICK_AIR)
                && item.getType().toString().contains("PAPER") && item.getItemMeta().getDisplayName().contains("家族創立卷")
                && item.getItemMeta().hasLore()) {

            if (p.party.length() == 0) {
                util.sendActionbarMessage(P, ChatColor.RED + "請和要一起創立家族的玩家待在同一個隊伍中!");
                return;
            }

            if (partys.get(p.party).cap != P) {
                util.sendActionbarMessage(P, ChatColor.RED + "必須是隊伍的隊長才能創立家族!");
                return;
            }

            if (partys.get(p.party).member.size() != 10 && !P.getName().equals("ken20001207")) {
                util.sendActionbarMessage(P, ChatColor.RED + "隊伍要有10個玩家才能創立家族!");
                return;
            }

            for (Player m : partys.get(p.party).member) {
                if (!players.get(m.getName()).family.equals("null")) {
                    util.sendActionbarMessage(P, ChatColor.RED + "玩家中已經有人有家族了!");
                    return;
                }
            }

            for (Map.Entry<String, family> entry : families.entrySet()) {
                if (entry.getKey().equals(p.party)) {
                    util.sendActionbarMessage(P, ChatColor.RED + "已經有這個名稱 " + entry.getKey() + " 的家族了!");
                    return;
                }
            }

            families.put(partys.get(p.party).name, new family(partys.get(p.party).name, partys.get(p.party).member, P));

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player m : partys.get(p.party).member) {
                        players.get(m.getName()).family = partys.get(p.party).name;
                        m.sendTitle("家族創立成功", "你現在是 " + partys.get(p.party).name + " 的家族成員了!", 20, 100, 20);
                        reloaddata(m.getName());
                    }

                    players.get(partys.get(p.party).cap.getName()).family = partys.get(p.party).name;
                    players.get(partys.get(p.party).cap.getName()).cla += ",家族代表";
                    players.get(partys.get(p.party).cap.getName()).displaycla = "家族代表";
                    reloaddata(partys.get(p.party).cap.getName());
                }
            }.runTaskLater(this, 10);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player m : partys.get(p.party).member) {
                        reloaddata(m.getName());
                    }
                    reloaddata(partys.get(p.party).cap.getName());
                    try {
                        PMH.SendPluginMessage("mainsystem_downloadfamily", "%family%" + p.family + "%family%");
                    } catch (Exception e) {

                    }
                }
            }.runTaskLater(this, 20);

            if (P.getName().equals("ken20001207"))
                families.get(partys.get(p.party).name).compelete = true;

            P.getInventory().getItemInMainHand().setAmount(0);
            P.updateInventory();

            return;

        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        Action act = e.getAction();
        Player P = e.getPlayer();
        player p = players.get(P.getName());

        try {

            Material type = Material.AIR;
            if (act == Action.RIGHT_CLICK_BLOCK) {
                type = e.getClickedBlock().getType();
            }

            // 領地內使用容器方塊
            if (type == Material.CHEST || type == Material.HOPPER || type == Material.JUKEBOX
                    || type == Material.SHULKER_BOX || type == Material.DISPENSER || type == Material.FURNACE) {

                Location l = e.getClickedBlock().getLocation();

                if (inwhichregion(l) != null) {
                    region r = inwhichregion(l);

                    if (!r.hasPermission(p, "chest")) {
                        e.setCancelled(true);
                        util.sendActionbarMessage(P, ChatColor.RED + "你在這個領地的身分組無法使用容器類方塊。");
                    }
                }
            }

            // 流體放置事件
            if (act == Action.RIGHT_CLICK_BLOCK
                    && (P.getInventory().getItemInMainHand().getType() == Material.WATER_BUCKET
                            || P.getInventory().getItemInMainHand().getType() == Material.LAVA_BUCKET)) {

                if (getConfig().getInt("server") == 0)
                    e.setCancelled(true);

                Location l = e.getClickedBlock().getLocation();

                if (inwhichregion(l) != null) {
                    region r = inwhichregion(l);
                    // 沒有放置權限 不能放流體出來
                    if (!r.hasPermission(p, "place")) {
                        e.setCancelled(true);
                        util.sendActionbarMessage(P, ChatColor.RED + "你在這個領地的身分組無法放置方塊，流體也不例外。");
                    }
                }

            }

            if (act == Action.RIGHT_CLICK_BLOCK
                    && P.getInventory().getItemInMainHand().getType().toString().contains("BANNER")
                    && P.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("領地旗幟")
                    && P.getInventory().getItemInMainHand().getItemMeta().getLore() != null) {

                if (!e.getClickedBlock().getLocation().add(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
                    util.sendActionbarMessage(P, ChatColor.RED + "" + ChatColor.BOLD + "放旗子的地方必須是空氣");
                    e.setCancelled(true);
                    return;
                }

                if (config.getInt("server") == 0 && !p.id.equals("ken20001207")) {
                    util.sendActionbarMessage(P, ChatColor.RED + "" + ChatColor.BOLD + "該分流無法設置領地");
                    e.setCancelled(true);
                    return;
                }

                String region_name = "";
                String region_lord = "";
                int region_width = -1;

                String lores = P.getInventory().getItemInMainHand().getItemMeta().getLore().toString();
                for (String lore : lores.split(",")) {
                    if (lore.contains("領地名稱"))
                        region_name = lore.toString().split("f")[1];
                    else if (lore.contains("領主名稱"))
                        region_lord = lore.toString().split("f")[1];
                    else if (lore.contains("領地邊長"))
                        region_width = Integer.parseInt(lore.toString().split("f")[1]);
                }

                if (p.getPlayerRegionAvailible() - p.getUsingRegionSize() < region_width * region_width) {
                    util.sendActionbarMessage(P, ChatColor.RED + "" + ChatColor.BOLD + "可用領地格數不足");
                    e.setCancelled(true);
                    return;
                }

                if (Bukkit.getPlayer(region_lord).getUniqueId() != P.getUniqueId()) {
                    util.sendActionbarMessage(P, ChatColor.RED + "只有領主本人可以宣告領地。");
                    e.setCancelled(true);
                    return;
                } else {

                    Connection conn = null;
                    try {
                        int id = -1;
                        Class.forName("com.mysql.jdbc.Driver");
                        String datasource = "jdbc:mysql://localhost/mcserver?user=regiondownloader&password=Ken3228009!&useSSL=false";
                        conn = DriverManager.getConnection(datasource);
                        Statement st = conn.createStatement();
                        st.execute("SELECT * FROM `regions` ORDER BY `id` DESC LIMIT 1");
                        ResultSet rs = st.getResultSet();
                        while (rs.next()) {
                            id = rs.getInt("id") + 1;
                        }
                        int maxx = (int) e.getClickedBlock().getLocation().getX() + (region_width / 2);
                        int maxz = (int) e.getClickedBlock().getLocation().getZ() + (region_width / 2);
                        int minx = (int) e.getClickedBlock().getLocation().getX() - (region_width / 2);
                        int minz = (int) e.getClickedBlock().getLocation().getZ() - (region_width / 2);
                        World world = e.getClickedBlock().getLocation().getWorld();

                        region r = inwhichregion(new Location(world, maxx, 60, maxz));
                        if (r != null) {
                            util.sendActionbarMessage(P, ChatColor.RED + "該位置會重疊到領主 " + r.lordname + " 的領地 " + r.name);
                            e.setCancelled(true);
                            return;
                        }

                        r = inwhichregion(new Location(world, maxx, 60, minz));
                        if (r != null) {
                            util.sendActionbarMessage(P, ChatColor.RED + "該位置會重疊到領主 " + r.lordname + " 的領地 " + r.name);
                            e.setCancelled(true);
                            return;
                        }

                        r = inwhichregion(new Location(world, minx, 60, maxz));
                        if (r != null) {
                            util.sendActionbarMessage(P, ChatColor.RED + "該位置會重疊到領主 " + r.lordname + " 的領地 " + r.name);
                            e.setCancelled(true);
                            return;
                        }

                        r = inwhichregion(new Location(world, minx, 60, minz));
                        if (r != null) {
                            util.sendActionbarMessage(P, ChatColor.RED + "該位置會重疊到領主 " + r.lordname + " 的領地 " + r.name);
                            e.setCancelled(true);
                            return;
                        }

                        if (p.regionlist.size() >= p.maxregions) {
                            P.sendMessage(ChatColor.RED + "您的領地已經達到數量限制，刪除不必要的領地或至加值商城選購領地擴充卷。");
                            e.setCancelled(true);
                            return;
                        }

                        st.execute(
                                "INSERT INTO `regions`(`server`, `tp_x`, `tp_y`, `tp_z`, `id`, `name`, `lord`, `centerx`, `centerz`, `width`, `size`, `maxx`, `minx`, `maxz`, `minz`,`lordname`,`permission`,`flagy`,`world`) VALUES ("
                                        + getConfig().getInt("server") + "," + e.getClickedBlock().getLocation().getX()
                                        + "," + e.getClickedBlock().getLocation().getY() + 1 + ","
                                        + e.getClickedBlock().getLocation().getZ() + "," + Integer.toString(id) + ",'"
                                        + region_name + "','" + P.getUniqueId() + "','"
                                        + e.getClickedBlock().getLocation().getX() + "','"
                                        + e.getClickedBlock().getLocation().getZ() + "','" + region_width + "','"
                                        + Integer.toString(region_width * region_width) + "','" + maxx + "','" + minx
                                        + "','" + maxz + "','" + minz + "','" + P.getName()
                                        + "','{\"groups\": [{\"name\": \"其他所有人\", \"player\": [\"all\"], \"permission\": {\"break\": 0, \"enter\": 1,\"chest\": 0,\"place\": 0}}]}','"
                                        + e.getClickedBlock().getLocation().getY() + "','"
                                        + e.getClickedBlock().getLocation().getWorld().getName() + "')");

                        conn.close();
                        util.sendActionbarMessage(P, ChatColor.GREEN + "領地宣告成功, 您已成為 " + ChatColor.WHITE + region_name
                                + ChatColor.GREEN + " 的領主。");

                        // 移除手上的旗幟 (避免旗子還在造成洗錢)
                        if (P.getInventory().getItemInMainHand() != null) {
                            P.getInventory().getItemInMainHand().setAmount(0);
                        }

                        // 強制產生旗幟 (避免沒插上旗子但領地創立成功)
                        if (!e.getClickedBlock().getLocation().add(0, 1, 0).getBlock().getType().toString()
                                .contains("BANNER")) {
                            e.getClickedBlock().getLocation().add(0, 1, 0).getBlock().setType(Material.WHITE_BANNER);
                        }

                        regions.put(id, new region(id));
                        r = regions.get(id);
                        r.upload();
                        PMH.SendPluginMessage("mainsystem_downloadregion", "%region%" + r.id + "%region%");
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[領地] 玩家 " + P.getName()
                                + " 剛剛宣告了一個新領地 " + r.name + " (" + r.id + ")");
                        if (r.server == getConfig().getInt("server")) {
                            Hologram hologram = HologramsAPI.createHologram(this, new Location(Bukkit.getWorld(r.world),
                                    r.centerx + 0.5, r.flagy + 3.5, r.centerz + 0.5));
                            hologram.appendTextLine("[領地旗幟] " + ChatColor.GREEN + r.name);
                            if (r.lordname.contains("family:"))
                                hologram.appendTextLine(ChatColor.GRAY + "家族 " + r.name.split("family:")[1] + " 的家族領地");
                            else
                                hologram.appendTextLine(ChatColor.GRAY + "領主 " + r.lordname + " 的領地");
                        }

                        P.playSound(P.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 10, 5);
                        p.regionlist.put(r.id, r);

                    } catch (Exception exc) {
                        StringWriter errors = new StringWriter();
                        exc.printStackTrace(new PrintWriter(errors));
                        Bukkit.getConsoleSender()
                                .sendMessage(ChatColor.RED + "[資料庫] 玩家 " + p.id + " 宣告領地時發生問題 : " + errors.toString());
                    }

                }
            }

            if (act == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType().toString().contains("BED")
                    && !e.getClickedBlock().getType().toString().contains("ROCK")) {

                if (config.getInt("server") != 11) {
                    P.sendTitle(ChatColor.GREEN + "已設置家", "日後當你在這個分流，可以透過手機傳送回家", 10, 30, 10);
                    Location l = e.getClickedBlock().getLocation();
                    p.home = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ()
                            + "," + config.getString("server");
                    return;
                }
            }

            // 右鍵領地旗幟 查看領地資訊
            if (act == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType().toString().contains("BANNER")) {

                region r;
                if (inwhichregion(e.getClickedBlock().getLocation()) != null) {
                    r = inwhichregion(e.getClickedBlock().getLocation());
                } else
                    return;

                if (e.getClickedBlock().getLocation().getBlockX() == r.centerx
                        && e.getClickedBlock().getLocation().getBlockZ() == r.centerz
                        && e.getClickedBlock().getLocation().getBlockY() == r.flagy + 1) {
                    r.showRegionInfo(P);
                }
            }
        } catch (Exception ex) {
            Player Player = e.getPlayer();
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            ex.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            util.pleaseReport(Player);
        }
    }

    // 方塊破壞領地判斷
    @EventHandler
    public void CheckRegionWhenBlockBreak(BlockBreakEvent e) {

        if (e.isCancelled())
            return;

        Player P = e.getPlayer();
        player p = players.get(P.getName());
        Block b = e.getBlock();
        Location l = b.getLocation();

        try {

            // 傳送小鎮禁止破壞
            if (getConfig().getInt("server") == 0 && !P.getName().equals("ken20001207")) {
                util.sendActionbarMessage(P, ChatColor.RED + "傳送小鎮無法破壞!");
                e.setCancelled(true);
                return;
            }

            // 領地判斷
            if (inwhichregion(l) != null) {
                region r = inwhichregion(l);

                // 沒有破壞權限
                if (!r.hasPermission(p, "break")) {
                    e.setCancelled(true);
                    util.sendActionbarMessage(P, ChatColor.RED + "你在這個領地的身分組無法破壞方塊。");
                    return;
                }

            }

        } catch (Exception exc) {
            Player Player = e.getPlayer();
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            exc.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            util.pleaseReport(Player);
        }

    }

    // 禁止挖掉領地旗幟下方方塊
    @EventHandler
    public void BlockUnderBannerBreakEvent(BlockBreakEvent e) {
        if (e.getBlock().getLocation().add(0, 1, 0).getBlock().getType().toString().contains("BANNER"))
            e.setCancelled(true);
    }

    // 刪除領地事件
    @EventHandler
    public void PlayerDeleteRegionEvent(BlockBreakEvent e) {

        if (e.isCancelled())
            return;

        Player P = e.getPlayer();
        player p = players.get(P.getName());
        Block b = e.getBlock();
        Location l = b.getLocation();

        // 破壞旗幟有可能是要刪除領地
        if (b.getType().toString().contains("BANNER")) {

            // 破壞的旗幟在領地內
            if (inwhichregion(l) != null) {
                region r = inwhichregion(l);

                // 破壞的旗幟是該領地的領地旗幟
                if (e.getBlock().getLocation().getBlockX() == r.centerx
                        && e.getBlock().getLocation().getBlockZ() == r.centerz) {

                    // 如果玩家有管理(刪除)領地的權限
                    if (r.hasPermission(p, "manage")) {

                        // 提出警告
                        if (!p.removeingregion) {
                            e.setCancelled(true);
                            P.sendMessage(" ");
                            P.sendMessage(ChatColor.GOLD + "請注意，破壞領地旗幟代表你將移除這個領地");
                            P.sendMessage(ChatColor.GOLD + "該領地將直接移除，旗幟無法再次使用");
                            P.sendMessage(ChatColor.GOLD + "您必須重新申請旗幟及重新設定身分組權限");
                            P.sendMessage(ChatColor.GOLD + "10秒內再次破壞旗幟代表你已經詳細閱讀本說明。");
                            p.removeingregion = true;

                            // 10 秒後取消刪除領地
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (p.removeingregion) {
                                        P.sendMessage(" ");
                                        P.sendMessage(ChatColor.GOLD + "十秒到了。");
                                        p.removeingregion = false;
                                    }
                                }
                            }.runTaskLater(this, 200);
                            return;
                        }

                        p.removeingregion = false;

                        // 從資料庫刪除領地
                        Connection conn = null;
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            String datasource = "jdbc:mysql://localhost/mcserver?user=regionuploader&password=Ken3228009!&useSSL=false";
                            conn = DriverManager.getConnection(datasource);
                            Statement st = conn.createStatement();
                            st.execute("DELETE FROM `regions` WHERE `id`=" + r.id);
                            conn.close();

                            // 伺服端發送紀錄
                            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[領地] 玩家 " + P.getName()
                                    + " 剛剛刪除了領地 " + r.name + " (" + r.id + ")");

                            // 玩家發送通知
                            util.sendActionbarMessage(P, ChatColor.GREEN + "領地 " + r.name + " 已經刪除。");

                            // 修改領地內玩家的 inregion 避免 null 例外
                            for (Player theplayer : Bukkit.getOnlinePlayers()) {
                                if (players.get(theplayer.getName()).inregion == r.id) {
                                    players.get(theplayer.getName()).inregion = -1;
                                    util.sendActionbarMessage(theplayer,
                                            ChatColor.YELLOW + "當前所在領地 " + r.name + " 已被刪除。");
                                }
                            }

                            // 刪除該領地旗幟的 Hologram
                            for (Hologram pe : HologramsAPI.getHolograms(this)) {
                                if (pe.getLine(0).toString().contains(r.name)
                                        && pe.getLine(1).toString().contains(p.id)) {
                                    pe.delete();
                                    break;
                                }
                            }

                            // 從玩家領地列表或家族領地列表刪除該領地
                            if (!r.lordname.contains("family:")) {
                                p.regionlist.remove(r.id);
                            } else {
                                family f = families.get(r.lordname.split(":")[1]);
                                f.regions = f.regions.replace("," + r.id, "");
                            }

                            // 透過銀行紀錄該筆退款
                            util.bankTransfer("領地大臣", p.id, r.size / 2, "領地 " + r.name + " 的刪除退款", true);

                            // 要求其他分流重載該領地
                            PMH.SendPluginMessage("mainsystem_downloadregion", "%region%" + r.id + "%region%");

                            // 不掉落方塊
                            e.setDropItems(false);

                            // 從該分流刪除領地
                            regions.remove(r.id);

                            return;

                        } catch (Exception exc) {
                            StringWriter errors = new StringWriter();
                            exc.printStackTrace(new PrintWriter(errors));
                            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[領地] 刪除領地 " + r.name + "(" + r.id
                                    + ") 時發生問題 : " + errors.toString());
                        }
                    } else {
                        util.sendActionbarMessage(P, ChatColor.RED + "你沒有刪除這個領地的權限");
                        e.setCancelled(true);
                    }
                }
            }
        }

    }

    @EventHandler
    public void explodeEvent(EntityExplodeEvent event) {
        for (Block b : event.blockList()) {
            if (inwhichregion(b.getLocation()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void fish(PlayerFishEvent e) {

        Player P = e.getPlayer();

        try {

            if (e.getCaught() == null)
                return;

            if (e.getExpToDrop() <= 0) {
                return;
            }

            String c = e.getCaught().getName();
            if (c.contains("Cod") || c.contains("Pufferfish") || c.contains("Salmon") || c.contains("fish")) {
                players.get(e.getPlayer().getName()).fish += 1;
                util.sendActionbarMessage(e.getPlayer(), "釣魚 : 生活經驗 +15 , 釣魚熟練度 +1");
                addexp(e.getPlayer().getName(), 15);

                // 機率性給予特殊道具
                if (players.get(e.getPlayer().getName()).cla.contains("漁夫")) {
                    giveRandomItem(P);
                }

            }

            // 將釣到附魔書的機率下修為 25% (釣到書就有 75%機率變成其他東西)
            Item d = (Item) e.getCaught();
            ItemStack item = d.getItemStack();
            if (item.getType().equals(Material.ENCHANTED_BOOK)) {
                int random = (int) (Math.random() * 4);
                if (random == 0) {
                    item.setType(Material.PRISMARINE_CRYSTALS);
                } else if (random == 1) {
                    item.setType(Material.PRISMARINE_SHARD);
                } else if (random == 2) {
                    item.setType(Material.GLOWSTONE_DUST);
                }
            }

        } catch (Exception ex) {
            Player Player = e.getPlayer();
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            ex.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            util.pleaseReport(Player);
        }
    }

    public static void addexp(String id, int exp) {
        try {
            players.get(id).exp += exp;
            if (players.get(id).exp >= players.get(id).lv * 1000) {
                players.get(id).exp -= players.get(id).lv * 1000;
                players.get(id).lv++;
                Bukkit.getPlayer(id).sendTitle("Level UP!", "你現在是 " + players.get(id).lv + " 等 " + players.get(id).cla,
                        20, 100, 10);
                reloaddata(id);
            }

        } catch (Exception ex) {
            Player Player = Bukkit.getPlayer(id);
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            ex.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            util.pleaseReport(Player);
        }

    }

    public TextComponent getnamehover(String ID, Boolean withtp, boolean tag) {
        try {
            Player player = Bukkit.getPlayer(ID);
            player p = players.get(ID);

            TextComponent playername;

            if (tag) {
                playername = new TextComponent(ChatColor.UNDERLINE + player.getName());
            } else {
                playername = new TextComponent(player.getDisplayName());
            }

            String regionstring = "\n";
            for (Map.Entry<Integer, region> entry : p.regionlist.entrySet()) {
                region r = entry.getValue();
                regionstring += ChatColor.WHITE + "領地 " + ChatColor.GREEN + regions.get(r.id).name + ChatColor.GRAY
                        + " (" + regions.get(r.id).size + " 平方公尺)" + ChatColor.WHITE + " 的領主\n";
            }

            String loca = "";
            if (p.inregion != -1)
                loca = ChatColor.GRAY + "\n當前位置: " + ChatColor.WHITE + "領地 " + ChatColor.GREEN
                        + regions.get(p.inregion).name + " \n" + ChatColor.WHITE;

            playername.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(player.getDisplayName() + "\n" + ChatColor.GRAY + " (真實ID " + p.id + ") "
                            + "\n\n" + ChatColor.GRAY + "當前分流 " + ChatColor.WHITE + servername + ChatColor.GRAY
                            + " 分流\n\n" + ChatColor.GRAY + "等級 " + ChatColor.WHITE + p.lv + ChatColor.GRAY + " 等\n"
                            + ChatColor.GRAY + "經驗值 " + ChatColor.WHITE + p.exp + " / " + p.lv * 1000 + ChatColor.GRAY
                            + " 點\n" + ChatColor.GRAY + "家族 " + ChatColor.WHITE + p.family.replace("null", "無家族")
                            + ChatColor.GRAY + "\n身分 " + ChatColor.WHITE + p.cla.replace(",", "、") + "\n"
                            + ChatColor.GRAY + "商城幣 " + ChatColor.WHITE + p.coin + ChatColor.GRAY + " 元\n" + loca
                            + regionstring + ChatColor.UNDERLINE + "\n( 點擊玩家名稱即可傳送悄悄話 )").create()));
            if (withtp)
                playername.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + player.getName()));

            return playername;

        } catch (Exception ex) {
            Player Player = Bukkit.getPlayer(ID);
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            ex.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            Bukkit.getScheduler().runTask(this, new Runnable() {
                public void run() {
                    Player.kickPlayer(ChatColor.RED + "您的資料出現問題，請用 discord 通知系統管理員");
                }
            });
            return textcomp("Error!", "Error!", "");
        }
    }

    // 終界不行放床
    @EventHandler
    public void cantplacebed(BlockPlaceEvent e) {
        if (e.getBlock().getType().name().contains("BED") && e.getBlock().getWorld().getName().contains("_the_end")) {
            e.setCancelled(true);
        }
    }

    // 活塞事件也要增加方塊紀錄，避免刷經驗
    @EventHandler
    public void recordonpiston(BlockPistonExtendEvent e) {
        for(Block b : e.getBlocks()) {
            new BukkitRunnable(){
                @Override
                public void run() {

                    int x = 0;
                    int y = 0;
                    int z = 0;

                    if(b.getLocation().getX() - e.getBlock().getX() < 0) x = -1;
                    else if(b.getLocation().getX() - e.getBlock().getX() > 0) x = 1;
                    if(b.getLocation().getY() - e.getBlock().getY() < 0) y = -1;
                    else if(b.getLocation().getY() - e.getBlock().getY() > 0) y = 1;
                    if(b.getLocation().getZ() - e.getBlock().getZ() < 0) z = -1;
                    else if(b.getLocation().getZ() - e.getBlock().getZ() > 0) z = 1;
                    
                    b.getWorld().getBlockAt(b.getLocation().add(x,y,z)).setMetadata("placeby", new FixedMetadataValue(getPlugin(), "piston"));
                }
            }.runTaskLater(this, 1);
        }
    }

    // 防止物品框被破壞
    @EventHandler
    public void itemframebreakevent(HangingBreakByEntityEvent e) {

        if(e.isCancelled()) return;

        if(e.getEntity() instanceof ItemFrame && e.getRemover() instanceof Player) {

            Player P = (Player) e.getRemover();
            player p = players.get(P.getName());

            if(p.inregion == -1) return;

            region r = regions.get(p.inregion);

            if( !r.hasPermission(p, "break")) {
                util.sendActionbarMessage(P, ChatColor.RED + "" + ChatColor.BOLD +  "你在這個領地的身分組無法破壞方塊");
                e.setCancelled(true);
            }

        }

    }

    // 防止物品框內的物品被取出
    @EventHandler
    public void itemframebreakevent(EntityDamageByEntityEvent e) {

        if(e.isCancelled()) return;

        if(e.getEntity() instanceof ItemFrame && e.getDamager() instanceof Player) {

            Player P = (Player) e.getDamager();
            player p = players.get(P.getName());

            if(p.inregion == -1) return;

            region r = regions.get(p.inregion);

            if( !r.hasPermission(p, "chest")) {
                util.sendActionbarMessage(P, ChatColor.RED + "" + ChatColor.BOLD +  "你在這個領地的身分組無法使用容器類方塊");
                e.setCancelled(true);
            }

        }

    }

    // 防止物品框被破壞
    @EventHandler
    public void itemframetakeevent(PlayerInteractEntityEvent e) {

        if(e.isCancelled()) return;

        if(e.getRightClicked() instanceof ItemFrame) {

            Player P = e.getPlayer();
            player p = players.get(P.getName());

            if(p.inregion == -1) return;

            region r = regions.get(p.inregion);

            if( !r.hasPermission(p, "chest")) {
                util.sendActionbarMessage(P, ChatColor.RED + "" + ChatColor.BOLD +  "你在這個領地的身分組無法使用容器類方塊");
                e.setCancelled(true);
            }

        }

    }

    // 方塊放置事件
    @EventHandler
    public void blockPlaced(BlockPlaceEvent e) {

        Player P = e.getPlayer();
        player p = players.get(P.getName());
        Block b = e.getBlock();
        Location l = b.getLocation();

        // 玩家放置的方塊加入紀錄，避免刷經驗
        if (!p.id.equals("ken20001207"))
            b.setMetadata("placeby", new FixedMetadataValue(this, P.getUniqueId()));

        try {

            // 傳送小鎮禁止放置方塊
            if (getConfig().getInt("server") == 0 && !P.getName().equals("ken20001207")) {
                util.sendActionbarMessage(P, ChatColor.RED + "傳送小鎮很美了! 不用幫忙蓋沒關係");
                e.setCancelled(true);
                return;
            }

            // 沒有放置的方塊在領地內
            if (inwhichregion(l) != null) {
                region r = inwhichregion(l);

                // 沒有放置權限
                if (!r.hasPermission(p, "place")) {
                    e.setCancelled(true);
                    util.sendActionbarMessage(P, ChatColor.RED + "你在這個領地的身分組無法放置方塊。");
                }

            }

        } catch (Exception ex) {
            Player Player = e.getPlayer();
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            ex.printStackTrace(new PrintWriter(stringWriter));
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            util.pleaseReport(Player);
        }

    }

    // 玩家進入中界和地域發送警告
    @EventHandler()
    public void warning(PlayerTeleportEvent e) {

        Player P = e.getPlayer();

        if (!e.getFrom().getWorld().equals(e.getTo().getWorld())) {
            if (e.getTo().getWorld().getName().contains("nether")) {
                P.sendTitle(ChatColor.RED + "地域", "本世界無開啟噴裝防護，死亡將造成財產損失", 10, 100, 10);
            } else if (e.getTo().getWorld().getName().contains("end")) {
                P.sendTitle(ChatColor.RED + "終界", "本世界無開啟噴裝防護，死亡將造成財產損失", 10, 100, 10);
            }
        }
    }

    // 玩家傳送進入領地事件處理
    @EventHandler()
    public void enderPearlThrown(PlayerTeleportEvent e) {
        try {
            Location to = e.getTo();
            Player P = e.getPlayer();
            player p = players.get(P.getName());

            if (P.hasMetadata("NPC"))
                return;

            if (p == null)
                return;

            if (inwhichregion(to) == null)
                return;

            region r = inwhichregion(to);

            if (p.inregion == r.id)
                return;

            if (r.hasPermission(p, "enter")) {
                p.inregion = r.id;

                // 發送進入領地通知
                if (r.lordname.contains("family:")) {
                    P.sendTitle(ChatColor.YELLOW + r.name, ChatColor.GRAY + "家族 " + r.lordname.split(":")[1] + " 的家族領地",
                            5, 30, 10);
                } else {
                    P.sendTitle(r.name, ChatColor.GRAY + "隸屬於領主 " + r.lordname + " 的領地", 5, 30, 10);
                }

                // 處理領地飛行權限
                if (r.hasPermission(p, "fly")) {
                    P.setAllowFlight(true);
                    util.sendActionbarMessage(P, "您在該領地可以飛行, 剩餘飛行時數 " + (p.fly / 20) + " 秒");
                } else {
                    util.sendActionbarMessage(P, "您在該領地無法飛行");
                }

                return;
            } else {
                e.setCancelled(true);
                P.sendTitle("領地 " + r.name + " 禁止您的身分組進入", "該領地隸屬於領主 " + r.lordname, 5, 30, 10);
                return;
            }

        } catch (Exception ex) {
            Player Player = e.getPlayer();
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            ex.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            util.pleaseReport(Player);
        }
    }

    // 贊助玩家粒子特效處理
    private static void createPlayerParticles(final Player P, final String particledata) {

        // 火焰粒子環形特效 FLAMERIGNS
        if (particledata.contains("FLAMERINGS")) {
            BukkitTask task = new BukkitRunnable() {
                double alpha = 0;

                public void run() {
                    alpha += Math.PI / 16;
                    Location loc = P.getLocation();
                    Location firstLocation = loc.clone().add(Math.cos(alpha), (Math.sin(alpha) + 1) / 2,
                            Math.sin(alpha));
                    Location secondLocation = loc.clone().add(Math.cos(alpha + Math.PI), (Math.sin(alpha) + 1) / 2,
                            Math.sin(alpha + Math.PI));
                    loc.getWorld().spawnParticle(Particle.FLAME, firstLocation, 0, 0, 0, 0, 0);
                    loc.getWorld().spawnParticle(Particle.FLAME, secondLocation, 0, 0, 0, 0, 0);
                }
            }.runTaskTimer(getPlugin(), 0, 1);

            // 將 task 放進 Hash Map , 玩家登出時才可以關閉
            if (tasks.containsKey(P)) {
                tasks.get(P).add(task);
            } else {
                List<BukkitTask> tasklist = new ArrayList<>();
                tasklist.add(task);
                tasks.put(P, tasklist);
            }

            // RGB 紅石粉塵尾流特效 REDSTONE(數量-R-G-B-速度)
        } else if (particledata.contains("REDSTONE(")) {

            String particledata_process = particledata.replace("REDSTONE", "").replace("(", "").replace(")", "");
            int a = Integer.parseInt(particledata_process.split("-")[0]);
            int r = Integer.parseInt(particledata_process.split("-")[1]);
            int g = Integer.parseInt(particledata_process.split("-")[2]);
            int b = Integer.parseInt(particledata_process.split("-")[3]);
            int speed = Integer.parseInt(particledata_process.split("-")[4]);
            DustOptions dustOptions = new Particle.DustOptions(Color.fromBGR(b, g, r), 1);

            BukkitTask task = new BukkitRunnable() {

                public void run() {
                    Location l = P.getLocation();
                    P.getWorld().spawnParticle(Particle.REDSTONE, l, a, 0, 0, 0, speed, dustOptions);
                }
            }.runTaskTimer(getPlugin(), 0, 1);

            // 將 task 放進 Hash Map , 玩家登出時才可以關閉
            if (tasks.containsKey(P)) {
                tasks.get(P).add(task);
            } else {

                List<BukkitTask> tasklist = new ArrayList<>();
                tasklist.add(task);
                tasks.put(P, tasklist);
            }

            // RGB 粉塵環形粒子特效 REDSTONERINGS(數量-R-G-B-速度)
        } else if (particledata.contains("REDSTONERINGS(")) {

            String particledata_process = particledata.replace("REDSTONERINGS", "").replace("(", "").replace(")", "");
            int a = Integer.parseInt(particledata_process.split("-")[0]);
            int r = Integer.parseInt(particledata_process.split("-")[1]);
            int g = Integer.parseInt(particledata_process.split("-")[2]);
            int b = Integer.parseInt(particledata_process.split("-")[3]);
            int speed = Integer.parseInt(particledata_process.split("-")[4]);
            DustOptions dustOptions = new Particle.DustOptions(Color.fromBGR(b, g, r), 1);

            BukkitTask task = new BukkitRunnable() {
                double alpha = 0;

                public void run() {
                    alpha += Math.PI / 16;
                    Location l = P.getLocation();
                    Location firstLocation = l.clone().add(Math.cos(alpha), (Math.sin(alpha) + 1) / 2, Math.sin(alpha));
                    Location secondLocation = l.clone().add(Math.cos(alpha + Math.PI), (Math.sin(alpha) + 1) / 2,
                            Math.sin(alpha + Math.PI));
                    l.getWorld().spawnParticle(Particle.REDSTONE, firstLocation, a, 0, 0, 0, speed, dustOptions);
                    l.getWorld().spawnParticle(Particle.REDSTONE, secondLocation, a, 0, 0, 0, speed, dustOptions);
                }
            }.runTaskTimer(getPlugin(), 0, 1);

            // 將 task 放進 Hash Map , 玩家登出時才可以關閉
            if (tasks.containsKey(P)) {
                tasks.get(P).add(task);
            } else {
                List<BukkitTask> tasklist = new ArrayList<>();
                tasklist.add(task);
                tasks.put(P, tasklist);
            }

            // 火焰粒子尾流特效 FLAME(數量)
        } else if (particledata.contains("FLAME(")) {

            String particledata_process = particledata.replace("FLAME", "").replace("(", "").replace(")", "");
            int a = Integer.parseInt(particledata_process);

            BukkitTask task = new BukkitRunnable() {
                public void run() {
                    Location l = P.getLocation();
                    P.getWorld().spawnParticle(Particle.FLAME, l, a, 0, 0, 0, 0);
                }
            }.runTaskTimer(getPlugin(), 0, 1);

            // 將 task 放進 Hash Map , 玩家登出時才可以關閉
            if (tasks.containsKey(P)) {
                tasks.get(P).add(task);
            } else {
                List<BukkitTask> tasklist = new ArrayList<>();
                tasklist.add(task);
                tasks.put(P, tasklist);
            }

        }
    }

    // 玩家移動事件處理
    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        try {

            Player P = e.getPlayer();
            String name = e.getPlayer().getName();
            player p = players.get(name);
            Location to = e.getTo();

            if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockY() == e.getFrom().getBlockY()
                    && e.getTo().getBlockZ() == e.getFrom().getBlockZ())
                return; // The player hasn't moved

            // 取消暫離 hologram
            if (p.hasAFKflag) {
                for (Hologram pe : HologramsAPI.getHolograms(this)) {

                    if (pe.getLine(0).toString().contains(p.id) && pe.getLine(0).toString().contains("玩家暫離")) {
                        pe.delete();
                        p.hasAFKflag = false;
                        break;
                    }
                }
            }
            p.afk = 0;

            // 沒有進入領地或離開領地
            if (inwhichregion(to) == null) {
                if (p.inregion != -1) {
                    P.sendTitle("離開領地 " + regions.get(p.inregion).name, "", 5, 30, 10);

                    // 關閉飛行
                    if (P.getAllowFlight()) {

                        if (!P.getName().equals("ken20001207"))
                            P.setAllowFlight(false);
                    }

                    p.inregion = -1;
                }
                return;
            }

            // 進入領地
            region r = inwhichregion(to);
            if (p.inregion == r.id)
                return;
            if (r.hasPermission(p, "enter")) {
                p.inregion = r.id;

                // 發送進入領地訊息
                if (r.lordname.contains("family:")) {
                    P.sendTitle(ChatColor.YELLOW + r.name, ChatColor.GRAY + "家族 " + r.lordname.split(":")[1] + " 的家族領地",
                            5, 30, 10);
                } else {
                    P.sendTitle(r.name, ChatColor.GRAY + "隸屬於領主 " + r.lordname + " 的領地", 5, 30, 10);
                }

                // 處理領地飛行權限
                if (r.hasPermission(p, "fly")) {
                    P.setAllowFlight(true);
                    util.sendActionbarMessage(P, "您在該領地可以飛行, 剩餘飛行時數 " + (p.fly / 20) + " 秒");
                } else {
                    util.sendActionbarMessage(P, "您在該領地無法飛行");
                }

                // 用於分流傳送的領地
                if (getConfig().getInt("server") == 0 && r.name.equals("往北域")) {
                    util.sendActionbarMessage(P, ChatColor.GOLD + "即將傳送分流，上傳資料中");
                    p.upload();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ByteArrayDataOutput out = ByteStreams.newDataOutput();
                            out.writeUTF("ConnectOther");
                            out.writeUTF(p.id);
                            out.writeUTF("server_1");
                            P.sendPluginMessage(App.this, "BungeeCord", out.toByteArray());
                        }
                    }.runTaskLater(this, 20);

                    return;
                } else if (getConfig().getInt("server") == 0 && r.name.equals("往南域")) {
                    util.sendActionbarMessage(P, ChatColor.GOLD + "即將傳送分流，上傳資料中");
                    p.upload();

                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            ByteArrayDataOutput out = ByteStreams.newDataOutput();
                            out.writeUTF("ConnectOther");
                            out.writeUTF(p.id);
                            out.writeUTF("server_2");
                            P.sendPluginMessage(App.this, "BungeeCord", out.toByteArray());
                        }
                    }.runTaskLater(this, 20);

                    return;
                }
                return;
            } else {
                e.setCancelled(true);
                P.sendTitle("領地 " + r.name + " 禁止您的身分組進入", "該領地隸屬於領主 " + r.lordname, 5, 30, 10);
                if (P.isInsideVehicle()) {
                    P.leaveVehicle();
                }
                P.setVelocity(
                        new Vector(e.getFrom().getX() - e.getTo().getX(), 0, e.getFrom().getZ() - e.getTo().getZ())
                                .multiply(3));
                return;
            }
        } catch (

        Exception ex) {
            Player Player = e.getPlayer();
            StringWriter stringWriter = new StringWriter();
            String stackTrace = null;
            ex.printStackTrace(new PrintWriter(stringWriter));
            // get the stackTrace as String...
            stackTrace = stringWriter.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MainSystem Error] " + stackTrace);
            Player.kickPlayer(ChatColor.RED + "您的資料出現問題，請用 discord 通知系統管理員");
        }

    }

    // 用地點判斷在哪個領地
    public static region inwhichregion(Location l) {

        for (Map.Entry<Integer, region> entry : regions.entrySet()) {
            region r = entry.getValue();

            // 不同分流不用判斷
            if (r.server != config.getInt("server"))
                continue;

            // 不同世界不用判斷
            if (!r.world.equals(l.getWorld().getName()))
                continue;

            int X = l.getBlockX();
            int Z = l.getBlockZ();
            if (r.id != 0 && r.maxx > X && r.minx < X && r.minz < Z && r.maxz > Z) {
                return r;
            }
        }
        return null;
    }

    public void allserverbrocast(String msg) throws IOException {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("mainsystem_chat");
        out.writeUTF("%msg%" + msg + "%msg%");
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(this, "BungeeCord", out.toByteArray());

    }

    public static void moveallplayertoserver(String server_name) {

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.performCommand("connect " + server_name);
        }
    }

}