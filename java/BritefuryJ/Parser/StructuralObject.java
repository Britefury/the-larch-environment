//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * StructuralObject
 * 
 * StructuralObject:node( input )					->  objClass.isInstance( input )  ?  success  :  fail
 * StructuralObject:string( input, start )			->  fail
 * StructuralObject:richStr( input, start )			->  item = input.consumeStructuralItem(); objClass.isInstance( item )  ?  success  :  fail
 * StructuralObject:list( input, start )				->  iobjClass.isInstance( input[start] )  ?  success  :  fail
 */
public class StructuralObject extends ParserExpression
{
	protected Class<?> objClass;
	protected PyObject pyType;

	
	
	public StructuralObject()
	{
	}

	public StructuralObject(Class<?> objClass)
	{
		this.objClass = objClass;
	}

	public StructuralObject(PyObject pyType)
	{
		this.pyType = pyType;
	}

	
	
	private boolean testObject(Object x)
	{
		if ( pyType == null  &&  objClass == null )
		{
			return true;
		}
		
		if ( pyType != null  &&  x instanceof PyObject )
		{
			return Py.isInstance( (PyObject)x, pyType );
		}
		
		if ( objClass != null )
		{
			return objClass.isInstance( x );
		}
		
		return false;
	}
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( testObject( input ) )
		{
			return new ParseResult( input, 0, 1 );
		}
		

		return ParseResult.failure( 0 );
	}
	
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		if ( start < input.length() )
		{
			start = state.skipJunkChars( input, start );
			
			Object valueArray[] = input.matchStructuralNode( start );
			
			if ( valueArray != null )
			{
				if ( testObject( valueArray[0] ) )
				{
					return new ParseResult( valueArray[0], start, start + 1 );
				}
			}
		}
		
		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		if ( start < input.size() )
		{
			Object x = input.get( start );
			if ( testObject( x ) )
			{
				return new ParseResult( x, start, start + 1 );
			}
		}
		

		return ParseResult.failure( start );
	}

	
	
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof StructuralObject )
		{
			StructuralObject xo = (StructuralObject)x;
			
			return objClass.equals( xo.objClass );
		}
		else
		{
			return false;
		}
	}
	

	
	public String toString()
	{
		return "StructuralObject( " + objClass.getName() + " )";
	}
}
