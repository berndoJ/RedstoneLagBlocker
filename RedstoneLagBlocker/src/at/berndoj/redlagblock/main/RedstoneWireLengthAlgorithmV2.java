package at.berndoj.redlagblock.main;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class RedstoneWireLengthAlgorithmV2 {

	/**
	 * The plugin that hosts this util class.
	 */
	public RedstoneLagBlocker parentPlugin;
	
	/**
	 * List of valid redstone dusts.
	 * Standard allocated size is 256
	 */
	public static ArrayList<Location> validRedstoneList = new ArrayList<Location>(256);
	
	/**
	 * List of invalid redstone dusts.
	 * Standard allocated size is 256
	 */
	public static ArrayList<Location> invalidRedstoneList = new ArrayList<Location>(256);
	
	/**
	 * Constructor of this util class.
	 * @param parentPlugin The plugin that hosts this util class.
	 */
	public RedstoneWireLengthAlgorithmV2(RedstoneLagBlocker parentPlugin)
	{
		this.parentPlugin = parentPlugin;
	}
	
	/**
	 * Checks if a redstone dust is allowed to update by checking the length of the redstone line.
	 * (Option: Custom max length)
	 * @param dust The dust block to check
	 * @param maxDusts The maximum of allowed dusts
	 * @return <code>true</code> if the redstone wire is within the maximum length.
	 */
	public boolean canRedstoneDustUpdate(Block dust, int maxDusts)
	{
		// Checks if the given block is a redstone dust. (Assertion)
		if (dust.getType() != Material.REDSTONE_WIRE)
			return false;
		// Check if the block is contained in the valid redstone dust list. If so, return true.
		if (validRedstoneList.contains(dust.getLocation()))
			return true;
		// Check if the block is contained in the invalid redstone dust list. If so, return false.
		if (invalidRedstoneList.contains(dust.getLocation()))
			return false;
		// Gets the wire length of the redstone line
		ArrayList<Location> dustLine = getRedstoneLine(dust, maxDusts + 1);
		// Checks if the wire length is invalid and returns.
		if (dustLine.size() > maxDusts)
		{
			// Save all not included locations to the invalid redstone dust list and return.
			for (Location loc : dustLine)
				if (!invalidRedstoneList.contains(loc))
					invalidRedstoneList.add(loc);
			return false;
		}
		else
		{
			// Save all not included locations to the valid redstone dust list and return.
			for (Location loc : dustLine)
				if (!validRedstoneList.contains(loc))
					validRedstoneList.add(loc);
			return true;
		}
	}
	
	/**
	 * Checks if a redstone dust is allowed to update by checking the length of the redstone line.
	 * (Option: Config max length)
	 * @param dust The dust block to check
	 * @return <code>true</code> if the redstone wire is within the maximum length.
	 */
	public boolean canRedstoneDustUpdate(Block dust)
	{
		// Run the method with the config value of the max length.
		return this.canRedstoneDustUpdate(dust, this.parentPlugin.getConfig().getInt("redstoneWireLengthRestriction"));
	}
	
	/**
	 * Gets the length of a redstone line.
	 * @param dust The initial block of the line
	 * @param maxCount The maximum this algorithm should count up to.
	 * @return The length of the redstone line. (is always <= maxCount)
	 */
	public int getRedstoneLineLength(Block dust, int maxCount)
	{
		// Assertion: The given block is a redstone dust block.
		if (dust.getType() != Material.REDSTONE_WIRE)
			return 0;
		// Define two longs for time measurement (debug mode)
		long startTime = 0, endTime = 0;
		// If the plugin is in debug mode, save the start time.
		if (this.parentPlugin.isInDebugMode)
			startTime = System.nanoTime();
		// Create a new registration list of the size allocSpace.
		ArrayList<Location> dustList = new ArrayList<Location>(maxCount + 1);
		// Gets all the dusts connected (Stops when exceeding maxCount)
		dustList = this.continueRedstoneLengthCalc(dust, dustList, maxCount);
		// Save the size of the list.
		int lineLength = dustList.size();
		// Save all not included locations to the valid redstone dust list.
		for (Location loc : dustList)
			if (!validRedstoneList.contains(loc))
				validRedstoneList.add(loc);
		// If the plugin is in debug mode, save the end time.
		if (this.parentPlugin.isInDebugMode)
		{
			endTime = System.nanoTime();
			// Log statistics.
			this.parentPlugin.logDebug("Redstone wire-length calculation statistics: Time: " + (double)(endTime - startTime) / (double) 1000000 + "ms, Length calculated: " + lineLength + "dust.");
		}
		return lineLength;
	}
	
	/**
	 * Gets all dust locations of a redstone line.
	 * @param dust The initial block of the line
	 * @param maxCount The maximum this algorithm should count up to.
	 * @return The redstone line block locations. (size() is always <= maxCount)
	 */
	public ArrayList<Location> getRedstoneLine(Block dust, int maxCount)
	{
		// Assertion: The given block is a redstone dust block.
		if (dust.getType() != Material.REDSTONE_WIRE)
			return null;
		// Define two longs for time measurement (debug mode)
		long startTime = 0, endTime = 0;
		// If the plugin is in debug mode, save the start time.
		if (this.parentPlugin.isInDebugMode)
			startTime = System.nanoTime();
		// Create a new registration list of the size allocSpace.
		ArrayList<Location> dustList = new ArrayList<Location>(maxCount + 1);
		// Gets all the dusts connected (Stops when exceeding maxCount)
		dustList = this.continueRedstoneLengthCalc(dust, dustList, maxCount);
		
		// If the plugin is in debug mode, save the end time.
		if (this.parentPlugin.isInDebugMode)
		{
			endTime = System.nanoTime();
			// Log statistics.
			this.parentPlugin.logDebug("Redstone wire-length calculation statistics: Time: " + (double)(endTime - startTime) / (double) 1000000 + "ms, Length calculated: " + dustList.size() + "dust.");
		}
		return dustList;
	}
	
	/**
	 * Continues with the dust search.
	 * (Iteration function of this algorithm)
	 * @param currentDust The dust to start from.
	 * @param listedDusts The list of already captured dusts. (Dust locations for saving space)
	 * @param maxCount The maximum count of dusts. (If exceeded, the function will return.)
	 * @return The new list of captured dusts.
	 */
	private ArrayList<Location> continueRedstoneLengthCalc(Block currentDust, ArrayList<Location> listedDusts, int maxCount)
	{
		// Gets all connected redstone dusts around.
		ArrayList<Block> dustsAround = this.getConnectedRedstoneDusts(currentDust);
		
		// Iterates over all the dusts around
		for (Block dust : dustsAround)
		{
			// If the dust count is bigger than the maxCount, abort.
			if (listedDusts.size() >= maxCount)
				return listedDusts;
			// Gets the dust location
			Location dustLoc = dust.getLocation();
			// Checks if the dust is already in the list. If it is not, add it to the list.
			if (!listedDusts.contains(dustLoc))
			{
				listedDusts.add(dustLoc);
				// Countinue with the dust search.
				listedDusts = this.continueRedstoneLengthCalc(dust, listedDusts, maxCount);
			}
		}
		
		// All dusts checked. Return the list to the parent.
		return listedDusts;
	}
	
	/**
	 * Gets all the connected redstone dusts around a given dust.
	 * @param dust The dust to check the connected dusts of
	 * @return An ArrayList with the connected dusts
	 */
	private ArrayList<Block> getConnectedRedstoneDusts(Block dust)
	{
		// Create a new ArrayList object for storing the connected redstone dusts. There can only be 8 redstone dust connected to one other dust, so the initial capacity will be set to 8. (Usually 4, with glowstone or slabs 8)
		ArrayList<Block> dusts = new ArrayList<Block>(8);
		// Create a readonly array that stores the 4 directions a redstone dust can be connected (viewed form top)
		final BlockFace[] checkDirections = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
		
		// Checks all blocks on the same y-level as the dust.
		for (BlockFace face : checkDirections)
		{
			Block relative = dust.getRelative(face);
			if (relative.getType() == Material.REDSTONE_WIRE)
				dusts.add(relative);
		}
		
		// Checks all blocks on the y-level below the dust.
		for (BlockFace face : checkDirections)
		{
			Block relative = dust.getRelative(BlockFace.DOWN).getRelative(face);
			if (relative.getType() == Material.REDSTONE_WIRE && !dust.getRelative(face).getType().isOccluding())
				dusts.add(relative);
		}
		
		// Check if the above block is occluding. If it is, just continue (no connection to the top.)
		if (!dust.getRelative(BlockFace.UP).getType().isOccluding())
		{
			// Checks all blocks on the y-level above the dust.
			for (BlockFace face : checkDirections)
			{
				Block relative = dust.getRelative(BlockFace.UP).getRelative(face);
				if (relative.getType() == Material.REDSTONE_WIRE)
					dusts.add(relative);
			}
		}
		// Return the found dusts.
		return dusts;
	}
	
}
