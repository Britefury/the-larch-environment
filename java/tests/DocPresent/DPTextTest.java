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
