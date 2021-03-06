package com.gmail.gidonyouyt.gameAge.events;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

import com.gmail.gidonyouyt.gameAge.GameSettings;
import com.gmail.gidonyouyt.gameAge.Sequence;
import com.gmail.gidonyouyt.gameAge.core.GameStatus;
import com.gmail.gidonyouyt.gameAge.core.SendMessage;

public class BlockBreak implements Listener {

	private static HashMap<Block, Integer> blockDB = new HashMap<Block, Integer>();
	// private static ArrayList<Block> blockDB = new ArrayList<Block>();

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		// player.sendMessage(event.getBlock().getState().toString());
		if (!Sequence.getPlayerPlaying().contains(player))
			return;
		if ((GameStatus.getStatus() == GameStatus.RUNNING || GameStatus.getStatus() == GameStatus.COUNT_DOWN) && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
			if (GameSettings.ALLOW_BREAK_SPLATE.value() == 1) {
				Block block = event.getBlock();
				if (block.getType() == Material.STONE_PLATE) {
					SendMessage.sendMessagePlayer(player, ChatColor.DARK_RED + "게임중에는 압력판을 부술때마다 반피씩 깎입니다.");
					SendMessage.logConsole("강압판 부서짐  " + block.getLocation());
					blockDB.put(block, (int) GameSettings.TIME_RECOVER_SPLATE.value());
					player.damage(1);
					player.getWorld().strikeLightningEffect(block.getLocation());
					return;
				}
				SendMessage.sendMessagePlayer(player, ChatColor.DARK_RED + "게임중에는 압력판 외에 블럭을 부술수 없습니다.");
				event.setCancelled(true);
				return;
			}
			SendMessage.sendMessagePlayer(player, ChatColor.DARK_RED + "게임중에는 블럭을 부술수 없습니다.");
			event.setCancelled(true);

			// // Advanced Gameplay
			// SendMessage.sendMessagePlayer(player, ChatColor.DARK_RED + "게임중에는 블럭을 부술때마다
			// 반피씩 깎입니다.");
			// blockDB.add(event.getBlock());
		}
	}

	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		// if (event.getCause() != RemoveCause.ENTITY)
		// return;
		if (GameStatus.getStatus() != GameStatus.RUNNING)
			return;

		if (event.getRemover() instanceof Player) {
			Player player = (Player) event.getRemover();

			if (!Sequence.getPlayerPlaying().contains(player))
				return;

			// No Item Frame Break
			Entity block = event.getEntity();
			if (block instanceof ItemFrame || block instanceof Painting) {
				event.setCancelled(true);
				SendMessage.sendMessagePlayer(player, ChatColor.DARK_RED + "게임중에는 블럭을 부술수 없습니다.");
			}
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (GameStatus.getStatus() != GameStatus.RUNNING)
			return;

		// No Item Frame Break
		Entity block = event.getEntity();
		if (!(block instanceof ItemFrame || block instanceof Painting))
			return;

		if (event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();

			if (!Sequence.getPlayerPlaying().contains(player))
				return;

			event.setCancelled(true);
			SendMessage.sendMessagePlayer(player, ChatColor.DARK_RED + "게임중에는 블럭을 부술수 없습니다.");

		} else
			event.setCancelled(true);
	}

	public static void recoverAll() {
		for (Block b : blockDB.keySet()) {
			b.getLocation().getBlock().setType(Material.STONE_PLATE);
			SendMessage.logConsole("클린: 강압판 복구 성공  " + b.getLocation());
			blockDB.remove(b);
		}

		if (!blockDB.isEmpty())
			SendMessage.broadcastMessage(ChatColor.DARK_RED + "중요 애러: 압력판 복구 실패");
	}

	public static void autoRecover() {
		if (GameSettings.TIME_RECOVER_SPLATE.value() < 0)
			return;
		for (Block b : blockDB.keySet()) {
			int timeLeft = blockDB.get(b);
			if (timeLeft <= 1) {
				b.getLocation().getBlock().setType(Material.STONE_PLATE);
				SendMessage.logConsole("강압판 복구 성공  " + b.getLocation());
				blockDB.remove(b);
			} else {
				timeLeft--;
				blockDB.put(b, timeLeft);
			}
		}
	}

}
