package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;

import Britefury.DocPresent.DPText;
import Britefury.DocPresent.DPPresentationArea;

public class DPTextTest {
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "Text test" );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
		DPText text = new DPText( "Hello world abcdefghijklmnopqrstuvwxyz", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK );
	     
	     
		area.setChild( text );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
