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

import org.python.core.PyDictionary;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMModule.ClassAlreadyDefinedException;
import BritefuryJ.Parser.ObjectNode;
import BritefuryJ.Parser.ParserExpression.ParserCoerceException;


public class DMObjectClass
{
	public static class InvalidFieldNameException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public InvalidFieldNameException(String fieldName)
		{
			super( "Invalid field name: '" + fieldName + "'" );
		}
	};
	

	
	
	private DMModule module;
	private String name;
	private DMObjectClass superclass, superclasses[];
	private DMObjectField classFields[], allClassFields[];
	private HashMap<String, Integer> fieldNameToIndex;
	
	
	
	public DMObjectClass(DMModule module, String name, DMObjectField fields[]) throws ClassAlreadyDefinedException
	{
		this.module = module;
		this.name = name.intern();
		superclass = null;
		superclasses = new DMObjectClass[0];
		classFields = fields;
		allClassFields = fields;
		
		initialise();
	}
	
	public DMObjectClass(DMModule module, String name, String fieldNames[]) throws ClassAlreadyDefinedException
	{
		this( module, name, DMObjectField.nameArrayToFieldArray( fieldNames ) );
	}
	
	
	
	public DMObjectClass(DMModule module, String name, DMObjectClass superclass, DMObjectField fields[]) throws ClassAlreadyDefinedException
	{
		this.module = module;
		this.name = name.intern();
		
		this.superclass = superclass;
		superclasses = new DMObjectClass[superclass.superclasses.length + 1];
		superclasses[0] = superclass;
		System.arraycopy( superclass.superclasses, 0, superclasses, 1, superclass.superclasses.length );
		
		classFields = fields;
		allClassFields = new DMObjectField[superclass.allClassFields.length + classFields.length];
		System.arraycopy( superclass.allClassFields, 0, allClassFields, 0, superclass.allClassFields.length );
		System.arraycopy( classFields, 0, allClassFields, superclass.allClassFields.length, classFields.length );
		
		initialise();
	}

	public DMObjectClass(DMModule module, String name, DMObjectClass superclass, String fieldNames[]) throws ClassAlreadyDefinedException
	{
		this( module, name, superclass, DMObjectField.nameArrayToFieldArray( fieldNames ) );
	}
	
	
	
	private void initialise() throws ClassAlreadyDefinedException
	{
		fieldNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < allClassFields.length; i++)
		{
			fieldNameToIndex.put( allClassFields[i].getName(), new Integer( i ) );
		}
		
		module.registerClass( name, this );
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
		return superclass;
	}
	
	public boolean isSubclassOf(DMObjectClass c)
	{
		return c == this  ||  Arrays.asList( superclasses ).contains( c );
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

	
	public DMObject newInstance()
	{
		return new DMObject( this );
	}

	public DMObject newInstance(Object values[])
	{
		return new DMObject( this, values );
	}

	public DMObject newInstance(String keys[], Object values[]) throws InvalidFieldNameException
	{
		return new DMObject( this, keys, values );
	}

	public DMObject newInstance(PyObject values[])
	{
		return new DMObject( this, values );
	}

	public DMObject newInstance(PyObject values[], String names[])
	{
		return new DMObject( this, names, values );
	}

	public DMObject newInstance(Map<String, Object> data) throws InvalidFieldNameException
	{
		return new DMObject( this, data );
	}
	
	public DMObject newInstance(PyDictionary data) throws InvalidFieldNameException
	{
		return new DMObject( this, data );
	}
	
	
	
	public ObjectNode parser() throws InvalidFieldNameException
	{
		return new ObjectNode( this );
	}
	
	public ObjectNode parser(Object fieldExps[]) throws InvalidFieldNameException, ParserCoerceException
	{
		return new ObjectNode( this, fieldExps );
	}
	
	public ObjectNode parser(String fieldNames[], Object fieldExps[]) throws InvalidFieldNameException, ParserCoerceException
	{
		return new ObjectNode( this, fieldNames, fieldExps );
	}
	
	public ObjectNode parser(PyObject values[], String names[]) throws InvalidFieldNameException, ParserCoerceException
	{
		return new ObjectNode( this, names, values );
	}
	
	public ObjectNode parser(Map<String, Object> data) throws InvalidFieldNameException, ParserCoerceException
	{
		return new ObjectNode( this, data );
	}
	
	public ObjectNode parser(PyDictionary data) throws InvalidFieldNameException, ParserCoerceException
	{
		return new ObjectNode( this, data );
	}
	
	
	
	public DMObject __call__(PyObject values[])
	{
		return newInstance( values );
	}

	public DMObject __call__(PyObject values[], String names[])
	{
		return newInstance( values, names );
	}
}
