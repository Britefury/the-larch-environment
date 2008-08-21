package tests.DocPresent;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPScript;

public class DPScriptTest
{
	protected DPWidget makeText(String text, Font f, Color c)
	{
		if ( text != null )
		{
			return new DPText( text, f, c );
		}
		else
		{
			return null;
		}
	}

	
	protected DPWidget makeScriptWidget(String mainText, String leftSuperText, String leftSubText, String rightSuperText, String rightSubText)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 16 );
		DPText main = new DPText( mainText, f0, Color.black );
		
		DPScript script = new DPScript();
		
		script.setMainChild( main );
		script.setLeftSuperscriptChild( makeText( leftSuperText, f0, Color.black ) );
		script.setLeftSubscriptChild( makeText( leftSubText, f0, Color.black ) );
		script.setRightSuperscriptChild( makeText( rightSuperText, f0, Color.black ) );
		script.setRightSubscriptChild( makeText( rightSubText, f0, Color.black ) );

		DPText labelA = new DPText( "Label A yYgGjJpPqQ", new Font( "Sans serif", Font.PLAIN, 10 ), Color.blue );
		DPText labelB = new DPText( "Label B yYgGjJpPqQ", new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		
		DPHBox box = new DPHBox( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 );
		box.append( labelA );
		box.append( script );
		box.append( labelB );
		
		return box;
	}

	
	
	protected DPWidget createContentNode()
	{
		DPVBox box = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		
		for (int i = 0; i < 16; i++)
		{
			String leftSuperText, leftSubText, rightSuperText, rightSubText;
			
			leftSuperText = ( i & 1 ) != 0   ?   "left super"  :  null; 
			leftSubText = ( i & 2 ) != 0   ?   "left sub"  :  null; 
			rightSuperText = ( i & 4 ) != 0   ?   "right super"  :  null; 
			rightSubText = ( i & 8 ) != 0   ?   "right sub"  :  null;
			
			DPWidget script = makeScriptWidget( "MAIN" + String.valueOf( i ), leftSuperText, leftSubText, rightSuperText, rightSubText );
			
			box.append( script );
		}
		
		return box;
	}



	public DPScriptTest()
	{
		JFrame frame = new JFrame( "Script test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
	     
	     
		area.setChild( createContentNode() );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
	
	
	public static void main(String[] args)
	{
		new DPScriptTest();
	}

}
