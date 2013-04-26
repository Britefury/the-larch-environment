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

import BritefuryJ.LSpace.Clipboard.DataTransfer;
import BritefuryJ.LSpace.Clipboard.LocalDataFlavor;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;

public abstract class AbstractDataImporter<TargetType extends Target> extends DataImporterInterface<TargetType>
{
	//
	//
	// OVERRIDE THESE TWO (canImport is optional), and canImportFlavor
	//
	//
	
	protected boolean canImportChecked(TargetType target, Selection selection, Object data)
	{
		return true;
	}
	
	abstract protected Object importCheckedData(TargetType target, Selection selection, Object data);

	
	
	
	@Override
	protected boolean canImport(TargetType target, Selection selection, DataTransfer dataTransfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if ( canImportFlavor( flavor ) )
		{
			return canImportChecked( (TargetType)target, selection, dataTransfer.getTransferData( flavor ) );
		}

		return false;
	}

	@Override
	protected Object importData(TargetType target, Selection selection, DataTransfer dataTransfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if ( canImportFlavor( flavor ) )
		{
			Object data = dataTransfer.getTransferData( flavor );
			if ( canImportChecked( (TargetType)target, selection, data ) )
			{
				return importCheckedData( (TargetType)target, selection, data );
			}
		}
		return false;
	}
	
	
	
	protected static DataFlavor dataFlavorForClass(Class<?> type)
	{
		return new LocalDataFlavor( type );
	}
}
