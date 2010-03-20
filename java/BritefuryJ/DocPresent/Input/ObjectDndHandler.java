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
import java.util.Arrays;
import java.util.HashMap;

import BritefuryJ.DocPresent.Clipboard.LocalDataFlavor;
import BritefuryJ.Math.Point2;
import BritefuryJ.Utils.HashUtils;


public class ObjectDndHandler extends DndHandler
{
	public static interface SourceDataFn
	{
		public Object createSourceData(PointerInputElement sourceElement);
	}
	
	public static interface ExportDoneFn
	{
		public void exportDone(PointerInputElement sourceElement, Object data, int action);
	}
	
	public static interface DropFn
	{
		public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data);
	}
	
	public static interface CanDropFn
	{
		public boolean canDrop(PointerInputElement destElement, Point2 targetPosition, Object data);
	}

	
	
	public static class DndSource
	{
		private Class<?> dataType;
		private SourceDataFn sourceDataFn;
		private ExportDoneFn exportDoneFn;
		
		
		public DndSource(Class<?> dataType, SourceDataFn sourceDataFn, ExportDoneFn exportDoneFn)
		{
			this.dataType = dataType;
			this.sourceDataFn = sourceDataFn;
			this.exportDoneFn = exportDoneFn;
		}

		public DndSource(Class<?> dataType, SourceDataFn sourceDataFn)
		{
			this( dataType, sourceDataFn, null );
		}
		
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			
			if ( x instanceof DndSource )
			{
				DndSource sx = (DndSource)x;
				
				return dataType.equals( sx.dataType )  &&
					( sourceDataFn != null  ?  sourceDataFn.equals( sx.sourceDataFn )  :  sourceDataFn == sx.sourceDataFn )  &&
					( exportDoneFn != null  ?  exportDoneFn.equals( sx.exportDoneFn )  :  exportDoneFn == sx.exportDoneFn );
			}
			
			return false;
		}

		public int hashCode()
		{
			int a = dataType.hashCode();
			int b = sourceDataFn != null  ?  sourceDataFn.hashCode()  :  0;
			int c = exportDoneFn != null  ?  exportDoneFn.hashCode()  :  0;
			return HashUtils.tripleHash( a, b, c );
		}
	}
	
	public static class DndDest
	{
		private Class<?> dataType;
		private CanDropFn canDropFn;
		private DropFn dropFn;
		
		
		public DndDest(Class<?> dataType, CanDropFn canDropFn, DropFn dropFn)
		{
			this.dataType = dataType;
			this.canDropFn = canDropFn;
			this.dropFn = dropFn;
		}

		public DndDest(Class<?> dataType, DropFn dropFn)
		{
			this( dataType, null, dropFn );
		}
		
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			
			if ( x instanceof DndDest )
			{
				DndDest dx = (DndDest)x;
				
				return dataType.equals( dx.dataType )  &&
					( canDropFn != null  ?  canDropFn.equals( dx.canDropFn )  :  canDropFn == dx.canDropFn )  &&
					( dropFn != null  ?  dropFn.equals( dx.dropFn )  :  dropFn == dx.dropFn );
			}
			
			return false;
		}

		public int hashCode()
		{
			int a = dataType.hashCode();
			int b = canDropFn != null  ?  canDropFn.hashCode()  :  0;
			int c = dropFn != null  ?  dropFn.hashCode()  :  0;
			return HashUtils.tripleHash( a, b, c );
		}
	}
	
	
	
	private static class TransferMatch
	{
		private DndSource source;
		private DndDest dest;
		private Object dragData;
		private boolean bHasDragData;
		
		public TransferMatch(DndSource source, DndDest dest, Object dragData, boolean bHasDragData)
		{
			this.source = source;
			this.dest = dest;
			this.dragData = dragData;
			this.bHasDragData = bHasDragData;
		}
	}
	
	
	private static class ObjectDndTransferData
	{
		private PointerInputElement sourceElement;
		private ObjectDndHandler sourceHandler;
		private ArrayList<TransferMatch> transferMatches;
		private TransferMatch acceptedMatch;
		private HashMap<DndSource, Object> dragDataTable = new HashMap<DndSource, Object>();
		
		
		public ObjectDndTransferData(PointerInputElement sourceElement, ObjectDndHandler sourceHandler)
		{
			this.sourceElement = sourceElement;
			this.sourceHandler = sourceHandler;
		}
		
		
		
		public Object getDragDataForSrc(DndSource src)
		{
			if ( dragDataTable.containsKey( src ) )
			{
				return dragDataTable.get( src );
			}
			else
			{
				Object dragData = src.sourceDataFn.createSourceData( sourceElement );
				dragDataTable.put( src, dragData );
				return dragData;
			}
		}
		
		public void acceptDrop(TransferMatch match)
		{
			acceptedMatch = match;
		}
	}
	
	private static class ObjectDndDataFlavor extends LocalDataFlavor
	{
		public static ObjectDndDataFlavor flavor = new ObjectDndDataFlavor();
		
		
		private ObjectDndDataFlavor()
		{
			super( ObjectDndTransferData.class );
		}
	}
	
	private static class ObjectDndTransferable implements Transferable
	{
		private PointerInputElement sourceElement;
		private ObjectDndHandler handler;
		private ObjectDndTransferData objectDndTransferData;
		
		
		public ObjectDndTransferable(PointerInputElement sourceElement, ObjectDndHandler handler)
		{
			this.sourceElement = sourceElement;
			this.handler = handler;
		}
		
		
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
		{
			if ( flavor.equals( ObjectDndDataFlavor.flavor ) )
			{
				if ( objectDndTransferData == null )
				{
					objectDndTransferData =  new ObjectDndTransferData( sourceElement, handler );
				}
				return objectDndTransferData;
			}
			else
			{
				throw new UnsupportedFlavorException( flavor );
			}
		}

		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] { ObjectDndDataFlavor.flavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return flavor.equals( ObjectDndDataFlavor.flavor );
		}
	}
	
	
	

	
	
	private ArrayList<DndSource> sources = new ArrayList<DndSource>();
	private ArrayList<DndDest> dests = new ArrayList<DndDest>();
	private HashMap<Class<?>, DndDest> typeToDest = new HashMap<Class<?>, DndDest>();
	
	
	
	public ObjectDndHandler(DndSource sources[], DndDest dests[])
	{
		this.sources.addAll( Arrays.asList( sources ) );
		this.dests.addAll( Arrays.asList( dests ) );
		
		for (DndDest dest: dests)
		{
			typeToDest.put( dest.dataType, dest );
		}
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
		return new ObjectDndTransferable( sourceElement, this );
	}
	
	public void exportDone(PointerInputElement sourceElement, Transferable data, int action)
	{
		ObjectDndTransferData sourceData = null;
		try
		{
			sourceData = (ObjectDndTransferData)data.getTransferData( ObjectDndDataFlavor.flavor );
		}
		catch (UnsupportedFlavorException e)
		{
			return;
		}
		catch (IOException e)
		{
			return;
		}
		
		ExportDoneFn exportDoneFn = sourceData.acceptedMatch.source.exportDoneFn;
		exportDoneFn.exportDone( sourceElement, sourceData.acceptedMatch.dragData, action );
	}

	
	
	public boolean canDrop(PointerInputElement destElement, DndDrop drop)
	{
		ObjectDndTransferData transferData = null;
		try
		{
			transferData = (ObjectDndTransferData)drop.getTransferable().getTransferData( ObjectDndDataFlavor.flavor );
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
		
		
		transferData.transferMatches = negotiateTransferMatches( transferData, destElement, drop );
		
		return transferData.transferMatches.size() > 0;
	}
	
	public boolean acceptDrop(PointerInputElement destElement, DndDrop drop)
	{
		ObjectDndTransferData transferData = null;
		try
		{
			transferData = (ObjectDndTransferData)drop.getTransferable().getTransferData( ObjectDndDataFlavor.flavor );
		}
		catch (UnsupportedFlavorException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}

		
		ArrayList<TransferMatch> matches = transferData.transferMatches;
		if ( matches == null )
		{
			throw new RuntimeException( "canDrop not invoked - no transfer matches available" );
		}
		
		for (int i = matches.size() - 1; i >= 0; i--)
		{
			TransferMatch match = matches.get( i );
			if ( !match.bHasDragData )
			{
				match.dragData = transferData.getDragDataForSrc( match.source );
				match.bHasDragData = true;
			}
			boolean bResult = match.dest.dropFn.acceptDrop( destElement, drop.getTargetPosition(), match.dragData );
			if ( bResult )
			{
				transferData.acceptDrop( match );
				return true;
			}
		}
		return false;
	}

	
	
	private ArrayList<TransferMatch> negotiateTransferMatches(ObjectDndTransferData transferData, PointerInputElement destElement, DndDrop drop)
	{
		ArrayList<TransferMatch> matches = new ArrayList<TransferMatch>();
		
		ObjectDndHandler sourceHandler = transferData.sourceHandler;
		for (DndSource src: sourceHandler.sources)
		{
			DndDest dest = getDestForType( src.dataType );
			
			if ( dest != null )
			{
				boolean bCanDrop = true;
				Object dragData = null;
				boolean bHasDragData = false;
				if ( dest.canDropFn != null )
				{
					dragData = transferData.getDragDataForSrc( src );
					bCanDrop = dest.canDropFn.canDrop( destElement, drop.getTargetPosition(), dragData );
					bHasDragData = true;
				}

				if ( bCanDrop )
				{
					boolean bInserted = false;
					for (int i = 0; i < matches.size(); i++)
					{
						TransferMatch match = matches.get( i );
						if ( match.source.dataType.isAssignableFrom( src.dataType ) )
						{
							matches.set( i, new TransferMatch( src, dest, dragData, bHasDragData ) );
						}
					}
					
					if ( !bInserted )
					{
						matches.add( new TransferMatch( src, dest, dragData, bHasDragData ) );
					}
				}
			}
		}
		
		return matches;
	}

	private DndDest getDestForType(Class<?> type)
	{
		if ( typeToDest.containsKey( type ) )
		{
			return typeToDest.get( type );
		}
		else
		{
			Class<?> superClass = type.getSuperclass();
			DndDest destForSuperClass = null;  
			
			if ( superClass != null )
			{
				destForSuperClass = getDestForType( superClass );
			}
			
			typeToDest.put( type, destForSuperClass );
			return destForSuperClass;
		}
	}
}
