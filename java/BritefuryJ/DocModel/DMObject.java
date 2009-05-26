//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.Map;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.CommandTrackerFactory;
import BritefuryJ.CommandHistory.Trackable;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;

public class DMObject extends DMNode implements DMObjectInterface, Trackable
{
	private DMObjectClass objClass;
	private LiteralCell cell;
	private DMObjectCommandTracker commandTracker;
	
	
	
	
	public DMObject(DMObjectClass objClass)
	{
		this.objClass = objClass;

		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
		
		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}
	
	public DMObject(DMObjectClass objClass, Object values[])
	{
		this.objClass = objClass;
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
		
		int numToCopy = Math.min( values.length, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			fieldData[i] = coerce( values[i] );
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}
	
	public DMObject(DMObjectClass objClass, PyObject values[])
	{
		this.objClass = objClass;
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
		
		int numToCopy = Math.min( values.length, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			fieldData[i] = coerce( Py.tojava( values[i], Object.class ) );
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}
	
	public DMObject(DMObjectClass objClass, String[] keys, Object[] values) throws InvalidFieldNameException
	{
		assert keys.length == values.length;
		
		this.objClass = objClass;
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
	
		for (int i = 0; i < keys.length; i++)
		{
			int index = objClass.getFieldIndex( keys[i] );
			if ( index == -1 )
			{
				throw new InvalidFieldNameException( keys[i] );
			}
			else
			{
				fieldData[index] = coerce( values[i] );
			}
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}

	public DMObject(DMObjectClass objClass, String[] names, PyObject[] values)
	{
		assert names.length == values.length;
		
		this.objClass = objClass;
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
	
		for (int i = 0; i < names.length; i++)
		{
			int index = objClass.getFieldIndex( names[i] );
			if ( index == -1 )
			{
				throw Py.KeyError( names[i] );
			}
			else
			{
				fieldData[index] = coerce( Py.tojava( values[i], Object.class ) );
			}
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}
	
	public DMObject(DMObjectInterface obj)
	{
		this.objClass = obj.getDMClass();
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
		
		for (int i = 0; i < fieldData.length; i++)
		{
			fieldData[i] = coerce( obj.get( i ) );
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}

	public DMObject(PyObject values[])
	{
		objClass = Py.tojava( values[0], DMObjectClass.class );
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
		
		int numToCopy = Math.min( values.length - 1, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			fieldData[i] = coerce( Py.tojava( values[i+1], Object.class ) );
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}

	public DMObject(PyObject values[], String names[])
	{
		assert values.length == ( names.length + 1 );
		
		objClass = Py.tojava( values[0], DMObjectClass.class );
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
		
		for (int i = 0; i < names.length; i++)
		{
			int index = objClass.getFieldIndex( names[i] );
			if ( index == -1 )
			{
				throw Py.KeyError( names[i] );
			}
			else
			{
				fieldData[index] = coerce( Py.tojava( values[i+1], Object.class ) );
			}
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}

	public DMObject(DMObjectClass objClass, Map<String, Object> data) throws InvalidFieldNameException
	{
		this.objClass = objClass;
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
		
		for (Map.Entry<String, Object> entry: data.entrySet())
		{
			int index = objClass.getFieldIndex( entry.getKey() );
			if ( index == -1 )
			{
				throw new InvalidFieldNameException( entry.getKey() );
			}
			else
			{
				fieldData[index] = coerce( entry.getValue() );
			}
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}
	
	@SuppressWarnings("unchecked")
	public DMObject(DMObjectClass objClass, PyDictionary data) throws InvalidFieldNameException
	{
		this.objClass = objClass;
		Object fieldData[] = new Object[objClass.getNumFields()];
		fillArrayWithNulls( fieldData );
		
		for (Object e: data.entrySet())
		{
			Map.Entry<Object,Object> entry = (Map.Entry<Object,Object>)e;
			Object k = entry.getKey();
			String key;
		
			if ( k instanceof PyString  ||  k instanceof PyUnicode )
			{
				key = k.toString();
			}
			else
			{
				throw Py.TypeError( "All keys must be of type string" );
			}
		
			int index = objClass.getFieldIndex( key );
			if ( index == -1 )
			{
				throw new InvalidFieldNameException( key );
			}
			else
			{
				fieldData[index] = coerce( Py.tojava( (PyObject)entry.getValue(), Object.class ) );
			}
		}

		cell = new LiteralCell();
		cell.setLiteralValue( fieldData );
		commandTracker = null;
	}
	
	
	
	public DMObjectClass getDMClass()
	{
		// Get the cell value, so that the access is tracked
		cell.getValue();
		return objClass;
	}
	
	public boolean isInstanceOf(DMObjectClass cls)
	{
		// Get the cell value, so that the access is tracked
		cell.getValue();
		return objClass.isSubclassOf( cls );
	}

	public int getFieldIndex(String key)
	{
		// Get the cell value, so that the access is tracked
		cell.getValue();
		return objClass.getFieldIndex( key );
	}

	
	
	public int indexOfById(Object x)
	{
		Object[] fieldData = (Object[])cell.getValue();
		for (int i = 0; i < fieldData.length; i++)
		{
			if ( fieldData[i] == x )
			{
				return i;
			}
		}
		return -1;
	}

	public Object get(int value)
	{
		Object[] fieldData = (Object[])cell.getValue();
		return fieldData[value];
	}
	
	public Object get(String key) throws InvalidFieldNameException
	{
		Object[] fieldData = (Object[])cell.getValue();
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
			throw new InvalidFieldNameException( key );
		}
		else
		{
			return fieldData[index];
		}
	}
	
	
	public void set(int index, Object x)
	{
		Object[] fieldData = (Object[])cell.getLiteralValue();
		x = coerce( x );
		Object oldX = fieldData[index];
		fieldData[index] = x;
		cell.setLiteralValue( fieldData );
		if ( commandTracker != null )
		{
			commandTracker.onSet( this, index, oldX, x );
		}
	}
	
	public void set(String key, Object x) throws InvalidFieldNameException
	{
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
			throw new InvalidFieldNameException( key );
		}
		else
		{
			set( index, x );
		}
	}

	
	
	public String[] getFieldNames()
	{
		// Get the cell value, so that the access is tracked
		cell.getValue();
		return objClass.getFieldNames();
	}
	
	public Object[] getFieldValuesImmutable()
	{
		Object[] fieldData = (Object[])cell.getValue();
		return fieldData;
	}
	
	
	
	public void update(Map<String, Object> table) throws InvalidFieldNameException
	{
		Object[] fieldData = (Object[])cell.getLiteralValue();
		int indices[] = new int[table.size()];
		Object oldContents[] = new Object[table.size()];
		Object newContents[] = new Object[table.size()];
		int i = 0;
		for (Map.Entry<String, Object> e: table.entrySet())
		{
			int index = objClass.getFieldIndex( e.getKey() );
			if ( index == -1 )
			{
				throw new InvalidFieldNameException( e.getKey() );
			}
			else
			{
				Object x = coerce( e.getValue() );
				indices[i] = index;
				oldContents[i] = fieldData[index];
				newContents[i] = x;
				fieldData[index] = x;
				i++;
			}
		}
		cell.setLiteralValue( fieldData );
		if ( commandTracker != null )
		{
			commandTracker.onUpdate( this, indices, oldContents, newContents );
		}
	}

	protected void update(int indices[], Object xs[])
	{
		assert indices.length == xs.length;
		
		Object[] fieldData = (Object[])cell.getLiteralValue();
		Object oldContents[] = new Object[indices.length];
		Object newContents[] = new Object[indices.length];
		for (int i = 0; i < indices.length; i++)
		{
			int index = indices[i];
			oldContents[i] = fieldData[index];
			Object x = coerce( xs[i] );
			newContents[i] = x;
			fieldData[index] = x;
		}
		cell.setLiteralValue( fieldData );
		if ( commandTracker != null )
		{
			commandTracker.onUpdate( this, indices, oldContents, newContents );
		}
	}

	
	
	public void become(DMObject obj)
	{
		become( obj.objClass, (Object[])obj.cell.getLiteralValue() );
	}

	protected void become(DMObjectClass cls, Object[] data)
	{
		Object oldFieldData[] = (Object[])cell.getLiteralValue();
		Object fieldData[] = new Object[data.length];
		System.arraycopy( data, 0, fieldData, 0, data.length );
		DMObjectClass oldClass = objClass;
		objClass = cls;
		cell.setLiteralValue( fieldData );
		if ( commandTracker != null )
		{
			commandTracker.onBecome( this, oldClass, oldFieldData, cls, data );
		}
	}

	
	
	
	
	public Object __getitem__(int fieldIndex)
	{
		return get( fieldIndex );
	}
	
	public Object __getitem__(String key)
	{
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
		        throw Py.KeyError( key );
		}
		else
		{
			return get( index );
		}
	}
	

	
	public void __setitem__(int index, Object x)
	{
		set( index, x );
	}
	
	public void __setitem__(String key, Object x)
	{
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
		        throw Py.KeyError( key );
		}
		else
		{
			set( index, x );
		}
	}
	
	
	
	public boolean equals(Object x)
	{
		if ( this == x )
		{
			return true;
		}
		
		if ( x instanceof DMObjectInterface )
		{
			// Get the cell value, so that the access is tracked
			cell.getValue();
			DMObjectInterface dx = (DMObjectInterface)x;
			if ( dx.getDMClass() == objClass )
			{
				for (int i = 0; i < objClass.getNumFields(); i++)
				{
					Object v = get( i );
					Object xv = dx.get( i );
					if ( !v.equals( xv ) )
					{
						return false;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	
	
	private void fillArrayWithNulls(Object[] xs)
	{
		for (int i = 0; i < xs.length; i++)
		{
			xs[i] = newNull();
		}
	}
	
	
	
	//
	// Trackable interface
	//
	
	public CommandTrackerFactory getTrackerFactory()
	{
		return DMObjectTrackerFactory.factory;
	}

	public void setTracker(CommandTracker tracker)
	{
		commandTracker = (DMObjectCommandTracker)tracker;
	}
	
	
	
	
	//
	// toString()
	//
	
	public String toString()
	{
		if ( objClass.getNumFields() > 0 )
		{
			StringBuilder builder = new StringBuilder();
			builder.append( "(" );
			builder.append( objClass.getName() );
			builder.append( " :" );
			
			Object[] d = getFieldValuesImmutable();
			
			for (int i = 0; i < d.length; i++)
			{
				Object x = d[i];
				if ( x != null  &&  !isNull( x ) )
				{
					builder.append( " " );
					builder.append( objClass.getField( i ).getName() );
					builder.append( "=" );
					builder.append( x.toString() );
				}
			}
			builder.append( ")" );
			
			return builder.toString();
		}
		else
		{
			return "(" + objClass.getName() + " :)";
		}
	}
}
