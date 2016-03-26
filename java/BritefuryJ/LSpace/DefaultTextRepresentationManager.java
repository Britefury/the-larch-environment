//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;



class DefaultTextRepresentationManager extends AbstractTextRepresentationManager
{
	protected DefaultTextRepresentationManager()
	{
		super();
	}

	protected DefaultTextRepresentationManager(ElementValueCache<String> cache)
	{
		super( cache );
	}

	
	
	@Override
	protected String getElementContent(LSElement e)
	{
		return e.getLeafTextRepresentation();
	}
}
