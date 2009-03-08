//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class DMObjectClass implements Iterable<DMObjectField>
{
	protected static class KeySetView implements Set<String>
	{
		protected static class KeySetViewIterator implements Iterator<String>
		{
			private DMObjectClass objClass;
			private int index;
			
			
			public KeySetViewIterator(DMObjectClass objClass)
			{
				this.objClass = objClass;
				index = 0;
			}


			public boolean hasNext()
			{
				return index < objClass.getNumFields();
			}

			public String next()
			{
				return objClass.getField( index++ ).getName();
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		}
		
		
		
		private DMObjectClass objClass;
		
		
		public KeySetView(DMObjectClass objClass)
		{
			this.objClass = objClass;
		}


		
		public boolean add(String arg0)
		{
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends String> arg0)
		{
			throw new UnsupportedOperationException();
		}

		public void clear()
		{
			throw new UnsupportedOperationException();
		}


		public boolean contains(Object x)
		{
			for (int i = 0; i < objClass.allClassFields.length; i++)
			{
				if ( x.equals( objClass.allClassFields[i].getName() ) )
				{
					return true;
				}
			}
			
			return false;
		}

		public boolean containsAll(Collection<?> xs)
		{
			for (Object x: xs)
			{
				if ( !contains( x ) )
				{
					return false;
				}
			}
			return true;
		}


		public boolean isEmpty()
		{
			return objClass.isEmpty();
		}


		public Iterator<String> iterator()
		{
			return new KeySetViewIterator( objClass );
		}


		public boolean remove(Object x)
		{
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> arg0)
		{
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection<?> arg0)
		{
			throw new UnsupportedOperationException();
		}


		public int size()
		{
			return objClass.getNumFields();
		}


		public Object[] toArray()
		{
			int len = objClass.getNumFields();
			String xs[] = new String[len];
			int index = 0;
			for (DMObjectField f: objClass)
			{
				xs[index++] = f.getName();
			}
			return xs;
		}

		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] arg0)
		{
			return (T[])toArray();
		}
	}

	
	
	
	
	private String name;
	private DMObjectClass superClass, superClasses[];
	private DMObjectField classFields[], allClassFields[];
	private HashMap<String, Integer> fieldNameToIndex;
	
	
	
	public DMObjectClass(String name, DMObjectField fields[])
	{
		this.name = name;
		superClass = null;
		superClasses = new DMObjectClass[0];
		classFields = fields;
		allClassFields = fields;
		
		initialise();
	}
	
	public DMObjectClass(String name, String fieldNames[])
	{
		this( name, DMObjectField.nameArrayToFieldArray( fieldNames ) );
	}
	
	
	
	public DMObjectClass(String name, DMObjectClass superClass, DMObjectField fields[])
	{
		this.name = name;
		
		this.superClass = superClass;
		superClasses = new DMObjectClass[superClass.superClasses.length + 1];
		superClasses[0] = superClass;
		System.arraycopy( superClass.superClasses, 0, superClasses, 1, superClass.superClasses.length );
		
		classFields = fields;
		allClassFields = new DMObjectField[superClass.allClassFields.length + classFields.length];
		System.arraycopy( superClass.allClassFields, 0, allClassFields, 0, superClass.allClassFields.length );
		System.arraycopy( classFields, 0, allClassFields, superClass.allClassFields.length, classFields.length );

		initialise();
	}

	public DMObjectClass(String name, DMObjectClass superClass, String fieldNames[])
	{
		this( name, superClass, DMObjectField.nameArrayToFieldArray( fieldNames ) );
	}
	
	
	
	public String getName()
	{
		return name;
	}
	
	
	public DMObjectClass getSuperclass()
	{
		return superClass;
	}
	
	public boolean isSubclassOf(DMObjectClass c)
	{
		return c == this  ||  Arrays.asList( superClasses ).contains( c );
	}
	
	
	public int getFieldIndex(String name)
	{
		Integer index = fieldNameToIndex.get( name );
		if ( index != null )
		{
			return index.intValue();
		}
		else
		{
			return -1;
		}
	}
	
	public boolean hasField(String name)
	{
		return fieldNameToIndex.containsKey( name );
	}
	
	public DMObjectField getField(int index)
	{
		return allClassFields[index];
	}
	
	public int getNumFields()
	{
		return allClassFields.length;
	}
	
	public List<DMObjectField> getFields()
	{
		return Arrays.asList( allClassFields );
	}
	
	
	public boolean isEmpty()
	{
		return allClassFields.length == 0;
	}
	

	public Iterator<DMObjectField> iterator()
	{
		return Arrays.asList( allClassFields ).iterator();
	}
	
	
	public Set<String> fieldNameSet()
	{
		return new KeySetView( this );
	}
	
	
	
	
	
	private void initialise()
	{
		fieldNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < allClassFields.length; i++)
		{
			fieldNameToIndex.put( allClassFields[i].getName(), new Integer( i ) );
		}
	}
	
}
