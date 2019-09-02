package mainsystem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

public class chatchannel {

    List<Player> member = new ArrayList<>();
    String channelname;

    public chatchannel(String name) {
        channelname = name;
    }

    public void addplayer(Player P) {
        member.add(P);
        util.sendActionbarMessage(P, "已經進入 " + channelname);
    }

    public void removeplayer(Player P) {
        member.remove(P);
        util.sendActionbarMessage(P, "已經退出 " + channelname);
    }

    public void sendmessage(String msg) {
        for (Player P : member) {
            P.sendMessage(msg);
        }
    }

    public void sendmessage(BaseComponent textcomp) {
        for (Player P : member) {
            P.spigot().sendMessage(textcomp);
        }
    }

}