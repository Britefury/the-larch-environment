//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.core.PyObject;


public class DMObjectClass
{
	public static class InvalidFieldNameException extends Exception
	{
		private static final long serialVersionUID = 1L;
	};
	

	
	
	private DMModule module;
	private String name;
	private DMObjectClass superClass, superClasses[];
	private DMObjectField classFields[], allClassFields[];
	private HashMap<String, Integer> fieldNameToIndex;
	
	
	
	public DMObjectClass(DMModule module, String name, DMObjectField fields[])
	{
		this.module = module;
		this.name = name;
		superClass = null;
		superClasses = new DMObjectClass[0];
		classFields = fields;
		allClassFields = fields;
		
		initialise();
	}
	
	public DMObjectClass(DMModule module, String name, String fieldNames[])
	{
		this( module, name, DMObjectField.nameArrayToFieldArray( fieldNames ) );
	}
	
	
	
	public DMObjectClass(DMModule module, String name, DMObjectClass superClass, DMObjectField fields[])
	{
		this.module = module;
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

	public DMObjectClass(DMModule module, String name, DMObjectClass superClass, String fieldNames[])
	{
		this( module, name, superClass, DMObjectField.nameArrayToFieldArray( fieldNames ) );
	}
	
	
	
	public DMModule getModule()
	{
		return module;
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
	
	
	
	public int getNumFields()
	{
		return allClassFields.length;
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
	
	public List<DMObjectField> getFields()
	{
		return Arrays.asList( allClassFields );
	}
	
	
	public boolean isEmpty()
	{
		return allClassFields.length == 0;
	}
	

	public String[] getFieldNames()
	{
		String fieldNames[] = new String[allClassFields.length];
		int i = 0;
		for (DMObjectField field: allClassFields)
		{
			fieldNames[i++] = field.getName();
		}
		return fieldNames;
	}

	
	private void initialise()
	{
		fieldNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < allClassFields.length; i++)
		{
			fieldNameToIndex.put( allClassFields[i].getName(), new Integer( i ) );
		}
	}
	
	
	
	public DMObject newInstance(Object values[])
	{
		return new DMObject( this, values );
	}

	public DMObject newInstance(String keys[], Object values[]) throws InvalidFieldNameException
	{
		return new DMObject( this, keys, values );
	}

	public DMObject newInstance(PyObject values[], String names[])
	{
		return new DMObject( this, names, values );
	}

	public DMObject newInstance(Map<String, Object> data) throws InvalidFieldNameException
	{
		return new DMObject( this, data );
	}
	
	
	public DMObject __call__(PyObject values[], String names[])
	{
		return newInstance( values, names );
	}
}
