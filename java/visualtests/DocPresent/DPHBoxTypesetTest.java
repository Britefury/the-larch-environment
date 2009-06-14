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

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPHBoxTypesetTest
{
	protected static DPText[] makeTexts(String header)
	{
		TextStyleSheet t12 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t0 = new DPText( t12, "Hello" );
		DPText t1 = new DPText( t12, "World" );
		DPText t2 = new DPText( t12, "Foo" );
		DPText t3 = new DPText( t12, "Bar" );
		
		DPText[] texts = { t0, t1, t2, t3 };
		return texts;
	}
	
	
	protected static DPHBox makeTypesetHBox(DPVBox.Typesetting typesetting, String header)
	{
		DPText[] txt = makeTexts( header );
		VBoxStyleSheet vs = new VBoxStyleSheet( typesetting, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		DPVBox v = new DPVBox( vs );
		v.extend( txt );
		TextStyleSheet t18 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText before = new DPText( t18, header );
		DPText after = new DPText( t18, " After" );
		HBoxStyleSheet ts = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 );
		DPHBox t = new DPHBox( ts );
		t.append( before );
		t.append( v );
		t.append( after );
		return t;
	}
	
	
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "HBox typeset test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
		
		DPHBox t0 = makeTypesetHBox( DPVBox.Typesetting.NONE, "NONE" );
		DPHBox t1 = makeTypesetHBox( DPVBox.Typesetting.ALIGN_WITH_TOP, "ALIGN_WITH_TOP" );
		DPHBox t2 = makeTypesetHBox( DPVBox.Typesetting.ALIGN_WITH_BOTTOM, "ALIGN_WITH_BOTTOM" );
		
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 20.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		box.append( t0 );
		box.append( t1 );
		box.append( t2 );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
