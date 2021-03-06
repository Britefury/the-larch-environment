//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DocModel;

import java.util.*;

import org.python.core.*;

import BritefuryJ.Parser.ObjectNode;
import BritefuryJ.Parser.ParserExpression.ParserCoerceException;


public class DMObjectClass extends DMNodeClass
{
	public static class UnknownFieldNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public UnknownFieldNameException(String fieldName)
		{
			super( "Unknown field name: '" + fieldName + "'" );
		}
	}
	
	public static class InvalidClassNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public InvalidClassNameException(String message)
		{
			super( message );
		}
	}

	
	private static final HashSet<String> disallowedClassNames = new HashSet<String>();

	
	static
	{
		disallowedClassNames.add( "DMNode" );
		disallowedClassNames.add( "DMNodeClass" );
		disallowedClassNames.add( "DMObject" );
		disallowedClassNames.add( "DMObjectClass" );
		disallowedClassNames.add( "DMList" );
		disallowedClassNames.add( "DMSchema" );
	}

	
	
	

	
	
	private DMSchema schema;
	private DMObjectClass superclass, superclasses[];
	private DMObjectField classFields[], allClassFields[];
	private HashMap<String, Integer> fieldNameToIndex;
	private DMObjectReaderDefault defaultReader = new DMObjectReaderDefault( this );

	private PyTuple pickleClassDescriptor = null;

	
	
	public DMObjectClass(DMSchema schema, String name, DMObjectField fields[])
	{
		super( name );
		checkClassNameValidity( name );
		this.schema = schema;
		superclass = null;
		superclasses = new DMObjectClass[0];
		classFields = fields;
		allClassFields = fields;
		
		initialise();
	}
	
	public DMObjectClass(DMSchema schema, String name, String fieldNames[])
	{
		this( schema, name, DMObjectField.nameArrayToFieldArray( fieldNames ) );
	}
	
	
	
	public DMObjectClass(DMSchema schema, String name, DMObjectClass superclass, DMObjectField fields[])
	{
		super( name );
		
		checkClassNameValidity( name );
		this.schema = schema;
		
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

	public DMObjectClass(DMSchema schema, String name, DMObjectClass superclass, String fieldNames[])
	{
		this( schema, name, superclass, DMObjectField.nameArrayToFieldArray( fieldNames ) );
	}
	
	
	
	private void initialise()
	{
		fieldNameToIndex = new HashMap<String, Integer>();
		for (int i = 0; i < allClassFields.length; i++)
		{
			fieldNameToIndex.put( allClassFields[i].getName(), i );
		}
		
		schema.registerClass( name, this );
	}	
	
	
	protected DMObjectReaderDefault getDefaultReader()
	{
		return defaultReader;
	}
	
	
	
	public DMSchema getSchema()
	{
		return schema;
	}
	
	
	public DMNodeClass getSuperclass()
	{
		return superclass;
	}
	
	public boolean isSubclassOf(DMNodeClass c)
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
			return index;
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

	public DMObject newInstance(String keys[], Object values[])
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

	public DMObject newInstance(Map<String, Object> data)
	{
		return new DMObject( this, data );
	}
	
	
	
	public ObjectNode parser()
	{
		return new ObjectNode( this );
	}
	
	public ObjectNode parser(Object fieldExps[]) throws ParserCoerceException
	{
		return new ObjectNode( this, fieldExps );
	}
	
	public ObjectNode parser(String fieldNames[], Object fieldExps[]) throws ParserCoerceException
	{
		return new ObjectNode( this, fieldNames, fieldExps );
	}
	
	public ObjectNode parser(PyObject values[], String names[]) throws ParserCoerceException
	{
		return new ObjectNode( this, names, values );
	}
	
	public ObjectNode parser(Map<String, Object> data) throws ParserCoerceException
	{
		return new ObjectNode( this, data );
	}
	
	
	
	/*public DMObject __call__(PyObject values[])
	{
		return newInstance( values );
	}*/

	public DMObject __call__(PyObject values[], String names[])
	{
		return newInstance( values, names );
	}



	//
	// Pickling
	//

	protected PyTuple getPickleClassDescriptor()
	{
		if ( pickleClassDescriptor == null )
		{
			// String schemaLocation, Integer version, String className, PyList<String> fieldNames
			PyString schemaLoc = __builtin__.intern( Py.newString( schema.getLocation() ) );
			PyInteger schemaVersion = Py.newInteger( schema.getVersion() );
			PyString className = __builtin__.intern( Py.newString( getName() ) );
			PyList fieldNames = new PyList();
			for (String field: getFieldNames())
			{
				fieldNames.append( Py.newString( field ) );
			}
			pickleClassDescriptor = new PyTuple( schemaLoc, schemaVersion, className, fieldNames );
		}
		return pickleClassDescriptor;
	}



	
	
	
	private static void checkClassNameValidity(String name)
	{
		if ( DMNodeClass.validNamePattern.matcher( name ).matches() )
		{
			if ( disallowedClassNames.contains( name ) )
			{
				throw new InvalidClassNameException( "Invalid class name '" + name + "'; name cannot be any of " + disallowedClassNames );
			}
		}
		else
		{
			throw new InvalidClassNameException( "Invalid class name '" + name + "'; name should be an identifier" );
		}
	}
}
