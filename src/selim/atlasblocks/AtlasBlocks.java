package selim.atlasblocks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AtlasBlocks extends JavaPlugin implements Listener {

	public static PluginManager MANAGER;
	public static AtlasBlocks INSTANCE;
	public static Logger LOGGER;
	public static String VERSION;

	private final List<ExecuteCommand> COMMANDS = new LinkedList<ExecuteCommand>();
	// private final HashMap<Integer, String> COMMANDS = new HashMap<Integer,
	// String>();
	private final HashMap<UUID, Integer> BLOCK_BREAKS = new HashMap<UUID, Integer>();

	@Override
	public void onEnable() {
		INSTANCE = this;
		LOGGER = this.getLogger();
		MANAGER = this.getServer().getPluginManager();
		VERSION = this.getDescription().getVersion();
		MANAGER.registerEvents(this, this);
		createConfig();
		FileConfiguration config = INSTANCE.getConfig();
		List<String> cmds = config.getStringList("breakCommands");
		for (String c : cmds) {
			int num = Integer.valueOf(c.substring(0, c.indexOf(';')));
			String cmd = c.substring(c.indexOf(';') + 1);
			boolean isConsole = cmd.equalsIgnoreCase("c");
			if (isConsole)
				cmd = c.substring(c.lastIndexOf(';') + 1);
			COMMANDS.add(new ExecuteCommand(num, cmd, isConsole));
		}
		File dataFile = new File(this.getDataFolder(), "data");
		if (dataFile.exists() && !dataFile.isDirectory()) {
			try {
				FileReader fr = new FileReader(dataFile);
				BufferedReader br = new BufferedReader(fr);
				String line = br.readLine();
				while (line != null) {
					int blocks = Integer.valueOf(line.substring(0, line.indexOf('-')));
					UUID uuid = UUID.fromString(line.substring(line.indexOf('-') + 1));
					BLOCK_BREAKS.put(uuid, blocks);
					line = br.readLine();
				}
				if (fr != null)
					fr.close();
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDisable() {
		INSTANCE = null;
		LOGGER = null;
		MANAGER = null;
		File dataFile = new File(this.getDataFolder(), "data");
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (dataFile.exists() && !dataFile.isDirectory()) {
			try {
				FileWriter fw = new FileWriter(dataFile);
				BufferedWriter bw = new BufferedWriter(fw);
				for (Entry<UUID, Integer> s : BLOCK_BREAKS.entrySet()) {
					bw.write(s.getValue() + "-" + s.getKey().toString() + '\n');
					bw.flush();
				}
				if (fw != null)
					fw.close();
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Player p = event.getPlayer();
		UUID pUUID = p.getUniqueId();
		int breaks;
		if (BLOCK_BREAKS.containsKey(pUUID))
			breaks = BLOCK_BREAKS.get(pUUID) + 1;
		else
			breaks = 1;
		BLOCK_BREAKS.put(pUUID, breaks);
		for (ExecuteCommand cmd : COMMANDS)
			if (cmd.getNumBlocks() == breaks)
				cmd.execute(p);
	}

	private void createConfig() {
		try {
			if (!getDataFolder().exists()) {
				getDataFolder().mkdirs();
			}
			File file = new File(getDataFolder(), "config.yml");
			if (!file.exists()) {
				getLogger().info("Config.yml not found, creating!");
				saveDefaultConfig();
			} else {
				getLogger().info("Config.yml found, loading!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
