//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocTree;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;

import BritefuryJ.DocModel.DMListInterface;


public class DocTree
{
	public static class NodeTypeNotSupportedException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	
	
	private static class Key
	{
		private WeakReference<Object> docNode;
		private WeakReference<DocTreeNode> parentTreeNode;
		private int index;
		private int hash;
		
		public Key(Object docNode, DocTreeNode parentTreeNode, int index)
		{
			this.docNode = new WeakReference<Object>( docNode );
			if ( parentTreeNode != null )
			{
				this.parentTreeNode = new WeakReference<DocTreeNode>( parentTreeNode );
			}
			this.index = index;
			
			this.hash = tripleHash( docNode.hashCode(), parentTreeNode != null  ?  parentTreeNode.hashCode()  :  0, index );
		}
		
		public Key(Object docNode, DocTreeNode parentTreeNode, int index, DocTreeNodeTable table)
		{
			this.docNode = new WeakReference<Object>( docNode, table.refQueue );
			if ( parentTreeNode != null )
			{
				this.parentTreeNode = new WeakReference<DocTreeNode>( parentTreeNode, table.refQueue );
			}
			this.index = index;
			
			this.hash = tripleHash( docNode.hashCode(), parentTreeNode != null  ?  parentTreeNode.hashCode()  :  0, index );
		}
		
		
		public int hashCode()
		{
			return hash;
		}
		
		
		public boolean equals(Object x)
		{
			if ( x instanceof Key )
			{
				Key kx = (Key)x;
				if ( parentTreeNode == null )
				{
					return docNode.get() == kx.docNode.get()  &&  kx.parentTreeNode == null  &&  index == kx.index;
				}
				else
				{
					return docNode.get() == kx.docNode.get()  &&  parentTreeNode.get() == kx.parentTreeNode.get()  &&  index == kx.index;
				}
			}
			else
			{
				return false;
			}
		}
		
		
		public DocTreeKey docTreeKey()
		{
			if ( parentTreeNode != null )
			{
				return new DocTreeKey( docNode.get(), parentTreeNode.get(), index );
			}
			else
			{
				return new DocTreeKey( docNode.get(), null, index );
			}
		}
		
		
		public boolean hasWeakRef(Reference<? extends Object> r)
		{
			return r == docNode  ||  r == parentTreeNode;
		}
		
		private static int tripleHash(int a, int b, int c)
		{
			int mult = 1000003;
			int x = 0x345678;
			x = ( x ^ c ) * mult;
			mult += 82520 + 4;
			x = ( x ^ b ) * mult;
			mult += 82520 + 2;
			x = ( x ^ a ) * mult;
			return x + 97351;
		}
		
		
		public String toString()
		{
			String d = "<null>", p = "<null>";
			if ( docNode.get() != null )
			{
				d = docNode.get().toString();
			}
			if ( parentTreeNode != null )
			{
				if ( parentTreeNode.get() != null )
				{
					p = parentTreeNode.get().toString();
				}
			}
			return "Key( docNode=" + d + ", parentTreeNode=" + p + ", index=" + index + ", hash=" + hash + ")";
		}
	}
	
	
	private static class DocTreeKeyError extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	private static class DocTreeKey
	{
		private WeakReference<Object> docNode;
		private WeakReference<DocTreeNode> parentTreeNode;
		private int index;
		
		public DocTreeKey(Object docNode, DocTreeNode parentTreeNode, int index)
		{
			this.docNode = new WeakReference<Object>( docNode );
			if ( parentTreeNode != null )
			{
				this.parentTreeNode = new WeakReference<DocTreeNode>( parentTreeNode );
			}
			this.index = index;
		}
		
		
		public Object getDocNode()
		{
			return docNode.get();
		}
		
		public DocTreeNode getParentTreeNode()
		{
			if ( parentTreeNode != null )
			{
				return parentTreeNode.get();
			}
			else
			{
				return null;
			}
		}
		
		public int getIndex()
		{
			return index;
		}
		
		
		public Key key() throws DocTreeKeyError
		{
			Object docNode = getDocNode();
			DocTreeNode parent = getParentTreeNode();
			if ( docNode == null  ||  ( parentTreeNode != null  &&  parent == null ) )
			{
				throw new DocTreeKeyError();
			}
			return new Key( docNode, parent, index );
		}

		public Key key(DocTreeNodeTable table) throws DocTreeKeyError
		{
			Object docNode = getDocNode();
			DocTreeNode parent = getParentTreeNode();
			if ( docNode == null  ||  ( parentTreeNode != null  &&  parent == null ) )
			{
				throw new DocTreeKeyError();
			}
			return new Key( docNode, parent, index, table );
		}

	
		public String toString()
		{
			String d = "<null>", p = "<null>";
			if ( docNode.get() != null )
			{
				d = docNode.get().toString();
			}
			if ( parentTreeNode != null )
			{
				if ( parentTreeNode.get() != null )
				{
					p = parentTreeNode.get().toString();
				}
			}
			return "DocTreeKey( docNode=" + d + ", parentTreeNode=" + p + ", index=" + index + ")";
		}
	}



	private static class DocTreeNodeTable
	{
		private ReferenceQueue<Object> refQueue;
		private HashMap<Key,DocTreeNode> table;
		
		public DocTreeNodeTable()
		{
			refQueue = new ReferenceQueue<Object>();
			table = new HashMap<Key,DocTreeNode>();
		}
		
		
		public DocTreeNode get(DocTreeKey k)
		{
			try
			{
				return table.get( k.key() );
			}
			catch (DocTreeKeyError e)
			{
				return null;
			}
		}

		public void put(DocTreeKey k, DocTreeNode v)
		{
			removeDeadEntries();
			try
			{
				table.put( k.key(), v );
			}
			catch (DocTreeKeyError e)
			{
			}
		}
		
		public void remove(DocTreeKey k)
		{
			removeDeadEntries();
			try
			{
				table.remove( k.key() );
			}
			catch (DocTreeKeyError e)
			{
			}
		}
		
		public boolean containsKey(DocTreeKey k)
		{
			try
			{
				return table.containsKey( k.key() );
			}
			catch (DocTreeKeyError e)
			{
				return false;
			}
		}
		
		
		@SuppressWarnings("unchecked")
		private void removeDeadEntries()
		{
			Reference<Object> r = (Reference<Object>)refQueue.poll();
			
			while ( r != null )
			{
				r = (Reference<Object>)refQueue.poll();
				
				HashSet<Key> keysToRemove = new HashSet<Key>();
				for (Key k: table.keySet())
				{
					if ( k.hasWeakRef( r ) )
					{
						keysToRemove.add( k );
					}
				}
				
				for (Key k: keysToRemove)
				{
					table.remove( k );
				}
			}
		}
	}
	
	
	
	
	private DocTreeNodeTable table;
	
	
	public DocTree()
	{
		table = new DocTreeNodeTable();
	}
	
	
	public DocTreeNode treeNode(Object x)
	{
		return treeNode( x, null, -1 );
	}
	
	
/*	public DocTreeNode treeNode(DMListInterface x, DocTreeNode parentTreeNode, int indexInParent)
	{
		DocTreeKey key = new DocTreeKey( x, parentTreeNode, indexInParent );
		
		DocTreeNode node = table.get( key );
		
		if ( node != null )
		{
			return node;
		}
		else
		{
			node = new DocTreeList( this, (DMListInterface)x, parentTreeNode, indexInParent );

			table.put( key, node );
				
			return node;
		}
	}
	
	public DocTreeNode treeNode(String x, DocTreeNode parentTreeNode, int indexInParent)
	{
		DocTreeKey key = new DocTreeKey( x, parentTreeNode, indexInParent );
		
		DocTreeNode node = table.get( key );
		
		if ( node != null )
		{
			return node;
		}
		else
		{
			node = new DocTreeString( (String)x, parentTreeNode, indexInParent );

			table.put( key, node );
				
			return node;
		}
	}*/

	public DocTreeNode treeNode(Object x, DocTreeNode parentTreeNode, int indexInParent)
	{
		DocTreeKey key = new DocTreeKey( x, parentTreeNode, indexInParent );
		
		DocTreeNode node = table.get( key );
		
		if ( node != null )
		{
			return node;
		}
		else
		{
			if ( x instanceof String )
			{
				node = new DocTreeString( (String)x, parentTreeNode, indexInParent );
			}
			else if ( x instanceof DMListInterface )
			{
				node = new DocTreeList( this, (DMListInterface)x, parentTreeNode, indexInParent );
			}
			else
			{
				node = new DocTreeNodeObject( x, parentTreeNode, indexInParent );
			}

			table.put( key, node );
				
			return node;
		}
	}
}

