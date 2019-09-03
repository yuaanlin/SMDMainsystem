package mainsystem;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PluginMessageHandler implements PluginMessageListener {

    public static App getPlugin() {
        return JavaPlugin.getPlugin(App.class);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("mainsystem_chat")) {
            String msg = in.readUTF();
            App.sendMessageToAllPlyaer(new TextComponent(msg.split("%msg%")[1]));
            Bukkit.getConsoleSender().sendMessage(msg.split("%msg%")[1]);
        } else if (subchannel.equals("mainsystem_uploadplayer")) {
            String id = in.readUTF().split("%player%")[1];
            if (Bukkit.getPlayer(id) != null) {
                datasync.upload_player(id);
            } else if (id.equals("ALL")) {
                datasync.upload_player();
            }
        } else if (subchannel.equals("mainsystem_downloadplayer")) {
            String id = in.readUTF().split("%player%")[1];
            if (Bukkit.getPlayer(id) != null) {
                datasync.download_player(id);
            } else if (id.equals("ALL")) {
                datasync.download_player();
            }
        } else if (subchannel.equals("mainsystem_uploadregion")) {
            String id = in.readUTF().split("%region%")[1];
            if (id.equals("ALL")) {
                datasync.download_region();
            } else {
                datasync.download_region(Integer.parseInt(id));
            }
        } else if (subchannel.equals("mainsystem_downloadregion")) {
            String id = in.readUTF().split("%region%")[1];
            if (id.equals("ALL")) {
                App.regions.clear();
                datasync.download_region();
            } else {
                datasync.download_region(Integer.parseInt(id));
            }
        } else if (subchannel.equals("mainsystem_mcommand")) {
            String data = in.readUTF();
            String from = data.split("%f%")[1];
            String to = data.split("%t%")[1];
            String msg = data.split("%m%")[1];
            if (Bukkit.getPlayer(to) != null) {
                TextComponent m = new TextComponent(ChatColor.GRAY + " " + from + " >> (悄悄話) " + msg);
                m.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + from));
                m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊回覆密語").create()));
                Bukkit.getPlayer(to).spigot().sendMessage(m);
            }
        } else if (subchannel.equals("mainsystem_uploadfamily")) {
            String id = in.readUTF().split("%family%")[1];
            if (id.equals("ALL")) {
                datasync.upload_family();
            } else {
                datasync.upload_family(id);
            }
        } else if (subchannel.equals("mainsystem_downloadfamily")) {
            String id = in.readUTF().split("%family%")[1];
            if (id.equals("ALL")) {
                App.families.clear();
                datasync.download_family();
            } else {
                datasync.download_family(id);
            }
        } else if (subchannel.equals("mainsystem_uploadshop")) {
            String id = in.readUTF().split("%shop%")[1];
            if (id.equals("ALL")) {
                datasync.upload_shop();
            } else {
                datasync.upload_shop(Integer.parseInt(id));
            }
        } else if (subchannel.equals("mainsystem_downloadshop")) {
            String id = in.readUTF().split("%shop%")[1];
            if (id.equals("ALL")) {
                App.shop_sell.clear();
                datasync.download_shop();
            } else {
                datasync.download_shop(Integer.parseInt(id));
            }
        } else if (subchannel.equals("mainsystem_playerjoin")) {
            String data = in.readUTF();
            String id = data.split("%p%")[1];
            String server = data.split("%s%")[1];
            App.allplayers.put(id, Integer.parseInt(server));
        } else if (subchannel.equals("mainsystem_playerquit")) {
            String data = in.readUTF();
            String id = data.split("%p%")[1];
            App.allplayers.remove(id);
        } else if (subchannel.equals("AllServerBrocast")) {
            String data = in.readUTF();
            String msg = data.split("%m%")[1];
            App.chatchannels.get("全伺服頻道").sendmessage(msg);
        } else if (subchannel.equals("mainsystem_askplayerlist")) {
            for (Player P : Bukkit.getOnlinePlayers()) {
                // 通知其他分流加入該玩家到在線玩家列表
                try {
                    SendPluginMessage("mainsystem_playerjoin",
                            "%p%" + P.getName() + "%p%%s%" + App.config.getString("server") + "%s%");
                } catch (Exception exc) {

                }
            }
        } else if (subchannel.equals("AllServerChat")) {

            // 接收資料
            String data = in.readUTF();

            String id = data.split("%i%")[1];
            String hoverdata = data.split("%h%")[1];
            String displayname = data.split("%d%")[1];
            String msg = data.split("%m%")[1];
            String chatchannel = data.split("%c%")[1];

            // 取得目標聊天頻道
            chatchannel cc;
            if (App.chatchannels.containsKey(chatchannel))
                cc = App.chatchannels.get(chatchannel);
            else
                return;

            // 建立 Name Hover Component
            TextComponent playername = new TextComponent(displayname);
            playername.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverdata).create()));
            playername.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + id));

            // 架構 Chat Format
            TextComponent head = new TextComponent("");
            head.toPlainText();
            head.addExtra(playername);
            head.addExtra(" §8>> ");
            head.addExtra(msg);

            // 發出訊息
            cc.sendmessage(head);

            // 輸出到監控終端
            Bukkit.getConsoleSender().sendMessage("(" + cc.channelname + ") " + head.toPlainText());

            // 官網引導訊息
            String strip_color_msg = ChatColor.stripColor(msg);
            if (strip_color_msg.contains("官網") || strip_color_msg.contains("官方網站")) {
                TextComponent web = new TextComponent(
                        ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服" + ChatColor.WHITE + "] Bot9453 §8>> "
                                + ChatColor.GRAY + " 有人提到官方網站嗎? " + ChatColor.GRAY + "按我這條訊息可以前往喔 OuOb");
                web.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/index.php"));
                web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("點擊前往 SMD:Kingdoms 官方網站").create()));
                cc.sendmessage(web);
            } else if (strip_color_msg.contains("商城") || strip_color_msg.contains("加值商城")) {
                TextComponent web = new TextComponent(
                        ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服" + ChatColor.WHITE + "] Bot9453 §8>> "
                                + ChatColor.GRAY + " 有人提到加值商城嗎? " + ChatColor.GRAY + "按我這條訊息可以前往喔 OuOb");
                web.setClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/donateshop.php"));
                web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("點擊前往 SMD:Kingdoms 加值商城").create()));
                cc.sendmessage(web);
            } else if (strip_color_msg.contains("贊助")) {
                TextComponent web = new TextComponent(
                        ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服" + ChatColor.WHITE + "] Bot9453 §8>> "
                                + ChatColor.GRAY + " 有人提到贊助伺服嗎? " + ChatColor.GRAY + "按我這條訊息可以前往喔 OuOb");
                web.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/donate.php"));
                web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("點擊前往 SMD:Kingdoms 贊助頁面").create()));
                cc.sendmessage(web);
            } else if (strip_color_msg.contains("教學") || strip_color_msg.contains("怎麼玩")
                    || strip_color_msg.contains("怎玩")) {
                TextComponent web = new TextComponent(
                        ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服" + ChatColor.WHITE + "] Bot9453 §8>> "
                                + ChatColor.GRAY + " 有人提到新手教學嗎? " + ChatColor.GRAY + "按我這條訊息可以前往喔 OuOb");
                web.setClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_URL, "http://yuanyuan.cloud/smd/help.php?page=1.php"));
                web.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("點擊前往 SMD:Kingdoms 新手教學").create()));
                cc.sendmessage(web);
            } else if (strip_color_msg.contains("手機") || strip_color_msg.contains("選單")
                    || strip_color_msg.contains("菜單")) {
                TextComponent web = new TextComponent(ChatColor.WHITE + " SMD [" + ChatColor.LIGHT_PURPLE + "客服"
                        + ChatColor.WHITE + "] Bot9453 §8>> " + ChatColor.GRAY + " 有人提到手機選單嗎? " + ChatColor.GRAY
                        + "按\"背包合成表\"的右邊那一格就可以開啟喔! " + ChatColor.GRAY + "(按這條訊息也可以)");
                web.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mainsystem phone"));
                web.setHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("點擊開啟手機選單").create()));
                cc.sendmessage(web);
            }

        }
    }

    public void sendAllServerBrocast(String msg) {

        if (Bukkit.getOnlinePlayers().size() == 0)
            return;

        // 傳送到所有其他分流
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("AllServerBrocast");
        out.writeUTF("%m%" + msg + "%m%");
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(App.getPlugin(), "BungeeCord",
                out.toByteArray());
    }

    public void SendPluginMessage(String Subchannel, String PluginMessage) throws IOException {

        if (Bukkit.getOnlinePlayers().size() == 0)
            return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(Subchannel);
        out.writeUTF(PluginMessage);
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(getPlugin(), "BungeeCord",
                out.toByteArray());
    }

    public static void sendAllServerChat(String player, String msg, String chatchannel) {

        if (Bukkit.getOnlinePlayers().size() == 0)
            return;

        player p = App.players.get(player);
        String DisplayName = Bukkit.getPlayer(player).getDisplayName();

        // 建立要傳送到其他分流的玩家 Name Hover
        String NameHoverData = "";
        String region = "\n";
        for (Map.Entry<Integer, region> entry : p.regionlist.entrySet()) {
            region r = entry.getValue();
            region += ChatColor.WHITE + "領地 " + ChatColor.GREEN + r.name + ChatColor.GRAY + " (" + r.size + " 平方公尺)"
                    + ChatColor.WHITE + " 的領主\n";
        }

        String loca = "";
        if (p.inregion != -1)
            loca = ChatColor.GRAY + "\n當前位置: " + ChatColor.WHITE + "領地 " + ChatColor.GREEN
                    + App.regions.get(p.inregion).name + " \n" + ChatColor.WHITE;

        NameHoverData += DisplayName + "\n" + ChatColor.GRAY + "(真實ID " + p.id + ") " + "\n\n" + ChatColor.GRAY
                + "當前分流 " + ChatColor.WHITE + App.servername + ChatColor.GRAY + " 分流\n\n" + ChatColor.GRAY + "等級 "
                + ChatColor.WHITE + p.lv + ChatColor.GRAY + " 等\n" + ChatColor.GRAY + "\n" + "經驗值 " + ChatColor.WHITE
                + p.exp + " / " + p.lv * 1000 + ChatColor.GRAY + " 點\n" + ChatColor.GRAY + "家族 " + ChatColor.WHITE
                + p.family.replace("null", "無家族") + "\n" + ChatColor.GRAY + "身分 " + ChatColor.WHITE
                + p.cla.replace(",", "、") + "\n" + ChatColor.GRAY + "商城幣 " + ChatColor.WHITE + p.coin + ChatColor.GRAY
                + " 元\n" + loca + region + ChatColor.UNDERLINE + "\n( 點擊玩家名稱即可傳送悄悄話 )";

        // 傳送到所有其他分流
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("AllServerChat");
        out.writeUTF("%h%" + NameHoverData + "%h%" + "%d%" + DisplayName + "%d%" + "%m%" + msg + "%m%" + "%c%"
                + chatchannel + "%c%%i%" + player + "%i%");
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(App.getPlugin(), "BungeeCord",
                out.toByteArray());

    }

}