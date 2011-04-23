//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.Color;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleSheet;


public class CommandName implements Presentable
{
	private String charSequence;
	private String name;
	private int charIndices[];
	
	private Pres completePres;
	
	
	public CommandName(String charSequence, String name)
	{
		this.charSequence = charSequence;
		this.name = name;
		charIndices = computeIndices( charSequence, name );
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
			int j =0;
			Pres xs[] = new Pres[name.length()+1];
			for (int i = 0; i < name.length(); i++)
			{
				String ch = name.substring( i, i + 1 );
				Pres chPres;
				
				if ( j < charIndices.length  &&  i == charIndices[j] )
				{
					j++;
					chPres = cmdCharStyle.applyTo( new Text( ch ) );
				}
				else
				{
					chPres = cmdNameStyle.applyTo( new Label( ch ) );
				}
				xs[i] = chPres;
			}
			xs[name.length()] = new Text( "" );
			completePres = cmdBorderStyle.applyTo( new Border( new Row( xs ) ) );
		}
		else
		{
			Pres namePres = cmdNameStyle.applyTo( new Label( name ) );
			completePres = cmdBorderStyle.applyTo( new Border( new Row( new Object[] { namePres, new Label( " " ), cmdCharStyle.applyTo( new Text( charSequence ) ) } ) ) );
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


	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return completePres;
	}



	private static final StyleSheet cmdBorderStyle = StyleSheet.instance.withAttr( Primitive.border, Command.cmdBorder( new Color( 0.0f, 0.7f, 0.0f ), new Color( 0.85f, 0.95f, 0.85f ) ) );
	private static final StyleSheet cmdNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static final StyleSheet cmdCharStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.0f ) ).withAttr( Primitive.fontBold, true );
}
