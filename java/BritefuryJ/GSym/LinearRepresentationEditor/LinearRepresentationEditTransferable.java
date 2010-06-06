//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.LinearRepresentationEditor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class LinearRepresentationEditTransferable implements Transferable
{
	protected LinearRepresentationBuffer buffer;
	protected DataFlavor bufferFlavor;
	
	
	
	public LinearRepresentationEditTransferable(LinearRepresentationBuffer buffer, DataFlavor bufferFlavor)
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
		else
		{
			throw new UnsupportedFlavorException( flavor );
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { bufferFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor.equals( bufferFlavor );
	}
}
