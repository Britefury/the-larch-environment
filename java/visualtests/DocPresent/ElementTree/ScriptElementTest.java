//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package visualtests.DocPresent.ElementTree;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.ElementTree.ScriptElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class ScriptElementTest extends ElementTreeTestBase
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
		
		HBoxStyleSheet boxs = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 0.0 );
		HBoxElement box = new HBoxElement( boxs );
		ArrayList<Element> boxChildren = new ArrayList<Element>();
		boxChildren.add( labelA );
		boxChildren.add( script );
		boxChildren.add( labelB );
		box.setChildren( boxChildren );
		
		return box;
	}

	
	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		
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



	public ScriptElementTest()
	{
		JFrame frame = new JFrame( "Script test" );
		initFrame( frame );
	}
	
	
	public static void main(String[] args)
	{
		new ScriptElementTest();
	}
}
