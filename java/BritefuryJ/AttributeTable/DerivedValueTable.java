//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.AttributeTable;


public abstract class DerivedValueTable <V>
{
	private AttributeNamespace namespace;
	
	
	public DerivedValueTable(AttributeNamespace namespace)
	{
		this.namespace = namespace;
	}
	
	
	public AttributeNamespace getNamespace()
	{
		return namespace;
	}
	
	
	public V get(AttributeTable attribs)
	{
		return attribs.getDerivedValuesForTable( this );
	}
	
	
	protected abstract V evaluate(AttributeTable attribs);
}
