//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashMap;

public class FontMap
{
	private static ArrayList<String> allFontNames = new ArrayList<String>();
	private static HashMap<String, String> fontNameToAvailableFontName = new HashMap<String, String>();
	
	
	static
	{
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font[] allFonts = env.getAllFonts();
		
		allFontNames.ensureCapacity( allFonts.length + 5 );
		allFontNames.add( Font.DIALOG.toLowerCase() );
		allFontNames.add( Font.DIALOG_INPUT.toLowerCase() );
		allFontNames.add( Font.MONOSPACED.toLowerCase() );
		allFontNames.add( Font.SANS_SERIF.toLowerCase() );
		allFontNames.add( Font.SERIF.toLowerCase() );
		for (Font font: allFonts)
		{
			allFontNames.add( font.getName().toLowerCase() );
		}
	}
	
	
	public static Iterable<String> getAvailableFontNames()
	{
		return allFontNames;
	}
	
	
	public static String getAvailableFontName(String name)
	{
		String availableName = fontNameToAvailableFontName.get( name );
		
		if ( availableName == null )
		{
			availableName = findAvailableFont( name );
			fontNameToAvailableFontName.put( name, availableName );
		}
		
		return availableName;
	}


	private static String findAvailableFont(String name)
	{
		String[] choices = name.split( ";" );
		
		for (String choice: choices)
		{
			choice = choice.trim();
			
			if ( !choice.equals( "" ) )
			{
				String choiceLower = choice.toLowerCase();
				if ( allFontNames.contains( choiceLower ) )
				{
					return choice;
				}
			}
		}
		
		return Font.SANS_SERIF;
	}
}
