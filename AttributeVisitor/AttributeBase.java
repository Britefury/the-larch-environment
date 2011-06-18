//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeVisitor;

import BritefuryJ.DocModel.DMNode;

public abstract class AttributeBase
{
	protected final String name;
	
	
	public AttributeBase(String name)
	{
		this.name = name;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	
	protected abstract AVResult getFallbackResult(IncrementalAttributeVisitorEvaluator evaluator, DMNode node, AttributeEvaluationTable attribEvalTable);
	
	
	public String toString()
	{
		return "AttributeBase( name='" + name + "' )"; 
	}
}
