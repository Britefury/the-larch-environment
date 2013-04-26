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
import java.util.List;

import BritefuryJ.LSpace.Clipboard.DataTransfer;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;

public class TargetImporter <TargetType extends Target>
{
	public interface ImportedDataInsertFn <TargetType extends Target>
	{
		boolean insertImportedData(TargetType target, Selection selection, Object importedData);
	}


	private Class<? extends Target> targetClass;
	private ImportedDataInsertFn insertFn;
	protected ArrayList<DataImporterInterface<TargetType>> importers = new ArrayList<DataImporterInterface<TargetType>>();

	
	public TargetImporter(Class<? extends Target> targetClass, ImportedDataInsertFn insertFn, List<? extends DataImporterInterface<TargetType>> importers)
	{
		this.targetClass = targetClass;
		this.insertFn = insertFn;
		this.importers.addAll( importers );
	}


	public void addDataImporter(DataImporterInterface<TargetType> importer)
	{
		importers.add( 0, importer );
	}


	public Class<? extends Target> getTargetClass()
	{
		return targetClass;
	}
	
	
	@SuppressWarnings("unchecked")
	protected boolean canImport(Target target, Selection selection, DataTransfer dataTransfer)
	{
		for (DataFlavor flavor: dataTransfer.getDataFlavors())
		{
			for (DataImporterInterface<TargetType> importer: importers)
			{
				if ( importer.canImportFlavor( flavor ) )
				{
					try
					{
						if ( importer.canImport( (TargetType)target, selection, dataTransfer, flavor ) )
						{
							return true;
						}
					}
					catch (UnsupportedFlavorException e)
					{
					}
					catch (IOException e)
					{
					}
				}
			}
		}
		
		return false;
	}


	@SuppressWarnings("unchecked")
	protected boolean importData(Target target, Selection selection, DataTransfer dataTransfer)
	{
		for (DataFlavor flavor: dataTransfer.getDataFlavors())
		{
			for (DataImporterInterface<TargetType> importer: importers)
			{
				if ( importer.canImportFlavor( flavor ) )
				{
					try
					{
						Object importedData = importer.importData( (TargetType)target, selection, dataTransfer, flavor );
						if ( importedData != null )
						{
							insertImportedData( (TargetType)target, selection, importedData );
							return true;
						}
					}
					catch (UnsupportedFlavorException e)
					{
					}
					catch (IOException e)
					{
					}
				}
			}
		}
		
		return false;
	}



	private boolean insertImportedData(TargetType target, Selection selection, Object importedData)
	{
		return insertFn.insertImportedData( target, selection, importedData );
	}
}
