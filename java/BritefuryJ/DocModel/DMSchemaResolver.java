//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

public interface DMSchemaResolver
{
	public static class CouldNotResolveSchemaException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public CouldNotResolveSchemaException(String schemaName)
		{
			super( "Could not resolve schema: " + schemaName );
		}
	}
	
	public DMSchema getSchema(String location) throws CouldNotResolveSchemaException;
}
