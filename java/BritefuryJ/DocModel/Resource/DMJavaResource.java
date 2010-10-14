//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

public class DMJavaResource extends DMResource
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private Object value[] = null;
	
	
	public DMJavaResource(Object value)
	{
		this.value = new Object[] { value };
	}
	
	
	public Object getValue()
	{
		if ( value == null )
		{
			byte bytes[];
			try
			{
				bytes = serialised.getBytes( "UTF-8" );
				ByteArrayInputStream inStream = new ByteArrayInputStream( bytes );
				ObjectInputStream objIn = new ObjectInputStream( inStream );
				Object v = objIn.readObject();
				this.value = new Object[] { v };
				this.serialised = null;
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException( "Cannot get UTF-8 encoding" );
			}
			catch (IOException e)
			{
				throw new RuntimeException( "IOError while reading from serialised form" );
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException( "Cannot read object; class not found" );
			}
		}
		
		return value[0];
	}
	
	
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream( outStream );
		objOut.writeObject( getValue() );
		String serialised = outStream.toString();
		out.writeUTF( serialised );
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		serialised = in.readUTF();
		value = null;
	}
}
