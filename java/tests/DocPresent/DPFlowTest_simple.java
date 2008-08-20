package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPFlow;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;

public class DPFlowTest_simple
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
		JFrame frame = new JFrame( "Flow; simple test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
		DPText[] c0 = makeTexts( "UNINDENTED" );
		DPText[] c1 = makeTexts( "INDENTED" );
		
		DPFlow b0 = new DPFlow( 10.0, 0.0, 0.0 );
		b0.extend( c0 );
		
		DPFlow b1 = new DPFlow( 10.0, 0.0, 30.0 );
		b1.extend( c1 );
		
		DPVBox box = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 20.0, false, false, false, 0.0 );
		box.append( b0 );
		box.append( b1 );
	     
	     
		area.setChild( box );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
