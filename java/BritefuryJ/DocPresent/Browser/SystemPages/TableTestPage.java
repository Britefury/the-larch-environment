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
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Table;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

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

	private static final StyleSheet2 styleSheet = StyleSheet2.instance;
	private static StyleSheet2 t12 = styleSheet.withAttr( Primitive.fontSize, 12 );
	private static StyleSheet2 t18 = styleSheet.withAttr( Primitive.fontSize, 18 );
	private static StyleSheet2 sectionStyle = styleSheet.withAttr( Primitive.vboxSpacing, 5.0 ).withAttr( Primitive.border, new SolidBorder( 2.0, 3.0, new Color( 0.0f, 0.3f, 0.7f ), new Color( 0.95f, 0.975f, 1.0f  ) ) );
	private static StyleSheet2 outlineStyle = styleSheet.withAttr( Primitive.border, new SolidBorder( 1.0, 0.0, new Color( 0.5f, 0.5f, 0.5f ), new Color( 0.9f, 0.9f, 0.9f ) ) );
	private static StyleSheet2 tableStyle = styleSheet.withAttr( Primitive.tableColumnSpacing, 5.0 ).withAttr( Primitive.tableRowSpacing, 5.0 );

	private Table makeTable0()
	{
		Table table = new Table();
		for (int row = 0; row < 6; row++)
		{
			for (int col = 0; col < 6; col++)
			{
				table.put( col, row, outlineStyle.applyTo( t12.applyTo( new Border( new Text( ("<" + col + "_" + row + ">") ) ) ) ) );
			}
		}
		return table;
	}
	
	private Table makeTable1()
	{
		Table table = makeTable0();
		table.put( 2, 2, null );
		table.put( 3, 2, null );
		table.put( 4, 2, null );
		table.put( 2, 2, 3, 1, outlineStyle.applyTo( new Border( t12.applyTo( new Text( "<<wide>>" ) ).alignHCentre() ) ) );
		return table;
	}
	
	private Table makeTable2()
	{
		Table table = makeTable0();
		table.put( 2, 2, null );
		table.put( 2, 3, null );
		table.put( 2, 4, null );
		table.put( 2, 2, 1, 3, outlineStyle.applyTo( new Border( t18.applyTo( new Text( "T" ) ).alignHCentre() ) ) );
		return table;
	}
	
	private Table makeTable3()
	{
		Table table = makeTable0();
		table.put( 2, 2, null );
		table.put( 2, 3, null );
		table.put( 2, 4, null );
		table.put( 3, 2, null );
		table.put( 3, 3, null );
		table.put( 3, 4, null );
		table.put( 4, 2, null );
		table.put( 4, 3, null );
		table.put( 4, 4, null );
		table.put( 2, 2, 3, 3, outlineStyle.applyTo( new Border( t18.applyTo( new Text( "T" ) ).alignHCentre() ) ) );
		return table;
	}
	
	
	
	protected Pres createContents()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		children.add( sectionStyle.applyTo( new Border( tableStyle.applyTo( makeTable0() ) ) ) );
		children.add( sectionStyle.applyTo( new Border( tableStyle.applyTo( makeTable1() ) ) ) );
		children.add( sectionStyle.applyTo( new Border( tableStyle.applyTo( makeTable2() ) ) ) );
		children.add( sectionStyle.applyTo( new Border( tableStyle.applyTo( makeTable3() ) ) ) );
		
		return new Body( children );
	}
}
