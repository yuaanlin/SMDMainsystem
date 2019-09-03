package mainsystem;

import java.util.Comparator;

import org.bukkit.inventory.ItemStack;

class itemcompartor implements Comparator<ItemStack> {
    @Override
    public int compare(ItemStack item1, ItemStack item2) {
        String name1 = "zzzzzzz";
        String name2 = "zzzzzzz";
        if(item1 != null) name1 = item1.getType().toString();
        if(item2 != null) name2 = item2.getType().toString();
        return name1.compareTo(name2);
    }
}