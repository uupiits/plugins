package net.runelite.client.plugins.praypotdrinker;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Extension
@PluginDescriptor(
		name = "Prayer Pot Drinker",
		description = "Automatically drink pray pots",
		tags = {"combat", "notifications", "prayer"},
		enabledByDefault = false,
		type = PluginType.PVM
)
public class PrayPotDrinkerPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private PrayPotDrinkerConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	private ItemManager itemManager;

	private String[] potions;

	private MenuEntry entry;

	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
			new ThreadPoolExecutor.DiscardPolicy());

	@Provides
	PrayPotDrinkerConfig provideConfig(final ConfigManager configManager) {
		return configManager.getConfig(PrayPotDrinkerConfig.class);
	}

	@Override
	protected void startUp() throws Exception {
		potions = config.potionNames().split(",");
	}

	@Override
	protected void shutDown() throws Exception {
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals("praypotdrinker"))
			return;

		potions = config.potionNames().split(",");
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		this.executor.submit(() -> {
			try {
				//7 + 25%
				int currentPrayerPoints = client.getBoostedSkillLevel(Skill.PRAYER);
				int maxPrayerPoints = client.getRealSkillLevel(Skill.PRAYER);
				int boostAmount = 7 + (int) Math.floor(maxPrayerPoints * .25);
				
				if (currentPrayerPoints + boostAmount > maxPrayerPoints) {
					return;
				}

				Widget inventory = client.getWidget(WidgetInfo.INVENTORY);

				if (inventory == null)
					return;

				for (WidgetItem item : inventory.getWidgetItems()) {
					final String name = this.itemManager.getItemDefinition(item.getId()).getName();
					if (Arrays.asList(potions).contains(name)) {
						entry = getConsumableEntry(name, item.getId(), item.getIndex());
						InputHandler.click(client);
						Thread.sleep(50);
						return;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (entry != null) {
			event.setMenuEntry(entry);
		}

		entry = null;
	}

	private MenuEntry getConsumableEntry(String itemName, int itemId, int itemIndex) {
		return new MenuEntry("Drink", "<col=ff9040>" + itemName, itemId, MenuOpcode.ITEM_FIRST_OPTION.getId(), itemIndex, 9764864, false);
	}
}
