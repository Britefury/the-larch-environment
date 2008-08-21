package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;

public class DPHBoxTypesetTest
{
	protected static DPText[] makeTexts(String header)
	{
		DPText t0 = new DPText( "Hello", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t1 = new DPText( "World", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t2 = new DPText( "Foo", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t3 = new DPText( "Bar", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		
		DPText[] texts = { t0, t1, t2, t3 };
		return texts;
	}
	
	
	protected static DPHBox makeTypesetHBox(DPVBox.Typesetting typesetting, String header)
	{
		DPText[] txt = makeTexts( header );
		DPVBox v = new DPVBox( typesetting, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		v.extend( txt );
		DPText before = new DPText( header, new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText after = new DPText( " After", new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPHBox t = new DPHBox( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 );
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
		
		DPVBox box = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 20.0, false, 0.0 );
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
