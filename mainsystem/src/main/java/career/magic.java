package career;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Furnace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import mainsystem.App;
import mainsystem.player;
import mainsystem.util;
import net.md_5.bungee.api.ChatColor;

public class magic implements Listener {

    private final App plugin;
    // 設定燃燒中的界伏盒(用來判斷是否與目前界伏盒相同)
    private final ItemStack MAGMA;
    private final ItemStack CLAY;

    public magic(App plugin) {
        this.plugin = plugin;
        // ------------------------------------------------
        MAGMA = new ItemStack(Material.MAGMA_BLOCK);
        ItemMeta itemMeta = MAGMA.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + "燃燒內容物中，請稍後......");
        itemMeta.setLore(Arrays.asList(ChatColor.RED + "請勿將我拿出熔爐", ChatColor.RED + "若充能失敗概不負責"));
        MAGMA.setItemMeta(itemMeta);
        // ------------------------------------------------
        CLAY = new ItemStack(Material.CLAY_BALL);
        ItemMeta clayMeta = CLAY.getItemMeta();
        clayMeta.setDisplayName(ChatColor.GRAY + "爛泥");
        clayMeta.setLore(Arrays.asList(ChatColor.GRAY + "充能失敗了，看來你中間有步驟錯誤喔!"));
        CLAY.setItemMeta(clayMeta);
        // -----------------------------------------------
        plugin.getServer().resetRecipes();
        setRecipe("dimond1", Material.DIAMOND_SWORD);
        setRecipe("dimond2", Material.DIAMOND_PICKAXE);
        setRecipe("dimond3", Material.DIAMOND_AXE);
        setRecipe("dimond4", Material.DIAMOND_HOE);
        setRecipe("bow1", Material.BOW);
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent e) {
        Player P = e.getEnchanter();
        player p = App.players.get(P.getName());

        p.magic += e.getExpLevelCost() * 1;
        App.addexp(P.getName(), e.getExpLevelCost() * 1);
        util.sendActionbarMessage(P, "附魔道具, +" + e.getExpLevelCost() * 1 + "生活經驗, +" + e.getExpLevelCost() * 1 + "加工值");
    }

    // ==================================================================
    // 當容器被打開時 (指執行一次)
    @EventHandler
    public void PutFuel(InventoryOpenEvent e) {
        if (!e.isCancelled()) {
            // 玩家
            Player player = (Player) e.getPlayer();
            player p = App.players.get(player.getName());
            // 判斷是否開啟起熔爐
            if (e.getInventory() instanceof FurnaceInventory) {
                // 取得玩家手上物品資料
                ItemStack item = player.getInventory().getItemInMainHand();
                // 取的熔爐的得 Inventory
                FurnaceInventory furnace = (FurnaceInventory) e.getInventory();
                // 判斷熔爐是否叫做 "充能熔爐" 這樣可以免除一些與原版熔爐造成的BUG
                if (furnace.getHolder().getCustomName() != null
                        && furnace.getHolder().getCustomName().equals(ChatColor.WHITE + "充能熔爐")) {
                    // 判斷是否有燃料 且 手上物品是否為空
                    if (furnace.getFuel() == null && item != null) {
                        // 判斷玩家手上的物品是否為界伏盒
                        if (p.cla.contains("工匠") && item.getType().equals(Material.SHULKER_BOX)) {
                            // 把玩家手上的物品放到燃料裡(要這樣設定是因為在 Vanilla 的設定中除了燃料都不能放進燃料 Slot)
                            furnace.setFuel(item);
                            // 把手上的物品清空
                            player.getInventory().setItemInMainHand(null);
                        }
                    }
                }
            }
        }
    }

    // 當燃料開始燃燒時 (只在燃料燒起時動作一次)
    @EventHandler
    public void useFuels(FurnaceBurnEvent e) {
        // 取得熔爐方塊
        Furnace furnace = (Furnace) e.getBlock().getState();
        // 取的被燒的物品
        ItemStack smltingItem = furnace.getInventory().getSmelting();
        // 熔爐名稱必須是要為 "充能熔爐"，防止BUG的產生在原有舊的熔爐裡
        String blockName = furnace.getInventory().getHolder().getCustomName();
        if (blockName != null && blockName.equals(ChatColor.WHITE + "充能熔爐")) {
            // 判斷武器是否有"能量"這個Lore
            if (smltingItem.hasItemMeta() && smltingItem.getItemMeta().hasLore()
                    && smltingItem.getItemMeta().getLore().toString().contains("能量:")) {
                // 判斷燃料區裡是否放的是界伏盒
                if (e.getFuel() != null && e.getFuel().getType().equals(Material.SHULKER_BOX)) {
                    // 判斷界伏盒裡的燃料數量
                    short fuelTime = (short) getFuels(e.getFuel());
                    // 假如燃料量大於 0
                    if (fuelTime != 0) {
                        // 取得被燃燒的物品
                        ItemMeta item = smltingItem.getItemMeta();
                        // 將燃燒(充能量)的數值 寫在本地名子裡當作數值傳遞
                        item.setLocalizedName(Integer.toString(fuelTime));
                        smltingItem.setItemMeta(item);
                        // 讓熔爐開始燃燒
                        e.setBurning(true);
                        e.setBurnTime(1000);
                        furnace.setCookTime((short) 995);
                        // 因為熔爐開始燃燒時會消耗物品，所以延遲 1 tick 把新的物品新增到燃料區裡
                        // 需要這樣設也是為了防止一些BUG的濫用
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                furnace.getInventory().setFuel(MAGMA);
                            }
                        }.runTaskLater(plugin, 1L);
                        furnace.getWorld().playSound(furnace.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 1.0f);
                        furnace.getWorld().spawnParticle(Particle.FLAME, furnace.getLocation().add(0.5, 0.5, 0.5), 250,
                                0, 0, 0, 0.04, null, true);
                    }
                }
            }
        }
    }

    // 成功燒完物品
    @EventHandler
    public void SuccessSmlting(FurnaceSmeltEvent e) {
        // 取得熔爐方塊
        Furnace furnace = (Furnace) e.getBlock().getState();
        // 取得被燃燒的物品
        ItemStack smltingItem = e.getSource();
        // 熔爐名稱必須是要為 "充能熔爐"，防止BUG的產生在原有舊的熔爐裡
        if (furnace.getCustomName() != null && furnace.getCustomName().equals(ChatColor.WHITE + "充能熔爐")) {
            // 被燃燒的物品的 Lore != null
            if (smltingItem.getItemMeta().getLore() != null) {
                // 判斷武器是否有"能量"這個Lore
                if (smltingItem.getItemMeta().getLore().toString().contains("能量:")) {
                    // 判斷燃料區是否有燃料
                    if (furnace.getInventory().getFuel() != null) {
                        // 取得燃料區的燃料
                        ItemStack fuel = furnace.getInventory().getFuel();
                        // 判斷 (現有燃料區物品 與 預設的燃料區物品) 是否相同 註：防止燃料區的物品被替換
                        if (fuel.equals(MAGMA)) {
                            // 將燃料所能提供的數值轉換出來
                            int energy = Integer.parseInt(smltingItem.getItemMeta().getLocalizedName());
                            // 取的被燃燒的物品 增加它的能量
                            ItemStack item = e.getSource();
                            item.setItemMeta(Function.OperaEnergy(item, energy));
                            ItemMeta itemMeta = item.getItemMeta();
                            // 把區域物品的名稱歸零
                            itemMeta.setLocalizedName("0");
                            item.setItemMeta(itemMeta);
                            // 更改燃燒完的結果
                            e.setResult(item);
                            // 把燃料區的界伏盒歸還
                            furnace.getInventory().setFuel(new ItemStack(Material.SHULKER_BOX));
                            furnace.getWorld().spawnParticle(Particle.NAUTILUS,
                                    furnace.getLocation().add(0.5, 0.5, 0.5), 800, 0.5, 0.5, 0.5, 3, null, true);
                            furnace.getWorld().playSound(furnace.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 0.5f, 0.5f);
                            return;
                        }
                    }
                }
            }
            // 假如有人想使用BUG就會執行此程式 把燃燒完的結果設為黏土
            e.setResult(CLAY);
            furnace.getWorld().createExplosion(furnace.getX() + 0.5, furnace.getY() + 1.5, furnace.getZ() + 0.5, 12,
                    false, false);
        }
    }

    // 把物品新增到熔爐合成表裡 讓熔爐可以燃燒此物品
    public void setRecipe(String ID, Material material) {
        // 新增合成表 結果為黏土
        FurnaceRecipe recipe = new FurnaceRecipe(NamespacedKey.minecraft(ID), new ItemStack(CLAY), material, 0, 1000);
        plugin.getServer().addRecipe(recipe);
    }

    // 取得能量
    public int getFuels(ItemStack fuelItem) {
        // 判斷燃料區裡是否存在燃料
        if (fuelItem != null && fuelItem.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta itemMeta = (BlockStateMeta) fuelItem.getItemMeta();
            // 判斷是否是界伏盒
            if (itemMeta.getBlockState() instanceof ShulkerBox) {
                // 轉型
                ShulkerBox shulker = (ShulkerBox) itemMeta.getBlockState();
                // 能量量
                int energy = 0;
                // 把界伏盒的物品全部抓出來算能量
                for (ItemStack item : shulker.getInventory().getContents()) {
                    energy += getFuelType(item);
                }
                return energy;
            }
        }
        return 0;
    }

    // 判斷不同的物品所提供的能量
    public int getFuelType(ItemStack item) {
        if (item != null) {
            // 因數(用來設定不同物品所提供的能量倍數)
            float mutiple = 0;
            Material material = item.getType();
            // 如果經濟系統出來的話 希望倍數可以依照市場上的價格而做變動
            if (material.equals(Material.COOKED_PORKCHOP))
                mutiple = 0.3F;
            else if (material.equals(Material.COOKED_BEEF))
                mutiple = 0.3F;
            else if (material.equals(Material.COOKED_CHICKEN))
                mutiple = 0.3F;
            else if (material.equals(Material.COOKED_MUTTON))
                mutiple = 0.3F;
            else if (material.equals(Material.COOKED_RABBIT))
                mutiple = 0.5F;
            else if (material.equals(Material.COOKED_SALMON))
                mutiple = 0.2F;
            else if (material.equals(Material.WHEAT))
                mutiple = 0.15F;
            else if (material.equals(Material.CARROT))
                mutiple = 0.1F;
            else if (material.equals(Material.POTATO))
                mutiple = 0.1F;
            else if (material.equals(Material.MELON_SLICE))
                mutiple = 0.1F;
            else if (material.equals(Material.APPLE))
                mutiple = 0.4F;
            else if (material.equals(Material.GOLDEN_APPLE))
                mutiple = 20F;
            else if (material.toString().contains("_LOG"))
                mutiple = 0.3F;

            return (int) (item.getAmount() * mutiple);
        }
        return 0;
    }

    // ===============================================================================
    @EventHandler
    public static void onInventoryClick(InventoryClickEvent e) {
        // check whether the event has been cancelled by another plugin
        if (!e.isCancelled()) {
            HumanEntity ent = e.getWhoClicked();

            // not really necessary
            if (ent instanceof Player) {
                Player P = (Player) ent;
                player p = App.players.get(P.getName());
                if (e.getInventory() instanceof AnvilInventory) {
                    AnvilInventory inv = (AnvilInventory) e.getInventory();
                    if (e.getSlot() == 2 && e.getClickedInventory().getItem(2) != null
                            && P.getLevel() >= inv.getRepairCost()) {

                        if (inv.getRepairCost() == 0)
                            return;
                        if (inv.getItem(1) != null && inv.getItem(1).hasItemMeta()
                                && !inv.getItem(1).getItemMeta().hasLore()) {
                            p.magic += inv.getRepairCost() * 10;
                            App.addexp(P.getName(), inv.getRepairCost() * 10);
                            util.sendActionbarMessage(P, "加工道具, +" + inv.getRepairCost() * 10 + "生活經驗, +"
                                    + inv.getRepairCost() * 10 + "加工值");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public static void burning(InventoryClickEvent e) {

        if (e.getRawSlot() > -1 && e.getClickedInventory().getItem(e.getRawSlot()) != null) {
            ItemStack item = e.getClickedInventory().getItem(e.getRawSlot());
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && item.getItemMeta().getDisplayName().contains("燃燒內容物")) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void avoidMendingWithSpecial(InventoryClickEvent e) {

        if (e.isCancelled())
            return;

        HumanEntity ent = e.getWhoClicked();

        if (!(ent instanceof Player))
            return;

        Player P = (Player) ent;

        if (!(e.getInventory() instanceof AnvilInventory))
            return;

        AnvilInventory inv = (AnvilInventory) e.getInventory();

        if (e.getSlot() == 0 && P.getItemOnCursor() != null & P.getItemOnCursor().hasItemMeta() && P.getItemOnCursor().getItemMeta().hasLore() && inv.getItem(1) != null
                && inv.getItem(1).getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) inv.getItem(1).getItemMeta();
            Map<Enchantment, Integer> enchants = esm.getEnchants();
            if (enchants.containsKey(Enchantment.MENDING)) {
                e.setCancelled(true);
            }
        }

        if (e.getSlot() == 1 && inv.getItem(0) != null && inv.getItem(0).getItemMeta().hasLore()
                && P.getItemOnCursor().getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) P.getItemOnCursor().getItemMeta();
            Map<Enchantment, Integer> enchants = esm.getEnchants();
            if (enchants.containsKey(Enchantment.MENDING)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void special(InventoryClickEvent e) {
        // check whether the event has been cancelled by another plugin
        if (!e.isCancelled()) {
            HumanEntity ent = e.getWhoClicked();

            // not really necessary
            if (ent instanceof Player) {
                Player P = (Player) ent;
                player p = App.players.get(P.getName());
                if (e.getInventory() instanceof AnvilInventory) {
                    AnvilInventory inv = (AnvilInventory) e.getInventory();
                    if (e.getSlot() == 0 || e.getSlot() == 1) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (inv.getItem(0) != null && !inv.getItem(0).getItemMeta().hasLore()
                                        && inv.getItem(1) != null) {
                                    // 不可經驗驗修補放在一起
                                    if (!inv.getItem(1).containsEnchantment(Enchantment.MENDING)) {
                                        AvnilSetEnchant(inv, Material.DIAMOND_AXE, "重力砍伐", 1280);
                                        AvnilSetEnchant(inv, Material.DIAMOND_HOE, "自動種植", 1280);
                                        AvnilSetEnchant(inv, Material.BOW, "爆破箭矢", 640);
                                        AvnilSetEnchant(inv, Material.DIAMOND_SWORD, "威力打擊", 640);
                                        AvnilSetEnchant(inv, Material.DIAMOND_PICKAXE, "深度挖掘", 640);
                                        P.updateInventory();
                                    }
                                }
                                if (inv.getItem(0) != null && inv.getItem(1) != null
                                        && inv.getItem(0).getItemMeta().hasLore()) {
                                    if (inv.getItem(2) != null
                                            && inv.getItem(2).containsEnchantment(Enchantment.MENDING)) {
                                        inv.setItem(2, null);
                                        P.updateInventory();
                                    }
                                }
                            }
                        }.runTaskLater(plugin, 1);

                    } else if (e.getSlot() == 2) {
                        if (inv.getItem(1) != null && inv.getItem(1).hasItemMeta()
                                && inv.getItem(1).getItemMeta().hasLore() && inv.getItem(1).getItemMeta().hasLore()) {
                            if (!p.cla.contains("工匠")) {
                                P.closeInventory();
                                util.sendActionbarMessage(P, ChatColor.GOLD + "只有工匠懂得如何特殊附魔");
                            } else {
                                P.playSound(P.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                                P.getInventory().addItem(inv.getItem(2));
                                inv.clear();
                                P.updateInventory();
                            }
                        }
                    }
                }
            }
        }
    }

    // ============================================================================================
    public void AvnilSetEnchant(AnvilInventory anvilInv, String Tool, String enchant, int maxValue) {
        ItemStack item0 = anvilInv.getItem(0);
        ItemStack item1 = anvilInv.getItem(1);
        if (item1.getItemMeta().getLore() != null) {
            if (item1.getItemMeta().getLore().toString().contains(enchant)) {
                if (item0 != null && item0.getType().toString().contains(Tool)) {
                    int EnchantLevel = Function.GetEnchantmentLevel(item1.getItemMeta());
                    ItemStack newstack = new ItemStack(item0);
                    ItemMeta im = newstack.getItemMeta();
                    im.setLore(Arrays.asList(ChatColor.GRAY + enchant + " " + GetLevelSign(EnchantLevel), "",
                            ChatColor.AQUA + "" + ChatColor.BOLD + ("能量: " + 0 + " / " + maxValue)));
                    newstack.setItemMeta(im);
                    anvilInv.setItem(3, newstack);
                }
            }
        }
        return;
    }

    public void AvnilSetEnchant(AnvilInventory anvilInv, Material Tool, String enchant, int maxValue) {
        ItemStack item0 = anvilInv.getItem(0);
        ItemStack item1 = anvilInv.getItem(1);
        if (item1.getItemMeta().getLore() != null) {
            if (item1.getItemMeta().getLore().toString().contains(enchant)) {
                if (item0 != null && item0.getType().equals(Tool)) {
                    int EnchantLevel = Function.GetEnchantmentLevel(item1.getItemMeta());
                    ItemStack newstack = new ItemStack(item0);
                    ItemMeta im = newstack.getItemMeta();
                    im.setLore(Arrays.asList(ChatColor.GRAY + enchant + " " + GetLevelSign(EnchantLevel), "",
                            ChatColor.AQUA + "" + ChatColor.BOLD + ("能量: " + 0 + " / " + maxValue)));
                    newstack.setItemMeta(im);
                    anvilInv.setItem(3, newstack);
                }
            }
        }
        return;
    }

    public String GetLevelSign(int level) {
        return (String) ((level == 3) ? "III" : ((level == 2) ? "II" : ((level == 1) ? "I" : "")));
    }

}