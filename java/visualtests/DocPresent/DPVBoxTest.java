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

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPVBoxTest
{
	protected static DPText[] makeTexts(String header)
	{
		TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText h = new DPText( t18, header );
		DPText t0 = new DPText( t12, "Hello" );
		DPText t1 = new DPText( t12, "World" );
		DPText t2 = new DPText( t12, "Foo" );
		
		DPText[] texts = { h, t0, t1, t2 };
		return texts;
	}
	
	
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "VBox test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
		DPText[] c0 = makeTexts( "LEFT" );
		DPText[] c1 = makeTexts( "CENTRE" );
		DPText[] c2 = makeTexts( "RIGHT" );
		DPText[] c3 = makeTexts( "EXPAND" );
		
		VBoxStyleSheet b0s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox b0 = new DPVBox( b0s );
		b0.extend( c0 );
		
		VBoxStyleSheet b1s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 0.0, false, 0.0 );
		DPVBox b1 = new DPVBox( b1s );
		b1.extend( c1 );
		
		VBoxStyleSheet b2s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.RIGHT, 0.0, false, 0.0 );
		DPVBox b2 = new DPVBox( b2s );
		b2.extend( c2 );
		
		VBoxStyleSheet b3s = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, true, 0.0 );
		DPVBox b3 = new DPVBox( b3s );
		b3.extend( c3 );
		
		VBoxStyleSheet boxS = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxS );
		box.append( b0 );
		box.append( b1 );
		box.append( b2 );
		box.append( b3 );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		area.getComponent().requestFocusInWindow();
		frame.setVisible(true);
	}
}
