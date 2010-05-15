//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

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
	private static PrimitiveStyleSheet scriptPreStyleSheet = styleSheet.withFontSize( 12 ).withForeground( Color.blue );
	private static PrimitiveStyleSheet scriptPostStyleSheet = styleSheet.withFontSize( 24 ).withForeground( Color.red );
	private static PrimitiveStyleSheet dividerStyleSheet = styleSheet.withFontSize( 24 );

	private static PrimitiveStyleSheet scriptStyleSheet = styleSheet.withFontSize( 16 ).withForeground( Color.black );

	
	private ElementFactory textFactory(final String text)
	{
		return new ElementFactory()
		{
			@Override
			public DPElement createElement(StyleSheet styleSheet)
			{
				return ((PrimitiveStyleSheet)styleSheet).text( text );
			}
		};
	}
	
	private ElementFactory fractionFactory(final ElementFactory num, final ElementFactory denom, final String barContent)
	{
		return new ElementFactory()
		{
			@Override
			public DPElement createElement(StyleSheet styleSheet)
			{
				PrimitiveStyleSheet numStyle = ((PrimitiveStyleSheet)styleSheet).fractionNumeratorStyle();
				PrimitiveStyleSheet denomStyle = ((PrimitiveStyleSheet)styleSheet).fractionDenominatorStyle();
				return ((PrimitiveStyleSheet)styleSheet).fraction( num.createElement( numStyle ), denom.createElement( denomStyle ) , barContent );
			}
		};
	}

	private ElementFactory scriptFactory(final ElementFactory main, final ElementFactory leftSuper, final ElementFactory leftSub, final ElementFactory rightSuper, final ElementFactory rightSub)
	{
		return new ElementFactory()
		{
			@Override
			public DPElement createElement(StyleSheet styleSheet)
			{
				PrimitiveStyleSheet childStyle = ((PrimitiveStyleSheet)styleSheet).scriptScriptChildStyle();
				return ((PrimitiveStyleSheet)styleSheet).script( main.createElement( styleSheet ),
						leftSuper != null  ?  leftSuper.createElement( childStyle )  :  null,
						leftSub != null  ?  leftSub.createElement( childStyle )  :  null,
						rightSuper != null  ?  rightSuper.createElement( childStyle )  :  null,
						rightSub != null  ?  rightSub.createElement( childStyle )  :  null
					);
			}
		};
	}

	
	
	protected DPElement makeScriptLine(ElementFactory main, ElementFactory leftSuper, ElementFactory leftSub, ElementFactory rightSuper, ElementFactory rightSub)
	{
		ElementFactory scriptFac = scriptFactory( main, leftSuper, leftSub, rightSuper, rightSub );
		return styleSheet.hbox( new DPElement[] { scriptPreStyleSheet.text( "<<Left<<" ),
				scriptFac.createElement( styleSheet ),
				scriptPostStyleSheet.text( ">>Right>>" ) } );
	}

	
	
	protected DPElement makeScriptFraction(String mainText, String numText, String denomText)
	{
		ElementFactory fractionFac = fractionFactory( textFactory( numText ), textFactory( denomText ), "/" );
		ElementFactory scriptFac = scriptFactory( textFactory( mainText ), null, null, fractionFac, null );
		
		return styleSheet.hbox( new DPElement[] { scriptPreStyleSheet.text( "Label A yYgGjJpPqQ" ), scriptFac.createElement( scriptStyleSheet ), scriptPostStyleSheet.text( "Label B yYgGjJpPqQ" ) } );
	}

	
	
	protected DPElement createContents()
	{
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		
		for (int i = 0; i < 16; i++)
		{
			ElementFactory leftSuperText = ( i & 1 ) != 0   ?   textFactory( "left super" )  :  null; 
			ElementFactory leftSubText = ( i & 2 ) != 0   ?   textFactory( "left sub" )  :  null; 
			ElementFactory rightSuperText = ( i & 4 ) != 0   ?   textFactory( "right super" )  :  null; 
			ElementFactory rightSubText = ( i & 8 ) != 0   ?   textFactory( "right sub" )  :  null;
			
			children.add( makeScriptLine( textFactory( "MAIN" + String.valueOf( i ) ), leftSuperText, leftSubText, rightSuperText, rightSubText ) );
		}
		
		children.add( dividerStyleSheet.text( "---" ) );

		for (int i = 0; i < 16; i++)
		{
			ElementFactory leftSuperText = ( i & 1 ) != 0   ?   fractionFactory( textFactory( "a" ), textFactory( "x" ), "/" )  :  textFactory( "a" ); 
			ElementFactory leftSubText = ( i & 2 ) != 0   ?   fractionFactory( textFactory( "b" ), textFactory( "x" ), "/" )  :  textFactory( "b" ); 
			ElementFactory rightSuperText = ( i & 4 ) != 0   ?   fractionFactory( textFactory( "c" ), textFactory( "x" ), "/" )  :  textFactory( "c" ); 
			ElementFactory rightSubText = ( i & 8 ) != 0   ?   fractionFactory( textFactory( "d" ), textFactory( "x" ), "/" )  :  textFactory( "d" );
			
			children.add( makeScriptLine( textFactory( "MAIN" + String.valueOf( i ) ), leftSuperText, leftSubText, rightSuperText, rightSubText ) );
		}
		
		return styleSheet.withVBoxSpacing( 10.0 ).vbox( children.toArray( new DPElement[0] ) );
	}
}
