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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocModel.DMObjectClass.UnknownFieldNameException;
import BritefuryJ.Incremental.IncrementalValueMonitor;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Util.HashUtils;

public class DMObject extends DMNode implements DMObjectInterface, Trackable, Presentable
{
	private static final long serialVersionUID = 1L;

	
	private IncrementalValueMonitor incr;
	private DMObjectClass objClass;
	private Object fieldData[];
	private ChangeHistory changeHistory = null;
	
	
	
	
	public DMObject()
	{
		incr = new IncrementalValueMonitor( this );
		this.objClass = null;
		fieldData = new Object[0];
	}
	
	public DMObject(DMObjectClass objClass)
	{
		incr = new IncrementalValueMonitor( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
	}
	
	public DMObject(DMObjectClass objClass, Object values[])
	{
		incr = new IncrementalValueMonitor( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
		int numToCopy = Math.min( values.length, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			Object x = coerce( values[i] );
			notifyAddChild( x );
			fieldData[i] = coerce( x );
		}
	}
	
	public DMObject(DMObjectClass objClass, PyObject values[])
	{
		incr = new IncrementalValueMonitor( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
		int numToCopy = Math.min( values.length, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			Object x = coerce( Py.tojava( values[i], Object.class ) );
			notifyAddChild( x );
			fieldData[i] = x;
		}
	}
	
	public DMObject(DMObjectClass objClass, String[] keys, Object[] values)
	{
		assert keys.length == values.length;
		
		incr = new IncrementalValueMonitor( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
	
		for (int i = 0; i < keys.length; i++)
		{
			int index = objClass.getFieldIndex( keys[i] );
			if ( index == -1 )
			{
				throw new UnknownFieldNameException( keys[i] );
			}
			else
			{
				Object x = coerce( values[i] );
				notifyAddChild( x );
				fieldData[index] = x;
			}
		}
	}

	public DMObject(DMObjectClass objClass, String[] names, PyObject[] values)
	{
		assert names.length == values.length;
		
		incr = new IncrementalValueMonitor( this );
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
				notifyAddChild( x );
				fieldData[index] = x;
			}
		}
	}
	
	public DMObject(DMObjectInterface obj)
	{
		incr = new IncrementalValueMonitor( this );
		this.objClass = obj.getDMObjectClass();
		fieldData = new Object[objClass.getNumFields()];
		
		for (int i = 0; i < fieldData.length; i++)
		{
			Object x = coerce( obj.get( i ) );
			notifyAddChild( x );
			fieldData[i] = x;
		}
	}

	public DMObject(PyObject values[])
	{
		incr = new IncrementalValueMonitor( this );
		objClass = Py.tojava( values[0], DMObjectClass.class );
		fieldData = new Object[objClass.getNumFields()];
		
		int numToCopy = Math.min( values.length - 1, fieldData.length );
		for (int i = 0; i < numToCopy; i++)
		{
			Object x = coerce( Py.tojava( values[i+1], Object.class ) );
			notifyAddChild( x );
			fieldData[i] = x;
		}
	}

	public DMObject(PyObject values[], String names[])
	{
		if ( values.length == 0  &&  names.length == 0 )
		{
			incr = new IncrementalValueMonitor( this );
			this.objClass = null;
			fieldData = new Object[0];
		}
		else
		{
			if ( values.length != ( names.length + 1 ) )
			{
				throw new RuntimeException( "DMObject constructor takes parameters of form (obj_class, fieldname0=value0, fieldname1=value1, ... fieldnameN=valueN)" );
			}
			
			incr = new IncrementalValueMonitor( this );
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
					notifyAddChild( x );
					fieldData[index] = x;
				}
			}
		}
	}

	public DMObject(DMObjectClass objClass, Map<String, Object> data)
	{
		incr = new IncrementalValueMonitor( this );
		this.objClass = objClass;
		fieldData = new Object[objClass.getNumFields()];
		
		for (Map.Entry<String, Object> entry: data.entrySet())
		{
			int index = objClass.getFieldIndex( entry.getKey() );
			if ( index == -1 )
			{
				throw new UnknownFieldNameException( entry.getKey() );
			}
			else
			{
				Object x = coerce( entry.getValue() );
				notifyAddChild( x );
				fieldData[index] = x;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public DMObject(DMObjectClass objClass, PyDictionary data)
	{
		incr = new IncrementalValueMonitor( this );
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
				throw new UnknownFieldNameException( key );
			}
			else
			{
				Object x = coerce( Py.tojava( (PyObject)entry.getValue(), Object.class ) );
				notifyAddChild( x );
				fieldData[index] = x;
			}
		}
	}
	
	
	
	protected Object createDeepCopy(PyDictionary memo)
	{
		onAccess();
		Object[] ys = new Object[fieldData.length];
		
		int i = 0;
		for (Object x: fieldData)
		{
			ys[i++] = deepCopyOf( x, memo );
		}
		
		return new DMObject( objClass, ys );
	}
	

	
	public DMNodeClass getDMNodeClass()
	{
		return getDMObjectClass();
	}

	public DMObjectClass getDMObjectClass()
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
	
	public Object get(String key)
	{
		onAccess();
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
			throw new UnknownFieldNameException( key );
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
			notifyRemoveChild( oldX );
			notifyAddChild( x );
			updateChildParentage( oldX );
		}
		incr.onChanged();
		DMObject_changes.onSet( changeHistory, this, index, oldX, x );
	}
	
	public void set(String key, Object x)
	{
		int index = objClass.getFieldIndex( key );
		if ( index == -1 )
		{
			throw new UnknownFieldNameException( key );
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
	
	public Object[] getFieldValues()
	{
		onAccess();
		Object d[] = new Object[fieldData.length];
		System.arraycopy( fieldData, 0, d, 0, fieldData.length );
		return d;
	}
	
	public Object[] getFieldValuesImmutable()
	{
		onAccess();
		return fieldData;
	}
	
	
	
	public void update(Map<String, Object> table)
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
				throw new UnknownFieldNameException( e.getKey() );
			}
			else
			{
				Object oldX = fieldData[index];
				Object x = coerce( e.getValue() );
				if ( oldX != x )
				{
					notifyRemoveChild( oldX );
					notifyAddChild( x );
					updateChildParentage( oldX );
				}
				indices[i] = index;
				oldContents[i] = oldX;
				newContents[i] = x;
				fieldData[index] = x;
				i++;
			}
		}
		incr.onChanged();
		DMObject_changes.onUpdate( changeHistory, this, indices, oldContents, newContents );
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
				notifyRemoveChild( oldX );
				notifyAddChild( x );
				updateChildParentage( oldX );
			}
			oldContents[i] = fieldData[index];
			newContents[i] = x;
			fieldData[index] = x;
		}
		incr.onChanged();
		DMObject_changes.onUpdate( changeHistory, this, indices, oldContents, newContents );
	}

	
	
	public void become(Object x)
	{
		if ( x instanceof DMObjectInterface )
		{
			DMObjectInterface obj = (DMObjectInterface)x;
			become( obj.getDMObjectClass(), obj.getFieldValues() );
		}
		else
		{
			throw new CannotChangeNodeClassException( x.getClass(), getClass() );
		}
	}

	protected void become(DMObjectClass cls, Object[] data)
	{
		Object oldFieldData[] = new Object[fieldData.length];
		System.arraycopy( fieldData, 0, oldFieldData, 0, fieldData.length );
		
		Object newData[] = new Object[data.length];
		System.arraycopy( data, 0, newData, 0, data.length );

		if ( fieldData.length != data.length )
		{
			fieldData = new Object[data.length];
		}
		System.arraycopy( data, 0, fieldData, 0, data.length );
		
		for (Object x: oldFieldData)
		{
			notifyRemoveChild( x );
		}
		for (Object x: data)
		{
			notifyAddChild( x );
		}
		for (Object x: oldFieldData)
		{
			updateChildParentage( x );
		}
		DMObjectClass oldClass = objClass;
		objClass = cls;
		incr.onChanged();
		DMObject_changes.onBecome( changeHistory, this, oldClass, oldFieldData, cls, newData );
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
			if ( dx.getDMObjectClass() == objClass )
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
	
	public int hashCode()
	{
		// Get the cell value, so that the access is tracked
		onAccess();
		int hashes[] = new int[objClass.getNumFields() + 1];
		hashes[0] = objClass.hashCode();
		for (int i = 0; i < objClass.getNumFields(); i++)
		{
			Object v = get( i );
			
			hashes[i+1] = v != null  ?  v.hashCode()  :  0;
		}
		return HashUtils.nHash( hashes );
	}
	
	
	
	//
	// Children
	//
	
	public Iterable<Object> getChildren()
	{
		Iterable<Object> iterable = new Iterable<Object>()
		{
			public Iterator<Object> iterator()
			{
				Iterator<Object> iter = new Iterator<Object>()
				{
					int index = 0;
					
					public boolean hasNext()
					{
						onAccess();
						return index < fieldData.length;
					}

					public Object next()
					{
						onAccess();
						if ( index < fieldData.length )
						{
							return fieldData[index++];
						}
						else
						{
							throw new NoSuchElementException();
						}
					}

					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
				
				return iter;
			}
		};
		return iterable;
	}
	
	
	
	//
	// Incremental computation
	//
	
	private void onAccess()
	{
		incr.onAccess();
	}

	
	
	
	//
	// Trackable interface
	//
	
	public void setChangeHistory(ChangeHistory h)
	{
		changeHistory = h;
	}
	
	public ChangeHistory getChangeHistory()
	{
		return changeHistory;
	}
	
	
	public List<Object> getTrackableContents()
	{
		return Arrays.asList( getFieldValuesImmutable() );
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
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		DMObjectReader objReader = DMObjectInputStream.readDMObjectReader( stream );

		String keys[] = (String[])stream.readObject();
		Object values[] = (Object[])stream.readObject();
		
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		for (int i = 0; i < keys.length; i++)
		{
			fieldValues.put( keys[i], values[i] );
		}
		
		DMObject val = objReader.readObject( fieldValues );
		objClass = val.objClass;
		fieldData = val.fieldData;
		
		incr = new IncrementalValueMonitor( this );
		incr.onChanged();
		
		changeHistory = null;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		DMObjectOutputStream.writeDMObjectClass( stream, objClass );
		
		String fieldNames[] = objClass.getFieldNames();
		
		stream.writeObject( fieldNames );
		stream.writeObject( fieldData );
	}


	
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return DocModelPresenter.presentDMObject( this, fragment, inheritedState );
	}
}
