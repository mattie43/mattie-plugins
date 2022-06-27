package com.example.nseersvillage;

import com.google.common.primitives.Ints;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

import static net.runelite.api.AnimationID.IDLE;

@Extension
@PluginDescriptor(
	name = "nSeersVillage",
	enabledByDefault = false,
	description = "Seers Village auto runner.",
	tags = {"agility", "bot", "runner"}
)
@Slf4j
public class nSeersVillage extends Plugin
{
	// Injects our config
	@Inject
	private nSeersVillageConfig config;

	// Inject ext tools
	@Inject
	private ExtTools extTools;

	//Inject Client to use
	@Inject
	private Client client;

	// Provides our config
	@Provides
	nSeersVillageConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(nSeersVillageConfig.class);
	}

	private Instant lastAnimating;
	private int lastAnimation = IDLE;
	private Instant lastInteracting;
	private Actor lastInteract;
	private Instant lastMoving;
	private WorldPoint lastPosition;
	private Tile mogTile = null;
	private boolean notifyPosition = false;
	private boolean notifyIdleLogout = true;
	private int lastCombatCountdown = 0;
	private boolean pluginStart = true;
	private boolean manualTeleport = false;
	private boolean noDiary = false;

	@Override
	protected void startUp()
	{
		// runs on plugin startup
		log.info("Plugin started");

		// example how to use config items
		if (config.manualTeleport())
			manualTeleport = true;
		if (config.noDiary())
			noDiary = true;
	}

	@Override
	protected void shutDown()
	{
		// runs on plugin shutdown
		log.info("Plugin stopped");
	}

	private boolean checkMovementIdle(Duration waitDuration, Player local)
	{
		if (lastPosition == null)
		{
			lastPosition = local.getWorldLocation();
			return false;
		}

		WorldPoint position = local.getWorldLocation();

		if (lastPosition.equals(position))
		{
			if (notifyPosition
					&& local.getAnimation() == IDLE
					&& Instant.now().compareTo(lastMoving.plus(waitDuration)) >= 0)
			{
				notifyPosition = false;
				// Return true only if we weren't just breaking out of an animation
				return lastAnimation == IDLE;
			}
		}
		else
		{
			notifyPosition = true;
			lastPosition = position;
			lastMoving = Instant.now();
		}

		return false;
	}

	private boolean checkInteractionIdle(Duration waitDuration, Player local)
	{
		if (lastInteract == null)
		{
			return false;
		}

		final Actor interact = local.getInteracting();

		if (interact == null)
		{
			if (lastInteracting != null
					&& Instant.now().compareTo(lastInteracting.plus(waitDuration)) >= 0
					&& lastCombatCountdown == 0)
			{
				lastInteract = null;
				lastInteracting = null;

				// prevent animation notifications from firing too
				lastAnimation = IDLE;
				lastAnimating = null;

				return true;
			}
		}
		else
		{
			lastInteracting = Instant.now();
		}

		return false;
	}

	private void pluginStartup()
	{
		assert client.isClientThread();

		(new Thread(() ->
		{
			try
			{
				client.setCameraYawTarget(0);
				extTools.arrowKeyEvent(401, "UP");
				Thread.sleep(1000);
				extTools.arrowKeyEvent(402, "UP");
				extTools.click(client.getWidget(WidgetInfo.FIXED_VIEWPORT_OPTIONS_TAB).getBounds());
				Thread.sleep(500);
				extTools.click(client.getWidget(7602268).getBounds());
				Thread.sleep(500);
				if(noDiary)
					lastPosition = new WorldPoint(0,0,0);
				else
				{
					extTools.click(client.getWidget(WidgetInfo.FIXED_VIEWPORT_MAGIC_TAB).getBounds());
					Thread.sleep(500);
					extTools.click(client.getWidget(WidgetInfo.SPELL_CAMELOT_TELEPORT).getBounds());
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		})).start();
	}

	private boolean mogCheck()
	{
		final Player local = client.getLocalPlayer();

		if(mogTile == null)
			return false;

		int distance = local.getLocalLocation().distanceTo(mogTile.getLocalLocation());

		if(distance < 1200){
			extTools.clickTile(mogTile.getLocalLocation(), mogTile.getPlane());
			mogTile = null;
			return true;
		}
		return false;
	}

	private void walkToStart()
	{
		final Player local = client.getLocalPlayer();
		int[] randomX = {0,128,256};
		int[] randomY = {768,896,1024};

		WorldPoint currentLocation = local.getWorldLocation();

		GameObject tree1 = extTools.findNearestGameObject(10832);
		LocalPoint tree1Local = tree1.getLocalLocation();
		LocalPoint clickLocal1 = new LocalPoint(
				tree1Local.getX()-randomX[extTools.getRandomIntBetweenRange(0,2)],
				tree1Local.getY()+randomY[extTools.getRandomIntBetweenRange(0,2)]);

		if(currentLocation.getX() == 2704) {
			extTools.clickTile(clickLocal1, 0);
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		final TileItem item = itemSpawned.getItem();
		final Tile tile = itemSpawned.getTile();

		if (item.getId() == ItemID.MARK_OF_GRACE)
			mogTile = tile;
	}

	@Subscribe
	private void onGameTick(GameTick gameTick)
	{
		final Player local = client.getLocalPlayer();
		final Duration waitDuration = Duration.ofMillis(extTools.getRandomIntBetweenRange(1250, 2000));

		int[] agil_obj_ids = {14927,14928,14932,14929,14930,14931}; // agility obj ids

		if(pluginStart){
			pluginStart=false;
			pluginStartup();
		}

		if (checkMovementIdle(waitDuration, local) || checkInteractionIdle(waitDuration, local)) {
			if(mogCheck())
				return;
			if(!extTools.findAndClickNearestObj(agil_obj_ids) && !manualTeleport) {
				if (noDiary)
					walkToStart();
				else
					extTools.clickWidget(client.getWidget(WidgetInfo.SPELL_CAMELOT_TELEPORT));
			}
		}
	}
}