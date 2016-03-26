//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashMap;

public class FontMap
{
	private static ArrayList<String> allFontNames = null;
	private static HashMap<String, String> fontNameToAvailableFontName = null;
	
	

	public static Iterable<String> getAvailableFontNames()
	{
		return allFontNames;
	}
	
	
	public static String getAvailableFontName(String name)
	{
		refreshFontMap();

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
		refreshFontMap();

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



	private static void refreshFontMap()
	{
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font[] allFonts = env.getAllFonts();
		int numFontNames = allFonts.length + 5;

		if (allFontNames == null  ||  allFontNames.size() != numFontNames)
		{
			allFontNames = new ArrayList<String>();
			allFontNames.ensureCapacity( numFontNames );
			allFontNames.add( Font.DIALOG.toLowerCase() );
			allFontNames.add( Font.DIALOG_INPUT.toLowerCase() );
			allFontNames.add( Font.MONOSPACED.toLowerCase() );
			allFontNames.add( Font.SANS_SERIF.toLowerCase() );
			allFontNames.add( Font.SERIF.toLowerCase() );
			for (Font font: allFonts)
			{
				allFontNames.add( font.getName().toLowerCase() );
			}

			fontNameToAvailableFontName = new HashMap<String, String>();
		}
	}

}
