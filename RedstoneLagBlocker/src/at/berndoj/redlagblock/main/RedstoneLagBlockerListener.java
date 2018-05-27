package at.berndoj.redlagblock.main;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneLagBlockerListener implements Listener
{

	/**
	 * Contains a list of ticking dusts.
	 */
	public static ArrayList<Location> tickingRedstoneDusts = new ArrayList<Location>(512);
	
	/**
	 * Contains a hash map with all the ticking overloads of blocks.
	 */
	public static HashMap<Location, Integer> tickingOverloads = new HashMap<Location, Integer>();
	
	/**
	 * Contains a list of overloaded dusts.
	 */
	public static ArrayList<Location> overloadedRedstoneDusts = new ArrayList<Location>(128);
	
	/**
	 * Contains a hash map with all overload blocks per block.
	 */
	public static HashMap<Location, Integer> numOverloadBlocks = new HashMap<Location, Integer>();
	
	/**
	 * The host plugin of this listener.
	 */
	public RedstoneLagBlocker pluginInstance;
	
	/**
	 * Constructor of this event listener.
	 * @param pluginInstance The host plugin of this listener.
	 */
	public RedstoneLagBlockerListener(RedstoneLagBlocker pluginInstance)
	{
		this.pluginInstance = pluginInstance;
	}
	
	/**
	 * Event: Fired when a block's redstone current changes.
	 * @param ev The event object.
	 */
	@EventHandler
	public void onRedstoneBlockChange(BlockRedstoneEvent ev)
	{
		// Gets the block.
		Block dust = ev.getBlock();
		// Checks if the block is a redstone dust.
		if (dust.getType() != Material.REDSTONE_WIRE)
			return;
		// Gets the location of the block
		Location dustLoc = dust.getLocation();
		// Check if the dust location is in the ticking register list.
		if (tickingRedstoneDusts.contains(dustLoc) && !overloadedRedstoneDusts.contains(dustLoc))
		{
			// The dust is ticking to fast.
			// Increase the tick overload number for this location.
			if (!tickingOverloads.keySet().contains(dustLoc))
				tickingOverloads.put(dustLoc, 1);
			else
				tickingOverloads.put(dustLoc, tickingOverloads.get(dustLoc) + 1);
			// If there is a tick overload, lock the redstone dust.
			if (tickingOverloads.get(dustLoc) > this.pluginInstance.getConfig().getInt("redstoneOverloadSensitivity") * 5)
			{
				// There is an overload at this redstone location. Message the console if in DebugMode.
				if (this.pluginInstance.isInDebugMode)
					this.pluginInstance.logDebug("[Info] Ticking overload detected (overloads: " + tickingOverloads.get(dustLoc) + ") at X:" + dustLoc.getBlockX() + ", Y:" + dustLoc.getBlockY() + ", Z:" + dustLoc.getBlockZ());
				// For a specific amont of time, this redstone will not be able to tick.
				overloadedRedstoneDusts.add(dustLoc);
				// Add the overload block to the list of overload blocks triggered.
				if (!numOverloadBlocks.keySet().contains(dustLoc))
					numOverloadBlocks.put(dustLoc, 1);
				else
					numOverloadBlocks.put(dustLoc, numOverloadBlocks.get(dustLoc) + 1);
				// Send a message if the overload blocks exceed the message level.
				this.pluginInstance.log("DustLoc num overloads: " + numOverloadBlocks.get(dustLoc));
				if (numOverloadBlocks.get(dustLoc) >= this.pluginInstance.getConfig().getInt("sendMessageOnOverloadBlockNums"))
				{
					//TODO: Send Message to admins
					this.pluginInstance.log("[WARNING] Found ticking redstone line! Overload block count exceeded maxmimum. Location: X:" + dustLoc.getBlockX() + ", Y:" + dustLoc.getBlockY() + ", Z:" + dustLoc.getBlockZ());
				}
				// Add task that removes dusts.
				this.pluginInstance.getServer().getScheduler().scheduleSyncDelayedTask(this.pluginInstance, new Runnable() {

					/**
					 * Resets the overload block. (It is always on index 1)
					 */
					@Override
					public void run()
					{
						if (RedstoneLagBlockerListener.overloadedRedstoneDusts.size() > 0)
							RedstoneLagBlockerListener.overloadedRedstoneDusts.remove(0);
					}
					
				}, this.pluginInstance.getConfig().getLong("redstoneTickOverloadResetTicks"));
			}
			// Set the new redstone current to 0. (Abort event)
			ev.setNewCurrent(0);
			return;
		}
		else if (overloadedRedstoneDusts.contains(dustLoc))
		{
			// The dust is overloaded. Cancel the event.
			ev.setNewCurrent(0);
			return;
		}
		else
		{
			// Register the dust location as ticking.
			tickingRedstoneDusts.add(dustLoc);
			// If the length is not valid, set the new redstone current to 0.
			if (!this.pluginInstance.wireLengthAlgorithm.canRedstoneDustUpdate(dust))
				ev.setNewCurrent(0);
			return;
		}
	}
	
}
