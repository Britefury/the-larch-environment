//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.IncrementalView;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Stack;


public class IncrementalViewTable
{
	private static class TableForModel
	{
		private IncrementalViewTable table;
		private Object model;
		
		private HashSet<FragmentView> refedNodes = new HashSet<FragmentView>();
		private HashSet<FragmentView> unrefedNodes;
		
		
		
		public TableForModel(IncrementalViewTable table, Object model)
		{
			this.table = table;
			this.model = model;
		}
		
		
		private void addUnrefedFragment(FragmentView fragment)
		{
			if ( unrefedNodes == null )
			{
				unrefedNodes = new HashSet<FragmentView>();
			}
			unrefedNodes.add( fragment );
		}
		
		private void removeUnrefedNode(FragmentView fragment)
		{
			if ( unrefedNodes != null )
			{
				unrefedNodes.remove( fragment );
				if ( unrefedNodes.isEmpty() )
				{
					unrefedNodes = null;
				}
			}
		}
		
		private FragmentView getUnrefedFragmentFor(IncrementalView.FragmentFactory nodeResultFactory)
		{
			if ( unrefedNodes != null )
			{
				for (FragmentView node: unrefedNodes)
				{
					if ( node.getFragmentFactory() == nodeResultFactory )
					{
						return node;
					}
				}
			}
			return null;
		}
		
		
		
		public Collection<FragmentView> getRefedFragments()
		{
			return refedNodes;
		}
		
		
		public int size()
		{
			return refedNodes.size();
		}
		
		public int getNumUnrefedFragments()
		{
			return unrefedNodes != null  ?  unrefedNodes.size()  :  0;
		}
		
		
		
		public void refFragment(FragmentView incrementalNode)
		{
			removeUnrefedNode( incrementalNode );
			refedNodes.add( incrementalNode );
		}
		
		public void unrefFragment(FragmentView incrementalNode)
		{
			refedNodes.remove( incrementalNode );
			addUnrefedFragment( incrementalNode );
		}
		
		
		
		
		private void clean()
		{
			unrefedNodes = null;
			if ( refedNodes.size() == 0 )
			{
				table.removeViewTable( model );
			}
		}
	}
	
	
	
	private IdentityHashMap<Object, TableForModel> table = new IdentityHashMap<Object, TableForModel>();
	private HashSet<FragmentView> unrefedFragments = new HashSet<FragmentView>();
	
	
	
	
	
	public IncrementalViewTable()
	{
	}
	
	
	

	public FragmentView getUnrefedFragmentForModel(Object node, IncrementalView.FragmentFactory resultFactory)
	{
		TableForModel subTable = table.get( node );
		if ( subTable != null )
		{
			return subTable.getUnrefedFragmentFor( resultFactory );
		}
		else
		{
			return null;
		}
	}
	
	
	public Collection<FragmentView> get(Object node)
	{
		TableForModel subTable = table.get( node );
		if ( subTable != null )
		{
			return subTable.getRefedFragments();
		}
		else
		{
			return Arrays.asList();
		}
	}
	
	
	public boolean containsKey(Object node)
	{
		return getNumFragmentsForModel( node ) > 0;
	}
	
	
	public int size()
	{
		int s = 0;
		for (TableForModel subTable: table.values())
		{
			s += subTable.size();
		}
		return s;
	}
	
	public int getNumModels()
	{
		return table.size();
	}
	
	public int getNumFragmentsForModel(Object node)
	{
		TableForModel subTable = table.get( node );
		if ( subTable != null )
		{
			return subTable.size();
		}
		else
		{
			return 0;
		}
	}
	
	public int getNumUnrefedFragmentsForModel(Object node)
	{
		TableForModel subTable = table.get( node );
		if ( subTable != null )
		{
			return subTable.getNumUnrefedFragments();
		}
		else
		{
			return 0;
		}
	}
	
	public void clean()
	{
		// We need to remove all nodes within the sub-trees rooted at the unrefed nodes
		Stack<FragmentView> unrefedStack = new Stack<FragmentView>();
		unrefedStack.addAll( unrefedFragments );
		
		while ( !unrefedStack.isEmpty() )
		{
			FragmentView fragment = unrefedStack.pop();
			
			for (FragmentView child: fragment.getChildren())
			{
				unrefedStack.push( child );
			}

			TableForModel subTable = table.get( fragment.getModel() );
			if ( subTable != null )
			{
				subTable.clean();
			}
			
			fragment.dispose();
		}
		
		unrefedFragments.clear();
	}
	
	
	protected void refFragment(FragmentView node)
	{
		node.setRefStateRefed();
		Object model = node.getModel();
		TableForModel subTable = table.get( model );
		if ( subTable == null )
		{
			subTable = new TableForModel( this, model );
			table.put( model, subTable );
		}
		subTable.refFragment( node );
		unrefedFragments.remove( node );
	}

	protected void unrefFragment(FragmentView node)
	{
		node.setRefStateUnrefed();
		Object key = node.getModel();
		TableForModel subTable = table.get( key );
		if ( subTable == null )
		{
			subTable = new TableForModel( this, key );
			table.put( key, subTable );
		}
		subTable.unrefFragment( node );
		unrefedFragments.add( node );
	}


	

	private void removeViewTable(Object key)
	{
		table.remove( key );
	}
}
