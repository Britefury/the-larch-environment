//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.util.List;

import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Target.Target;

public class SelectionExporter <SelectionContentsType, SelectionType extends Selection> extends AbstractSelectionExporter<SelectionContentsType, SelectionType>
{
	public interface SelectionContentsFn <SelectionContentsType, SelectionType extends Selection>
	{
		public SelectionContentsType getSelectionContents(SelectionType selection);
	}

	public interface ExportDoneFn <SelectionContentsType, SelectionType extends Selection>
	{
		public void exportDone(SelectionType selection, Target target, SelectionContentsType selectionContets, int action);
	}

	
	
	private SelectionContentsFn<SelectionContentsType, SelectionType> selectionContentsFn;
	private ExportDoneFn<SelectionContentsType, SelectionType> exportDoneFn;
	
	
	public SelectionExporter(Class<? extends Selection> selectionClass, int actions, SelectionContentsFn<SelectionContentsType, SelectionType> selectionContentsFn,
			ExportDoneFn<SelectionContentsType, SelectionType> exportDoneFn, List<? extends DataExporterInterface<SelectionContentsType>> exporters)
	{
		super( selectionClass, actions, exporters );
		this.selectionContentsFn = selectionContentsFn;
		this.exportDoneFn = exportDoneFn;
	}

	public SelectionExporter(Class<? extends Selection> selectionClass, int actions, SelectionContentsFn<SelectionContentsType, SelectionType> selectionContentsFn,
			List<DataExporterInterface<SelectionContentsType>> exporters)
	{
		this( selectionClass, actions, selectionContentsFn, null, exporters );
	}

	public SelectionExporter(Class<? extends Selection> selectionClass, SelectionContentsFn<SelectionContentsType, SelectionType> selectionContentsFn,
			ExportDoneFn<SelectionContentsType, SelectionType> exportDoneFn, List<? extends DataExporterInterface<SelectionContentsType>> exporters)
	{
		this( selectionClass, COPY_OR_MOVE, selectionContentsFn, exportDoneFn, exporters );
	}
	
	public SelectionExporter(Class<? extends Selection> selectionClass, SelectionContentsFn<SelectionContentsType, SelectionType> selectionContentsFn,
			List<? extends DataExporterInterface<SelectionContentsType>> exporters)
	{
		this( selectionClass, COPY_OR_MOVE, selectionContentsFn, null, exporters );
	}
	
	
	
	public SelectionContentsFn<SelectionContentsType, SelectionType> getSelectionContentsFunction()
	{
		return selectionContentsFn;
	}
	
	public ExportDoneFn<SelectionContentsType, SelectionType> getExportDoneFunction()
	{
		return exportDoneFn;
	}
	
	
	
	protected SelectionContentsType getSelectionContents(SelectionType selection)
	{
		return selectionContentsFn.getSelectionContents( selection );
	}
	
	protected void exportDone(SelectionType selection, Target target, SelectionContentsType selectionContents, int action)
	{
		exportDoneFn.exportDone( selection, target, selectionContents, action );
	}
}
