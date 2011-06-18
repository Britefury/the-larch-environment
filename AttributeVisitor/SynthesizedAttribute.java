//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeVisitor;

import java.util.LinkedList;

import BritefuryJ.DocModel.DMNode;

public class SynthesizedAttribute extends AttributeBase
{
	public SynthesizedAttribute(String name)
	{
		super( name );
	}
	
	
	protected AVResult getFallbackResult(IncrementalAttributeVisitorEvaluator evaluator, DMNode node, AttributeEvaluationTable attribEvalTable)
	{
		LinkedList<DMNode> queue = new LinkedList<DMNode>();
		queue.addFirst( node );
		while ( !queue.isEmpty() )
		{
			DMNode n = queue.removeLast();
			AVResult result = attribEvalTable.lookupAttributeResult( evaluator, this, n );
			if ( result == null )
			{
				for (Object c: n.getChildren())
				{
					if ( c instanceof DMNode )
					{
						queue.addFirst( (DMNode)c );
					}
				}
			}
			else
			{
				result.incr.onAccess();
				evaluator.linkInwardDependency( result );
			}
		}
		return null;
	}


	public String toString()
	{
		return "SynthesizedAttribute( name='" + name + "' )"; 
	}
}
