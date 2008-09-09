//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.DocPresent;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPPresentationArea;

public class DPTextTest
{
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "Text test" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();

		DPText text = new DPText( "Hello world abcdefghijklmnopqrstuvwxyz" );


		area.setChild( text );



		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
