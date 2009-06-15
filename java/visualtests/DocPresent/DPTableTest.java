//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPTableTest
{
	private static TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
	private static TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 18 ), Color.BLACK );

	protected static DPText text12(String s)
	{
		return new DPText( t12, s );
	}
	
	protected static DPText text18(String s)
	{
		return new DPText( t18, s );
	}
	
	protected static DPTable makeTable0()
	{
		TableStyleSheet tbls0 = new TableStyleSheet( VAlignment.BASELINES, HAlignment.CENTRE, 5.0, false, 0.0, 5.0, false, 0.0 );
		DPTable table = new DPTable( tbls0 );
		for (int row = 0; row < 6; row++)
		{
			for (int col = 0; col < 6; col++)
			{
				table.put( col, row, text12( "<" + col + "_" + row + ">" ) );
			}
		}
		return table;
	}
	
	protected static DPTable makeTable1()
	{
		DPTable table = makeTable0();
		table.put( 2, 2, null );
		table.put( 3, 2, null );
		table.put( 4, 2, null );
		table.put( 2, 2, 3, 1, text12( "<<wide>>" ) );
		return table;
	}
	
	protected static DPTable makeTable2()
	{
		DPTable table = makeTable0();
		table.put( 2, 2, null );
		table.put( 2, 3, null );
		table.put( 2, 4, null );
		table.put( 2, 2, 1, 3, text18( "T" ) );
		return table;
	}
	
	protected static DPTable makeTable3()
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
		table.put( 2, 2, 3, 3, text18( "T" ) );
		return table;
	}
	
	
	public static void main(final String[] args)
	{
		JFrame frame = new JFrame( "Table" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
		VBoxStyleSheet boxS = new VBoxStyleSheet( HAlignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( makeTable0() );
		box.append( makeTable1() );
		box.append( makeTable2() );
		box.append( makeTable3() );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
