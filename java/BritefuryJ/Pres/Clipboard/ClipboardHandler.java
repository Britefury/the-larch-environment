//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.Transferable;
import java.util.List;

import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.LSpace.Clipboard.DataTransfer;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.Util.PolymorphicMap;

public class ClipboardHandler extends ClipboardHandlerInterface
{
	private PolymorphicMap<AbstractSelectionExporter<?,?>> exporters = null;
	private PolymorphicMap<TargetImporter<?>> importers = null;
	private PolymorphicMap<SelectionEditorInterface> selectionEditors = null;
	
	
	
	public ClipboardHandler()
	{
	}

	public ClipboardHandler(List<AbstractSelectionExporter<?,?>> exporters, List<TargetImporter<?>> importers, List<SelectionEditorInterface> selectionEditors)
	{
		if ( exporters != null  &&  exporters.size() > 0 )
		{
			this.exporters = new PolymorphicMap<AbstractSelectionExporter<?,?>>();
			for (AbstractSelectionExporter<?,?> exporter: exporters)
			{
				this.exporters.put( exporter.getSelectionClass(), exporter );
			}
		}

		if ( importers != null  &&  importers.size() > 0 )
		{
			this.importers = new PolymorphicMap<TargetImporter<?>>();
			for (TargetImporter<?> importer: importers)
			{
				this.importers.put( importer.getTargetClass(), importer );
			}
		}

		if ( selectionEditors != null  &&  selectionEditors.size() > 0 )
		{
			this.selectionEditors = new PolymorphicMap<SelectionEditorInterface>();
			for (SelectionEditorInterface selectionEditor: selectionEditors)
			{
				this.selectionEditors.put( selectionEditor.getSelectionClass(), selectionEditor );
			}
		}
	}
	
	
	public void addExporter(AbstractSelectionExporter<?,?> exporter)
	{
		if ( exporters == null )
		{
			exporters = new PolymorphicMap<AbstractSelectionExporter<?,?>>();
		}
		exporters.put( exporter.getSelectionClass(), exporter );
	}
	
	public void addImporter(TargetImporter<?> importer)
	{
		if ( importers == null )
		{
			importers = new PolymorphicMap<TargetImporter<?>>();
		}
		importers.put( importer.getTargetClass(), importer );
	}
	
	public void addSelectionEditor(SelectionEditorInterface selectionEditor)
	{
		if ( selectionEditors == null )
		{
			selectionEditors = new PolymorphicMap<SelectionEditorInterface>();
		}
		selectionEditors.put( selectionEditor.getSelectionClass(), selectionEditor );
	}


	public <SelectionType> AbstractSelectionExporter<SelectionType,?> getExporter(Class<SelectionType> selectionClass)
	{
		return (AbstractSelectionExporter<SelectionType,?>)(exporters == null  ?  null  :  exporters.getWithoutInheritance( selectionClass ));
	}

	public <TargetType extends Target> TargetImporter<TargetType> getImporter(Class<TargetType> targetClass)
	{
		return (TargetImporter<TargetType>)(importers == null  ?  null  :  importers.getWithoutInheritance( targetClass ));
	}

	public SelectionEditorInterface getSelectionEditor(Class<?> selectionClass)
	{
		return selectionEditors == null  ?  null  :  selectionEditors.getWithoutInheritance( selectionClass );
	}


	@Override
	public boolean deleteSelection(Selection selection, Target target)
	{
		SelectionEditorInterface selectionEditor = getSelectionEditorForSelection( selection );

		try {
			return selectionEditor != null && selectionEditor.deleteSelection( selection, target );
		}
		catch (Throwable t) {
			target.getElement().getRootElement().notifyExceptionDuringClipboardOperation(target.getRegion(), selectionEditor, "SelectionEditorInterface.deleteSelection", t);
			return false;
		}
	}

	@Override
	public boolean replaceSelectionWithText(Selection selection, Target target, String replacement)
	{
		SelectionEditorInterface selectionEditor = getSelectionEditorForSelection( selection );
		
		try {
			return selectionEditor != null && selectionEditor.replaceSelectionWithText( selection, target, replacement );
		}
		catch (Throwable t) {
			target.getElement().getRootElement().notifyExceptionDuringClipboardOperation(target.getRegion(), selectionEditor, "SelectionEditorInterface.replaceSelectionWithText", t);
			return false;
		}
	}

	@Override
	public int getExportActions(Selection selection)
	{
		AbstractSelectionExporter<?,?> exporter = getExporterForSelection( selection );
		
		try {
			return exporter != null  ?  exporter.getActions()  :  0;
		}
		catch (Throwable t) {
			selection.getRootElement().notifyExceptionDuringClipboardOperation(selection.getRegion(), exporter, "AbstractSelectionExporter.getActions", t);
			return 0;
		}
	}

	@Override
	public Transferable createExportTransferable(Selection selection)
	{
		AbstractSelectionExporter<?,?> exporter = getExporterForSelection( selection );
		
		if ( exporter != null )
		{
			try {
				return exporter.createExportTransferable( selection );
			}
			catch (Throwable t) {
				selection.getRootElement().notifyExceptionDuringClipboardOperation(selection.getRegion(), exporter, "AbstractSelectionExporter.createExportTransferable", t);
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public void exportDone(Selection selection, Target target, Transferable transferable, int action)
	{
		AbstractSelectionExporter<?,?> exporter = getExporterForSelection( selection );
		
		if ( exporter != null )
		{
			try {
				exporter.exportTransferableDone( selection, target, transferable, action );
			}
			catch (Throwable t) {
				target.getElement().getRootElement().notifyExceptionDuringClipboardOperation(target.getRegion(), exporter, "AbstractSelectionExporter.exportTransferableDone", t);
			}
		}
	}

	@Override
	public boolean canImport(Target target, Selection selection, DataTransfer dataTransfer)
	{
		TargetImporter<?> importer = getImporterForTarget( target );
		
		if ( importer != null )
		{
			try {
				return importer.canImport( target, selection, dataTransfer );
			}
			catch (Throwable t) {
				target.getElement().getRootElement().notifyExceptionDuringClipboardOperation(target.getRegion(), importer, "TargetImporter.canImport", t);
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean importData(Target target, Selection selection, DataTransfer dataTransfer)
	{
		TargetImporter<?> importer = getImporterForTarget( target );
		
		if ( importer != null )
		{
			try {
				return importer.importData( target, selection, dataTransfer );
			}
			catch (Throwable t) {
				target.getElement().getRootElement().notifyExceptionDuringClipboardOperation(target.getRegion(), importer, "TargetImporter.importData", t);
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	
	private AbstractSelectionExporter<?,?> getExporterForSelection(Selection selection)
	{
		return exporters != null  ?  exporters.getForInstance( selection )  :  null;
	}

	private TargetImporter<?> getImporterForTarget(Target target)
	{
		return importers != null  ?  importers.getForInstance( target )  :  null;
	}
	
	private SelectionEditorInterface getSelectionEditorForSelection(Selection selection)
	{
		return selectionEditors != null  ?  selectionEditors.getForInstance( selection )  :  null;
	}
}
