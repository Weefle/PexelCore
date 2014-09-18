// @formatter:off
/*
 * Pexel Project - Minecraft minigame server platform. 
 * Copyright (C) 2014 Matej Kormuth <http://www.matejkormuth.eu>
 * 
 * This file is part of Pexel.
 * 
 * Pexel is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Pexel is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
// @formatter:on
package eu.matejkormuth.pexel.PexelCore;

import java.io.File;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import eu.matejkormuth.pexel.PexelCore.actions.CommandAction;
import eu.matejkormuth.pexel.PexelCore.actions.TeleportAction;
import eu.matejkormuth.pexel.PexelCore.areas.AreaFlag;
import eu.matejkormuth.pexel.PexelCore.areas.Lobby;
import eu.matejkormuth.pexel.PexelCore.arenas.ArenaMap;
import eu.matejkormuth.pexel.PexelCore.core.Region;
import eu.matejkormuth.pexel.PexelCore.core.StorageEngine;
import eu.matejkormuth.pexel.PexelCore.core.TeleportGate;
import eu.matejkormuth.pexel.PexelCore.util.SerializableLocation;
import eu.matejkormuth.pexel.PexelNetworking.Server;

/**
 * Class where is all hard coded stuff stored.
 * 
 * @author Mato Kormuth
 * 
 */
public class HardCoded {
    /**
     * Main method called from Plugin.onEnable()
     */
    public static final void main() {
        //Initialize color war mingiame
        //new ColorWarMinigame();
        //Initialize zabi pitkesa minigame
        //new ZabiPitkesaMinigame();
        //new TntTagMinigame();
        
        //new KingdomWarsMingame();
        
        // Test XML
        class SampleArenaMap extends ArenaMap {
            public SampleArenaMap() {
                this.name = "sampleMap";
                this.minigameName = "sampleMInigame";
                
                this.locations.put("loc1",
                        new SerializableLocation(Pexel.getHubLocation()));
                this.locations.put("testloc",
                        SerializableLocation.fromLocation(new Location(
                                Bukkit.getWorld("world"), 16, 32, 64)));
                
                this.options.put("option1", "yes");
                this.options.put("option2", "yes");
                this.options.put("option3", "no");
                
                this.regions.put("region_one", new Region(new Vector(5, 10, 88),
                        new Vector(50, 50, 70), Bukkit.getWorld("world")));
            }
        }
        
        try {
            new SampleArenaMap().save(new File(
                    Pexel.getCore().getDataFolder().getAbsolutePath() + "/sampleMap.xml"));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        //Initialize main gates
        StorageEngine.addGate("Lsurvival", new TeleportGate(new Region(new Vector(-7,
                50, 258), new Vector(-9, 54, 264), Bukkit.getWorld("world")),
                new TeleportAction(null, new Server(null, "survival", "survival"))));
        
        StorageEngine.addGate("Lstarving", new TeleportGate(new Region(new Vector(26,
                50, 266), new Vector(28, 55, 260), Bukkit.getWorld("world")),
                new TeleportAction(null, new Server(null, "starving", "starving"))));
        
        StorageEngine.addGate("Lminigame", new TeleportGate(new Region(new Vector(7, 50,
                280), new Vector(13, 55, 282), Bukkit.getWorld("world")),
                new TeleportAction(new Location(Bukkit.getWorld("world"), 1972.5, 147.5,
                        2492.5), Server.THIS_SERVER)));
        
        //Initialize gates
        StorageEngine.addGate("mg_colorwar", new TeleportGate(
                new Region(new Vector(1976, 147, 2532), new Vector(1972, 153, 2534),
                        Bukkit.getWorld("world")), new CommandAction("pcmd cwtest")));
        
        StorageEngine.addGate("mg_tnttag", new TeleportGate(new Region(new Vector(1962,
                147, 2532), new Vector(1967, 153, 2534), Bukkit.getWorld("world")),
                new CommandAction("pcmd tnttest")));
        
        //Initialize lobbies
        StorageEngine.addLobby(new Lobby("hub", new Region(new Vector(52, 107, 226),
                new Vector(-30, 1, 303), Bukkit.getWorld("world"))));
        
        StorageEngine.getLobby("hub").setThresholdY(10);
        
        //dobrakmato - block interactions
        StorageEngine.getLobby("hub").setPlayerFlag(AreaFlag.BLOCK_BREAK, true,
                UUID.fromString("966ad920-d45e-3fe5-8956-bf7a7a877ab4"));
        StorageEngine.getLobby("hub").setPlayerFlag(AreaFlag.BLOCK_PLACE, true,
                UUID.fromString("966ad920-d45e-3fe5-8956-bf7a7a877ab4"));
        
        StorageEngine.addLobby(new Lobby("minigamelobby", new Region(new Vector(2038, 0,
                2571), new Vector(1910, 255, 2437), Bukkit.getWorld("world"))));
        
        StorageEngine.getLobby("minigamelobby").setSpawn(
                new Location(Bukkit.getWorld("world"), 1972.5, 148, 2492.5));
        
        StorageEngine.getLobby("minigamelobby").setPlayerFlag(AreaFlag.BLOCK_BREAK,
                true, UUID.fromString("966ad920-d45e-3fe5-8956-bf7a7a877ab4"));
        StorageEngine.getLobby("minigamelobby").setPlayerFlag(AreaFlag.BLOCK_PLACE,
                true, UUID.fromString("966ad920-d45e-3fe5-8956-bf7a7a877ab4"));
    }
}