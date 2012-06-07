//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.LSpace.TableBackgroundPainter;
import BritefuryJ.LSpace.TableElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class TableTestPage extends SystemPage
{
	protected TableTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Table test";
	}

	protected String getDescription()
	{
		return "The table element arranges is children in a table layout. Holes can be present. Child elements may span multiple columns and rows.";
	}
	
	protected static final TableBackgroundPainter checkeredTableBackgroundPainter = new TableBackgroundPainter()
	{
		@Override
		public void paintTableBackground(TableElement table, Graphics2D graphics)
		{
			graphics.setPaint( new Color( 0.85f, 0.85f, 0.85f ) );
			for (int y = 0; y < table.getNumRows(); y++)
			{
				for (int x = 0; x < table.getNumColumns(); x++)
				{
					if ( ( ( x + y ) & 1 ) == 0  &&  table.hasChildAt( x, y ) )
					{
						int colSpan = table.getChildColSpan( x, y );
						int rowSpan = table.getChildRowSpan( x, y );
						double y0 = table.getRowBoundaryY( y );
						double y1 = table.getRowBoundaryY( y + rowSpan );
						double x0 = table.getColumnBoundaryX( x );
						double x1 = table.getColumnBoundaryX( x + colSpan );
						
						Rectangle2D.Double r = new Rectangle2D.Double( x0, y0, x1-x0, y1-y0 );
						
						graphics.fill( r );
					}
				}
			}
		}
	};

	private static final StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet t12 = styleSheet.withValues( Primitive.fontSize.as( 12 ) );
	private static StyleSheet t18 = styleSheet.withValues( Primitive.fontSize.as( 18 ) );
	private static StyleSheet sectionStyle = styleSheet.withValues( Primitive.columnSpacing.as( 5.0 ), Primitive.border.as( new SolidBorder( 2.0, 3.0, new Color( 0.0f, 0.3f, 0.7f ), new Color( 0.95f, 0.975f, 1.0f ) ) ) );
	private static StyleSheet tableStyle = styleSheet.withValues( Primitive.tableColumnSpacing.as( 5.0 ), Primitive.tableRowSpacing.as( 5.0 ), Primitive.tableBorder.as( new SolidBorder( 1.0, 0.0, Color.BLACK, null ) ),
		    Primitive.tableCellBoundaryPaint.as( new Color( 0.5f, 0.5f, 0.5f ) ), Primitive.tableBackgroundPainter.as( checkeredTableBackgroundPainter ) );

	private Table makeTable0()
	{
		Table table = new Table();
		for (int row = 0; row < 6; row++)
		{
			for (int col = 0; col < 6; col++)
			{
				table.put( col, row, new Text( ("<" + col + "_" + row + ">") ) );
			}
		}
		return table;
	}
	
	private Table makeTable1()
	{
		Table table = new Table();
		for (int row = 0; row < 6; row++)
		{
			for (int col = 0; col < 6; col++)
			{
				table.put( col, row, t12.applyTo( new Text( ("<" + col + "_" + row + ">") ) ) );
			}
		}
		return table;
	}
	
	private Table makeTable2()
	{
		Table table = makeTable1();
		table.put( 2, 2, null );
		table.put( 3, 2, null );
		table.put( 4, 2, null );
		table.put( 2, 2, 3, 1,t12.applyTo( new Text( "<<wide>>" ) ).alignHCentre() );
		return table;
	}
	
	private Table makeTable3()
	{
		Table table = makeTable1();
		table.put( 2, 2, null );
		table.put( 2, 3, null );
		table.put( 2, 4, null );
		table.put( 2, 2, 1, 3, t18.applyTo( new Text( "T" ) ).alignHCentre() );
		return table;
	}
	
	private Table makeTable4()
	{
		Table table = makeTable1();
		table.put( 2, 2, null );
		table.put( 2, 3, null );
		table.put( 2, 4, null );
		table.put( 3, 2, null );
		table.put( 3, 3, null );
		table.put( 3, 4, null );
		table.put( 4, 2, null );
		table.put( 4, 3, null );
		table.put( 4, 4, null );
		table.put( 2, 2, 3, 3, t18.applyTo( new Text( "T" ) ).alignHCentre() );
		return table;
	}
	
	
	
	protected Pres createContents()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		children.add( tableStyle.applyTo( makeTable0().alignHPack().alignVRefY() ) );
		children.add( sectionStyle.applyTo( new Border( tableStyle.applyTo( makeTable1() ) ) ) );
		children.add( sectionStyle.applyTo( new Border( tableStyle.applyTo( makeTable2() ) ) ) );
		children.add( sectionStyle.applyTo( new Border( tableStyle.applyTo( makeTable3() ) ) ) );
		children.add( sectionStyle.applyTo( new Border( tableStyle.applyTo( makeTable4() ) ) ) );
		
		return new Body( children );
	}
}
