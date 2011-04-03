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
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Clipboard.DataTransfer;
import BritefuryJ.DocPresent.Clipboard.LocalDataFlavor;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Target.Target;

public class DataImporter <TargetType extends Target> extends DataImporterInterface<TargetType>
{
	public interface ImportDataFn <TargetType extends Target>
	{
		boolean importData(TargetType target, Selection selection, Object data);
	}
	
	public interface CanImportFn <TargetType extends Target>
	{
		boolean canImport(TargetType target, Selection selection, Object data);
	}
	
	
	private DataFlavor flavor;
	private ImportDataFn<TargetType> importDataFn;
	private CanImportFn<TargetType> canImportFn;
	
	
	
	public DataImporter(DataFlavor flavor, ImportDataFn<TargetType> importDataFn, CanImportFn<TargetType> canImportFn)
	{
		this.flavor = flavor;
		this.importDataFn = importDataFn;
		this.canImportFn = canImportFn;
	}
	
	public DataImporter(DataFlavor flavor, ImportDataFn<TargetType> importDataFn)
	{
		this( flavor, importDataFn, null );
	}
	
	public DataImporter(Class<?> type, ImportDataFn<TargetType> importDataFn, CanImportFn<TargetType> canImportFn)
	{
		this( new LocalDataFlavor( type ), importDataFn, canImportFn );
	}
	
	public DataImporter(Class<?> type, DataFlavor flavor, ImportDataFn<TargetType> importDataFn)
	{
		this( new LocalDataFlavor( type ), importDataFn, null );
	}
	
	
	@Override
	protected List<DataFlavor> getDataFlavors()
	{
		return Arrays.asList( new DataFlavor[] { flavor } );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean canImport(Target target, Selection selection, DataTransfer dataTransfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if ( flavor.equals( this.flavor ) )
		{
			return canImportFn == null  ||  canImportFn.canImport( (TargetType)target, selection, dataTransfer.getTransferData( flavor ) );
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean importData(Target target, Selection selection, DataTransfer dataTransfer, DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if ( flavor.equals( this.flavor ) )
		{
			Object data = dataTransfer.getTransferData( flavor );
			if ( canImportFn == null  ||  canImportFn.canImport( (TargetType)target, selection, data ) )
			{
				return importDataFn.importData( (TargetType)target, selection, data );
			}
		}
		return false;
	}
}
