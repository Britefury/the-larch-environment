//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.LSpace.Clipboard.LocalDataFlavor;

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
			return Arrays.asList( getDataFlavor() );
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
