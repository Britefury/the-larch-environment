package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import Britefury.DocPresent.DPPresentationArea;
import Britefury.DocPresent.DPText;
import Britefury.DocPresent.DPHBox;
import Britefury.DocPresent.DPVBox;

public class DPHBoxTest
{
	protected static DPText[] makeTexts(String header)
	{
		DPText h = new DPText( header, new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText t0 = new DPText( "Hello", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t1 = new DPText( "World", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t2 = new DPText( "Foo", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t3 = new DPText( "j", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t4 = new DPText( "q", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t5 = new DPText( "'", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t6 = new DPText( ".", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t7 = new DPText( "Bar", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		
		DPText[] texts = { h, t0, t1, t2, t3, t4, t5, t6, t7 };
		return texts;
	}
	
	
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "HBox test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
		DPText[] c0 = makeTexts( "TOP" );
		DPText[] c1 = makeTexts( "CENTRE" );
		DPText[] c2 = makeTexts( "BOTTOM" );
		DPText[] c3 = makeTexts( "EXPAND" );
		
		DPHBox b0 = new DPHBox( DPHBox.Alignment.TOP, 0.0, false, false, false, 0.0 );
		b0.extend( c0 );
		
		DPHBox b1 = new DPHBox( DPHBox.Alignment.CENTRE, 0.0, false, false, false, 0.0 );
		b1.extend( c1 );
		
		DPHBox b2 = new DPHBox( DPHBox.Alignment.BOTTOM, 0.0, false, false, false, 0.0 );
		b2.extend( c2 );
		
		DPHBox b3 = new DPHBox( DPHBox.Alignment.EXPAND, 0.0, true, false, false, 0.0 );
		b3.extend( c3 );
		
		DPVBox box = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 20.0, false, false, false, 0.0 );
		box.append( b0 );
		box.append( b1 );
		box.append( b2 );
		box.append( b3 );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
