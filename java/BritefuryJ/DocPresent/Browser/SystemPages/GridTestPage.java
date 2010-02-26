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
import java.util.Arrays;

import BritefuryJ.DocPresent.DPGridRow;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class GridTestPage extends SystemPage
{
	protected GridTestPage()
	{
		register( "tests.grid" );
	}
	
	
	public String getTitle()
	{
		return "Grid test";
	}

	protected String getDescription()
	{
		return "The grid element arranges is children in a grid.";
	}

	private static final PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet t12 = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) );
	private static PrimitiveStyleSheet sectionStyle = styleSheet.withVBoxSpacing( 5.0 ).withBorder( new SolidBorder( 2.0, 3.0, new Color( 0.0f, 0.3f, 0.7f ), new Color( 0.95f, 0.975f, 1.0f  ) ) );
	private static PrimitiveStyleSheet outlineStyle = styleSheet.withBorder( new SolidBorder( 1.0, 0.0, new Color( 0.5f, 0.5f, 0.5f ), new Color( 0.9f, 0.9f, 0.9f ) ) );
	private static PrimitiveStyleSheet tableStyle = styleSheet.withTableColumnSpacing( 5.0 ).withTableRowSpacing( 5.0 );

	private DPWidget section(String description, DPWidget w)
	{
		return sectionStyle.vbox( Arrays.asList( new DPWidget[] { createTextParagraph( description ), sectionStyle.border( w ) } ) );
	}
	
	
	private DPWidget span(int row, int startCol, int endCol)
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		for (int col = startCol; col < endCol; col++)
		{
			children.add( outlineStyle.border( t12.text( "<" + col + "_" + row + ">" ) ) );
		}
		return styleSheet.span( children );
	}
	

	private DPGridRow makeGridRow(int row)
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		for (int col = 0; col < 6; col++)
		{
			children.add( outlineStyle.border( t12.text( "<" + col + "_" + row + ">" ) ) );
		}
		return styleSheet.gridRow( children );
	}
	
	private DPGridRow makeGridRowCollated(int row)
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		for (int col = 0; col < 2; col++)
		{
			children.add( outlineStyle.border( t12.text( "<" + col + "_" + row + ">" ) ) );
		}
		children.add( span( row, 2, 5 ) );
		children.add( outlineStyle.border( t12.text( "<" + 5 + "_" + row + ">" ) ) );
		return styleSheet.gridRow( children );
	}
	
	private DPRGrid makeGrid()
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		for (int row = 0; row < 6; row++)
		{
			children.add( makeGridRow( row ) );
		}
		return tableStyle.rgrid( children );
	}
	
	private DPRGrid makeGridwithShortenedRow()
	{
		DPRGrid grid = makeGrid();
		DPGridRow row2 = (DPGridRow)grid.get( 2 );
		row2.remove( row2.get( 5 ) );
		row2.remove( row2.get( 4 ) );
		row2.remove( row2.get( 3 ) );
		return grid;
	}
	
	private DPRGrid makeGridWithCollatedRows()
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		for (int row = 0; row < 6; row++)
		{
			children.add( makeGridRowCollated( row ) );
		}
		return tableStyle.rgrid( children );
	}
	
	private DPRGrid makeCollatedGridWithCollatedRows()
	{
		ArrayList<DPWidget> rows = new ArrayList<DPWidget>();
		for (int row = 0; row < 2; row++)
		{
			rows.add( makeGridRowCollated( row ) );
		}

		ArrayList<DPWidget> columns = new ArrayList<DPWidget>();
		for (int row = 2; row < 5; row++)
		{
			columns.add( makeGridRowCollated( row ) );
		}
		rows.add( styleSheet.span( columns ) );
		rows.add( makeGridRowCollated( 5 ) );
		return tableStyle.rgrid( rows );
	}
	
	private DPRGrid makeCollatedGridWithCollatedRowsAndNonRows()
	{
		ArrayList<DPWidget> rows = new ArrayList<DPWidget>();
		for (int row = 0; row < 2; row++)
		{
			rows.add( makeGridRowCollated( row ) );
		}
		
		ArrayList<DPWidget> columns = new ArrayList<DPWidget>();
		for (int row = 2; row < 5; row++)
		{
			columns.add( makeGridRowCollated( row ) );
		}
		columns.add( outlineStyle.border( t12.text( "Non-row in a span" ) ) );
		
		rows.add( styleSheet.span( columns ) );
		rows.add( makeGridRowCollated( 5 ) );
		rows.add( outlineStyle.border( t12.text( "Non-row in the grid" ) ) );
		return tableStyle.rgrid( rows );
	}
	
	
	
	protected DPWidget createContents()
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		children.add( section( "Grid row", makeGridRow( 0 ) ) );
		children.add( section( "Grid", makeGrid() ) );
		children.add( section( "Grid with shortened row", makeGridwithShortenedRow() ) );
		children.add( section( "Grid with collated rows", makeGridWithCollatedRows() ) );
		children.add( section( "Collated grid with collated rows", makeCollatedGridWithCollatedRows() ) );
		children.add( section( "Collated grid with collated rows and non-rows", makeCollatedGridWithCollatedRowsAndNonRows() ) );
		
		return styleSheet.withVBoxSpacing( 20.0 ).vbox( children );
	}
}
