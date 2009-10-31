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
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class TableTestPage extends SystemPage
{
	protected TableTestPage()
	{
		register( "tests.table" );
	}
	
	
	public String getTitle()
	{
		return "Table test";
	}

	protected String getDescription()
	{
		return "The table element arranges is children in a table layout. Holes can be present. Child elements may span multiple columns and rows.";
	}

	private static TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
	private static TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 18 ), Color.BLACK );
	private static SolidBorder b = new SolidBorder( 2.0, 3.0, new Color( 0.0f, 0.3f, 0.7f ), new Color( 1.0f, 0.99f, 0.9f ) );
	private static SolidBorder outlineBorder = new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null );

	protected DPText text12(String s)
	{
		return new DPText( getContext(), t12, s );
	}
	
	protected DPText text18(String s)
	{
		return new DPText( getContext(), t18, s );
	}
	
	protected DPWidget wrapInOutline(DPWidget w)
	{
		DPBorder border = new DPBorder( getContext(), outlineBorder );
		border.setChild( w );
		return border;
	}
	
	protected DPWidget wrapInBorder(DPWidget w)
	{
		DPBorder border = new DPBorder( getContext(), b );
		border.setChild( w );
		return border;
	}

	protected DPTable makeTable0()
	{
		TableStyleSheet tbls0 = new TableStyleSheet( 5.0, false, 5.0, false );
		DPTable table = new DPTable( getContext(), tbls0 );
		for (int row = 0; row < 6; row++)
		{
			for (int col = 0; col < 6; col++)
			{
				table.put( col, row, wrapInOutline( text12( "<" + col + "_" + row + ">" ) ) );
			}
		}
		return table;
	}
	
	protected DPTable makeTable1()
	{
		DPTable table = makeTable0();
		table.put( 2, 2, null );
		table.put( 3, 2, null );
		table.put( 4, 2, null );
		table.put( 2, 2, 3, 1, wrapInOutline( text12( "<<wide>>" ) ).alignHCentre() );
		return table;
	}
	
	protected DPTable makeTable2()
	{
		DPTable table = makeTable0();
		table.put( 2, 2, null );
		table.put( 2, 3, null );
		table.put( 2, 4, null );
		table.put( 2, 2, 1, 3, wrapInOutline( text18( "T" ) ).alignHCentre() );
		return table;
	}
	
	protected DPTable makeTable3()
	{
		DPTable table = makeTable0();
		table.put( 2, 2, null );
		table.put( 2, 3, null );
		table.put( 2, 4, null );
		table.put( 3, 2, null );
		table.put( 3, 3, null );
		table.put( 3, 4, null );
		table.put( 4, 2, null );
		table.put( 4, 3, null );
		table.put( 4, 4, null );
		table.put( 2, 2, 3, 3, wrapInOutline( text18( "T" ) ).alignHCentre() );
		return table;
	}
	
	
	
	protected DPWidget createContents()
	{
		VBoxStyleSheet boxS = new VBoxStyleSheet( VTypesetting.NONE, 20.0 );
		DPVBox box = new DPVBox( getContext(), boxS );
		box.append( wrapInBorder( makeTable0() ) );
		box.append( wrapInBorder( makeTable1() ) );
		box.append( wrapInBorder( makeTable2() ) );
		box.append( wrapInBorder( makeTable3() ) );
		
		return box;
	}
}
