//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocModel.DMNodeClass;


public class IncrementalAttributeVisitorEvaluator
{
	public static class EvalFnSpec
	{
		public AttributeBase attribute;
		public DMNodeClass nodeClass;
		public AttributeEvaluationFunction evalFn;
		
		public EvalFnSpec(AttributeBase attribute, DMNodeClass nodeClass, AttributeEvaluationFunction evalFn)
		{
			this.attribute = attribute;
			this.nodeClass = nodeClass;
			this.evalFn = evalFn;
		}

		public EvalFnSpec(AttributeBase attribute, DMNodeClass nodeClass, PyObject evalFn)
		{
			this.attribute = attribute;
			this.nodeClass = nodeClass;
			this.evalFn = new PyAttributeEvaluationFunction( evalFn );
		}
	}
	
	
	
	private List<AttributeBase> attributes;
	private HashMap<AttributeBase, AttributeEvaluationTable> attributeToEvalTable;
	private AVResult currentlyComputingAVResult;
	
	
	
	public IncrementalAttributeVisitorEvaluator(List<AttributeBase> attributes, List<EvalFnSpec> evalFnSpecs)
	{
		this.attributes = new ArrayList<AttributeBase>();
		this.attributes.addAll( attributes );
		
		attributeToEvalTable = new HashMap<AttributeBase, AttributeEvaluationTable>();
		for (AttributeBase attribute: attributes)
		{
			attributeToEvalTable.put( attribute, new AttributeEvaluationTable() );
		}
		
		
		for (EvalFnSpec spec: evalFnSpecs)
		{
			AttributeEvaluationTable attribEvalTable = attributeToEvalTable.get( spec.attribute );
			if ( attribEvalTable == null )
			{
				throw new RuntimeException( "Attribute " + spec.attribute.toString() + " was not present in the attribute list" );
			}
			attribEvalTable.registerEvaluationFunction( spec.nodeClass, spec.evalFn );
		}
		
		currentlyComputingAVResult = null;
	}
	
	
	
	public Object getAttributeValue(AttributeBase attribute, DMNode node)
	{
		AttributeEvaluationTable evalTable = attributeToEvalTable.get( attribute );
		
		if ( evalTable == null )
		{
			throw new RuntimeException( "Invalid attribute; the attribute " + attribute.toString() + " was not specified at construction time" );
		}
		
		AVResult result = evalTable.getAttributeResult( this, attribute, node );

		if ( result != null )
		{
			linkInwardDependency( result );
			result.incr.onAccess();
			return result.value;
		}
		else
		{
			return null;
		}
	}
	
	
	protected void linkInwardDependency(AVResult inwardDep)
	{
		if ( currentlyComputingAVResult != null  &&  inwardDep != null )
		{
			currentlyComputingAVResult.linkToInwardDependency( inwardDep );
		}
	}
	
	protected AVResult pushAVResult(AVResult result)
	{
		AVResult prevResult = currentlyComputingAVResult;
		currentlyComputingAVResult = result;
		return prevResult;
	}
	
	protected void popAVResult(AVResult prevResult)
	{
		currentlyComputingAVResult = prevResult;
	}
}