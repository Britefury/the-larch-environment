//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Fraction;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Script;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class ScriptTestPage extends TestPage
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
	private static StyleSheet scriptPreStyleSheet = styleSheet.withValues( Primitive.fontSize.as( 12 ), Primitive.foreground.as( Color.blue ) );
	private static StyleSheet scriptPostStyleSheet = styleSheet.withValues( Primitive.fontSize.as( 24 ), Primitive.foreground.as( Color.red ) );
	private static StyleSheet dividerStyleSheet = styleSheet.withValues( Primitive.fontSize.as( 24 ) );

	private static StyleSheet scriptStyleSheet = styleSheet.withValues( Primitive.fontSize.as( 16 ), Primitive.foreground.as( Color.black ) );

	
	
	protected Pres makeScriptLine(Pres main, Pres leftSuper, Pres leftSub, Pres rightSuper, Pres rightSub)
	{
		return new Row( new Pres[] { scriptPreStyleSheet.applyTo( new Text( "<<Left<<" ) ),
				scriptStyleSheet.applyTo( new Script( main, leftSuper, leftSub, rightSuper, rightSub ) ),
				scriptPostStyleSheet.applyTo( new Text( ">>Right>>" ) ) } );
	}

	
	
	protected Pres makeScriptFraction(String mainText, String numText, String denomText)
	{
		Pres fraction = new Fraction( new Text( numText ), new Text( denomText ), "/" );
		Pres script = new Script( new Text( mainText ), null, null, fraction, null );
		
		return new Row( new Pres[] { scriptPreStyleSheet.applyTo( new Text( "Label A yYgGjJpPqQ" ) ),
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
