//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;

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
		for (int i = numToCopy; i < fieldData.length; i++)
		{
			fieldData[i] = null;
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
				throw new InvalidFieldNameException();
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
				throw new InvalidFieldNameException();
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
	
	
	
	public DMObjectClass getDMClass()
	{
		return objClass;
	}
	
	public int getFieldIndex(String key)
	{
		return objClass.getFieldIndex( key );
	}

	
	
	
	public Object get(int value)
	{
		Object[] fieldData = (Object[])cell.getLiteralValue();
		return fieldData[value];
	}
	
	public Object get(String key) throws InvalidFieldNameException
	{
		Object[] fieldData = (Object[])cell.getLiteralValue();
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
			throw new InvalidFieldNameException();
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
			throw new InvalidFieldNameException();
		}
		else
		{
			set( index, x );
		}
	}

	
	
	public String[] getFieldNames()
	{
		return objClass.getFieldNames();
	}
	
	public Object[] getFieldValuesImmutable()
	{
		Object[] fieldData = (Object[])cell.getLiteralValue();
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
				throw new InvalidFieldNameException();
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
		if ( commandTracker != null )
		{
			commandTracker.onUpdate( this, indices, oldContents, newContents );
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
	

	
	public void __setitem__(int fieldIndex, Object x)
	{
		set( fieldIndex, x );
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
		if ( x instanceof DMObject )
		{
			DMObject dx = (DMObject)x;
			if ( dx.objClass == objClass )
			{
				for (int i = 0; i < objClass.getNumFields(); i++)
				{
					if ( !get( i ).equals( dx.get( i ) ) )
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

	
	
	public CommandTrackerFactory getTrackerFactory()
	{
		return DMObjectTrackerFactory.factory;
	}

	public void setTracker(CommandTracker tracker)
	{
		commandTracker = (DMObjectCommandTracker)tracker;
	}
}
