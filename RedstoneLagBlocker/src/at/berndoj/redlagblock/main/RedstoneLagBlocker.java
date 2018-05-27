package at.berndoj.redlagblock.main;

import java.util.HashMap;

import org.bukkit.plugin.java.JavaPlugin;

import at.berndoj.redlagblock.commands.RedstoneLagBlockerCommandRegistry;
import at.berndoj.redlagblock.util.BooleanToStringConverter;

public class RedstoneLagBlocker extends JavaPlugin {

	/**
	 * Default config values for this plugin
	 */
	public static final HashMap<String, Object> defaultConfigValues = new HashMap<String, Object>();
	
	/**
	 * Static initializer of this class
	 */
	static
	{
		defaultConfigValues.put("debugMode", false);
		defaultConfigValues.put("isRedstoneWireLengthRestrictionEnabled", true);
		defaultConfigValues.put("redstoneWireLengthRestriction", 30);
		defaultConfigValues.put("listRefreshCycleTicks", 5L);
		defaultConfigValues.put("resetTickingDustsTicks", 2L);
		defaultConfigValues.put("redstoneOverloadSensitivity", 16);
		defaultConfigValues.put("redstoneTickOverloadListResetTicks", 200L);
		defaultConfigValues.put("redstoneTickOverloadResetTicks", 200L);
		defaultConfigValues.put("sendMessageOnOverloadBlockNums", 3);
		defaultConfigValues.put("messageOverloadBlocksListResetTicks", 1200L);
	}
	
	/**
	 * Plugin debug status (true = enabled)
	 */
	public boolean isInDebugMode;
	
	/**
	 * Creates an instance of the wire length algorithm.
	 */
	public RedstoneWireLengthAlgorithmV2 wireLengthAlgorithm = new RedstoneWireLengthAlgorithmV2(this);
	
	/**
	 * Gets executed when the plugin gets loaded.
	 */
	@Override
	public void onEnable()
	{
		this.getServer().getPluginManager().registerEvents(new RedstoneLagBlockerListener(this), this);
		
		RedstoneLagBlockerCommandRegistry.registerCommands(this);
		
		this.onLoadConfig();
		
		this.isInDebugMode = this.getConfig().getBoolean("debugMode");
		System.out.println("[RedstoneLagBlocker] Debug mode is: " + BooleanToStringConverter.getEnabledDisabled(this.isInDebugMode));
		if (this.isInDebugMode)
			this.printConfiguration();
		
		this.registerSchedules();
		
		System.out.println("[RedstoneLagBlocker] Enabled RedstoneLagBlocker.");
	}
	
	/**
	 * Gets executed when the plugin gets de-loaded.
	 */
	@Override
	public void onDisable()
	{
		System.out.println("[RedstoneLagBlocker] Disabled RedstoneLagBlocker.");
	}
	
	/**
	 * Standard logging method for this plugin
	 * @param s The message to log
	 */
	public void log(String s)
	{
		System.out.println("[RedstoneLagBlocker] " + s);
	}
	
	/**
	 * Standard debug logging method for this plugin
	 * @param s The message to log
	 */
	public void logDebug(String s)
	{
		System.out.println("[RedstoneLagBlocker] [Debug] " + s);
	}
	
	/**
	 * Loads the config defaults etc.
	 */
	private void onLoadConfig()
	{
		for (String key : defaultConfigValues.keySet())
		{
			this.getConfig().addDefault(key, defaultConfigValues.get(key));
		}
		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().header("Configuration file for the RedstoneLagBlocker plugin.").copyHeader(true);
		this.saveConfig();
	}
	
	/**
	 * Prints the config file (used in debug mode)
	 */
	private void printConfiguration()
	{
		for (String key : defaultConfigValues.keySet())
		{
			this.logDebug("[Config] " + key + ": " + this.getConfig().get(key).toString());
		}
	}
	
	/**
	 * Registers the schedules of this plugin
	 */
	private void registerSchedules()
	{
		// Resets the redstone validation lists.
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
			/**
			 * Resets the redstone validation lists.
			 */
			@Override
			public void run()
			{
				RedstoneWireLengthAlgorithmV2.validRedstoneList.clear();
				RedstoneWireLengthAlgorithmV2.invalidRedstoneList.clear();
			}
			
		}, 0L, this.getConfig().getLong("listRefreshCycleTicks"));
		// Resets the ticking redstone lists.
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
			/**
			 * Resets the ticking redstone list.
			 */
			@Override
			public void run()
			{
				RedstoneLagBlockerListener.tickingRedstoneDusts.clear();
			}
			
		}, 0L, this.getConfig().getLong("resetTickingDustsTicks"));
		// Resets the overload list.
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
			/**
			 * Resets the ticking overload dust list.
			 */
			@Override
			public void run()
			{
				RedstoneLagBlockerListener.tickingOverloads.clear();
			}
			
		}, 0L, this.getConfig().getLong("redstoneTickOverloadListResetTicks"));
		// Resets the overlad message list.
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
			/**
			 * Resets the ticking overload dust list.
			 */
			@Override
			public void run()
			{
				RedstoneLagBlockerListener.numOverloadBlocks.clear();
			}
			
		}, 0L, this.getConfig().getLong("messageOverloadBlocksListResetTicks"));
	}
}
