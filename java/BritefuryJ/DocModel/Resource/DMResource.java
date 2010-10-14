//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocModel.Resource;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import BritefuryJ.DocModel.DMNode;

public abstract class DMResource extends DMNode implements Serializable
{
	protected static class ChildrenIterator implements Iterator<Object>
	{
		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Object next()
		{
			throw new NoSuchElementException();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	protected static class ChildrenIterable implements Iterable<Object>
	{
		private static ChildrenIterator iter = new ChildrenIterator();
		
		@Override
		public Iterator<Object> iterator()
		{
			return iter;
		}
	}
	
	protected static ChildrenIterable childrenIterable = new ChildrenIterable();
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected String serialised;
	
	
	protected DMResource()
	{
	}
	
	protected DMResource(String serialised)
	{
		this.serialised = serialised;
	}



	public abstract Object getValue();

	
	public abstract String getSerialisedForm();
}
