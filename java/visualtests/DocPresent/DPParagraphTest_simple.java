//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package visualtests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;

public class DPParagraphTest_simple
{
	protected static ArrayList<DPWidget> makeTexts(String header)
	{
		ElementStyleSheet t12 = DPText.styleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		ElementStyleSheet t18 = DPText.styleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText h = new DPText( t18, header );
		DPText t0 = new DPText( t12, "Hello" );
		DPText t1 = new DPText( t12, "World" );
		DPText t2 = new DPText( t12, "Foo" );
		DPText t3 = new DPText( t12, "j" );
		DPText t4 = new DPText( t12, "q" );
		DPText t5 = new DPText( t12, "'" );
		DPText t6 = new DPText( t12, "." );
		DPText t7 = new DPText( t12, "Bar" );
		
		DPText[] texts = { h, t0, t1, t2, t3, t4, t5, t6, t7 };
		return new ArrayList<DPWidget>( Arrays.asList( texts ) );
	}
	
	protected static ArrayList<DPWidget> addLineBreaks(ArrayList<DPWidget> nodesIn, int step)
	{
		ArrayList<DPWidget> nodesOut = new ArrayList<DPWidget>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				nodesOut.add( new DPLineBreak() );
			}
		}
		return nodesOut;
	}
	
	public static void main(final String[] args)
	{
		JFrame frame = new JFrame( "Flow; simple test" );
		
		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		DPPresentationArea area = new DPPresentationArea();
		     
		     
		ArrayList<DPWidget> c0 = makeTexts( "UNINDENTED" );
		c0 = addLineBreaks( c0, 1 );
		ArrayList<DPWidget> c1 = makeTexts( "INDENTED" );
		c1 = addLineBreaks( c1, 1 );
		
		ElementStyleSheet b0s = DPParagraph.styleSheet( VAlignment.BASELINES, 10.0, 0.0, 0.0, 0.0 );
		DPParagraph b0 = new DPParagraph( b0s );
		b0.extend( c0 );
		
		ElementStyleSheet b1s = DPParagraph.styleSheet( VAlignment.BASELINES, 10.0, 0.0, 0.0, 30.0 );
		DPParagraph b1 = new DPParagraph( b1s );
		b1.extend( c1 );
		
		ElementStyleSheet boxS = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( b0 );
		box.append( b1 );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
