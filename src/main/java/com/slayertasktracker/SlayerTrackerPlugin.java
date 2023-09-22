package com.slayertasktracker;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.slayer.SlayerPlugin;
import net.runelite.client.plugins.slayer.SlayerPluginService;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Slayer task tracker",
	description = "Chronologically track and detail your Slayer tasks"
)
@PluginDependency(SlayerPlugin.class)
public class SlayerTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SlayerTrackerConfig config;

	@Inject
	private SlayerPluginService slayerPluginService;

	@Inject
	private SlayerTaskDataStorage slayerTaskDataStorage;

	@Inject
	private ClientToolbar clientToolbar;

	private SlayerTaskData slayerTaskData;

	private NavigationButton navButton;

	private SlayerTrackerPanel panel;

	private Map<String, String> lastTaskDetails = null;

	private Actor actorInteractedWith;

	private String actorName;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Slayer tracker started!");
		if (isLoggedIn())
		{
			slayerTaskDataStorage = new SlayerTaskDataStorage();
			String accountHash = String.valueOf(client.getAccountHash());
			slayerTaskDataStorage.setAccountHash(accountHash);
			loadSlayerTaskData();
		}
		panel = injector.getInstance(SlayerTrackerPanel.class);
		addNavigationButton();
	}

	private boolean isLoggedIn()
	{
		return client.getAccountHash() != -1;
	}

	private void addNavigationButton()
	{
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "slayer-task-tracker-icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Slayer Task Tracker")
			.icon(icon)
			.panel(panel)
			.priority(5)
			.build();
		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		log.info("Slayer tracker stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		final GameState state = gameStateChanged.getGameState();

		if (state == GameState.LOGIN_SCREEN || state == GameState.LOGGED_IN)
		{
			SwingUtilities.invokeLater(() -> panel.refreshPanel());
		}

		if (state == GameState.LOGGED_IN && isLoggedIn())
		{
			String accountHash = String.valueOf(client.getAccountHash());
			slayerTaskDataStorage.setAccountHash(accountHash);
			loadSlayerTaskData();
		}
	}


	private void loadSlayerTaskData()
	{
		slayerTaskData = slayerTaskDataStorage.loadSlayerTaskData();

		if (slayerTaskData == null)
		{
			slayerTaskData = new SlayerTaskData();
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		Actor target = interactingChanged.getTarget();
		Actor source = interactingChanged.getSource();

		if (target != null && source == client.getLocalPlayer()) {
			actorInteractedWith = target;
			actorName = actorInteractedWith.getName();
		}
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (actorInteractedWith == null) {
			return;
		}

		Widget npcDialog = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
		if (npcDialog == null) return;

		String dialogText = npcDialog.getText();
		Matcher matcher = Pattern.compile("Your new task is to kill").matcher(dialogText);

		if (isLoggedIn())
		{
			if (matcher.find())
			{
				handleNewTask();
			}
		}
	}

	private void handleNewTask()
	{
		LocalDateTime now = LocalDateTime.now();
		String formatPattern = "MMMM dd, yyyy";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);

		String taskMonster = slayerPluginService.getTask() != null ? slayerPluginService.getTask() : "";
		int taskAmount = slayerPluginService.getInitialAmount();

		if (taskAmount > 0 && !taskMonster.isEmpty())
		{
			String taskTime = now.format(DateTimeFormatter.ofPattern("h:mma"));

			Map<String, String> newTaskDetails = new HashMap<>();
			newTaskDetails.put("monster", taskMonster);
			newTaskDetails.put("amount", String.valueOf(taskAmount));
			newTaskDetails.put("time", taskTime);
			newTaskDetails.put("npc", actorName);

			if (!newTaskDetails.equals(lastTaskDetails))
			{
				String currentDate = now.format(formatter);
				slayerTaskData = slayerTaskDataStorage.loadSlayerTaskData();
				slayerTaskData.addTaskForDate(currentDate, newTaskDetails);
				slayerTaskDataStorage.saveSlayerTaskData(slayerTaskData);

				lastTaskDetails = newTaskDetails;
			}
		}
	}

	@Provides
	SlayerTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SlayerTrackerConfig.class);
	}
}
