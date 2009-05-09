//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;

import BritefuryJ.DocModel.DMObject;
import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import BritefuryJ.TreeParser.TreeParseResult.NameAlreadyBoundException;

public class ObjectMatch extends TreeParserExpression
{
	protected DMObjectClass objClass;
	protected String fieldNames[];
	protected TreeParserExpression fieldExps[], fieldExpTable[];

	
	
	public ObjectMatch(DMObjectClass objClass) throws InvalidFieldNameException
	{
		assert fieldNames.length == fieldExps.length;
		
		this.objClass = objClass;
		
		this.fieldNames = new String[0];
		this.fieldExps = new TreeParserExpression[0];

		initialise();
	}

	public ObjectMatch(DMObjectClass objClass, Object[] fieldExps) throws InvalidFieldNameException
	{
		assert fieldNames.length == fieldExps.length;
		
		this.objClass = objClass;
		
		ArrayList<String> fn = new ArrayList<String>();
		ArrayList<TreeParserExpression> fe= new ArrayList<TreeParserExpression>();
		
		for (int i = 0; i < fieldExps.length; i++)
		{
			if ( fieldExps[i] != null )
			{
				fn.add( objClass.getField( i ).getName() );
				fe.add( coerce( fieldExps[i] ) );
			}
		}
		
		this.fieldNames = new String[fn.size()];
		this.fieldExps = new TreeParserExpression[fe.size()];
		
		this.fieldNames = fn.toArray( this.fieldNames );
		this.fieldExps = fe.toArray( this.fieldExps );

		initialise();
	}

	public ObjectMatch(DMObjectClass objClass, String[] fieldNames, Object[] fieldExps) throws InvalidFieldNameException
	{
		assert fieldNames.length == fieldExps.length;
		
		this.objClass = objClass;
		this.fieldNames = fieldNames;
		this.fieldExps = new TreeParserExpression[fieldExps.length];
		for (int i = 0; i < fieldExps.length; i++)
		{
			this.fieldExps[i] = coerce( fieldExps[i] );
		}
		
		initialise();
	}
	
	public ObjectMatch(DMObjectClass objClass, String[] fieldNames, PyObject[] fieldExps) throws InvalidFieldNameException
	{
		assert fieldNames.length == fieldExps.length;
		
		this.objClass = objClass;
		this.fieldNames = fieldNames;
		this.fieldExps = new TreeParserExpression[fieldExps.length];
		for (int i = 0; i < fieldExps.length; i++)
		{
			this.fieldExps[i] = coerce( Py.tojava( fieldExps[i], Object.class ) );
		}
		
		initialise();
	}
	
	public ObjectMatch(PyObject[] values, String[] names) throws InvalidFieldNameException
	{
		assert values.length == ( names.length + 1 );
		
		objClass = Py.tojava( values[0], DMObjectClass.class );
		
		
		fieldNames = new String[names.length];
		System.arraycopy( names, 0, fieldNames, 0, names.length );

		fieldExps = new TreeParserExpression[names.length];
		for (int i = 0; i < names.length; i++)
		{
			fieldExps[i] = coerce( Py.tojava( values[i+1], Object.class ) );
		}
		
		
		initialise();
	}
	
	public ObjectMatch(DMObjectClass objClass, Map<String, Object> data) throws InvalidFieldNameException
	{
		this.objClass = objClass;
		fieldNames = new String[data.size()];
		fieldExps = new TreeParserExpression[data.size()];
		
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
	public ObjectMatch(DMObjectClass objClass, PyDictionary data) throws InvalidFieldNameException
	{
		this.objClass = objClass;
		this.fieldNames = new String[data.size()];
		this.fieldExps = new TreeParserExpression[data.size()];
		
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
	
	
	private TreeParseResult matchObjectContents(TreeParserState state, DMObject input)
	{
		if ( input.isInstanceOf( objClass ) )
		{
			DMObject value = input.getDMClass().newInstance();
			HashMap<String, Object> bindings = null;
			
			for (int i = 0; i < fieldExpTable.length; i++)
			{
				TreeParserExpression expr = fieldExpTable[i];
				if ( expr != null )
				{
					TreeParseResult result = expr.processNode( state, input.get( i ) );
					
					if ( !result.isValid() )
					{
						return TreeParseResult.failure( 0 );
					}
					else
					{
						try
						{
							bindings = result.addBindingsTo( bindings );
						}
						catch (NameAlreadyBoundException e)
						{
							return TreeParseResult.failure( 0 );
						}
	
						value.set( i, result.value );
					}
				}
				else
				{
					value.set( i, input.get( i ) );
				}
			}
			
			return new TreeParseResult( value, 0, 1, bindings );
		}
		
		return TreeParseResult.failure( 0 );
	}
	
	
	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		if ( input instanceof DMObject )
		{
			DMObject node = (DMObject)input;
			TreeParseResult res = matchObjectContents( state, node );
			if ( res.isValid() )
			{
				return res.withRange( 0, 1 );
			}
		}
		

		return TreeParseResult.failure( 0 );
	}

	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		if ( stop > start )
		{
			Object x = input.get( start );
			if ( x instanceof DMObject )
			{
				DMObject node = (DMObject)x;
				TreeParseResult res = matchObjectContents( state, node );
				if ( res.isValid() )
				{
					return res.withRange( start, start + 1 );
				}
			}
		}
		

		return TreeParseResult.failure( start );
	}
	
	
	
	public List<TreeParserExpression> getSubExpressions()
	{
		return Arrays.asList( fieldExps );
	}


	public List<TreeParserExpression> getChildren()
	{
		return Arrays.asList( fieldExps );
	}


	public boolean compareTo(TreeParserExpression x)
	{
		if ( x instanceof ObjectMatch )
		{
			ObjectMatch xo = (ObjectMatch)x;
			
			if ( fieldExps.length != xo.fieldExps.length )
			{
				return false;
			}
			
			for (int i = 0; i < fieldExps.length; i++)
			{
				if ( !fieldNames[i].equals( xo.fieldNames[i] )  ||  !fieldExps[i].compareTo( xo.fieldExps[i] ) )
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
	
	
	
	private void initialise() throws InvalidFieldNameException
	{
		fieldExpTable = new TreeParserExpression[objClass.getNumFields()];
		for (int i = 0; i < fieldNames.length; i++)
		{
			int fieldIndex = objClass.getFieldIndex( fieldNames[i] );
			if ( fieldIndex == -1 )
			{
				throw new DMObjectClass.InvalidFieldNameException( fieldNames[i] );
			}
			fieldExpTable[fieldIndex] = fieldExps[i];
		}
	}
}
