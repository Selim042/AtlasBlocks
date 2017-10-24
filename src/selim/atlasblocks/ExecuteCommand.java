package selim.atlasblocks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExecuteCommand {

	private final int numBlocks;
	private final String cmd;
	private final boolean isConsole;

	public ExecuteCommand(int numBlocks, String cmd) {
		this(numBlocks, cmd, false);
	}

	public ExecuteCommand(int numBlocks, String cmd, boolean isConsole) {
		this.numBlocks = numBlocks;
		this.cmd = cmd;
		this.isConsole = isConsole;
	}

	public int getNumBlocks() {
		return this.numBlocks;
	}

	public void execute(Player target, Player sender) {
		cmd.replace("{{PLAYER}}", target.getName());
		if (isConsole)
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
		else
			sender.performCommand(cmd);
	}

}
