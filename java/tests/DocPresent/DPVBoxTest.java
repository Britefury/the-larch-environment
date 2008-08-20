package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;

public class DPVBoxTest {
	protected static DPText[] makeTexts(String header)
	{
		DPText h = new DPText( header, new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText t0 = new DPText( "Hello", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t1 = new DPText( "World", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t2 = new DPText( "Foo", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		
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
		
		DPVBox b0 = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, false, false, 0.0 );
		b0.extend( c0 );
		
		DPVBox b1 = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.CENTRE, 0.0, false, false, false, 0.0 );
		b1.extend( c1 );
		
		DPVBox b2 = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.RIGHT, 0.0, false, false, false, 0.0 );
		b2.extend( c2 );
		
		DPVBox b3 = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 0.0, true, false, false, 0.0 );
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
