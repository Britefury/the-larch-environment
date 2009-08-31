//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPGridRow;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class GridTestPage extends SystemPage
{
	protected GridTestPage()
	{
		register( "tests.grid" );
	}
	
	
	protected String getTitle()
	{
		return "Grid test";
	}

	protected String getDescription()
	{
		return "The grid element arranges is children in a grid.";
	}

	private static TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
	private static TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 18 ), Color.BLACK );
	private static SolidBorder b = new SolidBorder( 2.0, 3.0, new Color( 0.0f, 0.3f, 0.7f ), new Color( 1.0f, 0.99f, 0.9f ) );
	private static SolidBorder outlineBorder = new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null );

	protected static DPText text12(String s)
	{
		return new DPText( t12, s );
	}
	
	protected static DPText text18(String s)
	{
		return new DPText( t18, s );
	}
	
	protected static DPWidget wrapInOutline(DPWidget w)
	{
		DPBorder border = new DPBorder( outlineBorder );
		border.setChild( w );
		return border;
	}
	
	protected static DPWidget wrapInBorder(DPWidget w)
	{
		DPBorder border = new DPBorder( b );
		border.setChild( w );
		return border;
	}

	protected static DPGridRow makeGridRow(int row)
	{
		DPGridRow table = new DPGridRow();
		for (int col = 0; col < 6; col++)
		{
			table.append( wrapInOutline( text12( "<" + col + "_" + row + ">" ) ) );
		}
		return table;
	}
	
	protected static DPRGrid makeGrid()
	{
		TableStyleSheet tbls0 = new TableStyleSheet( 5.0, false, 5.0, false );
		DPRGrid grid = new DPRGrid( tbls0 );
		for (int row = 0; row < 6; row++)
		{
			grid.append( makeGridRow( row ) );
		}
		return grid;
	}
	
	protected static DPRGrid makeGrid1()
	{
		DPRGrid grid = makeGrid();
		DPGridRow row2 = (DPGridRow)grid.get( 2 );
		row2.remove( row2.get( 5 ) );
		row2.remove( row2.get( 4 ) );
		row2.remove( row2.get( 3 ) );
		return grid;
	}
	
	
	
	protected DPWidget createContents()
	{
		VBoxStyleSheet boxS = new VBoxStyleSheet( VTypesetting.NONE, 20.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( wrapInBorder( makeGridRow( 0 ) ) );
		box.append( wrapInBorder( makeGrid() ) );
		box.append( wrapInBorder( makeGrid1() ) );
		
		return box;
	}
}
