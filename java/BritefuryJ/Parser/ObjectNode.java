//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;

import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * ObjectNode
 * 
 * ObjectNode:node( input )				->  input instanceof DMObject  ?  ObjectNode.matchObjectContents( input )  :  fail
 * ObjectNode:string( input, start )			->  fail
 * ObjectNode:richStr( input, start )			->  item = input.consumeStructuralItem(); item instanceof DMObject  ?  ObjectNode.matchObjectContents( input )  :  fail
 * ObjectNode:list( input, start )				->  input[start] instanceof DMObject  ?  ObjectNode.matchObjectContents( input[start] )  :  fail
 * 
 * ObjectNode.matchObjectContents( input )	->  [ result[fieldName] = s:node( fieldValue )   for fieldName, fieldValue in fieldSubexps ]
 */
public class ObjectNode extends ParserExpression
{
	protected DMObjectClass objClass;
	protected String fieldNames[];
	protected ParserExpression fieldExps[], fieldExpTable[];

	
	
	public ObjectNode(DMObjectClass objClass)
	{
		this.objClass = objClass;
		
		this.fieldNames = new String[0];
		this.fieldExps = new ParserExpression[0];

		initialise();
	}

	public ObjectNode(DMObjectClass objClass, Object[] fieldExps) throws ParserCoerceException
	{
		assert objClass.getNumFields() == fieldExps.length;
		
		this.objClass = objClass;
		
		ArrayList<String> fn = new ArrayList<String>();
		ArrayList<ParserExpression> fe= new ArrayList<ParserExpression>();
		
		for (int i = 0; i < fieldExps.length; i++)
		{
			if ( fieldExps[i] != null )
			{
				fn.add( objClass.getField( i ).getName() );
				fe.add( coerce( fieldExps[i] ) );
			}
		}
		
		this.fieldNames = fn.toArray( new String[fn.size()] );
		this.fieldExps = fe.toArray( new ParserExpression[fe.size()] );

		initialise();
	}

	public ObjectNode(DMObjectClass objClass, String[] fieldNames, Object[] fieldExps) throws ParserCoerceException
	{
		assert fieldNames.length == fieldExps.length;
		
		this.objClass = objClass;
		this.fieldNames = fieldNames;
		this.fieldExps = new ParserExpression[fieldExps.length];
		for (int i = 0; i < fieldExps.length; i++)
		{
			this.fieldExps[i] = coerce( fieldExps[i] );
		}
		
		initialise();
	}
	
	public ObjectNode(DMObjectClass objClass, String[] fieldNames, PyObject[] fieldExps) throws ParserCoerceException
	{
		assert fieldNames.length == fieldExps.length;
		
		this.objClass = objClass;
		this.fieldNames = fieldNames;
		this.fieldExps = new ParserExpression[fieldExps.length];
		for (int i = 0; i < fieldExps.length; i++)
		{
			this.fieldExps[i] = coerce( Py.tojava( fieldExps[i], Object.class ) );
		}
		
		initialise();
	}
	
	public ObjectNode(PyObject[] values, String[] names) throws ParserCoerceException
	{
		assert values.length == ( names.length + 1 );
		
		objClass = Py.tojava( values[0], DMObjectClass.class );
		
		
		fieldNames = new String[names.length];
		System.arraycopy( names, 0, fieldNames, 0, names.length );

		fieldExps = new ParserExpression[names.length];
		for (int i = 0; i < names.length; i++)
		{
			fieldExps[i] = coerce( Py.tojava( values[i+1], Object.class ) );
		}
		
		
		initialise();
	}
	
	public ObjectNode(DMObjectClass objClass, Map<String, Object> data) throws ParserCoerceException
	{
		this.objClass = objClass;
		fieldNames = new String[data.size()];
		fieldExps = new ParserExpression[data.size()];
		
		int i = 0;
		for (Map.Entry<String, Object> e: data.entrySet())
		{
			fieldNames[i] = e.getKey();
			fieldExps[i] = coerce( e.getValue() );
			i++;
		}
		
		initialise();
	}
	
	
	@SuppressWarnings("unchecked")
	public ObjectNode(DMObjectClass objClass, PyDictionary data) throws ParserCoerceException
	{
		this.objClass = objClass;
		this.fieldNames = new String[data.size()];
		this.fieldExps = new ParserExpression[data.size()];
		
		int i = 0;
		for (Object e: data.entrySet())
		{
			Map.Entry<Object,Object> entry = (Map.Entry<Object,Object>)e;
			Object k = entry.getKey();
		
			if ( k instanceof PyString  ||  k instanceof PyUnicode )
			{
				fieldNames[i] = k.toString();
			}
			else
			{
				throw Py.TypeError( "All keys must be of type string" );
			}
		
			fieldExps[i] = coerce( Py.tojava( (PyObject)entry.getValue(), Object.class ) );
			i++;
		}
		
		initialise();
	}
	
	
	
	
	private ParseResult matchObjectContents(ParserState state, DMObject input)
	{
		if ( input.isInstanceOf( objClass ) )
		{
			if ( fieldExpTable != null )
			{
				DMObjectClass inputClass = input.getDMObjectClass();
				DMObject value = inputClass.newInstance();
				Map<String, Object> bindings = null;
				boolean bModified = false;
				
				for (int i = 0; i < fieldExpTable.length; i++)
				{
					ParserExpression expr = fieldExpTable[i];
					if ( expr != null )
					{
						ParseResult result = expr.handleNode( state, input.get( i ) );
						
						if ( !result.isValid() )
						{
							return ParseResult.failure( 0 );
						}
						else
						{
							bindings = ParseResult.addBindings( bindings, result.getBindings() );
							
							bModified |= result.value != input.get( i );
		
							value.set( i, result.value );
						}
					}
					else
					{
						value.set( i, input.get( i ) );
					}
				}
				
				if ( bModified )
				{
					int clsSize = inputClass.getNumFields();			
					for (int i = fieldExpTable.length; i < clsSize; i++)
					{
						value.set( i, input.get( i ) );
					}
					
					return new ParseResult( value, 0, 1, bindings );
				}
				else
				{
					return new ParseResult( input, 0, 1, bindings );
				}
			}
			else
			{
				return new ParseResult( input, 0, 1, null );
			}
		}
		
		return ParseResult.failure( 0 );
	}
	
	
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( input instanceof DMObject )
		{
			DMObject node = (DMObject)input;
			ParseResult res = matchObjectContents( state, node );
			if ( res.isValid() )
			{
				return res.withRange( 0, 1 );
			}
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
				if ( valueArray[0] instanceof DMObject )
				{
					DMObject node = (DMObject)valueArray[0];
					ParseResult res = matchObjectContents( state, node );
					if ( res.isValid() )
					{
						return res.withRange( start, start + 1 );
					}
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
			if ( x instanceof DMObject )
			{
				DMObject node = (DMObject)x;
				ParseResult res = matchObjectContents( state, node );
				if ( res.isValid() )
				{
					return res.withRange( start, start + 1 );
				}
			}
		}
		

		return ParseResult.failure( start );
	}

	
	
	
	public List<ParserExpression> getSubExpressions()
	{
		return Arrays.asList( fieldExps );
	}


	public List<ParserExpression> getChildren()
	{
		return Arrays.asList( fieldExps );
	}


	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof ObjectNode )
		{
			ObjectNode xo = (ObjectNode)x;
			
			if ( fieldExps.length != xo.fieldExps.length )
			{
				return false;
			}
			
			for (int i = 0; i < fieldExps.length; i++)
			{
				if ( !fieldNames[i].equals( xo.fieldNames[i] )  ||  !fieldExps[i].isEquivalentTo( xo.fieldExps[i] ) )
				{
					return false;
				}
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	

	
	public String toString()
	{
		StringBuilder f = new StringBuilder();
		for (int i = 0; i < fieldExps.length; i++)
		{
			if ( i != 0 )
			{
				f.append( " " );
			}
			f.append( fieldNames[i] );
			f.append( "=" );
			f.append( fieldExps[i].toString() );
		}
		return "ObjectNode( " + objClass.getName() + "  :  " + f.toString() + " )";
	}
	
	
	
	private void initialise()
	{
		if ( fieldNames.length > 0 )
		{
			fieldExpTable = new ParserExpression[objClass.getNumFields()];
			for (int i = 0; i < fieldNames.length; i++)
			{
				int fieldIndex = objClass.getFieldIndex( fieldNames[i] );
				if ( fieldIndex == -1 )
				{
					throw new DMObjectClass.UnknownFieldNameException( fieldNames[i] );
				}
				fieldExpTable[fieldIndex] = fieldExps[i];
			}
		}
		else
		{
			fieldExpTable = null;
		}
	}
}
