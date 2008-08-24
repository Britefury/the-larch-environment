package tests.DocPresent.ElementTree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;

import javax.swing.JFrame;

import tests.DocPresent.DPScriptTest;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.ElementTree.ScriptElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class ScriptElemenTest
{
	protected Element makeText(String text, TextStyleSheet styleSheet)
	{
		if ( text != null )
		{
			return new TextElement( styleSheet, text );
		}
		else
		{
			return null;
		}
	}

	
	protected Element makeScriptElement(String mainText, String leftSuperText, String leftSubText, String rightSuperText, String rightSubText)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 16 );
		TextStyleSheet s0 = new TextStyleSheet( f0, Color.black );
		TextStyleSheet s1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 10 ), Color.blue );
		TextStyleSheet s2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		TextElement main = new TextElement( s0, mainText);
		
		ScriptElement script = new ScriptElement();
		
		script.setMainChild( main );
		script.setLeftSuperscriptChild( makeText( leftSuperText, s0 ) );
		script.setLeftSubscriptChild( makeText( leftSubText, s0 ) );
		script.setRightSuperscriptChild( makeText( rightSuperText, s0 ) );
		script.setRightSubscriptChild( makeText( rightSubText, s0 ) );

		TextElement labelA = new TextElement( s1, "Label A yYgGjJpPqQ" );
		TextElement labelB = new TextElement( s2, "Label B yYgGjJpPqQ" );
		
		HBoxStyleSheet boxs = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 );
		HBoxElement box = new HBoxElement( boxs );
		Vector<Element> boxChildren = new Vector<Element>();
		boxChildren.add( labelA );
		boxChildren.add( script );
		boxChildren.add( labelB );
		box.setChildren( boxChildren );
		
		return box;
	}

	
	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		Vector<Element> children = new Vector<Element>();
		
		for (int i = 0; i < 16; i++)
		{
			String leftSuperText, leftSubText, rightSuperText, rightSubText;
			
			leftSuperText = ( i & 1 ) != 0   ?   "left super"  :  null; 
			leftSubText = ( i & 2 ) != 0   ?   "left sub"  :  null; 
			rightSuperText = ( i & 4 ) != 0   ?   "right super"  :  null; 
			rightSubText = ( i & 8 ) != 0   ?   "right sub"  :  null;
			
			Element script = makeScriptElement( "MAIN" + String.valueOf( i ), leftSuperText, leftSubText, rightSuperText, rightSubText );
			
			children.add( script );
		}
		
		box.setChildren( children );
		
		return box;
	}



	public ScriptElemenTest()
	{
		JFrame frame = new JFrame( "Script test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		ElementTree tree = new ElementTree();

		tree.getRoot().setChild( createContentNode() );
	     
	     
		DPPresentationArea area = tree.getPresentationArea();
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
