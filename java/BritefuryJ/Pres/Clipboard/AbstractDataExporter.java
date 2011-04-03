//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Clipboard.LocalDataFlavor;

public abstract class AbstractDataExporter <SelectionContentsType> extends DataExporterInterface<SelectionContentsType>
{
	static ArrayList<DataFlavor> emptyFlavorList = new ArrayList<DataFlavor>();
	
	
	//
	//
	// OVERRIDE THESE THREE (canExport is optional)
	//
	//
	
	abstract protected DataFlavor getDataFlavor();
	
	protected boolean canExport(SelectionContentsType selectionContents)
	{
		return true;
	}
	
	abstract protected Object export(SelectionContentsType selectionContents);
	
	
	
	
	
	protected List<DataFlavor> getTransferDataFlavors(SelectionContentsType selectionContents)
	{
		if ( canExport( selectionContents ) )
		{
			return Arrays.asList( new DataFlavor[] { getDataFlavor() } );
		}
		else
		{
			return emptyFlavorList;
		}
	}

	protected Object getTransferData(SelectionContentsType selectionContents, DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if ( canExport( selectionContents ) )
		{
			if ( flavor.equals( getDataFlavor() ) )
			{
				return export( selectionContents );
			}
		}
		
		throw new UnsupportedFlavorException( flavor );
	}
	
	
	
	protected static DataFlavor dataFlavorForClass(Class<?> type)
	{
		return new LocalDataFlavor( type );
	}
}
