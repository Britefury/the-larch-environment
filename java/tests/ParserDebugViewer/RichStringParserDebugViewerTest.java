//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.ParserDebugViewer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import BritefuryJ.Parser.AnyNode;
import BritefuryJ.Parser.TracedParseResult;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Util.RichString.RichStringBuilder;

public class RichStringParserDebugViewerTest
{
	public static TracedParseResult richStringParseDebugResultTest() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		RichStringBuilder builder1 = new RichStringBuilder();
		builder1.appendTextValue( "this[" );
		builder1.appendTextValue( "i" );
		//builder1.appendStructuralValue( xs );
		builder1.appendTextValue( "][j].x.m()" );

		
		ParserExpression parser = buildParser();
		return parser.traceParseRichStringItems( builder1.richString() );
	}
	
	
	
	private static ParserExpression buildParser() throws ParserExpression.ParserCoerceException, Production.CannotOverwriteProductionExpressionException
	{
		ParseAction arrayAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( "arrayAccess", v.get( 0 ), v.get( 2 ) );
			}
		};
		
		ParseAction fieldAccessAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( "fieldAccess", v.get( 0 ), v.get( 2 ) );
			}
		};
		
		ParseAction objectMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( "methodInvoke", v.get( 0 ), v.get( 2 ) );
			}
		};
		
		ParseAction thisMethodInvocationAction = new ParseAction()
		{
			@SuppressWarnings("unchecked")
			public Object invoke(Object input, int begin, int end, Object value, Map<String, Object> bindings)
			{
				List<Object> v = (List<Object>)value;
				return Arrays.asList( "methodInvoke", v.get( 0 ) );
			}
		};
		
		Production primary = new Production( "primary" );
		
		ParserExpression expression = new Production( "expression", new Literal( "i" ).__or__( new Literal( "j" ) ).__or__( new AnyNode() ) );
		ParserExpression methodName = new Production( "methodName", new Literal( "m" ).__or__( new Literal( "n" ) ) );
		ParserExpression interfaceTypeName = new Production( "interfaceTypeName", new Literal( "I" ).__or__( new Literal( "J" ) ) );
		ParserExpression className = new Production( "className", new Literal( "C" ).__or__( new Literal( "D" ) ) );

		ParserExpression classOrInterfaceType = new Production( "classOrInterfaceType", className.__or__( interfaceTypeName ) );

		ParserExpression identifier = new Production( "identifier", new Literal( "x" ).__or__( new Literal( "y" ).__or__( classOrInterfaceType ) ) );
		ParserExpression expressionName = new Production( "expressionName", identifier );

		ParserExpression arrayAccess = new Production( "arrayAccess", ( primary.__add__( "[" ).__add__( expression ).__add__( "]" ) ).action( arrayAccessAction ).__or__(
											( expressionName.__add__( "[" ).__add__( expression ).__add__( "]" ) ).action( arrayAccessAction ) ) );
		ParserExpression fieldAccess = new Production( "fieldAccess", ( primary.__add__( "." ).__add__( identifier ) ).action( fieldAccessAction ).__or__(
				( new Literal( "super" ).__add__( "." ).__add__( identifier ) ).action( fieldAccessAction ) ) );
		ParserExpression methodInvocation = new Production( "methodInvocation", ( primary.__add__( "." ).__add__( methodName ).__add__( "()" ) ).action( objectMethodInvocationAction ).__or__(
				( methodName.__add__( "()" ) ).action( thisMethodInvocationAction ) ) );
		
		ParserExpression classInstanceCreationExpression = new Production( "classInstanceCreationExpression", ( new Literal( "new" ).__add__( classOrInterfaceType ).__add__( "()" ) ).__or__(
				primary.__add__( "." ).__add__( "new" ).__add__( identifier ).__add__( "()" ) ) );
		
		ParserExpression fieldAccessOrArrayAccess = new Production( "fieldAccessOrArrayAccess", fieldAccess.__or__( arrayAccess ) );
		ParserExpression primaryNoNewArray = new Production( "primaryNoNewArray", classInstanceCreationExpression.__or__( methodInvocation ).__or__( fieldAccessOrArrayAccess ).__or__( "this" ) );
		primary.setExpression( primaryNoNewArray );
		
		return primary;
	}
}
