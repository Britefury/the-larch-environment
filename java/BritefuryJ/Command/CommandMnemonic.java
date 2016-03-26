//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import java.awt.Color;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleSheet;


public abstract class CommandMnemonic
{
	private static final Pattern annotationPattern = Pattern.compile( "[" + Pattern.quote( "&$" ) + "]." );
	
	private String charSequence;
	private String name;
	private int charIndices[];
	
	protected Pres completePres;
	
	
	public CommandMnemonic(String charSequence, String name)
	{
		this.charSequence = charSequence;
		this.name = name;
		charIndices = computeIndices( charSequence, name );
		computeCompletePres();
	}
	
	public CommandMnemonic(String annotatedName)
	{
		name = "";
		charSequence = "";
		Matcher m = annotationPattern.matcher( annotatedName );
		int pos = 0;
		ArrayList<Integer> indices = new ArrayList<Integer>();
		while ( m.find() )
		{
			int start = m.start();
			int end = m.end();
			boolean preserveCase = annotatedName.charAt( start ) == '$';

			name += annotatedName.substring( pos, start );
			String ch = annotatedName.substring( end - 1, end );
			
			indices.add( name.length() );

			name += ch;
			if ( preserveCase )
			{
				charSequence += ch;
			}
			else
			{
				charSequence += ch.toLowerCase();
			}
			
			pos = end;
		}
		
		name += annotatedName.substring( pos );
		
		charIndices = new int[indices.size()];
		int i = 0;
		for (int x: indices)
		{
			charIndices[i++] = x;
		}
		computeCompletePres();
	}
	
	
	public String getCharSequence()
	{
		return charSequence;
	}

	public String getName()
	{
		return name;
	}
	
	public int[] getCharIndices()
	{
		return charIndices;
	}
	
	
	private void computeCompletePres()
	{
		if ( charIndices != null )
		{
			int j = 0;
			Pres xs[] = new Pres[name.length()+1];
			for (int i = 0; i < name.length(); i++)
			{
				String ch = name.substring( i, i + 1 );
				Pres chPres;
				
				if ( j < charIndices.length  &&  i == charIndices[j] )
				{
					chPres = cmdCharStyle.applyTo( new Text( ch, charSequence.substring( j, j + 1 ) ) );
					j++;
				}
				else
				{
					chPres = cmdNameStyle.applyTo( new Label( ch ) );
				}
				xs[i] = chPres;
			}
			xs[name.length()] = new Text( "" );
			completePres = new Row( xs );
		}
		else
		{
			Pres namePres = cmdNameStyle.applyTo( new Label( name ) );
			completePres = new Row( new Object[] { namePres, new Label( " " ), cmdCharStyle.applyTo( new Text( charSequence ) ) } );
		}
	}
	
	
	
	private static int[] computeIndices(String charSequence, String name)
	{
		int currentIndex = 0;
		int indices[] = new int[charSequence.length()];
		int j = 0;
		for (int i = 0; i < charSequence.length(); i++)
		{
			char c = charSequence.charAt( i );
			
			int index = name.indexOf( c, currentIndex );
			if ( index == -1 )
			{
				indices = null;
				break;
			}
			else
			{
				indices[j++] = index;
				currentIndex = index + 1;
			}
		}
		
		return indices;
	}
	


	private static final StyleSheet cmdNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.5f, 1.0f, 0.5f ) ) );
	private static final StyleSheet cmdCharStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.75f, 1.0f, 0.75f ) ), Primitive.fontBold.as( true ) );
}
