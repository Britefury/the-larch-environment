//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

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
	
	
	
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet scriptPreStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) ).withForeground( Color.blue );
	private static PrimitiveStyleSheet scriptPostStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 24 ) ).withForeground( Color.red );
	private static PrimitiveStyleSheet dividerStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 24 ) );

	private static PrimitiveStyleSheet sMainStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 16 ) ).withForeground( Color.black );
	private static PrimitiveStyleSheet sScriptStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 16 ) ).withForeground( Color.black );

	
	protected DPElement makeScriptLine(DPElement main, DPElement leftSuper, DPElement leftSub, DPElement rightSuper, DPElement rightSub)
	{
		return styleSheet.hbox( Arrays.asList( new DPElement[] { scriptPreStyleSheet.text( "<<Left<<" ), styleSheet.script( main, leftSuper, leftSub, rightSuper, rightSub ),
				scriptPostStyleSheet.text( ">>Right>>" ) } ) );
	}

	
	
	protected DPElement makeScriptFraction(String mainText, String numText, String denomText)
	{
		DPFraction fraction = styleSheet.fraction( sScriptStyleSheet.text( numText ), sScriptStyleSheet.text( denomText ), "/" );
		
		DPScript script = styleSheet.scriptRSuper( sMainStyleSheet.text( mainText ), styleSheet.paragraph( Arrays.asList( new DPElement[] { fraction } ) ) );
		
		return styleSheet.hbox( Arrays.asList( new DPElement[] { scriptPreStyleSheet.text( "Label A yYgGjJpPqQ" ), script, scriptPostStyleSheet.text( "Label B yYgGjJpPqQ" ) } ) );
	}

	
	
	protected DPElement createContents()
	{
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		
		for (int i = 0; i < 16; i++)
		{
			DPElement leftSuperText = ( i & 1 ) != 0   ?   sScriptStyleSheet.text( "left super" )  :  null; 
			DPElement leftSubText = ( i & 2 ) != 0   ?   sScriptStyleSheet.text( "left sub" )  :  null; 
			DPElement rightSuperText = ( i & 4 ) != 0   ?   sScriptStyleSheet.text( "right super" )  :  null; 
			DPElement rightSubText = ( i & 8 ) != 0   ?   sScriptStyleSheet.text( "right sub" )  :  null;
			
			children.add( makeScriptLine( sMainStyleSheet.text( "MAIN" + String.valueOf( i ) ), leftSuperText, leftSubText, rightSuperText, rightSubText ) );
		}
		
		children.add( dividerStyleSheet.text( "---" ) );

		for (int i = 0; i < 16; i++)
		{
			DPElement leftSuperText = ( i & 1 ) != 0   ?   styleSheet.fraction( sScriptStyleSheet.text( "a" ), sScriptStyleSheet.text( "x" ), "/" )  :  sScriptStyleSheet.text( "a" ); 
			DPElement leftSubText = ( i & 2 ) != 0   ?   styleSheet.fraction( sScriptStyleSheet.text( "b" ), sScriptStyleSheet.text( "x" ), "/" )  :  sScriptStyleSheet.text( "b" ); 
			DPElement rightSuperText = ( i & 4 ) != 0   ?   styleSheet.fraction( sScriptStyleSheet.text( "c" ), sScriptStyleSheet.text( "x" ), "/" )  :  sScriptStyleSheet.text( "c" ); 
			DPElement rightSubText = ( i & 8 ) != 0   ?   styleSheet.fraction( sScriptStyleSheet.text( "d" ), sScriptStyleSheet.text( "x" ), "/" )  :  sScriptStyleSheet.text( "d" );
			
			children.add( makeScriptLine( sMainStyleSheet.text( "MAIN" + String.valueOf( i ) ), leftSuperText, leftSubText, rightSuperText, rightSubText ) );
		}
		
		return styleSheet.withVBoxSpacing( 10.0 ).vbox( children );
	}
}
