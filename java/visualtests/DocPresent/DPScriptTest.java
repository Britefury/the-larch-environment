//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package visualtests.DocPresent;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPScriptTest
{
	protected DPWidget makeText(String text, TextStyleSheet styleSheet)
	{
		if ( text != null )
		{
			return new DPText( styleSheet, text );
		}
		else
		{
			return null;
		}
	}

	
	protected DPWidget makeScriptWidget(String mainText, String leftSuperText, String leftSubText, String rightSuperText, String rightSubText)
	{
		TextStyleSheet sMain = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.black );
		TextStyleSheet sScript = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.black );
		TextStyleSheet sPre = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		TextStyleSheet sPost = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		DPText main = new DPText( sMain, mainText);
		
		DPScript script = new DPScript();
		
		script.setMainChild( main );
		script.setLeftSuperscriptChild( makeText( leftSuperText, sScript ) );
		script.setLeftSubscriptChild( makeText( leftSubText, sScript ) );
		script.setRightSuperscriptChild( makeText( rightSuperText, sScript ) );
		script.setRightSubscriptChild( makeText( rightSubText, sScript ) );

		DPText labelA = new DPText( sPre, "Label A yYgGjJpPqQ" );
		DPText labelB = new DPText( sPost, "Label B yYgGjJpPqQ" );
		
		HBoxStyleSheet boxs = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 0.0 );
		DPHBox box = new DPHBox( boxs );
		box.append( labelA );
		box.append( script );
		box.append( labelB );
		
		return box;
	}

	
	
	protected DPWidget makeScriptFraction(String mainText, String numText, String denomText)
	{
		TextStyleSheet sMain = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.black );
		TextStyleSheet sScript = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.black );
		TextStyleSheet sPre = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		TextStyleSheet sPost = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		DPText main = new DPText( sMain, mainText );
		
		DPFraction fraction = new DPFraction();
		fraction.setNumeratorChild( makeText( numText, sScript ) );
		fraction.setDenominatorChild( makeText( denomText, sScript ) );
		
		DPParagraph para = new DPParagraph();
		para.setChildren( Arrays.asList( new DPWidget[] { fraction } ) );
		
		DPScript script = new DPScript();
		
		script.setMainChild( main );
		script.setRightSuperscriptChild( para );

		DPText labelA = new DPText( sPre, "Label A yYgGjJpPqQ" );
		DPText labelB = new DPText( sPost, "Label B yYgGjJpPqQ" );
		
		HBoxStyleSheet boxs = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 0.0 );
		DPHBox box = new DPHBox( boxs );
		box.append( labelA );
		box.append( script );
		box.append( labelB );
		
		return box;
	}

	
	
	protected DPWidget createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		
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
		
		box.append( makeScriptFraction( "MAIN", "a", "b" ) );
		
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
