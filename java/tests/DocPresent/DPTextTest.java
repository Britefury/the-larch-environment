//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPTextTest
{
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "Text test" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		
		DPPresentationArea area = new DPPresentationArea();

		TextStyleSheet ts0 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		TextStyleSheet ts1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK, true );
		DPText t0 = new DPText( ts0, "Hello World Abcdefghijklmnopqrstuvwxyz" );
		DPText t1 = new DPText( ts1, "Hello World Abcdefghijklmnopqrstuvwxyz" );

		VBoxStyleSheet b0s = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		DPVBox b0 = new DPVBox( b0s );
		b0.append( t0 );
		b0.append( t1 );

		area.setChild( b0 );



		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
