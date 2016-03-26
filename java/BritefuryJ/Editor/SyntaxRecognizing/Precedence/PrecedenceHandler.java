//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing.Precedence;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ModelAccess.ModelReader;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleSheet;

public class PrecedenceHandler
{
	private ModelReader requiresBracketsReader, numBracketsReader, precedenceReader;
	private Pres openBracket, closeBracket;
	
	public PrecedenceHandler(ModelReader requiresBracketsReader, ModelReader numBracketsReader, ModelReader precedenceReader, Object openBracket, Object closeBracket)
	{
		this.requiresBracketsReader = requiresBracketsReader;
		this.numBracketsReader = numBracketsReader;
		this.precedenceReader = precedenceReader;
		this.openBracket = Pres.coerce( openBracket );
		this.closeBracket = Pres.coerce( closeBracket );
	}
	
	public PrecedenceHandler(ModelReader requiresBracketsReader, ModelReader numBracketsReader, ModelReader precedenceReader, String openBracket, String closeBracket, StyleSheet bracketStyle)
	{
		this( requiresBracketsReader, numBracketsReader, precedenceReader, bracketStyle.applyTo( new Text( openBracket ) ), bracketStyle.applyTo( new Text( closeBracket ) ) );
	}
	
	public PrecedenceHandler(ModelReader requiresBracketsReader, ModelReader numBracketsReader, ModelReader precedenceReader, StyleSheet bracketStyle)
	{
		this( requiresBracketsReader, numBracketsReader, precedenceReader, "(", ")", bracketStyle );
	}
	
	
	public Pres applyPrecedenceBrackets(Object model, Object view, SimpleAttributeTable inheritedState)
	{
		Object requiresBrackets = requiresBracketsReader.readFromModel( model );
		boolean bRequiresBrackets = ( requiresBrackets != null  &&  requiresBrackets instanceof Boolean )  ?  (Boolean)requiresBrackets  :  false;
		if ( bRequiresBrackets )
		{
			int numBrackets = (Integer)numBracketsReader.readFromModel( model );
			Object prec = precedenceReader.readFromModel( model );
			int precedence = prec != null  ?  (Integer)prec  :  -1;
			return PrecedenceBrackets.editorPrecedenceBrackets( view, precedence, numBrackets, inheritedState, openBracket, closeBracket );
		}
		else
		{
			return Pres.coerce( view );
		}
	}
}
