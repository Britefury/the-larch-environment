package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import Britefury.DocPresent.DPPresentationArea;
import Britefury.DocPresent.DPText;
import Britefury.DocPresent.DPVBox;
import Britefury.DocPresent.DPFlow;

public class DPFlowTest_2deep
{
	protected static DPText[] makeTexts(String header)
	{
		DPText h = new DPText( header, new Font( "Sans serif", Font.BOLD, 18 ), Color.BLACK );
		DPText t0 = new DPText( "Hello", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t1 = new DPText( "World", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t2 = new DPText( "Foo", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t3 = new DPText( "Bar", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t4 = new DPText( "Moo", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t5 = new DPText( "Test", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		DPText t6 = new DPText( "This", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
		
		DPText[] texts = { h, t0, t1, t2, t3, t4, t5, t6 };
		return texts;
	}
	
	
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "Flow; 2-deep test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
		DPText[] c0 = makeTexts( "FLOW1" );
		DPText[] c1 = makeTexts( "FLOW2" );
		DPText[] c2 = makeTexts( "FLOW3" );
		DPText[] c3 = makeTexts( "FLOW4" );
		
		DPFlow b0 = new DPFlow( 5.0, 0.0, 30.0 );
		b0.extend( c0 );
		
		DPFlow b1 = new DPFlow( 5.0, 0.0, 30.0 );
		b1.extend( c1 );
		
		DPFlow b2 = new DPFlow( 5.0, 0.0, 30.0 );
		b2.extend( c2 );
		
		DPFlow b3 = new DPFlow( 5.0, 0.0, 30.0 );
		b3.extend( c3 );
		
		DPFlow f = new DPFlow( 15.0, 0.0, 30.0 );
		f.append( b0 );
		f.append( b1 );
		f.append( b2 );
		f.append( b3 );
		
		DPVBox box = new DPVBox();
		box.append( f );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
