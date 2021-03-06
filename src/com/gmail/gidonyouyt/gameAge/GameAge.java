package com.gmail.gidonyouyt.gameAge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.gmail.gidonyouyt.gameAge.core.CGame;
import com.gmail.gidonyouyt.gameAge.core.SendMessage;
import com.gmail.gidonyouyt.gameAge.events.BlockBreak;
import com.gmail.gidonyouyt.gameAge.events.BlockPlace;
import com.gmail.gidonyouyt.gameAge.events.EntityDamage;
import com.gmail.gidonyouyt.gameAge.events.EntityRegainHealth;
import com.gmail.gidonyouyt.gameAge.events.ExtraChangeDect;
import com.gmail.gidonyouyt.gameAge.events.PlayerInteract;
import com.gmail.gidonyouyt.gameAge.events.PlayerLeave;
import com.gmail.gidonyouyt.gameAge.events.PlayerPreLogin;

public class GameAge extends JavaPlugin {

	public static String pluginName = "버그덩어리";
	public static String pluginVersion = "버그버전";
	public static String[] update = new String[3];
	public ConfigYml yml = new ConfigYml(this);

	public GameAge() {

	}

	public void onEnable() {
		pluginName = getDescription().getName();
		pluginVersion = "v" + getDescription().getVersion();

		PluginDescriptionFile pdfFile = getDescription();
		Logger logger = Bukkit.getServer().getLogger();
		logger.info("GID'S MINI CONTENT!!");
		logger.info("Plugin Name: " + pdfFile.getName());
		logger.info("Plugin Version: " + pdfFile.getVersion());

		// Load Config
		yml.loadConfiguration();

		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new MainTimer(), 0L, 20L);

		// Commands
		getCommand("game").setExecutor(new CGame());
		getCommand("play").setExecutor(new CPlay(this));
		getCommand("ga").setExecutor(new CGa());
		getCommand("play").setTabCompleter(new CPlayCompleter());

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerLeave(), this);
		pm.registerEvents(new PlayerPreLogin(), this);
		pm.registerEvents(new BlockPlace(), this);
		pm.registerEvents(new BlockBreak(), this);
		pm.registerEvents(new EntityDamage(), this);
		pm.registerEvents(new PlayerInteract(this), this);
		pm.registerEvents(new EntityRegainHealth(), this);
		pm.registerEvents(new ExtraChangeDect(), this);

		// Test Web UI
		try {
			if (GameSettings.WEBPAGE_ON.value() == 1)
				WebManager.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Update Checker
		checkVersion();
	}

	public void onDisable() {
		Bukkit.getServer().getScheduler().cancelAllTasks();
		Sequence.stop();

	}

	public void checkVersion() {
		getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				try {
					URL rssURL = new URL("https://github.com/gidonyou/MCPlugin_GameAge/releases.atom");
					BufferedReader in = new BufferedReader(new InputStreamReader(rssURL.openStream()));
					String title = "";
					String desc = "";
					String line;
					boolean firstEntry = false;
					while ((line = in.readLine()) != null) {
						if (line.contains("<entry>"))
							firstEntry = true;
						if (!firstEntry)
							continue;

						if (!(title.isEmpty() || desc.isEmpty()))
							break;
						if (line.contains("<title>")) {
							int firstPos = line.indexOf("<title>");
							String temp = line.substring(firstPos);
							temp = temp.replace("<title>", "");
							temp = temp.replace("</title>", "");
							title += temp.toLowerCase();
						} else if (line.contains("<content type=\"html\">")) {
							int firstPos = line.indexOf("<content type=\"html\">");
							String temp = line.substring(firstPos);
							temp = temp.replace("<content type=\"html\">", "");
							temp = temp.replace("</content>", "");
							temp = temp.replaceAll("&lt;([^.]*?)&gt;", "");
							desc += temp;
						}
					}
					in.close();
					if (pluginVersion.equals("버그버전")) {
						SendMessage.sendMessageOP("업데이트 확인 불가능. plugin.yml 애러?");
						return;
					}
					String ver = pluginVersion;

					title = title.toLowerCase().replaceAll("[^\\d.]", "");
					ver = ver.toLowerCase().replaceAll("[^\\d.]", "");

					String[] remoteParts = title.split("\\.");
					String[] currParts = ver.split("\\.");
					if (remoteParts.length == currParts.length) {
						List<Integer> current = new ArrayList<>();
						List<Integer> remote = new ArrayList<>();
						for (int i = 0; i < remoteParts.length; i++) {
							try {
								current.add(Integer.valueOf(currParts[i]));
								remote.add(Integer.valueOf(remoteParts[i]));
							} catch (NumberFormatException e) {
								SendMessage.sendMessageOP("버전확인애러: 버전이 번호가 아님");
								return;
							}
						}

						int updateResult = 0; // -2 Unknown; -1 Remote Oudated; 0 Same Version; 1 Current Outdated
						for (int i = 0; i < current.size(); i++) {
							if (remote.get(i) > current.get(i)) {
								updateResult = 1;
								break;
							} else if (remote.get(i) < current.get(i)) {
								updateResult = -1;
								break;
							}
						}

						update[0] = String.valueOf(updateResult);
						update[1] = title;
						update[2] = desc;

					} else
						SendMessage.sendMessageOP("버전확인 애러: 돈유 일 안함");

				} catch (Exception e) {
					System.out.println("Can not Check update");
					e.printStackTrace();
				}

			}
		});
	}

}
