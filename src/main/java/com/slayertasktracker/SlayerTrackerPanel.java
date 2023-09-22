package com.slayertasktracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

public class SlayerTrackerPanel extends PluginPanel
{

	private final SlayerTrackerPlugin plugin;

	private final SlayerTrackerConfig config;

	private final Client client;

	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	private final SlayerTaskDataStorage storage;

	private String accountHash;

	@Inject
	SlayerTrackerPanel(final SlayerTrackerPlugin plugin, final SlayerTrackerConfig config, Client client)
	{
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		storage = new SlayerTaskDataStorage();

		setupPanelContent();
	}

	public void refreshPanel()
	{
		// Remove all existing components from the panel
		removeAll();

		// Reuse shared logic
		setupPanelContent();

		// Revalidate and repaint the UI components to reflect the changes
		revalidate();
		repaint();
	}

	private void setupPanelContent()
	{
		if (client.getAccountHash() != -1)
		{
			if (storage.hasSubFolders())
			{
				loadStatsPanel();
				return;
			}
			else
			{
				errorPanel.setContent("Slayer task tracker", "You have not been assigned a slayer task yet.");
			}
		}
		else
		{
			if (storage.hasSubFolders())
			{
				errorPanel.setContent("Slayer task tracker", "Login to display your slayer tasks.");
			}
			else
			{
				errorPanel.setContent("Slayer task tracker", "You have not been assigned a slayer task yet.");
			}
		}

		add(errorPanel);
	}

	private void loadStatsPanel()
	{
		// Main Layout Panel
		final JPanel mainPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
		mainPanel.setLayout(boxLayout);
		add(mainPanel, BorderLayout.NORTH);

		// New Combined Panel for Text and Icon
		JPanel combinedPanel = new JPanel(new BorderLayout());
		combinedPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// Information Panel
		final JPanel statsPanel = new JPanel();
		statsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		statsPanel.setLayout(new GridLayout(5, 1));
		statsPanel.setBorder(new EmptyBorder(10, 0, -4, 10));

		// Small font for other labels
		//String formattedLabel1 = formatLabel("Completed tasks: ", "0");
		JLabel label1 = new JLabel(formatLabel("Completed tasks: ", "0"));
		JLabel label2 = new JLabel(formatLabel("Avg Task Duration: ", "0"));
		JLabel label3 = new JLabel(formatLabel("Streak: ", "0"));
		JLabel label4 = new JLabel(formatLabel("Points: ", "0"));

		// Adding them to the panel
		statsPanel.add(label1);
		statsPanel.add(label2);
		statsPanel.add(label3);
		statsPanel.add(label4);

		// Icon Panel
		JPanel iconPanel = new JPanel(new BorderLayout());
		iconPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		iconPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		ImageIcon slayerTrackerIcon = new ImageIcon(ImageUtil.loadImageResource(getClass(), "slayer-task-tracker-icon.png"));
		iconPanel.add(new JLabel(slayerTrackerIcon), BorderLayout.WEST);

		// Adding statsPanel and iconPanel to combinedPanel
		combinedPanel.add(statsPanel, BorderLayout.CENTER);
		combinedPanel.add(iconPanel, BorderLayout.WEST);

		// Add to Main Panel
		mainPanel.add(combinedPanel);
	}

	public static String formatLabel(String key, String value) {
		String keyHexColor = ColorUtil.toHexColor(ColorScheme.LIGHT_GRAY_COLOR);
		String valueHexColor = ColorUtil.toHexColor(Color.WHITE);

		return String.format("<html><font color='%s'>%s</font><font color='%s'>%s</font></html>",
			keyHexColor, key, valueHexColor, value);
	}

}