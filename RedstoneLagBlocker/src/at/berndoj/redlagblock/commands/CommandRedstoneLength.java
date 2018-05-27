package at.berndoj.redlagblock.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.berndoj.redlagblock.main.RedstoneLagBlocker;
import at.berndoj.redlagblock.main.RedstoneWireLengthAlgorithm;

public class CommandRedstoneLength implements CommandExecutor {

	/**
	 * The host plugin of this command executor.
	 */
	public RedstoneLagBlocker pluginInstance;
	
	/**
	 * Constructor of this command executor.
	 * @param pluginInstance The host plugin of this command executor.
	 */
	public CommandRedstoneLength(RedstoneLagBlocker pluginInstance) {
		this.pluginInstance = pluginInstance;
	}
	
	/**
	 * Command execution. (Override)
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		// Check if command executor is a player.
		if (sender instanceof Player)
		{
			// V2 algorithm option
			boolean useV2Algorithm = true;
			// If there are arguments, parse the given 1st argument.
			if (args.length == 1)
				useV2Algorithm = Boolean.parseBoolean(args[0]);
			// Get the player
			Player player = (Player) sender;
			// Get the start time
			long algorithmStartTime = System.nanoTime();
			// Create a variable for the wirelength
			int wirecnt;
			// Execute the algorithm (option)
			if (useV2Algorithm)
				wirecnt = this.pluginInstance.wireLengthAlgorithm.getRedstoneLineLength(player.getWorld().getBlockAt(player.getLocation()), 350);
			else
				wirecnt = RedstoneWireLengthAlgorithm.getRedstoneWireCount(player.getWorld().getBlockAt(player.getLocation()), 350);
			// Get the end time
			long algorithmEndTime = System.nanoTime();
			// Send the output message to the player.
			player.sendMessage("Calculated wire length (Done in " + ((double)(algorithmEndTime - algorithmStartTime) / (double)1000000) + "ms): " + wirecnt);
			// Return
			return true;
		}
		else
			return false;
	}
	
}
