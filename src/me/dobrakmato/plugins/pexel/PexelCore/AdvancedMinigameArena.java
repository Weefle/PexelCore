package me.dobrakmato.plugins.pexel.PexelCore;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Arena that has built-in support for pre-game lobby and stuff... Also implements {@link Listener} and calls
 * {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, org.bukkit.plugin.Plugin)} in constructor.
 * 
 * @author Mato Kormuth
 * 
 */
public class AdvancedMinigameArena extends MinigameArena implements Listener
{
	/**
	 * Amount of players, that is required to start the countdown.
	 */
	@ArenaOption(name = "minimalPlayers")
	public int		minimalPlayers				= 0;
	/**
	 * Lenght of countdown in seconds.
	 */
	@ArenaOption(name = "countdownLenght")
	public int		countdownLenght				= 10;
	/**
	 * Location of this arena lobby.
	 */
	@ArenaOption(name = "lobbyLocation")
	public Location	lobbyLocation;
	/**
	 * Location of this arena game spawn.
	 */
	@ArenaOption(name = "gameSpawn")
	public Location	gameSpawn;
	/**
	 * Specifies if the countdown should be canceled, if a player leaves arena and there is not enough players to start
	 * game, but the countdown is alredy running.
	 */
	@ArenaOption(name = "countdownCanCancel")
	public boolean	countdownCanCancel			= true;
	/**
	 * Specifies if the players should be teleported to gameSpawn and lobbyLocation automaticaly.
	 */
	public boolean	shouldTeleportPlayers		= true;
	/**
	 * Specifies if players can respawn in this arena, or not.
	 */
	public boolean	playersCanRespawn			= true;
	/**
	 * Specifies if players can join the game after the game was started.
	 */
	public boolean	playersCanJoinAfterStart	= false;
	/**
	 * Spcifies if the boss bar should be used for displaying time to start.
	 */
	@ArenaOption(name = "useBossBar")
	public boolean	useBossBar					= true;
	/**
	 * Time left to game start.
	 */
	@ArenaOption(name = "countdownTimeLeft")
	public int		countdownTimeLeft			= 30;
	/**
	 * Specifies, if the arena should call <code>reset()</code> function automaticaly when game ends.
	 */
	public boolean	autoReset					= true;
	/**
	 * Specifies, if inventory actions are enabled in this arena.
	 */
	public boolean	inventoryDisabled			= true;
	/**
	 * Chat format for countdown message.
	 */
	public String	countdownFormat				= "%timeleft% seconds to game start!";
	
	public int		countdownTaskId				= 0;
	/**
	 * Identifies if the game has started.
	 */
	private boolean	gameStarted					= false;
	
	/**
	 * @param minigame
	 * @param arenaName
	 * @param region
	 * @param maxPlayers
	 */
	public AdvancedMinigameArena(final Minigame minigame,
			final String arenaName, final Region region, final int maxPlayers,
			final int minPlayers, final Location lobbyLocation,
			final Location gameSpawn)
	{
		super(minigame, arenaName, region, maxPlayers);
		
		this.minimalPlayers = minPlayers;
		this.lobbyLocation = lobbyLocation;
		this.gameSpawn = gameSpawn;
		
		Bukkit.getPluginManager().registerEvents(this, Pexel.getCore());
	}
	
	/**
	 * Clear player's inventory, removes effects and set game mode.
	 * 
	 * @param player
	 */
	public void clearPlayer(final Player player)
	{
		if (player == null)
			System.out.println("WTF PLAYER IS NULL!?");
		
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
		
		/*
		 * for (PotionEffectType effect : PotionEffectType.values()) try { if (player.hasPotionEffect(effect))
		 * player.removePotionEffect(effect); } catch (Exception ex) { Log.warn("PlayerClearing_Error: " +
		 * ex.getMessage()); }
		 */
	}
	
	/**
	 * Teleports player to location by actual game state.
	 * 
	 * @param player
	 */
	public void teleportPlayer(final Player player)
	{
		if (this.state == GameState.WAITING_PLAYERS
				|| this.state == GameState.WAITING_EMPTY)
		{
			player.teleport(this.lobbyLocation);
		}
		else if (this.state == GameState.PLAYING_CANJOIN
				|| this.state == GameState.PLAYING_CANTJOIN)
		{
			player.teleport(this.gameSpawn);
		}
		else
		{
			throw new RuntimeException(
					"Arena is not in state, that supports teleporting players!");
		}
	}
	
	/**
	 * Teleports all players to specified location.
	 * 
	 * @param location
	 */
	public void teleportPlayers(final Location location)
	{
		for (Player p : this.activePlayers)
			p.teleport(location);
	}
	
	/**
	 * Returns boolean if is this arena prepeared for playing.
	 * 
	 * @return
	 */
	public boolean isPrepeared()
	{
		return this.gameSpawn != null && this.lobbyLocation != null
				&& this.minimalPlayers != 0;
	}
	
	/**
	 * Tries to start countdown.
	 */
	private void tryStartCountdown()
	{
		if (this.activePlayers.size() >= this.minimalPlayers)
			this.startCountdown();
		else
			this.onNotEnoughPlayers();
	}
	
	/**
	 * Tries to stop countdown.
	 */
	private void tryStopCountdown()
	{
		//Check if we can stop, once the countdown started.
		if (this.countdownCanCancel)
		{
			Bukkit.getScheduler().cancelTask(this.countdownTaskId);
			this.onCountdownCancelled();
		}
	}
	
	/**
	 * Returns world that this arena exists. (Got from world in this.gameSpawn).
	 * 
	 * @return world of this arena
	 */
	public World getWorld()
	{
		return this.gameSpawn.getWorld();
	}
	
	private void startCountdown()
	{
		if (this.countdownTaskId == 0)
		{
			//Reset countdown time.
			this.countdownTimeLeft = this.countdownLenght;
			//Start countdown.
			this.countdownTaskId = Pexel.schedule(new Runnable() {
				@Override
				public void run()
				{
					AdvancedMinigameArena.this.countdownTick();
				}
			}, 0L, 20L);
			
			this.onCountdownStart();
		}
	}
	
	private void onCountdownStop()
	{
		Bukkit.getScheduler().cancelTask(this.countdownTaskId);
	}
	
	/**
	 * Called once per second while countdown is running.
	 */
	private void countdownTick()
	{
		//Send a chat message.
		if (this.countdownTimeLeft < 10 || (this.countdownTimeLeft % 10) == 0)
			this.chatAll(ChatManager.minigame(
					this.minigame,
					this.countdownFormat.replace("%timeleft%",
							Integer.toString(this.countdownTimeLeft))));
		//If we are using boss bar.
		if (this.useBossBar)
			this.setBossBarAll(
					this.countdownFormat.replace("%timeleft%",
							Integer.toString(this.countdownTimeLeft)),
					this.countdownTimeLeft / this.countdownLenght * 100);
		
		//If we reached zero.
		if (this.countdownTimeLeft <= 0)
		{
			//Remove bossbar
			if (this.useBossBar)
				for (Player p : this.activePlayers)
					BarAPI.removeBar(p);
			//Stop the countdown task.
			this.onCountdownStop();
			//Start game.
			this.onGameStart();
			this.gameStarted = true;
		}
		//Decrement the time.
		this.countdownTimeLeft -= 1;
	}
	
	/**
	 * Set's boss bar content for all players.
	 * 
	 * @param replace
	 *            message (max 40 char.)
	 */
	public void setBossBarAll(final String message)
	{
		for (Player p : this.activePlayers)
			BarAPI.setMessage(p, message);
	}
	
	/**
	 * Set's boss bar content for all players.
	 * 
	 * @param replace
	 *            message (max 40 char.)
	 */
	public void setBossBarAll(final String message, final float percent)
	{
		for (Player p : this.activePlayers)
			BarAPI.setMessage(p, message, percent);
	}
	
	/**
	 * Reseta arena basic things. <b>Calls {@link AdvancedMinigameArena#onReset()} at the end of this function!</b></br>
	 * If you want to extend reset function, override onReset() function.
	 */
	public final void reset()
	{
		this.state = GameState.RESETING;
		Log.info("Resetting arena " + this.getName() + "...");
		this.gameStarted = false;
		this.countdownTaskId = 0;
		//Not many things happeing here. Leaving method for future.
		this.activePlayers.clear();
		//Invoke callback.
		this.onReset();
	}
	
	/**
	 * Called right after the arena resets it's basic things, after {@link AdvancedMinigameArena#reset()} was called.
	 * <b>Don't forget to change arena's state to {@link GameState#WAITING_EMPTY} after reset.</b>
	 */
	public void onReset()
	{
		
	}
	
	/**
	 * Called when countdown starts.
	 */
	public void onCountdownStart()
	{
		
	}
	
	/**
	 * Called when countdown stops.
	 */
	public void onCountdownCancelled()
	{
		
	}
	
	/**
	 * Called each time, a player joins arena and there is not enough players for countdown start.
	 */
	public void onNotEnoughPlayers()
	{
		
	}
	
	/**
	 * Called when game should start its logic. Called when lobby countdown has reached zero and there is enough
	 * players.
	 */
	public void onGameStart()
	{
		
	}
	
	/**
	 * Called when last player lefts the arena. Should call {@link AdvancedMinigameArena#reset()} function if
	 * <code>autoReset</code> is set to <b>false</b> (false by default).
	 */
	public void onGameEnd()
	{
		
	}
	
	/**
	 * Returns whether collection of activePlayers containts specified player.
	 * 
	 * @param player
	 *            player to check
	 * @return true or false
	 */
	public boolean isInArena(final Player player)
	{
		return this.activePlayers.contains(player);
	}
	
	/**
	 * Called when players join the arena. Also checks if there are enough players, if so, calls
	 * {@link AdvancedMinigameArena#onGameStart()}. If not, calls {@link AdvancedMinigameArena#onNotEnoughPlayers()}.
	 */
	@Override
	public void onPlayerJoin(final Player player)
	{
		if (!this.activePlayers.contains(player))
		{
			super.onPlayerJoin(player);
			
			this.tryStartCountdown();
			
			this.updateGameState();
			
			this.clearPlayer(player);
			
			this.chatAll(ChatManager.minigame(this.getMinigame(),
					ChatColor.GOLD + "Player '" + player.getName()
							+ "' joined arena! (" + this.playerCount() + "/"
							+ this.minimalPlayers + " - " + this.slots + ")"));
			
			player.teleport(this.lobbyLocation);
		}
		else
		{
			player.sendMessage(ChatManager.error("Alredy playing!"));
		}
	}
	
	/**
	 * Called when player left the arena. If is arena in LOBBY/WAITING_PLAYERS state, and flag
	 * {@link AdvancedMinigameArena#countdownCanCancel} is set to <b>true</b>, stops the countdown.
	 */
	@Override
	public void onPlayerLeft(final Player player)
	{
		super.onPlayerLeft(player);
		
		this.chatAll(ChatManager.minigame(this.getMinigame(), "Player '"
				+ player.getName() + "' has left arena!"));
		
		this.tryStopCountdown();
		
		this.checkForEnd();
		
		//BarApi fix
		if (BarAPI.hasBar(player))
			BarAPI.removeBar(player);
		
		this.updateGameState();
	}
	
	@EventHandler
	protected void onPlayerQuit(final PlayerQuitEvent event)
	{
		this.onPlayerLeft(event.getPlayer());
	}
	
	/**
	 * Updates game state.
	 */
	private void updateGameState()
	{
		if (!this.gameStarted)
		{
			if (this.playerCount() == 0)
				this.state = GameState.WAITING_EMPTY;
			else
				this.state = GameState.WAITING_PLAYERS;
		}
	}
	
	/**
	 * Checks if there are no players in arena, and if arena is in PLAYING state. If so, the
	 * {@link AdvancedMinigameArena#onGameEnd()}
	 */
	private void checkForEnd()
	{
		if (this.activePlayers.size() == 0 && this.state.isPlaying())
		{
			this.onGameEnd();
			if (this.autoReset)
				this.reset();
		}
	}
	
	@EventHandler
	public void ___onPlayerRespawn(final PlayerRespawnEvent event)
	{
		if (this.playersCanRespawn)
		{
			//nothing.
		}
		else
		{
			//Kick from arena
			this.onPlayerLeft(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerInventoryClick(final InventoryClickEvent event)
	{
		if (event.getWhoClicked() instanceof Player)
			if (this.activePlayers.contains(event.getWhoClicked()))
				if (this.inventoryDisabled)
					event.setCancelled(true);
	}
	
	public int getMinimalPlayers()
	{
		return this.minimalPlayers;
	}
	
	public void setMinimalPlayers(final int minimalPlayers)
	{
		this.minimalPlayers = minimalPlayers;
	}
	
	public int getCountdownLenght()
	{
		return this.countdownLenght;
	}
	
	public void setCountdownLenght(final int countdownLenght)
	{
		this.countdownLenght = countdownLenght;
	}
	
	public Location getLobbyLocation()
	{
		return this.lobbyLocation;
	}
	
	public void setLobbyLocation(final Location lobbyLocation)
	{
		this.lobbyLocation = lobbyLocation;
	}
	
	public Location getGameSpawn()
	{
		return this.gameSpawn;
	}
	
	public void setGameSpawn(final Location gameSpawn)
	{
		this.gameSpawn = gameSpawn;
	}
	
	public boolean countdownCanCancel()
	{
		return this.countdownCanCancel;
	}
	
	public void setCountdownCanCancel(final boolean countdownCanCancel)
	{
		this.countdownCanCancel = countdownCanCancel;
	}
	
	public boolean shouldTeleportPlayers()
	{
		return this.shouldTeleportPlayers;
	}
	
	public void setShouldTeleportPlayers(final boolean shouldTeleportPlayers)
	{
		this.shouldTeleportPlayers = shouldTeleportPlayers;
	}
	
	public boolean playersCanRespawn()
	{
		return this.playersCanRespawn;
	}
	
	public void setPlayersCanRespawn(final boolean playersCanRespawn)
	{
		this.playersCanRespawn = playersCanRespawn;
	}
	
	public int getCountdownTimeLeft()
	{
		return this.countdownTimeLeft;
	}
	
	public String getCountdownFormat()
	{
		return this.countdownFormat;
	}
	
	public void setCountdownFormat(final String countdownFormat)
	{
		this.countdownFormat = countdownFormat;
	}
	
	public boolean isPlayersCanJoinAfterStart()
	{
		return this.playersCanJoinAfterStart;
	}
	
	public void setPlayersCanJoinAfterStart(
			final boolean playersCanJoinAfterStart)
	{
		this.playersCanJoinAfterStart = playersCanJoinAfterStart;
	}
}
