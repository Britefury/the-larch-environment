//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.GridRow;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.RGrid;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class GridTestPage extends SystemPage
{
	protected GridTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Grid test";
	}

	protected String getDescription()
	{
		return "The grid element arranges is children in a grid.";
	}

	private static final StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet t12 = styleSheet.withAttr( Primitive.fontSize, 12 );
	private static StyleSheet tableStyle = styleSheet.withAttr( Primitive.tableColumnSpacing, 5.0 ).withAttr( Primitive.tableRowSpacing, 5.0 )
		.withAttr( Primitive.tableBorder, new SolidBorder( 1.0, 0.0, Color.BLACK, null ) ).withAttr( Primitive.tableCellBoundaryPaint, new Color( 0.5f, 0.5f, 0.5f ) );

	private Pres span(int row, int startCol, int endCol)
	{
		ArrayList<Object> children = new ArrayList<Object>();
		for (int col = startCol; col < endCol; col++)
		{
			children.add(t12.applyTo( new Text( "<" + col + "_" + row + ">" ) ) );
		}
		return styleSheet.applyTo( new Span( children ) );
	}
	

	private GridRow makeGridRow(int row, int length)
	{
		ArrayList<Object> children = new ArrayList<Object>();
		for (int col = 0; col < length; col++)
		{
			children.add( t12.applyTo( new Text( "<" + col + "_" + row + ">" ) ) );
		}
		return new GridRow( children );
	}
	
	private GridRow makeGridRowCollated(int row)
	{
		ArrayList<Object> children = new ArrayList<Object>();
		for (int col = 0; col < 2; col++)
		{
			children.add( t12.applyTo( new Text( "<" + col + "_" + row + ">" ) ) );
		}
		children.add( span( row, 2, 5 ) );
		children.add( t12.applyTo( new Text( "<" + 5 + "_" + row + ">" ) ) );
		return new GridRow( children );
	}
	
	private Pres makeGrid()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		for (int row = 0; row < 6; row++)
		{
			children.add( makeGridRow( row, 6 ) );
		}
		return tableStyle.applyTo( new RGrid( children ) );
	}
	
	private Pres makeGridwithShortenedRow()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		for (int row = 0; row < 6; row++)
		{
			children.add( makeGridRow( row, row == 2  ?  3  :  6 ) );
		}
		return tableStyle.applyTo( new RGrid( children ) );
	}
	
	private Pres makeGridWithCollatedRows()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		for (int row = 0; row < 6; row++)
		{
			children.add( makeGridRowCollated( row ) );
		}
		return tableStyle.applyTo( new RGrid( children ) );
	}
	
	private Pres makeCollatedGridWithCollatedRows()
	{
		ArrayList<Object> rows = new ArrayList<Object>();
		for (int row = 0; row < 2; row++)
		{
			rows.add( makeGridRowCollated( row ) );
		}

		ArrayList<Object> columns = new ArrayList<Object>();
		for (int row = 2; row < 5; row++)
		{
			columns.add( makeGridRowCollated( row ) );
		}
		rows.add( new Span( columns ) );
		rows.add( makeGridRowCollated( 5 ) );
		return tableStyle.applyTo( new RGrid( rows ) );
	}
	
	private Pres makeCollatedGridWithCollatedRowsAndNonRows()
	{
		ArrayList<Object> rows = new ArrayList<Object>();
		for (int row = 0; row < 2; row++)
		{
			rows.add( makeGridRowCollated( row ) );
		}
		
		ArrayList<Object> columns = new ArrayList<Object>();
		for (int row = 2; row < 5; row++)
		{
			columns.add( makeGridRowCollated( row ) );
		}
		columns.add( t12.applyTo( new Text( "Non-row in a span" ) ) );
		
		rows.add( new Span( columns ) );
		rows.add( makeGridRowCollated( 5 ) );
		rows.add( t12.applyTo( new Text( "Non-row in the grid" ) ) );
		return tableStyle.applyTo( new RGrid( rows ) );
	}
	
	
	
	protected Pres createContents()
	{
		return new Body( new Pres[] {
				new Heading2( "Grid row" ),
				makeGridRow( 0, 6 ),
				new Heading2( "Grid" ),
				makeGrid(),
				new Heading2( "Grid with shortened row" ),
				makeGridwithShortenedRow(),
				new Heading2( "Grid with collated rows" ),
				makeGridWithCollatedRows(),
				new Heading2( "Collated grid with collated rows" ),
				makeCollatedGridWithCollatedRows(),
				new Heading2( "Collated grid with collated rows and non-rows" ),
				makeCollatedGridWithCollatedRowsAndNonRows() } );
	}
}
