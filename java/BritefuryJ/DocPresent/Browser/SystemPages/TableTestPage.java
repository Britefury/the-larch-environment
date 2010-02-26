//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

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

	private static final PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet t12 = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) );
	private static PrimitiveStyleSheet t18 = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 18 ) );
	private static PrimitiveStyleSheet sectionStyle = styleSheet.withVBoxSpacing( 5.0 ).withBorder( new SolidBorder( 2.0, 3.0, new Color( 0.0f, 0.3f, 0.7f ), new Color( 0.95f, 0.975f, 1.0f  ) ) );
	private static PrimitiveStyleSheet outlineStyle = styleSheet.withBorder( new SolidBorder( 1.0, 0.0, new Color( 0.5f, 0.5f, 0.5f ), new Color( 0.9f, 0.9f, 0.9f ) ) );
	private static PrimitiveStyleSheet tableStyle = styleSheet.withTableColumnSpacing( 5.0 ).withTableRowSpacing( 5.0 );

	private DPTable makeTable0()
	{
		DPTable table = tableStyle.table();
		for (int row = 0; row < 6; row++)
		{
			for (int col = 0; col < 6; col++)
			{
				table.put( col, row, outlineStyle.border( t12.text( ("<" + col + "_" + row + ">") ) ) );
			}
		}
		return table;
	}
	
	private DPTable makeTable1()
	{
		DPTable table = makeTable0();
		table.put( 2, 2, null );
		table.put( 3, 2, null );
		table.put( 4, 2, null );
		table.put( 2, 2, 3, 1, outlineStyle.border( t12.text( "<<wide>>" ) ).alignHCentre() );
		return table;
	}
	
	private DPTable makeTable2()
	{
		DPTable table = makeTable0();
		table.put( 2, 2, null );
		table.put( 2, 3, null );
		table.put( 2, 4, null );
		table.put( 2, 2, 1, 3, outlineStyle.border( t18.text( "T" ) ).alignHCentre() );
		return table;
	}
	
	private DPTable makeTable3()
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
		table.put( 2, 2, 3, 3, outlineStyle.border( t18.text( "T" ) ).alignHCentre() );
		return table;
	}
	
	
	
	protected DPWidget createContents()
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		children.add( sectionStyle.border( makeTable0() ) );
		children.add( sectionStyle.border( makeTable1() ) );
		children.add( sectionStyle.border( makeTable2() ) );
		children.add( sectionStyle.border( makeTable3() ) );
		
		return styleSheet.withVBoxSpacing( 20.0 ).vbox( children );
	}
}
