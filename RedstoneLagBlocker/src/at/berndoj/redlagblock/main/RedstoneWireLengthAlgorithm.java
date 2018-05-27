package at.berndoj.redlagblock.main;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class RedstoneWireLengthAlgorithm {

	private static class WireCountResult
	{
		int countedDusts;
		int subroutineDepth;
		ArrayList<Block> updatedCheckedDusts;
		
		public WireCountResult(int countedDusts, ArrayList<Block> updatedCheckedDusts, int subroutineDepth) {
			this.countedDusts = countedDusts;
			this.updatedCheckedDusts = updatedCheckedDusts;
			this.subroutineDepth = subroutineDepth;
		}
	};
	
	/**
	 * Gets the count of redstone dust in a circuit
	 * @param b The block to check.
	 * @return The amount of blocks.
	 */
	public static int getRedstoneWireCount(Block b, int maxSubroutineExec)
	{
		if (b.getType() != Material.REDSTONE_WIRE)
			return 0;
		
		int connectedDusts = 1;
		ArrayList<Block> connectedDustsNear = getRedstoneDustsNear(b);
		ArrayList<Block> checkedDusts = new ArrayList<Block>();
		
		checkedDusts.add(b);
		
		for (Block dust : connectedDustsNear)
		{
			if (!checkedDusts.contains(dust))
			{
				WireCountResult result = getRedstoneWireCount(dust, checkedDusts, 1, maxSubroutineExec);
				connectedDusts += result.countedDusts;
				checkedDusts = result.updatedCheckedDusts;
			}
		}
		
		return connectedDusts;
	}
	
	/**
	 * Gets the count of redstone dust in a circuit
	 * (Option with 2 params)
	 * @param b The block to check.
	 * @param src The source block of the check.
	 * @return The amount of blocks.
	 */
	private static WireCountResult getRedstoneWireCount(Block b, ArrayList<Block> checkedDusts, int subroutineDepth, int maxSubroutineExec)
	{
		if (b.getType() != Material.REDSTONE_WIRE)
			return new WireCountResult(0, checkedDusts, subroutineDepth);
		
		if (checkedDusts.contains(b))
			return new WireCountResult(0, checkedDusts, subroutineDepth);
		
		int connectedDusts = 1;
		ArrayList<Block> connectedDustsNear = getRedstoneDustsNear(b);
		
		checkedDusts.add(b);
		subroutineDepth++;
		
		for (Block dust : connectedDustsNear)
		{
			if (subroutineDepth > maxSubroutineExec)
				return new WireCountResult(connectedDusts, checkedDusts, subroutineDepth);
			
			if (!checkedDusts.contains(dust))
			{
				WireCountResult result = getRedstoneWireCount(dust, checkedDusts, subroutineDepth, maxSubroutineExec);
				connectedDusts += result.countedDusts;
				checkedDusts = result.updatedCheckedDusts;
				subroutineDepth = result.subroutineDepth;
			}
		}
		
		return new WireCountResult(connectedDusts, checkedDusts, subroutineDepth);
	}
	
	/**
	 * Gets all near and connected redstone dusts to a block.
	 * @param b The block to get the connected redstone dusts near.
	 * @return A list of all connected redstone dusts.
	 */
	private static ArrayList<Block> getRedstoneDustsNear(Block b)
	{
		ArrayList<Block> dusts = new ArrayList<Block>();
		final BlockFace[] checkDirections = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
		
		// Check the blocks in the plane
		for (BlockFace face : checkDirections)
		{
			if (b.getRelative(face).getType() == Material.REDSTONE_WIRE)
				dusts.add(b.getRelative(face));
		}
		
		// Check the blocks in the upper plane
		for (BlockFace face : checkDirections)
		{
			if (b.getRelative(BlockFace.UP).getRelative(face).getType() == Material.REDSTONE_WIRE && !b.getRelative(BlockFace.UP).getType().isOccluding())
				dusts.add(b.getRelative(BlockFace.UP).getRelative(face));
		}
		
		// Check the blocks in the lower plane
		for (BlockFace face : checkDirections)
		{
			if (b.getRelative(BlockFace.DOWN).getRelative(face).getType() == Material.REDSTONE_WIRE && !b.getRelative(face).getType().isOccluding())
				dusts.add(b.getRelative(BlockFace.DOWN).getRelative(face));
		}
		
		return dusts;
	}
	
}
