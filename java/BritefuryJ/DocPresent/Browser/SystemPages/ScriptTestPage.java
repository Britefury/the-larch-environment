//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;

public class ScriptTestPage extends SystemPage
{
	protected ScriptTestPage()
	{
		register( "tests.script" );
	}
	
	
	public String getTitle()
	{
		return "Script test";
	}

	protected String getDescription()
	{
		return "The script element is used to produce super and subscript arrangements."; 
	}


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

	
	protected DPWidget makeFraction(DPWidget num, DPWidget denom)
	{
		DPFraction frac = new DPFraction();
		
		frac.setNumeratorChild( num );
		frac.setDenominatorChild( denom );

		return frac;
	}

	
	protected DPWidget makeScript(DPWidget main, DPWidget leftSuper, DPWidget leftSub, DPWidget rightSuper, DPWidget rightSub)
	{
		DPScript script = new DPScript();
		
		script.setMainChild( main );
		script.setLeftSuperscriptChild( leftSuper );
		script.setLeftSubscriptChild( leftSub );
		script.setRightSuperscriptChild( rightSuper );
		script.setRightSubscriptChild( rightSub );
		
		return script;
	}

	protected DPWidget makeScriptLine(DPWidget main, DPWidget leftSuper, DPWidget leftSub, DPWidget rightSuper, DPWidget rightSub)
	{
		TextStyleSheet sPre = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		TextStyleSheet sPost = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.red );
		
		DPWidget script = makeScript( main, leftSuper, leftSub, rightSuper, rightSub );
		
		DPText labelA = new DPText( sPre, "<<Left<<" );
		DPText labelB = new DPText( sPost, ">>Right>>" );
		
		DPHBox box = new DPHBox();
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
		
		DPHBox box = new DPHBox();
		box.append( labelA );
		box.append( script );
		box.append( labelB );
		
		return box;
	}

	
	
	protected DPWidget createContents()
	{
		TextStyleSheet sMain = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.black );
		TextStyleSheet sScript = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.black );
		TextStyleSheet blackStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 24 ), Color.black );

		DPVBox box = new DPVBox();
		
		for (int i = 0; i < 16; i++)
		{
			DPWidget leftSuperText, leftSubText, rightSuperText, rightSubText;
			
			leftSuperText = ( i & 1 ) != 0   ?   makeText( "left super", sScript )  :  null; 
			leftSubText = ( i & 2 ) != 0   ?   makeText( "left sub", sScript )  :  null; 
			rightSuperText = ( i & 4 ) != 0   ?   makeText( "right super", sScript )  :  null; 
			rightSubText = ( i & 8 ) != 0   ?   makeText( "right sub", sScript )  :  null;
			
			DPWidget script = makeScriptLine( makeText( "MAIN" + String.valueOf( i ), sMain ), leftSuperText, leftSubText, rightSuperText, rightSubText );
			
			box.append( script );
		}
		
		box.append( makeText( "---", blackStyle ) );

		for (int i = 0; i < 16; i++)
		{
			DPWidget leftSuperText, leftSubText, rightSuperText, rightSubText;
			
			leftSuperText = ( i & 1 ) != 0   ?   makeFraction( makeText( "a", sScript ), makeText( "x", sScript ) )  :  makeText( "a", sScript ); 
			leftSubText = ( i & 2 ) != 0   ?   makeFraction( makeText( "b", sScript ), makeText( "x", sScript ) )  :  makeText( "b", sScript ); 
			rightSuperText = ( i & 4 ) != 0   ?   makeFraction( makeText( "c", sScript ), makeText( "x", sScript ) )  :  makeText( "c", sScript ); 
			rightSubText = ( i & 8 ) != 0   ?   makeFraction( makeText( "d", sScript ), makeText( "x", sScript ) )  :  makeText( "d", sScript );
			
			DPWidget script = makeScriptLine( makeText( "MAIN" + String.valueOf( i ), sMain ), leftSuperText, leftSubText, rightSuperText, rightSubText );
			
			box.append( script );
		}
		
		return box;
	}
}
