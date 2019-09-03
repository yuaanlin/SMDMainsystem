package mainsystem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class party {

    int id;
    List<Player> member = new ArrayList<>();
    Player cap;
    String name;

    public party(Player captan, String party_name){
        cap = captan;
        member.add(captan);
        name = party_name;
    }

    public void addmember(Player p){
        member.add(p);
    }

    public int number(){
        return member.size();
    }

    public void removemember(Player p){
        member.remove(p);
    }


}