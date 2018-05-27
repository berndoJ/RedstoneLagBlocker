package at.berndoj.redlagblock.commands;

import at.berndoj.redlagblock.main.RedstoneLagBlocker;

public class RedstoneLagBlockerCommandRegistry {

	/**
	 * Registers all commands.
	 * @param p The host plugin of this registry.
	 */
	public static void registerCommands(RedstoneLagBlocker p)
	{
		p.getCommand("redstonelength").setExecutor(new CommandRedstoneLength(p));
	}
	
}
