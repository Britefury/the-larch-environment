//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.FlowGrid;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class FlowGridTestPage extends SystemPage
{
	protected FlowGridTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Flow grid test";
	}

	protected String getDescription()
	{
		return "The flow grid flows a sequence of child elements in a grid.";
	}

	private static final StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet t12 = styleSheet.withValues( Primitive.fontSize.as( 12 ) );
	private static StyleSheet tableStyle = styleSheet.withValues( Primitive.tableColumnSpacing.as( 5.0 ), Primitive.tableRowSpacing.as( 5.0 ),
			Primitive.tableBorder.as( new SolidBorder( 1.0, 0.0, Color.BLACK, null ) ),
			Primitive.tableCellBoundaryPaint.as( new Color( 0.5f, 0.5f, 0.5f ) ),
			Primitive.tableBackgroundPainter.as( TableTestPage.checkeredTableBackgroundPainter ) );
	private static AbstractBorder cellBorder = new SolidBorder( 1.0, 4.0, 20.0, 20.0, new Color( 1.0f, 0.8f, 0.6f ), new Color( 1.0f, 1.0f, 0.8f, 0.5f ) );


	private Pres makeCell(String text)
	{
		return cellBorder.surround( new SpaceBin( 150.0, 100.0, t12.applyTo( new Label( text ) ).alignHCentre().alignVCentre() ) );
	}
	
	private Pres makeDynamicFlowGrid(int numCells)
	{
		Pres[] cells = new Pres[numCells];
		for (int i = 0; i < numCells; i++)
		{
			cells[i] = makeCell( String.valueOf( i ) );
		}
		return tableStyle.applyTo( new FlowGrid( cells ).alignHPack() );
	}
	
	private Pres makeFixedFlowGrid(int maxColumns, int numCells)
	{
		Pres[] cells = new Pres[numCells];
		for (int i = 0; i < numCells; i++)
		{
			cells[i] = makeCell( String.valueOf( i ) );
		}
		return tableStyle.applyTo( new FlowGrid( maxColumns, cells ).alignHPack() );
	}
	
	
	protected Pres createContents()
	{
		return new Body( new Pres[] {
				new Heading2( "Flow grid" ),
				makeDynamicFlowGrid( 50 ),
				new Heading2( "Flow grid - attempt to hit 5 columns" ),
				makeFixedFlowGrid( 5, 50 )} );
	}
}
