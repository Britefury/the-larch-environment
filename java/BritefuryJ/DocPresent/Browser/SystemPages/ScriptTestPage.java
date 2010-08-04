//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Fraction;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Script;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class ScriptTestPage extends SystemPage
{
	protected ScriptTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Script test";
	}

	protected String getDescription()
	{
		return "The script element is used to produce super and subscript arrangements."; 
	}
	
	
	
	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet scriptPreStyleSheet = styleSheet.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, Color.blue );
	private static StyleSheet scriptPostStyleSheet = styleSheet.withAttr( Primitive.fontSize, 24 ).withAttr( Primitive.foreground, Color.red );
	private static StyleSheet dividerStyleSheet = styleSheet.withAttr( Primitive.fontSize, 24 );

	private static StyleSheet scriptStyleSheet = styleSheet.withAttr( Primitive.fontSize, 16 ).withAttr( Primitive.foreground, Color.black );

	
	
	protected Pres makeScriptLine(Pres main, Pres leftSuper, Pres leftSub, Pres rightSuper, Pres rightSub)
	{
		return new HBox( new Pres[] { scriptPreStyleSheet.applyTo( new Text( "<<Left<<" ) ),
				scriptStyleSheet.applyTo( new Script( main, leftSuper, leftSub, rightSuper, rightSub ) ),
				scriptPostStyleSheet.applyTo( new Text( ">>Right>>" ) ) } );
	}

	
	
	protected Pres makeScriptFraction(String mainText, String numText, String denomText)
	{
		Pres fraction = new Fraction( new Text( numText ), new Text( denomText ), "/" );
		Pres script = new Script( new Text( mainText ), null, null, fraction, null );
		
		return new HBox( new Pres[] { scriptPreStyleSheet.applyTo( new Text( "Label A yYgGjJpPqQ" ) ),
				scriptStyleSheet.applyTo( script ),
				scriptPostStyleSheet.applyTo( new Text( "Label B yYgGjJpPqQ" ) ) } );
	}

	
	
	protected Pres createContents()
	{
		ArrayList<Object> children = new ArrayList<Object>();
		
		for (int i = 0; i < 16; i++)
		{
			Pres leftSuperText = ( i & 1 ) != 0   ?   new Text( "left super" )  :  null; 
			Pres leftSubText = ( i & 2 ) != 0   ?   new Text( "left sub" )  :  null; 
			Pres rightSuperText = ( i & 4 ) != 0   ?   new Text( "right super" )  :  null; 
			Pres rightSubText = ( i & 8 ) != 0   ?   new Text( "right sub" )  :  null;
			
			children.add( makeScriptLine( new Text( "MAIN" + String.valueOf( i ) ), leftSuperText, leftSubText, rightSuperText, rightSubText ) );
		}
		
		children.add( dividerStyleSheet.applyTo( new Text( "---" ) ) );

		for (int i = 0; i < 16; i++)
		{
			Pres leftSuperText = ( i & 1 ) != 0   ?   new Fraction( new Text( "a" ), new Text( "x" ), "/" )  :  new Text( "a" ); 
			Pres leftSubText = ( i & 2 ) != 0   ?   new Fraction( new Text( "b" ), new Text( "x" ), "/" )  :  new Text( "b" ); 
			Pres rightSuperText = ( i & 4 ) != 0   ?   new Fraction( new Text( "c" ), new Text( "x" ), "/" )  :  new Text( "c" ); 
			Pres rightSubText = ( i & 8 ) != 0   ?   new Fraction( new Text( "d" ), new Text( "x" ), "/" )  :  new Text( "d" );
			
			children.add( makeScriptLine( new Text( "MAIN" + String.valueOf( i ) ), leftSuperText, leftSubText, rightSuperText, rightSubText ) );
		}
		
		return new Body( children );
	}
}
