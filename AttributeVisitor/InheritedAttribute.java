//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeVisitor;

import BritefuryJ.DocModel.DMNode;

public class InheritedAttribute extends AttributeBase
{
	public InheritedAttribute(String name)
	{
		super( name );
	}
	
	
	protected AVResult getFallbackResult(IncrementalAttributeVisitorEvaluator evaluator, DMNode node, AttributeEvaluationTable attribEvalTable)
	{
		DMNode p = parentOf( node );
		while ( p != null )
		{
			AVResult result = attribEvalTable.lookupAttributeResult( evaluator, this, p );
			if ( result != null )
			{
				return result;
			}
			else
			{
				p = parentOf( p );
			}
		}
		throw new NoAttributeEvaluationFunctionException();
	}
	
	
	private DMNode parentOf(DMNode node)
	{
		return node.getParent();
	}


	public String toString()
	{
		return "InheritedAttribute( name='" + name + "' )"; 
	}
}
