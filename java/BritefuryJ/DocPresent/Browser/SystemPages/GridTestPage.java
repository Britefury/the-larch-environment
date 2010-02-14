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
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
import BritefuryJ.DocPresent.StyleParams.VBoxStyleParams;

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

	private static TextStyleParams t12 = new TextStyleParams( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
	private static TextStyleParams t18 = new TextStyleParams( new Font( "Sans serif", Font.PLAIN, 18 ), Color.BLACK );
	private static VBoxStyleParams sectionStyle = new VBoxStyleParams( 5.0 );
	private static SolidBorder b = new SolidBorder( 2.0, 3.0, new Color( 0.0f, 0.3f, 0.7f ), new Color( 1.0f, 0.99f, 0.9f ) );
	private static SolidBorder outlineBorder = new SolidBorder( 1.0, 0.0, new Color( 0.0f, 0.3f, 0.7f ), null );

	protected DPText text12(String s)
	{
		return new DPText( t12, s );
	}
	
	protected DPText text18(String s)
	{
		return new DPText( t18, s );
	}
	
	protected DPWidget wrapInOutline(DPWidget w)
	{
		DPBorder border = new DPBorder( outlineBorder );
		border.setChild( w );
		return border;
	}
	
	protected DPWidget section(String description, DPWidget w)
	{
		DPWidget descriptionParagraph = createTextParagraph( description );
		DPBorder border = new DPBorder( b );
		border.setChild( w );
		DPVBox sectionBox = new DPVBox( sectionStyle );
		sectionBox.setChildren( new DPWidget[] { descriptionParagraph, border } );
		return sectionBox;
	}
	
	
	protected DPWidget span(int row, int startCol, int endCol)
	{
		DPSpan span = new DPSpan( );
		for (int col = startCol; col < endCol; col++)
		{
			span.append( wrapInOutline( text12( "<" + col + "_" + row + ">" ) ) );
		}
		return span;
	}
	

	protected DPGridRow makeGridRow(int row)
	{
		DPGridRow table = new DPGridRow( );
		for (int col = 0; col < 6; col++)
		{
			table.append( wrapInOutline( text12( "<" + col + "_" + row + ">" ) ) );
		}
		return table;
	}
	
	protected DPGridRow makeGridRowCollated(int row)
	{
		DPGridRow table = new DPGridRow( );
		for (int col = 0; col < 2; col++)
		{
			table.append( wrapInOutline( text12( "<" + col + "_" + row + ">" ) ) );
		}
		table.append( span( row, 2, 5 ) );
		table.append( wrapInOutline( text12( "<" + 5 + "_" + row + ">" ) ) );
		return table;
	}
	
	protected DPRGrid makeGrid()
	{
		TableStyleParams tbls0 = new TableStyleParams( 5.0, false, 5.0, false );
		DPRGrid grid = new DPRGrid( tbls0 );
		for (int row = 0; row < 6; row++)
		{
			grid.append( makeGridRow( row ) );
		}
		return grid;
	}
	
	protected DPRGrid makeGridwithShorenedRow()
	{
		DPRGrid grid = makeGrid();
		DPGridRow row2 = (DPGridRow)grid.get( 2 );
		row2.remove( row2.get( 5 ) );
		row2.remove( row2.get( 4 ) );
		row2.remove( row2.get( 3 ) );
		return grid;
	}
	
	protected DPRGrid makeGridWithCollatedRows()
	{
		TableStyleParams tbls0 = new TableStyleParams( 5.0, false, 5.0, false );
		DPRGrid grid = new DPRGrid( tbls0 );
		for (int row = 0; row < 6; row++)
		{
			grid.append( makeGridRowCollated( row ) );
		}
		return grid;
	}
	
	protected DPRGrid makeCollatedGridWithCollatedRows()
	{
		TableStyleParams tbls0 = new TableStyleParams( 5.0, false, 5.0, false );
		DPRGrid grid = new DPRGrid( tbls0 );
		for (int row = 0; row < 2; row++)
		{
			grid.append( makeGridRowCollated( row ) );
		}
		DPSpan span = new DPSpan( );
		for (int row = 2; row < 5; row++)
		{
			span.append( makeGridRowCollated( row ) );
		}
		grid.append( span );
		grid.append( makeGridRowCollated( 5 ) );
		return grid;
	}
	
	protected DPRGrid makeCollatedGridWithCollatedRowsAndNonRows()
	{
		TableStyleParams tbls0 = new TableStyleParams( 5.0, false, 5.0, false );
		DPRGrid grid = new DPRGrid( tbls0 );
		for (int row = 0; row < 2; row++)
		{
			grid.append( makeGridRowCollated( row ) );
		}
		DPSpan span = new DPSpan( );
		for (int row = 2; row < 5; row++)
		{
			span.append( makeGridRowCollated( row ) );
		}
		span.append( wrapInOutline( text12( "Non-row in a span" ) ) );
		grid.append( span );
		grid.append( makeGridRowCollated( 5 ) );
		grid.append( wrapInOutline( text12( "Non-row in the grid" ) ) );
		return grid;
	}
	
	
	
	protected DPWidget createContents()
	{
		VBoxStyleParams boxS = new VBoxStyleParams( 20.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( section( "Grid row", makeGridRow( 0 ) ) );
		box.append( section( "Grid", makeGrid() ) );
		box.append( section( "Grid with shortened row", makeGridwithShorenedRow() ) );
		box.append( section( "Grid with collated rows", makeGridWithCollatedRows() ) );
		box.append( section( "Collated grid with collated rows", makeCollatedGridWithCollatedRows() ) );
		box.append( section( "Collated grid with collated rows and non-rows", makeCollatedGridWithCollatedRowsAndNonRows() ) );
		
		return box;
	}
}
