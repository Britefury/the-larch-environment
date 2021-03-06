//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.TransferHandler;

import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;

abstract public class AbstractSelectionExporter <SelectionContentsType, SelectionType extends Selection>
{
	public static int COPY = TransferHandler.COPY;
	public static int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public static int LINK = TransferHandler.LINK;
	public static int MOVE = TransferHandler.MOVE;
	public static int NONE = TransferHandler.NONE;

	
	private Class<? extends Selection> selectionClass;
	private int actions;
	private ArrayList<DataExporterInterface<SelectionContentsType>> exporters = new ArrayList<DataExporterInterface<SelectionContentsType>>();
	
	
	public AbstractSelectionExporter(Class<? extends Selection> selectionClass, int actions, List<? extends DataExporterInterface<SelectionContentsType>> exporters)
	{
		this.selectionClass = selectionClass;
		this.actions = actions;
		this.exporters.addAll( exporters );
	}
	
	public AbstractSelectionExporter(Class<? extends Selection> selectionClass, List<? extends DataExporterInterface<SelectionContentsType>> exporters)
	{
		this( selectionClass, COPY_OR_MOVE, exporters );
	}
	
	
	
	public List<DataExporterInterface<SelectionContentsType>> getExporters()
	{
		return exporters;
	}

	public void addDataExporter(DataExporterInterface<SelectionContentsType> exporter)
	{
		exporters.add( 0, exporter );
	}
	
	
	

	public Class<? extends Selection> getSelectionClass()
	{
		return selectionClass;
	}

	public int getActions()
	{
		return actions;
	}
	

	
	//
	//
	// OVERRIDE THESE TWO (exportDone optional)
	//
	//
	
	abstract protected SelectionContentsType getSelectionContents(SelectionType selection);
	
	protected void exportDone(SelectionType selection, Target target, SelectionContentsType selectionContents, int action)
	{
	}
	
	
	
	

	// 
	//
	// Methods used the ClipboardHandler
	//
	//
	
	protected SelectionContentsTransferable<SelectionContentsType, SelectionType> createExportTransferable(Selection selection)
	{
		if ( !selectionClass.isInstance( selection ) )
		{
			throw new RuntimeException( "SelectionExporter.createExportTransferable(): selection is not an instance of " + selectionClass.getName() );
		}
		
		@SuppressWarnings("unchecked")
		SelectionContentsType contents = getSelectionContents( (SelectionType)selection );
		
		if ( contents != null )
		{
			@SuppressWarnings("unchecked")
			SelectionContentsTransferable<SelectionContentsType, SelectionType> xfer = new SelectionContentsTransferable<SelectionContentsType, SelectionType>( this, contents, (SelectionType)selection );
			return xfer;
		}
		else
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected void exportTransferableDone(Selection selection, Target target, Transferable transferable, int action)
	{
		if ( !selectionClass.isInstance( selection ) )
		{
			throw new RuntimeException( "SelectionExporter.exportDone(): selection is not an instance of " + selectionClass.getName() );
		}
		
		if ( transferable != null )
		{
			SelectionContentsTransferable<SelectionContentsType, SelectionType> t = (SelectionContentsTransferable<SelectionContentsType, SelectionType>)transferable;
			
			exportDone( (SelectionType)selection, target, t.selectionContents, action );
		}
	}
}
