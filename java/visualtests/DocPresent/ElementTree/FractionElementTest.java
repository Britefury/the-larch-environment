//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent.ElementTree;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.FractionElement;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class FractionElementTest extends ElementTreeTestBase
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

	
	protected Element makeFraction(String numeratorText, String denominatorText)
	{
		Font f0 = new Font( "Sans serif", Font.PLAIN, 16 );
		TextStyleSheet s0 = new TextStyleSheet( f0, new Color( 0.0f, 0.5f, 0.0f ) );
		TextStyleSheet s1 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 10 ), Color.blue );
		TextStyleSheet s2 = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		TextElement num = new TextElement( s0, numeratorText );
		TextElement denom = new TextElement( s0, denominatorText );
		
		FractionElement frac = new FractionElement();
		
		frac.setNumeratorChild( num );
		frac.setDenominatorChild( denom );

		TextElement labelA = new TextElement( s1, "Label A yYgGjJpPqQ" );
		TextElement labelB = new TextElement( s2, "Label B yYgGjJpPqQ" );
		
		HBoxStyleSheet boxs = new HBoxStyleSheet( DPHBox.Alignment.BASELINES, 0.0, false, 0.0 );
		HBoxElement box = new HBoxElement( boxs );
		ArrayList<Element> boxChildren = new ArrayList<Element>();
		boxChildren.add( labelA );
		boxChildren.add( frac );
		boxChildren.add( labelB );
		box.setChildren( boxChildren );
		
		return box;
	}

	
	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		
		children.add( makeFraction( "a", "p" ) );
		children.add( makeFraction( "a", "p+q" ) );
		children.add( makeFraction( "a+b", "p" ) );
		children.add( makeFraction( "a+b", "p+q" ) );
		
		box.setChildren( children );
		
		return box;
	}
	
	
	
	private FractionElementTest()
	{
		JFrame frame = new JFrame( "Fraction element test" );
		initFrame( frame );
		

		
		AbstractAction testEditAction = new AbstractAction( "Test edit" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				VBoxElement box = (VBoxElement)contentElement;
				
				ArrayList<Element> children = new ArrayList<Element>();
				
				children.add( makeFraction( "1", "p" ) );
				children.add( makeFraction( "1", "p+q" ) );
				children.add( makeFraction( "1+b", "p" ) );
				children.add( makeFraction( "1+b", "p+q" ) );
				
				box.setChildren( children );
			}
		};

		
		// Menu
		JMenu actionMenu = new JMenu( "Action" );
		actionMenu.add( testEditAction );
		
		menuBar.add( actionMenu );
	}



	public static void main(String[] args)
	{
		new FractionElementTest();
	}
}
