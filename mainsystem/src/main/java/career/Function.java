package career;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class Function {
    public static short GetEnchantmentLevel(ItemMeta itemMeta){
        String str = itemMeta.getLore().toString();
        return (short)(str.contains("III") ? 3 : 
                        (str.contains("II") ? 2 :
                            (str.contains("I") ? 1 : 0)));
    }
    //消耗一個單物品
    public static int SearchItem(Player player, ItemStack item){
        int slot = 0;
        for( ItemStack itemStack : player.getInventory().getContents() ){
            if(itemStack != null && itemStack.isSimilar(item)){
                break;
            }
            slot++;
        }
        return slot;
    }
    //消耗兩個以上的單物品
    public static int SearchItem(Player player, ItemStack item, int amount){
        int slot = 0;
        for( ItemStack itemStack : player.getInventory().getContents() ){
            if(itemStack != null && itemStack.isSimilar(item) && itemStack.getAmount() >= amount){
                break;
            }
            slot++;
        }
        return slot;
    }
    public static boolean JudgeHandItem(ItemStack item, String tool, String enhancement){
        try{
            //確定玩家不是空手，且手上到具有特殊附魔(用Lore顯示)
            if(item != null && item.getItemMeta().getLore() != null){
                //判斷手上的工具
                if(item.getType().toString().contains(tool)){
                    //確定玩家手上是有特殊的附魔
                    if(item.getItemMeta().getLore().toString().contains(enhancement)){
                        //判斷手上物品是否有經驗修補 有就不觸發技能
                        if(!item.containsEnchantment( Enchantment.MENDING )){
                            return true;
                        }
                    }
                }
            }
        }
        catch(NullPointerException e){}
        return false;
    }
    
    //判斷是否減少耐久度
    @SuppressWarnings( "deprecation" )
    public static void DurabilityReduce(ItemStack item, int times){
        int level = item.getEnchantmentLevel(Enchantment.DURABILITY); 
        for(int i = 0; i < times; i++)
            if((int)(Math.random() * 100) < (int)(100 / (level + 1)))item.setDurability( (short)(item.getDurability() + (short)1) );
    }
    public static boolean JudgeItemCharge(ItemStack item, int value){
        try{
             //標準 Lore 格式
            //["", ChatColor.GRAY + "重力砍伐 I", "", "", ChatColor.GRAY + "重力砍伐 : 使用時需消耗 8 能量", 能量: 84 / 100]
            //用 Energy 分開字串 確保分開時一定會取到數字部分
            String array[] = item.getItemMeta().getLore().toString().split("能量: ");
            array = array[1].split(" / ");
            int energy = Integer.parseInt(array[0]);
            return ((energy >= value) ? true : false);
        }
        catch(IndexOutOfBoundsException e){
            String array1 = item.getItemMeta().getLore().get(1);
            int maxValue = (item.getType().equals(Material.DIAMOND_AXE) || item.getType().equals(Material.DIAMOND_HOE)) ? 1280 : 640;
            ItemMeta im = item.getItemMeta();
            im.setLore(Arrays.asList( array1 ," ",
                    ChatColor.AQUA + "" + ChatColor.BOLD + ("能量: " + 0 + " / " + maxValue ) ));
            item.setItemMeta(im);
            return false;
        }
    }
    public static ItemMeta OperaEnergy(ItemStack item, int value){
        //標準 Lore 格式
        //[Blast III, 消耗 8 點能量 , Energy: 100 / 100]
        List<String> strList = item.getItemMeta().getLore();   
        //用 能量:  將字串分開確保數值一定在最後的陣列 last 
        String strTemp[] = strList.get( strList.size() - 1 ).split("能量: ");
        //用 / 把數值分開 (目前值) / (最大值)
        String arrayValue[] = strTemp[strTemp.length - 1].toString().split(" / ");
        //取得目前物品充能的值
        int energy = Integer.parseInt( arrayValue[0] );
        //取得目前物品充能最大值
        int maxEnergy = Integer.parseInt( arrayValue[1] );
        //判斷相減是否小於 0 是就將目前值設為 0
        if(value > 0)   energy =  ((energy + value) <= maxEnergy) ? (energy + value) : maxEnergy;
        else            energy =  ((energy + value) >= 0) ? (energy + value) : 0;
        //設定手上物品的 Lore
        ItemMeta newItemMeta = item.getItemMeta();
        strList.remove( strList.size() - 1 );
        strList.add( ChatColor.AQUA + "" + ChatColor.BOLD + ("能量: " + energy + " / " + maxEnergy) );
        newItemMeta.setLore( strList );
        item.setItemMeta( newItemMeta );
        return newItemMeta;
    }
}