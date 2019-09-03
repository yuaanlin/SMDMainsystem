package mainsystem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class items {

    Map<String, ItemStack> booklist = new HashMap<>();

    ItemStack ResetClassPaper = new ItemStack(Material.PAPER);
    ItemStack MissionPaper = new ItemStack(Material.PAPER);
    ItemStack PowerFurnace = new ItemStack(Material.FURNACE);
    ItemStack RegionTPPaper = new ItemStack(Material.PAPER);

    public items() {
        // 特殊附魔書 : 重力砍伐 I
        ItemStack wood1 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta wood1_im = wood1.getItemMeta();
        wood1_im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "重力砍伐 I");
        wood1_im.setLore(Arrays.asList("", ChatColor.GRAY + "重力砍伐 I"));
        wood1.setItemMeta(wood1_im);
        booklist.put("重力砍伐I", wood1);

        // 特殊附魔書 : 重力砍伐 II
        ItemStack wood1_2 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta wood1_2im = wood1_2.getItemMeta();
        wood1_2im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "重力砍伐 II");
        wood1_2im.setLore(Arrays.asList("", ChatColor.GRAY + "重力砍伐 II"));
        wood1_2.setItemMeta(wood1_2im);
        booklist.put("重力砍伐II", wood1_2);

        // 特殊附魔書 : 重力砍伐 III
        ItemStack wood1_3 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta wood1_3im = wood1_3.getItemMeta();
        wood1_3im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "重力砍伐 III");
        wood1_3im.setLore(Arrays.asList("", ChatColor.GRAY + "重力砍伐 III"));
        wood1_3.setItemMeta(wood1_3im);
        booklist.put("重力砍伐III", wood1_3);

        // 特殊附魔書 : 自動種植 I
        ItemStack ripe1 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta ripe1_im = ripe1.getItemMeta();
        ripe1_im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "自動種植 I");
        ripe1_im.setLore(Arrays.asList("", ChatColor.GRAY + "自動種植 I"));
        ripe1.setItemMeta(ripe1_im);
        booklist.put("自動種植I", ripe1);

        // 特殊附魔書 : 自動種植 II
        ItemStack ripe1_2 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta ripe1_2im = ripe1_2.getItemMeta();
        ripe1_2im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "自動種植 II");
        ripe1_2im.setLore(Arrays.asList("", ChatColor.GRAY + "自動種植 II"));
        ripe1_2.setItemMeta(ripe1_2im);
        booklist.put("自動種植II", ripe1_2);

        // 特殊附魔書 : 自動種植 III
        ItemStack ripe1_3 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta ripe1_3im = ripe1_3.getItemMeta();
        ripe1_3im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "自動種植 III");
        ripe1_3im.setLore(Arrays.asList("", ChatColor.GRAY + "自動種植 III"));
        ripe1_3.setItemMeta(ripe1_3im);
        booklist.put("自動種植III", ripe1_3);

        // 特殊附魔書 : 威力打擊 I
        ItemStack sword1_1 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta sword1_1im = sword1_1.getItemMeta();
        sword1_1im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "威力打擊 I");
        sword1_1im.setLore(Arrays.asList("", ChatColor.GRAY + "威力打擊 I"));
        sword1_1.setItemMeta(sword1_1im);
        booklist.put("威力打擊I", sword1_1);

        // 特殊附魔書 : 威力打擊 II
        ItemStack sword1_2 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta sword1_2im = sword1_2.getItemMeta();
        sword1_2im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "威力打擊 II");
        sword1_2im.setLore(Arrays.asList("", ChatColor.GRAY + "威力打擊 II"));
        sword1_2.setItemMeta(sword1_2im);
        booklist.put("威力打擊II", sword1_2);

        // 特殊附魔書 : 威力打擊 III
        ItemStack sword1_3 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta sword1_3im = sword1_3.getItemMeta();
        sword1_3im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "威力打擊 III");
        sword1_3im.setLore(Arrays.asList("", ChatColor.GRAY + "威力打擊 III"));
        sword1_3.setItemMeta(sword1_3im);
        booklist.put("威力打擊III", sword1_3);

        // 特殊附魔書 : 爆破箭矢 I
        ItemStack bow1_1 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta bow1_1im = bow1_1.getItemMeta();
        bow1_1im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "爆破箭矢 I");
        bow1_1im.setLore(Arrays.asList("", ChatColor.GRAY + "爆破箭矢 I"));
        bow1_1.setItemMeta(bow1_1im);
        booklist.put("爆破箭矢I", bow1_1);

        // 特殊附魔書 : 爆破箭矢 II
        ItemStack bow1_2 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta bow1_2im = bow1_2.getItemMeta();
        bow1_2im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "爆破箭矢 II");
        bow1_2im.setLore(Arrays.asList("", ChatColor.GRAY + "爆破箭矢 II"));
        bow1_2.setItemMeta(bow1_2im);
        booklist.put("爆破箭矢II", bow1_2);

        // 特殊附魔書 : 爆破箭矢 III
        ItemStack bow1_3 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta bow1_3im = bow1_3.getItemMeta();
        bow1_3im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "爆破箭矢 III");
        bow1_3im.setLore(Arrays.asList("", ChatColor.GRAY + "爆破箭矢 III"));
        bow1_3.setItemMeta(bow1_3im);
        booklist.put("爆破箭矢III", bow1_3);

        // 特殊附魔書 : 深度挖掘 I
        ItemStack iron1_1 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta iron1_1im = iron1_1.getItemMeta();
        iron1_1im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "深度挖掘 I");
        iron1_1im.setLore(Arrays.asList("", ChatColor.GRAY + "深度挖掘 I"));
        iron1_1.setItemMeta(iron1_1im);
        booklist.put("深度挖掘I", iron1_1);

        // 特殊附魔書 : 深度挖掘 II
        ItemStack iron1_2 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta iron1_2im = iron1_2.getItemMeta();
        iron1_2im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "深度挖掘 II");
        iron1_2im.setLore(Arrays.asList("", ChatColor.GRAY + "深度挖掘 II"));
        iron1_2.setItemMeta(iron1_2im);
        booklist.put("深度挖掘II", iron1_2);

        // 特殊附魔書 : 深度挖掘 III
        ItemStack iron1_3 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta iron1_3im = iron1_3.getItemMeta();
        iron1_3im.setDisplayName(ChatColor.DARK_PURPLE + "特殊附魔書: " + ChatColor.WHITE + "深度挖掘 III");
        iron1_3im.setLore(Arrays.asList("", ChatColor.GRAY + "深度挖掘 III"));
        iron1_3.setItemMeta(iron1_3im);
        booklist.put("深度挖掘III", iron1_3);

        // 職業重製卷
        ItemMeta im = ResetClassPaper.getItemMeta();
        im.setDisplayName(ChatColor.DARK_PURPLE + "職業重置卷");
        im.setLore(Arrays.asList("", ChatColor.GRAY + "點擊右鍵可重置玩家的職業，", ChatColor.GRAY + "將變為 無業者 Lv.1。",
                ChatColor.GRAY + "(不影響熟練度)", ""));
        ResetClassPaper.setItemMeta(im);

        // 屠龍副本挑戰卷
        im = MissionPaper.getItemMeta();
        im.setDisplayName(ChatColor.DARK_PURPLE + "副本任務卷" + ChatColor.WHITE + "：屠龍者挑戰");
        im.setLore(Arrays.asList("", ChatColor.GRAY + "點擊右鍵可和同隊伍的玩家出發前往副本。", "",
                ChatColor.WHITE + "副本任務 ： " + ChatColor.GRAY + "屠龍者挑戰",
                ChatColor.WHITE + "副本目標 ： " + ChatColor.GRAY + "成功擊殺終界龍，進入終界返回世界的傳送門即完成挑戰",
                ChatColor.WHITE + "副本人數 ： " + ChatColor.GRAY + "1 ~ 4 人",
                ChatColor.WHITE + "副本時限 ： " + ChatColor.GRAY + "需在一個星期內完成"));
        MissionPaper.setItemMeta(im);

        // 充能熔爐
        im = PowerFurnace.getItemMeta();
        im.setDisplayName(ChatColor.WHITE + "充能熔爐");
        im.setLore(Arrays.asList("", ChatColor.GRAY + "能夠幫特殊附魔道具充能的熔爐，", ChatColor.GRAY + "每位稱職的工匠家中都必須有一個。", ""));
        PowerFurnace.setItemMeta(im);

        // 領地傳送點設置卷
        im = RegionTPPaper.getItemMeta();
        im.setDisplayName(ChatColor.DARK_PURPLE + "領地傳送點設置卷");
        im.setLore(Arrays.asList("", ChatColor.WHITE + "持本卷點擊右鍵即可宣告領地之傳送位置。"));
        RegionTPPaper.setItemMeta(im);
    }

}