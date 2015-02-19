package net.jp.minecraft.plugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

/**
 * よく使うコマンドやアクションをチェスト画面を利用して操作しよう！ってプラグイン
 * AdminInventoryTools
 * @author syokkendesuyo
 */


public class ReportGUI extends JavaPlugin implements Listener {


	/**
	 * プラグインが有効になったときに呼び出されるメソッド
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */

	public Inventory createinv;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

	}

	//コマンドで杖を渡す処理
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("report")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Please excute this /ait command on a game!");
				sender.sendMessage("/report コマンドはゲーム内で実行してください。");
			}
			else {
				Player player = (Player) sender;
				if(player.hasPermission("report.give")||player.isOp()){
					//palyerがait.giveまたはopであれば杖を渡す

					ItemStack item = new ItemStack(Material.STICK);
					ItemMeta itemmeta = item.getItemMeta();
					itemmeta.setDisplayName(ChatColor.GOLD + "ReportTool");
					itemmeta.setLore(Arrays.asList(ChatColor.YELLOW + "魔法の杖:", ChatColor.WHITE + "この杖を空気に向かってクリックすると", ChatColor.WHITE + "通報画面が現れます。"));
					itemmeta.addEnchant(Enchantment.SILK_TOUCH , 1, true);
					item.setItemMeta(itemmeta);
					player.getInventory().addItem(item);

					player.sendMessage(ChatColor.AQUA + "[情報]レポートツールを与えました。");
				}
			}
			return true;
		}

		return false;
	}


	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event , ItemStack item , Player[] players){
		final Player p = event.getPlayer();
		if(p.hasPermission("report.open")||p.isOp()){
			if(p.getItemInHand().getType()==Material.AIR){
				//何でもなかった場合無視
			}
			else if(p.getItemInHand().getItemMeta().getDisplayName()==null){
				//通常の棒だった場合無視する
			}
			else if(p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GOLD + "ReportTool")){
				if(event.getAction() == Action.LEFT_CLICK_AIR){
					if(p.getItemInHand().getType() == Material.STICK){

						//ここの内容はインベントリGUIのアイテム設定です
						ItemStack Item = item;
						ItemMeta meta = item.getItemMeta();

						//□Report□インベントリに接続
						createinv = Bukkit.createInventory(p, 54, "□Report□");

						//ループ処理
						//zはカウントアップ
						for(int z=0 ; z<players.length;z++){
							if(players.length<54){
								//プレイヤーの名前をDipsplayNameへセット、Lore1行目にUUIDを記載し
								String puuid = players[z].getUniqueId().toString();
								meta.setDisplayName(players[z].getPlayer().getName());
								meta.setLore(Arrays.asList(puuid, ChatColor.WHITE + "プレイヤーを通報するには", ChatColor.WHITE + "クリックしましょう。"));
								Item.setItemMeta(meta);
								createinv.setItem(z, Item);
							}
							else{
								p.sendMessage(ChatColor.RED + "プレイヤーが54名以上なので対応していません。");
							}

						}
						//インベントリオープン
						//□Report□のクリックイベントを取得することで続きの処理が可能
						p.openInventory(createinv);
					}
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event){
		if (event.getInventory().getName().equalsIgnoreCase("□Report□")){
			if (event.getRawSlot() < 54 && event.getRawSlot() > -1){
				Player player = (Player) event.getWhoClicked();
				if(event.isRightClick() || event.isLeftClick()|| event.getAction()==InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.DROP_ONE_SLOT ||event.getAction() == InventoryAction.DROP_ALL_SLOT){

					//loreからuuidを取得
					ItemStack item = event.getCurrentItem();
					ItemMeta meta = item.getItemMeta();
					List<String> lore = meta.getLore();
					String puuid =lore.get(0);

					player.sendMessage(ChatColor.AQUA + "クリックしたPlayerのUUIDは" + puuid + "です。");

					player.closeInventory();

					event.setCancelled(true);
				}
			}
		}
	}
}
