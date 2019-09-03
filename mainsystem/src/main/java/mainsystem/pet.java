package mainsystem;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public class pet implements Listener, PacketListener {

    @EventHandler
    public void onPlayerRightClickEntity(PlayerInteractEntityEvent e) {

        // 避免觸發兩次
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (e.getPlayer().getName().equals("ken20001207") && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.LEVER)) {
            if (e.getRightClicked() instanceof LivingEntity) {
                e.getRightClicked().addPassenger(e.getPlayer());
            }
        }

    }

    @Override
    public Plugin getPlugin() {
        return App.getPlugin();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return null;
    }

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return null;
    }

    @Override
    public void onPacketReceiving(PacketEvent e) {
        if (e.getPacketType().equals(PacketType.Play.Client.STEER_VEHICLE)) {

        }
    }

    @Override
    public void onPacketSending(PacketEvent arg0) {

    }

}