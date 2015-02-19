package net.jp.minecraft.plugin;

import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.inventory.meta.SkullMeta;
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
		if (cmd.getName().equalsIgnoreCase("ait")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Please excute this /ait command on a game!");
				sender.sendMessage("/ait コマンドはゲーム内で実行してください。");
			}
			else {
				Player player = (Player) sender;
				if(player.hasPermission("ait.give")||player.isOp()){
					//palyerがait.giveまたはopであれば杖を渡す

					ItemStack item = new ItemStack(Material.STICK);
					ItemMeta itemmeta = item.getItemMeta();
					itemmeta.setDisplayName(ChatColor.GOLD + "AdminInventoryTools");
					itemmeta.setLore(Arrays.asList(ChatColor.YELLOW + "魔法の杖:", ChatColor.WHITE + "この杖を空気に向かってクリックすると", ChatColor.WHITE + "スバラシイ画面が現れるだろう。"));
					itemmeta.addEnchant(Enchantment.SILK_TOUCH , 1, true);
					item.setItemMeta(itemmeta);
					player.getInventory().addItem(item);

					player.sendMessage(ChatColor.AQUA + "[情報]AdminInventoryToolsを与えました。");
				}
			}
			return true;
		}

		else if (cmd.getName().equalsIgnoreCase("skull")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Please excute this /skull command on a game!");
				sender.sendMessage("/skull コマンドはゲーム内で実行してください。");
			}
			else {
				Player player = (Player) sender;
				if(player.hasPermission("ait.skull")||player.isOp()){
					//palyerがskull.giveまたはopであればここを抜ける
					if(args.length == 0){
						sender.sendMessage(ChatColor.AQUA + "[情報]/skull <player> でプレイヤーの頭を取得できます。");
					}
					else{
					 ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
					 SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
					 skull.setDurability((short) 3);
					 skullMeta.setDisplayName(ChatColor.GOLD + args[0] + "の頭");
					 skullMeta.setOwner(args[0]);
					 skull.setItemMeta(skullMeta);

					 skull.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
					 player.getInventory().addItem(skull);

					player.sendMessage(ChatColor.AQUA + "[情報]"+ args[0] + "の頭を与えました。");
					}
				}
			}
			return true;
		}
		return false;
	}


	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event , ItemStack item , Player[] players){
		final Player p = event.getPlayer();
		if(p.hasPermission("ait.open")||p.isOp()){
			if(p.getItemInHand().getType()==Material.AIR){
				//何でもなかった場合無視
			}
			else if(p.getItemInHand().getItemMeta().getDisplayName()==null){
				//通常の棒だった場合無視する
			}
			else if(p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GOLD + "AdminInventoryTools")){
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
				World world = player.getWorld();
				if(event.isRightClick() || event.isLeftClick()|| event.getAction()==InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.DROP_ONE_SLOT ||event.getAction() == InventoryAction.DROP_ALL_SLOT){
					if(event.getRawSlot()==0){
						if(player.hasPermission("ait.gui.timeset.morning")){
							world.setTime(0);
							player.sendMessage(ChatColor.AQUA + "[情報]時間を0に設定しました。");
							player.closeInventory();
						}
					}

					if(event.getRawSlot()==1){
						if(player.hasPermission("ait.gui.timeset.night")){
							world.setTime(12500);
							player.sendMessage(ChatColor.AQUA + "[情報]時間を12500に設定しました。");
							player.closeInventory();
						}
					}

					if(event.getRawSlot()==2){
						if(player.hasPermission("ait.gui.gamemode")){
							if(player.getGameMode() == GameMode.SURVIVAL){
								player.setGameMode(GameMode.CREATIVE);
								player.sendMessage(ChatColor.AQUA + "[情報]ゲームモードをクリエイティブにしました。");
							}
							else if(player.getGameMode() == GameMode.CREATIVE){
								player.setGameMode(GameMode.SURVIVAL);
								player.sendMessage(ChatColor.AQUA + "[情報]ゲームモードをサバイバルにしました。");
							}
							//サバイバル・クリエイティブでないゲームモードはあまり使わないよね？ってことで他はとりあえずサバイバルに設定
							else{
								player.setGameMode(GameMode.SURVIVAL);
								player.sendMessage(ChatColor.AQUA + "[情報]ゲームモードをサバイバルにしました。");
							}
							player.closeInventory();
						}
					}

					if(event.getRawSlot()==3){
						if(player.hasPermission("ait.gui.whitelist")){
							if(Bukkit.hasWhitelist()==true){
								Bukkit.setWhitelist(false);
								player.sendMessage(ChatColor.AQUA + "[情報]ホワイトリストを無効化しました。");
							}
							else if(Bukkit.hasWhitelist()==false){
								Bukkit.setWhitelist(true);
								player.sendMessage(ChatColor.AQUA + "[情報]ホワイトリストを有効化しました。");
							}
							player.closeInventory();
						}
					}

					if(event.getRawSlot()==4){
						if(player.hasPermission("ait.gui.skull")){
							 ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
							 SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
							 skull.setDurability((short) 3);
							 skullMeta.setDisplayName(ChatColor.GOLD + player.getName() + "の頭");
							 skullMeta.setOwner(player.getName());
							 skull.setItemMeta(skullMeta);

							 skull.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
							 player.getInventory().addItem(skull);

							 player.closeInventory();
						}
					}

					if(event.getRawSlot()==5){
						if(player.hasPermission("ait.gui.op")){
							if(player.isOp()==true){
								player.sendMessage(ChatColor.AQUA + "[情報]オペレータ権限を剥奪しました。");
								player.setOp(false);
							}
							else if(player.isOp()==false){
								player.sendMessage(ChatColor.AQUA + "[情報]オペレータ権限を取得しました。");
								player.setOp(true);
							}
							player.closeInventory();
						}
					}

					if(event.getRawSlot()==8){
						player.closeInventory();
					}

					event.setCancelled(true);
				}
			}
		}
	}
}
