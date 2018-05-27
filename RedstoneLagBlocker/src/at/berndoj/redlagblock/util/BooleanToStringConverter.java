package at.berndoj.redlagblock.util;

public class BooleanToStringConverter {

	public static String getEnabledDisabled(boolean value)
	{
		if (value)
			return "Enabled";
		else
			return "Disabled";
	}
	
}
