package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import Britefury.DocPresent.DPVBox;
import Britefury.DocPresent.DPHBoxTypeset;
import Britefury.DocPresent.DPPresentationArea;
import Britefury.DocPresent.DPText;

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
	
	
	protected static DPHBoxTypeset makeTypesetHBox(DPVBox.Typesetting typesetting, String header)
	{
		DPText[] txt = makeTexts( header );
		DPVBox v = new DPVBox( typesetting, DPVBox.Alignment.LEFT, 0.0, false, false, false, 0.0 );
		v.extend( txt );
		DPText before = new DPText( header, new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText after = new DPText( " After", new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPHBoxTypeset t = new DPHBoxTypeset( 0.0, false, false, false, 0.0 );
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
		
		DPHBoxTypeset t0 = makeTypesetHBox( DPVBox.Typesetting.NONE, "NONE" );
		DPHBoxTypeset t1 = makeTypesetHBox( DPVBox.Typesetting.ALIGN_WITH_TOP, "ALIGN_WITH_TOP" );
		DPHBoxTypeset t2 = makeTypesetHBox( DPVBox.Typesetting.ALIGN_WITH_BOTTOM, "ALIGN_WITH_BOTTOM" );
		DPHBoxTypeset t3 = makeTypesetHBox( DPVBox.Typesetting.IN_TO_TOP_OUT_FROM_BOTTOM, "IN_TO_TOP_OUT_FROM_BOTTOM" );
		
		DPVBox box = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 20.0, false, false, false, 0.0 );
		box.append( t0 );
		box.append( t1 );
		box.append( t2 );
		box.append( t3 );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
