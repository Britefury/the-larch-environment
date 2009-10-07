//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.python.core.Py;
import org.python.core.PyObject;

public class SimpleDndHandler extends DndHandler
{
	public static interface SourceDataFn
	{
		public Object createSourceData(PointerInputElement sourceElement);
	}
	
	public static class PySourceDataFn implements SourceDataFn
	{
		private PyObject fn;
		
		public PySourceDataFn(PyObject fn)
		{
			this.fn = fn;
		}
		
		public Object createSourceData(PointerInputElement sourceElement)
		{
			return Py.tojava( fn.__call__( Py.java2py( sourceElement ) ), Object.class );
		}
	}
	
	
	public static interface ExportDoneFn
	{
		public void exportDone(PointerInputElement sourceElement, Object data, int action);
	}

	public static class PyExportDoneFn implements ExportDoneFn
	{
		private PyObject fn;
		
		public PyExportDoneFn(PyObject fn)
		{
			this.fn = fn;
		}
		
		public void exportDone(PointerInputElement sourceElement, Object data, int action)
		{
			fn.__call__( Py.java2py( sourceElement ), Py.java2py( data ), Py.newInteger( action ) );
		}
	}
	
	
	public static interface DropFn
	{
		public boolean acceptDrop(PointerInputElement destElement, Object data);
	}
	
	public static class PyDropFn implements DropFn
	{
		private PyObject fn;
		
		public PyDropFn(PyObject fn)
		{
			this.fn = fn;
		}
		
		public boolean acceptDrop(PointerInputElement destElement, Object data)
		{
			return Py.py2boolean( fn.__call__( Py.java2py( destElement ), Py.java2py( data ) ) );
		}
	}
	
	
	public static interface CanDropFn
	{
		public boolean canDrop(PointerInputElement destElement, Object data);
	}
	
	public static class PyCanDropFn implements CanDropFn
	{
		private PyObject fn;
		
		public PyCanDropFn(PyObject fn)
		{
			this.fn = fn;
		}
		
		public boolean canDrop(PointerInputElement destElement, Object data)
		{
			return Py.py2boolean( fn.__call__( Py.java2py( destElement ), Py.java2py( data ) ) );
		}
	}
	
	
	
	private static class SimpleDataFlavor extends DataFlavor
	{
		public static SimpleDataFlavor flavor = new SimpleDataFlavor();
		
		
		public SimpleDataFlavor()
		{
			super( Object.class, DataFlavor.javaJVMLocalObjectMimeType );
		}
	}
	
	private static class SimpleTransferData
	{
		private PointerInputElement sourceElement;
		private SimpleDndHandler handler;
		private SourceEntry acceptedSourceEntry;
		private Object acceptedDragData;
		private HashMap<Object,Object> dragDataTable;
		
		
		public SimpleTransferData(PointerInputElement sourceElement, SimpleDndHandler handler)
		{
			this.sourceElement = sourceElement;
			this.handler = handler;
			dragDataTable = new HashMap<Object,Object>();
		}
		
		
		
		public Object getDragDataForKey(Object key, SourceEntry sourceEntry)
		{
			if ( dragDataTable.containsKey( key ) )
			{
				return dragDataTable.get( key );
			}
			else
			{
				Object dragData = sourceEntry.sourceDataFn.createSourceData( sourceElement );
				dragDataTable.put( key, dragData );
				return dragData;
			}
		}
		
		public void acceptDrop(SourceEntry sourceEntry, Object dragData)
		{
			acceptedSourceEntry = sourceEntry;
			acceptedDragData = dragData;
		}
	}
	
	private static class SimpleTransferable implements Transferable
	{
		private PointerInputElement sourceElement;
		private SimpleDndHandler handler;
		
		
		public SimpleTransferable(PointerInputElement sourceElement, SimpleDndHandler handler)
		{
			this.sourceElement = sourceElement;
			this.handler = handler;
		}
		
		
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
		{
			if ( flavor == SimpleDataFlavor.flavor )
			{
				return new SimpleTransferData( sourceElement, handler );
			}
			else
			{
				throw new UnsupportedFlavorException( flavor );
			}
		}

		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] { SimpleDataFlavor.flavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return flavor == SimpleDataFlavor.flavor;
		}
	}
	
	
	
	private static class SourceEntry
	{
		private SourceDataFn sourceDataFn;
		private ExportDoneFn exportDoneFn;
		
		public SourceEntry(SourceDataFn sourceDataFn, ExportDoneFn exportDoneFn)
		{
			this.sourceDataFn = sourceDataFn;
			this.exportDoneFn = exportDoneFn;
		}
	}
	
	private static class DestEntry
	{
		private Object key;
		private DropFn dropFn;
		private CanDropFn canDropFn;
		
		public DestEntry(Object key, DropFn dropFn, CanDropFn canDropFn)
		{
			this.key = key;
			this.dropFn = dropFn;
			this.canDropFn = canDropFn;
		}
	}
	

	
	
	
	private HashMap<Object, SourceEntry> sourceTable;
	private ArrayList<DestEntry> destTable;
	
	
	public SimpleDndHandler()
	{
		sourceTable = new HashMap<Object, SourceEntry>();
		destTable = new ArrayList<DestEntry>();
	}
	
	
	public void registerSource(Object key, SourceDataFn sourceDataFn, ExportDoneFn exportDoneFn)
	{
		sourceTable.put( key, new SourceEntry( sourceDataFn, exportDoneFn ) );
	}
	
	public void registerSource(Object key, SourceDataFn sourceDataFn)
	{
		sourceTable.put( key, new SourceEntry( sourceDataFn, null ) );
	}
	
	public void registerSource(Object key, PyObject sourceDataFn, PyObject exportDoneFn)
	{
		registerSource( key, new PySourceDataFn( sourceDataFn ), new PyExportDoneFn( exportDoneFn ) );
	}
	
	public void registerSource(Object key, PyObject sourceDataFn)
	{
		registerSource( key, new PySourceDataFn( sourceDataFn ) );
	}
	
	
	public void registerDest(Object key, DropFn dropFn)
	{
		destTable.add( new DestEntry( key, dropFn, null ) );
	}
	
	public void registerDest(Object key, PyObject dropFn)
	{
		registerDest( key, new PyDropFn( dropFn ) );
	}
	
	public void registerDest(Object key, DropFn dropFn, CanDropFn canDropFn)
	{
		destTable.add( new DestEntry( key, dropFn, canDropFn ) );
	}
	
	public void registerDest(Object key, PyObject dropFn, PyObject canDropFn)
	{
		registerDest( key, new PyDropFn( dropFn ), new PyCanDropFn( canDropFn ) );
	}
	
	
	
	
	public int getSourceRequestedAction(PointerInputElement sourceElement, PointerInterface pointer, int button)
	{
		int modifiers = pointer.getModifiers();
		int mods = modifiers & ( Modifier.CTRL | Modifier.SHIFT );
		if ( mods == Modifier.CTRL )
		{
			return COPY;
		}
		else if ( mods == ( Modifier.CTRL | Modifier.SHIFT ) )
		{
			return LINK;
		}
		else if ( mods == 0 )
		{
			return MOVE;
		}
		else
		{
			return COPY;
		}
	}
	
	public Transferable createTransferable(PointerInputElement sourceElement)
	{
		return new SimpleTransferable( sourceElement, this );
	}
	
	public void exportDone(PointerInputElement sourceElement, Transferable data, int action)
	{
		SimpleTransferData sourceData = null;
		try
		{
			sourceData = (SimpleTransferData)data.getTransferData( SimpleDataFlavor.flavor );
		}
		catch (UnsupportedFlavorException e)
		{
			return;
		}
		catch (IOException e)
		{
			return;
		}
		
		ExportDoneFn exportDoneFn = sourceData.acceptedSourceEntry.exportDoneFn;
		exportDoneFn.exportDone( sourceElement, sourceData.acceptedDragData, action );
	}

	
	
	
	public boolean canDrop(PointerInputElement destElement, DndDrop drop)
	{
		SimpleTransferData transferData = null;
		try
		{
			transferData = (SimpleTransferData)drop.getTransferable().getTransferData( SimpleDataFlavor.flavor );
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
		
		
		for (DestEntry destEntry: destTable)
		{
			SourceEntry sourceEntry = transferData.handler.sourceTable.get( destEntry.key );
			if ( sourceEntry != null )
			{
				if ( destEntry.canDropFn != null )
				{
					Object dragData = transferData.getDragDataForKey( destEntry.key, sourceEntry );
					return destEntry.canDropFn.canDrop( destElement, dragData );
				}
				else
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean acceptDrop(PointerInputElement destElement, DndDrop drop)
	{
		SimpleTransferData transferData = null;
		try
		{
			transferData = (SimpleTransferData)drop.getTransferable().getTransferData( SimpleDataFlavor.flavor );
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}

	
		
		
		for (DestEntry destEntry: destTable)
		{
			SourceEntry sourceEntry = transferData.handler.sourceTable.get( destEntry.key );
			if ( sourceEntry != null )
			{
				Object dragData = transferData.getDragDataForKey( destEntry.key, sourceEntry );
				boolean bResult = destEntry.dropFn.acceptDrop( destElement, dragData );
				if ( bResult )
				{
					transferData.acceptDrop( sourceEntry, dragData );
					return true;
				}
			}
		}
		return false;
	}
}
