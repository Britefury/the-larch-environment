//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.python.core.Py;

public class DMSchema
{
	public static class UnknownSchemaException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public UnknownSchemaException(String schemaLocation)
		{
			super( "Unknown schema '" + schemaLocation + "'" );
		}
	}

	public static class UnknownClassException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public UnknownClassException(String className)
		{
			super( "Unknown class '" + className + "'" );
		}
	}

	public static class ClassAlreadyDefinedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public ClassAlreadyDefinedException(String className)
		{
			super( "Class '" + className + "' already defined" );
		}
	}

	public static class CouldNotFindReaderException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public CouldNotFindReaderException(String className, int version)
		{
			super( "Could not find reader for class '" + className + "', version " + version );
		}
	}

	public static class UnsupportedSchemaVersionException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public UnsupportedSchemaVersionException(String schemaLocation, int supportedVersion, int requestedVersion)
		{
			super( "Cannot load schema '" + schemaLocation + "' version " + requestedVersion + ", only " + supportedVersion + " is supported" );
		}
	}

	
	
	
	
	public static class InvalidSchemaNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public InvalidSchemaNameException(String message)
		{
			super( message );
		}
	}

	
	private static final HashSet<String> disallowedSchemaNames = new HashSet<String>();

	
	static
	{
	}
	
	
	
	private static class ReadersByVersion
	{
		// Reader wrapper; contains a reader. This allows a map that contains readers as values to have an entry with a null reader.
		private static class ReaderWrapper
		{
			public DMObjectReader reader;
			
			public ReaderWrapper(DMObjectReader reader)
			{
				this.reader = reader;
			}
		}
		
		
		private ArrayList<Integer> versions = new ArrayList<Integer>();
		private ArrayList<DMObjectReader> readers = new ArrayList<DMObjectReader>();
		private ArrayList<ReaderWrapper> readerCache = new ArrayList<ReaderWrapper>();
		
		
		public ReadersByVersion()
		{
		}
		
		
		public void registerReader(DMObjectReader reader, int version)
		{
			int index = Arrays.binarySearch( versions.toArray( new Integer[versions.size()] ), version );
			if ( index >= 0 )
			{
				readers.set( index, reader );
			}
			else
			{
				int insertionPoint = -( index + 1 );
				versions.add( insertionPoint, version );
				readers.add( insertionPoint, reader );
			}
			readerCache.clear();
		}
		
		
		public DMObjectReader getReader(int version, String className)
		{
			if ( readers.size() == 1 )
			{
				if ( version <= versions.get( 0 ) )
				{
					return readers.get( 0 );
				}
				else
				{
					throw new CouldNotFindReaderException( className, version );
				}
			}
			else
			{
				ReaderWrapper readerWrapper = null;

				if ( version < readerCache.size() )
				{
					readerWrapper = readerCache.get( version );
				}
				
				if ( readerWrapper == null )
				{
					readerWrapper = createReaderWrapperForVersion( version );
					encacheReaderWrapper( version, readerWrapper );
				}
				
				DMObjectReader reader = readerWrapper.reader;
				if ( reader == null )
				{
					throw new CouldNotFindReaderException( className, version );
				}
				else
				{
					return reader;
				}
			}
		}
		
		
		private ReaderWrapper createReaderWrapperForVersion(int version)
		{
			int index = Arrays.binarySearch( versions.toArray( new Integer[versions.size()] ), version );
			if ( index >= 0 )
			{
				return new ReaderWrapper( readers.get( index ) );
			}
			else
			{
				index = -( index + 1 );
				if ( index == readers.size() )
				{
					return new ReaderWrapper( null );
				}
				else
				{
					return new ReaderWrapper( readers.get( index ) );
				}
			}
		}
		
		private void encacheReaderWrapper(int version, ReaderWrapper reader)
		{
			if ( version >= readerCache.size() )
			{
				for (int i = readerCache.size(); i <= version; i++)
				{
					readerCache.add( null );
				}
			}
			readerCache.set( version, reader );
		}
	}

	
	
	
	private String schemaName, shortName, moduleLocation;
	private int version;
	private HashMap<String, DMObjectClass> classes = new HashMap<String, DMObjectClass>();
	private HashMap<String, ReadersByVersion> readers = new HashMap<String, ReadersByVersion>();
	
	
	
	public DMSchema(String name, String shortName, String location)
	{
		this( name, shortName, location, 1 );
	}
	
	public DMSchema(String name, String shortName, String location, int version)
	{
		checkSchemaNameValidity( name );
		this.schemaName = name;
		this.shortName = shortName;
		this.moduleLocation = location;
		this.version = version;
		DMSchemaResolver.registerSchema( this );
	}
	
	
	
	public String getName()
	{
		return schemaName;
	}
	
	public String getShortName()
	{
		return shortName;
	}
	
	public String getLocation()
	{
		return moduleLocation;
	}
	
	public int getVersion()
	{
		return version;
	}
	
	
	
	public DMObjectClass get(String name)
	{
		DMObjectClass c = classes.get( name );
		if ( c == null )
		{
			throw new UnknownClassException( name );
		}
		return c;
	}
	
	protected void registerClass(String name, DMObjectClass c)
	{
		if ( classes.containsKey( name ) )
		{
			throw new ClassAlreadyDefinedException( name );
		}
		classes.put( name, c );
		
		// Register a default reader
		registerReader( name, version, new DMObjectReaderDefault( c ) );
	}
	
	
	
	public DMObjectReader getReader(String name, int version)
	{
		ReadersByVersion verReader = readers.get( name );
		if ( verReader == null )
		{
			throw new UnknownClassException( name );
		}
		return verReader.getReader( version, name );
	}
	
	public void registerReader(String name, int version, DMObjectReader reader)
	{
		ReadersByVersion verReader = readers.get( name );
		if ( verReader == null )
		{
			verReader = new ReadersByVersion();
			readers.put( name, verReader );
		}
		
		verReader.registerReader( reader, version );
	}
	
	
	
	public DMObjectClass __getitem__(String name)
	{
		DMObjectClass c = classes.get( name );
		if ( c == null )
		{
			throw Py.KeyError( name );
		}
		return c;
	}
	
	
	
	public DMObjectClass newClass(String name, DMObjectField fields[])
	{
		return new DMObjectClass( this, name, fields );
	}

	public DMObjectClass newClass(String name, String fieldNames[])
	{
		return new DMObjectClass( this, name, fieldNames );
	}

	public DMObjectClass newClass(String name, DMObjectClass superClass, DMObjectField fields[])
	{
		return new DMObjectClass( this, name, superClass, fields );
	}

	public DMObjectClass newClass(String name, DMObjectClass superClass, String fieldNames[])
	{
		return new DMObjectClass( this, name, superClass, fieldNames );
	}
	
	
	
	
	
	private static void checkSchemaNameValidity(String name)
	{
		if ( DMNodeClass.validNamePattern.matcher( name ).matches() )
		{
			if ( disallowedSchemaNames.contains( name ) )
			{
				throw new InvalidSchemaNameException( "Invalid schema name '" + name + "'; name cannot be any of " + disallowedSchemaNames );
			}
		}
		else
		{
			throw new InvalidSchemaNameException( "Invalid schema name '" + name + "'; name should be an identifier" );
		}
	}
}
