//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;

import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.CommandTrackerFactory;
import BritefuryJ.CommandHistory.Trackable;
import BritefuryJ.DocModel.DMModule.UnknownClassException;
import BritefuryJ.DocModel.DMModuleResolver.CouldNotResolveModuleException;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import BritefuryJ.Incremental.IncrementalOwner;
import BritefuryJ.Incremental.IncrementalValue;

public class DMObject extends DMNode implements DMObjectInterface, Trackable, Serializable, IncrementalOwner
{
	public static class NotADMObjectStreamClassException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	};
	
	
	private static final long serialVersionUID = 1L;

	
	private IncrementalValue incr;
	private DMObjectClass objClass;
	private Object fieldData[];
	private DMObjectCommandTracker commandTracker;
	
	
	
	
	public DMObject(DMObjectClass objClass)
	{
		incr = new IncrementalValue( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		commandTracker = null;
	}
	
	public DMObject(DMObjectClass objClass, Object values[])
	{
		incr = new IncrementalValue( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
		int numToCopy = Math.min( values.length, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			Object x = coerce( values[i] );
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
			fieldData[i] = coerce( x );
		}

		commandTracker = null;
	}
	
	public DMObject(DMObjectClass objClass, PyObject values[])
	{
		incr = new IncrementalValue( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
		int numToCopy = Math.min( values.length, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			Object x = coerce( Py.tojava( values[i], Object.class ) );
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
			fieldData[i] = x;
		}

		commandTracker = null;
	}
	
	public DMObject(DMObjectClass objClass, String[] keys, Object[] values) throws InvalidFieldNameException
	{
		assert keys.length == values.length;
		
		incr = new IncrementalValue( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
	
		for (int i = 0; i < keys.length; i++)
		{
			int index = objClass.getFieldIndex( keys[i] );
			if ( index == -1 )
			{
				throw new InvalidFieldNameException( keys[i] );
			}
			else
			{
				Object x = coerce( values[i] );
				if ( x instanceof DMNode )
				{
					((DMNode)x).addParent( this );
				}
				fieldData[index] = x;
			}
		}

		commandTracker = null;
	}

	public DMObject(DMObjectClass objClass, String[] names, PyObject[] values)
	{
		assert names.length == values.length;
		
		incr = new IncrementalValue( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
	
		for (int i = 0; i < names.length; i++)
		{
			int index = objClass.getFieldIndex( names[i] );
			if ( index == -1 )
			{
				throw Py.KeyError( names[i] );
			}
			else
			{
				Object x = coerce( Py.tojava( values[i], Object.class ) );
				if ( x instanceof DMNode )
				{
					((DMNode)x).addParent( this );
				}
				fieldData[index] = x;
			}
		}

		commandTracker = null;
	}
	
	public DMObject(DMObjectInterface obj)
	{
		incr = new IncrementalValue( this );
		this.objClass = obj.getDMClass();
		fieldData = new Object[objClass.getNumFields()];
		
		for (int i = 0; i < fieldData.length; i++)
		{
			Object x = coerce( obj.get( i ) );
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
			fieldData[i] = x;
		}

		commandTracker = null;
	}

	public DMObject(PyObject values[])
	{
		incr = new IncrementalValue( this );
		objClass = Py.tojava( values[0], DMObjectClass.class );
		fieldData = new Object[objClass.getNumFields()];
		
		int numToCopy = Math.min( values.length - 1, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			Object x = coerce( Py.tojava( values[i+1], Object.class ) );
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
			fieldData[i] = x;
		}

		commandTracker = null;
	}

	public DMObject(PyObject values[], String names[])
	{
		assert values.length == ( names.length + 1 );
		
		incr = new IncrementalValue( this );
		objClass = Py.tojava( values[0], DMObjectClass.class );
		fieldData = new Object[objClass.getNumFields()];
		
		for (int i = 0; i < names.length; i++)
		{
			int index = objClass.getFieldIndex( names[i] );
			if ( index == -1 )
			{
				throw Py.KeyError( names[i] );
			}
			else
			{
				Object x = coerce( Py.tojava( values[i+1], Object.class ) );
				if ( x instanceof DMNode )
				{
					((DMNode)x).addParent( this );
				}
				fieldData[index] = x;
			}
		}

		commandTracker = null;
	}

	public DMObject(DMObjectClass objClass, Map<String, Object> data) throws InvalidFieldNameException
	{
		incr = new IncrementalValue( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
		for (Map.Entry<String, Object> entry: data.entrySet())
		{
			int index = objClass.getFieldIndex( entry.getKey() );
			if ( index == -1 )
			{
				throw new InvalidFieldNameException( entry.getKey() );
			}
			else
			{
				Object x = coerce( entry.getValue() );
				if ( x instanceof DMNode )
				{
					((DMNode)x).addParent( this );
				}
				fieldData[index] = x;
			}
		}

		commandTracker = null;
	}
	
	@SuppressWarnings("unchecked")
	public DMObject(DMObjectClass objClass, PyDictionary data) throws InvalidFieldNameException
	{
		incr = new IncrementalValue( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
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
				Object x = coerce( Py.tojava( (PyObject)entry.getValue(), Object.class ) );
				if ( x instanceof DMNode )
				{
					((DMNode)x).addParent( this );
				}
				fieldData[index] = x;
			}
		}

		commandTracker = null;
	}
	
	
	
	protected Object createDeepCopy(Map<Object, Object> memo)
	{
		onAccess();
		Object[] ys = new Object[fieldData.length];
		
		int i = 0;
		for (Object x: fieldData)
		{
			if ( x instanceof DMNode )
			{
				ys[i++] = ((DMNode)x).deepCopy( memo );
			}
			else
			{
				ys[i++] = x;
			}
		}
		
		return new DMObject( objClass, ys );
	}
	

	
	public DMObjectClass getDMClass()
	{
		// Get the cell value, so that the access is tracked
		onAccess();
		return objClass;
	}
	
	public boolean isInstanceOf(DMObjectClass cls)
	{
		// Get the cell value, so that the access is tracked
		onAccess();
		return objClass.isSubclassOf( cls );
	}

	public int getFieldIndex(String key)
	{
		// Get the cell value, so that the access is tracked
		onAccess();
		return objClass.getFieldIndex( key );
	}

	
	
	public int indexOfById(Object x)
	{
		onAccess();
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
		onAccess();
		return fieldData[value];
	}
	
	public Object get(String key) throws InvalidFieldNameException
	{
		onAccess();
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
		x = coerce( x );
		Object oldX = fieldData[index];
		fieldData[index] = x;
		if ( oldX != x )
		{
			if ( oldX instanceof DMNode )
			{
				((DMNode)oldX).removeParent( this );
			}
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		incr.onChanged();
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
		onAccess();
		return objClass.getFieldNames();
	}
	
	public Object[] getFieldValuesImmutable()
	{
		onAccess();
		return fieldData;
	}
	
	
	
	public void update(Map<String, Object> table) throws InvalidFieldNameException
	{
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
				Object oldX = fieldData[index];
				Object x = coerce( e.getValue() );
				if ( oldX != x )
				{
					if ( oldX instanceof DMNode )
					{
						((DMNode)oldX).removeParent( this );
					}
					if ( x instanceof DMNode )
					{
						((DMNode)x).addParent( this );
					}
				}
				indices[i] = index;
				oldContents[i] = oldX;
				newContents[i] = x;
				fieldData[index] = x;
				i++;
			}
		}
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onUpdate( this, indices, oldContents, newContents );
		}
	}

	protected void update(int indices[], Object xs[])
	{
		assert indices.length == xs.length;
		
		Object oldContents[] = new Object[indices.length];
		Object newContents[] = new Object[indices.length];
		for (int i = 0; i < indices.length; i++)
		{
			int index = indices[i];
			Object oldX = fieldData[index];
			Object x = coerce( xs[i] );
			if ( oldX != x )
			{
				if ( oldX instanceof DMNode )
				{
					((DMNode)oldX).removeParent( this );
				}
				if ( x instanceof DMNode )
				{
					((DMNode)x).addParent( this );
				}
			}
			oldContents[i] = fieldData[index];
			newContents[i] = x;
			fieldData[index] = x;
		}
		incr.onChanged();
		if ( commandTracker != null )
		{
			commandTracker.onUpdate( this, indices, oldContents, newContents );
		}
	}

	
	
	public void become(DMObject obj)
	{
		obj.onAccess();
		become( obj.objClass, obj.fieldData );
	}

	protected void become(DMObjectClass cls, Object[] data)
	{
		Object oldFieldData[] = fieldData;
		fieldData = new Object[data.length];
		System.arraycopy( data, 0, fieldData, 0, data.length );
		for (Object x: oldFieldData)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).removeParent( this );
			}
		}
		for (Object x: fieldData)
		{
			if ( x instanceof DMNode )
			{
				((DMNode)x).addParent( this );
			}
		}
		DMObjectClass oldClass = objClass;
		objClass = cls;
		incr.onChanged();
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
			onAccess();
			DMObjectInterface dx = (DMObjectInterface)x;
			if ( dx.getDMClass() == objClass )
			{
				for (int i = 0; i < objClass.getNumFields(); i++)
				{
					Object v = get( i );
					Object xv = dx.get( i );
					if ( v == null  ||  xv == null )
					{
						if ( ( v != null )  !=  ( xv != null ) )
						{
							return false;
						}
					}
					else
					{
						if ( !v.equals( xv ) )
						{
							return false;
						}
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	
	
	//
	// Incremental computation
	//
	
	private void onAccess()
	{
		Object refreshState = incr.onRefreshBegin();
		incr.onRefreshEnd( refreshState );
		incr.onAccess();
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
				builder.append( " " );
				builder.append( objClass.getField( i ).getName() );
				builder.append( "=" );
				builder.append( x != null  ?  x.toString()  :  "<null>" );
			}
			builder.append( ")" );
			
			return builder.toString();
		}
		else
		{
			return "(" + objClass.getName() + " :)";
		}
	}




	//
	// Serialisation
	//
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException, CouldNotResolveModuleException, UnknownClassException, InvalidFieldNameException
	{
		if ( stream instanceof DMObjectInputStream )
		{
			DMObjectClass objClass = ((DMObjectInputStream)stream).readDMObjectClass();

			String keys[] = (String[])stream.readObject();
			Object values[] = (Object[])stream.readObject();
			
			
			assert keys.length == values.length;
			
			this.objClass = objClass;
			Object fieldData[] = new Object[objClass.getNumFields()];
		
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
			
			incr = new IncrementalValue( this );
			incr.onChanged();
			commandTracker = null;
		}
		else
		{
			throw new NotADMObjectStreamClassException();
		}
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		if ( stream instanceof DMObjectOutputStream )
		{
			((DMObjectOutputStream)stream).writeDMObjectClass( objClass );
			
			String fieldNames[] = objClass.getFieldNames();
			
			stream.writeObject( fieldNames );
			stream.writeObject( fieldData );
		}
		else
		{
			throw new NotADMObjectStreamClassException();
		}
	}
}
