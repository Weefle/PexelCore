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
package me.dobrakmato.plugins.pexel.PexelCore;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that turns off all parts when needed. Idk what is this...
 * 
 * @author Mato Kormuth
 * 
 */
public class UpdatedParts
{
	private static List<UpdatedPart>	parts	= new ArrayList<UpdatedPart>();
	
	public static void shutdown()
	{
		for (UpdatedPart part : UpdatedParts.parts)
			part.updateStop();
		UpdatedParts.parts.clear();
	}
	
	public static void registerPart(final UpdatedPart part)
	{
		Log.info("[UpdatedParts] Registering: "
				+ part.getClass().getSimpleName());
		UpdatedParts.parts.add(part);
	}
}
