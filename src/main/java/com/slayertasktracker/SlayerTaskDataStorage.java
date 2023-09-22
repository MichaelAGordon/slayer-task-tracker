package com.slayertasktracker;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import lombok.Getter;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

public class SlayerTaskDataStorage
{
	private static final File SLAYER_TRACKER_DIR = new File(RUNELITE_DIR, "slayer_tracker_data");

	private final Gson gson;

	@Getter
	private String accountHash;

	public SlayerTaskDataStorage()
	{
		gson = new Gson();
		if (!SLAYER_TRACKER_DIR.exists())
		{
			SLAYER_TRACKER_DIR.mkdirs(); // Create the main plugin directory if it doesn't exist
		}
	}

	public void setAccountHash(String accountHash)
	{
		this.accountHash = accountHash;
	}

	private File getPlayerSpecificDir()
	{
		File playerDir = new File(SLAYER_TRACKER_DIR, accountHash);
		if (!playerDir.exists())
		{
			playerDir.mkdirs(); // Create the player specific directory if it doesn't exist
		}
		return playerDir;
	}

	public boolean hasSubFolders() {
		File[] files = SLAYER_TRACKER_DIR.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					return true;
				}
			}
		}
		return false;
	}

	public void saveSlayerTaskData(SlayerTaskData slayerTaskData)
	{
		File playerDir = getPlayerSpecificDir();
		File dataFile = new File(playerDir, "slayer_tracker_data.json");

		try (FileWriter writer = new FileWriter(dataFile))
		{
			gson.toJson(slayerTaskData, writer);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public SlayerTaskData loadSlayerTaskData()
	{
		File playerDir = getPlayerSpecificDir();
		File dataFile = new File(playerDir, "slayer_tracker_data.json");

		if (!dataFile.exists())
		{
			return new SlayerTaskData(); // Return a new empty object if the file doesn't exist
		}

		try (FileReader reader = new FileReader(dataFile))
		{
			return gson.fromJson(reader, SlayerTaskData.class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return new SlayerTaskData(); // Return a new empty object in case of an error
		}
	}
}