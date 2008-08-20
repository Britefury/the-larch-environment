package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPFlow;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPHBoxTypeset;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPVBox.Alignment;
import BritefuryJ.DocPresent.DPVBox.Typesetting;

public class DPFlowTest_2deepwithhboxes
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
		DPHBoxTypeset h0 = new DPHBoxTypeset();
		h0.append( b0 );
		h0.append( new DPText( ":", new Font( "Sans serif", Font.BOLD, 12 ), Color.black ) );
		
		DPFlow b1 = new DPFlow( 5.0, 0.0, 30.0 );
		b1.extend( c1 );
		DPHBoxTypeset h1 = new DPHBoxTypeset();
		h1.append( b1 );
		h1.append( new DPText( ":", new Font( "Sans serif", Font.BOLD, 12 ), Color.black ) );
		
		DPFlow b2 = new DPFlow( 5.0, 0.0, 30.0 );
		b2.extend( c2 );
		DPHBoxTypeset h2 = new DPHBoxTypeset();
		h2.append( b2 );
		h2.append( new DPText( ":", new Font( "Sans serif", Font.BOLD, 12 ), Color.black ) );
		
		DPFlow b3 = new DPFlow( 5.0, 0.0, 30.0 );
		b3.extend( c3 );
		DPHBoxTypeset h3 = new DPHBoxTypeset();
		h3.append( b3 );
		h3.append( new DPText( ":", new Font( "Sans serif", Font.BOLD, 12 ), Color.black ) );
		
		DPFlow f = new DPFlow( 15.0, 0.0, 30.0 );
		f.append( h0 );
		f.append( h1 );
		f.append( h2 );
		f.append( h3 );
		
		DPVBox box = new DPVBox( Typesetting.NONE, Alignment.LEFT, 0.0, false, false, false, 0.0 );
		box.append( f );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}

}
