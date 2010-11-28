//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Editor.Sequential;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class SequentialEditTransferable implements Transferable
{
	protected SequentialBuffer buffer;
	protected DataFlavor bufferFlavor;
	
	
	
	public SequentialEditTransferable(SequentialBuffer buffer, DataFlavor bufferFlavor)
	{
		this.buffer = buffer;
		this.bufferFlavor = bufferFlavor;
	}
	
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if ( flavor.equals( bufferFlavor ) )
		{
			return buffer;
		}
		else if ( flavor.equals( DataFlavor.stringFlavor ) )
		{
			if ( buffer.isTextual() )
			{
				return buffer.getTextualValue();
			}
			else
			{
				throw new UnsupportedFlavorException( flavor );
			}
		}
		else
		{
			throw new UnsupportedFlavorException( flavor );
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		if ( buffer.isTextual() )
		{
			return new DataFlavor[] { bufferFlavor, DataFlavor.stringFlavor };
		}
		else
		{
			return new DataFlavor[] { bufferFlavor };
		}
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if ( buffer.isTextual() )
		{
			return flavor.equals( bufferFlavor )  ||  flavor.equals( DataFlavor.stringFlavor );
		}
		else
		{
			return flavor.equals( bufferFlavor );
		}
	}
}
