package analysis.dynamicsim.hierarchical.util.interpreter;

import java.util.Map;

import org.sbml.jsbml.ASTNode;

import analysis.dynamicsim.hierarchical.util.math.AbstractHierarchicalNode.Type;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.ValueNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public final class MathInterpreter
{

	public static HierarchicalNode parseASTNode(ASTNode math, Map<String, VariableNode> variableToNodes, Map<String, VariableNode> dimensionNode)
	{
		return parseASTNode(math, variableToNodes, dimensionNode, null);
	}

	public static HierarchicalNode parseASTNode(ASTNode math, Map<String, VariableNode> variableToNodes, VariableNode parent)
	{
		return parseASTNode(math, variableToNodes, null, parent);
	}

	public static HierarchicalNode parseASTNode(ASTNode math, Map<String, VariableNode> variableToNodes)
	{
		return parseASTNode(math, variableToNodes, null, null);
	}

	public static HierarchicalNode parseASTNode(ASTNode math, Map<String, VariableNode> variableToNodes, Map<String, VariableNode> dimensionNodes, VariableNode parent)
	{

		HierarchicalNode node;

		switch (math.getType())
		{
		case CONSTANT_E:
			node = new HierarchicalNode(Type.CONSTANT_E);
			break;
		case CONSTANT_FALSE:
			node = new HierarchicalNode(Type.CONSTANT_FALSE);
			break;
		case CONSTANT_PI:
			node = new HierarchicalNode(Type.CONSTANT_PI);
			break;
		case CONSTRUCTOR_PIECE:
			node = new HierarchicalNode(Type.CONSTRUCTOR_PIECE);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case CONSTRUCTOR_OTHERWISE:
			node = new HierarchicalNode(Type.CONSTRUCTOR_OTHERWISE);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case CONSTANT_TRUE:
			node = new HierarchicalNode(Type.CONSTANT_TRUE);
			break;
		case DIVIDE:
			node = new HierarchicalNode(Type.DIVIDE);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case FUNCTION:
			node = new HierarchicalNode(Type.FUNCTION);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case FUNCTION_ABS:
			node = new HierarchicalNode(Type.FUNCTION_ABS);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCCOS:
			node = new HierarchicalNode(Type.FUNCTION_ARCCOS);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCCOSH:
			node = new HierarchicalNode(Type.FUNCTION_ARCCOSH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCCOT:
			node = new HierarchicalNode(Type.FUNCTION_ARCCOT);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCCOTH:
			node = new HierarchicalNode(Type.FUNCTION_ARCCOTH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCCSC:
			node = new HierarchicalNode(Type.FUNCTION_ARCCSC);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCCSCH:
			node = new HierarchicalNode(Type.FUNCTION_ARCCSCH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCSEC:
			node = new HierarchicalNode(Type.FUNCTION_ARCSEC);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCSECH:
			node = new HierarchicalNode(Type.FUNCTION_ARCSECH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCSIN:
			node = new HierarchicalNode(Type.FUNCTION_ARCSIN);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCSINH:
			node = new HierarchicalNode(Type.FUNCTION_ARCSINH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCTAN:
			node = new HierarchicalNode(Type.FUNCTION_ARCTAN);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_ARCTANH:
			node = new HierarchicalNode(Type.FUNCTION_ARCTANH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_CEILING:
			node = new HierarchicalNode(Type.FUNCTION_CEILING);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_COS:
			node = new HierarchicalNode(Type.FUNCTION_COS);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_COSH:
			node = new HierarchicalNode(Type.FUNCTION_COSH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_COT:
			node = new HierarchicalNode(Type.FUNCTION_COT);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_COTH:
			node = new HierarchicalNode(Type.FUNCTION_COTH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_CSC:
			node = new HierarchicalNode(Type.FUNCTION_CSC);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_CSCH:
			node = new HierarchicalNode(Type.FUNCTION_CSCH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_DELAY:
			node = new HierarchicalNode(Type.FUNCTION_DELAY);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_EXP:
			node = new HierarchicalNode(Type.FUNCTION_EXP);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_FACTORIAL:
			node = new HierarchicalNode(Type.FUNCTION_FACTORIAL);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_FLOOR:
			node = new HierarchicalNode(Type.FUNCTION_FLOOR);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_LN:
			node = new HierarchicalNode(Type.FUNCTION_LN);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_LOG:
			node = new HierarchicalNode(Type.FUNCTION_LOG);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case FUNCTION_PIECEWISE:
			node = new HierarchicalNode(Type.FUNCTION_PIECEWISE);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case FUNCTION_POWER:
			node = new HierarchicalNode(Type.FUNCTION_POWER);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case FUNCTION_ROOT:
			node = new HierarchicalNode(Type.FUNCTION_ROOT);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case FUNCTION_SEC:
			node = new HierarchicalNode(Type.FUNCTION_SEC);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_SECH:
			node = new HierarchicalNode(Type.FUNCTION_SECH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_SELECTOR:
			node = new HierarchicalNode(Type.FUNCTION_SELECTOR);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_SIN:
			node = new HierarchicalNode(Type.FUNCTION_SIN);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_SINH:
			node = new HierarchicalNode(Type.FUNCTION_SINH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_TAN:
			node = new HierarchicalNode(Type.FUNCTION_TAN);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case FUNCTION_TANH:
			node = new HierarchicalNode(Type.FUNCTION_TANH);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case INTEGER:
			node = new ValueNode(math.getInteger());
			break;
		case LAMBDA:
			node = new HierarchicalNode(Type.LAMBDA);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case LOGICAL_AND:
			node = new HierarchicalNode(Type.LOGICAL_AND);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case LOGICAL_NOT:
			node = new HierarchicalNode(Type.LOGICAL_NOT);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case LOGICAL_OR:
			node = new HierarchicalNode(Type.LOGICAL_OR);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case LOGICAL_XOR:
			node = new HierarchicalNode(Type.LOGICAL_XOR);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case MINUS:
			node = new HierarchicalNode(Type.MINUS);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case NAME:
			String name = math.getName();
			if (dimensionNodes != null && dimensionNodes.containsKey(name))
			{
				node = dimensionNodes.get(name);
			}
			else
			{
				node = variableToNodes.get(name);
			}
			if (parent != null && parent.isReaction())
			{
				((VariableNode) node).addReactionDependency((ReactionNode) parent);
			}
			break;
		case NAME_AVOGADRO:
			node = new HierarchicalNode(Type.NAME_AVOGADRO);
			node.addChild(parseASTNode(math.getChild(0), variableToNodes, parent));
			break;
		case NAME_TIME:
			node = variableToNodes.get("_time");
			break;
		case PLUS:
			node = new HierarchicalNode(Type.PLUS);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case POWER:
			node = new HierarchicalNode(Type.POWER);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case PRODUCT:
			node = new HierarchicalNode(Type.PRODUCT);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case QUALIFIER_BVAR:
			node = new HierarchicalNode(Type.QUALIFIER_BVAR);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case QUALIFIER_DEGREE:
			node = new HierarchicalNode(Type.QUALIFIER_DEGREE);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case QUALIFIER_LOGBASE:
			node = new HierarchicalNode(Type.QUALIFIER_LOGBASE);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case RATIONAL:
			node = new ValueNode(1.0 * math.getNumerator() / math.getDenominator());
			break;
		case REAL:
			node = new ValueNode(math.getReal());
			break;
		case REAL_E:
			double value = math.getMantissa() * Math.pow(10, math.getExponent());
			node = new ValueNode(value);

			break;
		case RELATIONAL_EQ:
			node = new HierarchicalNode(Type.RELATIONAL_EQ);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case RELATIONAL_GEQ:
			node = new HierarchicalNode(Type.RELATIONAL_GEQ);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case RELATIONAL_GT:
			node = new HierarchicalNode(Type.RELATIONAL_GT);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case RELATIONAL_LEQ:
			node = new HierarchicalNode(Type.RELATIONAL_LEQ);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case RELATIONAL_LT:
			node = new HierarchicalNode(Type.RELATIONAL_LT);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case RELATIONAL_NEQ:
			node = new HierarchicalNode(Type.RELATIONAL_NEQ);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case SUM:
			node = new HierarchicalNode(Type.SUM);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case TIMES:
			node = new HierarchicalNode(Type.TIMES);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		case UNKNOWN:
			node = new HierarchicalNode(Type.UNKNOWN);
			break;
		case VECTOR:
			node = new HierarchicalNode(Type.VECTOR);
			for (ASTNode child : math.getListOfNodes())
			{
				node.addChild(parseASTNode(child, variableToNodes, dimensionNodes, parent));
			}
			break;
		default:
			node = new ValueNode(0);
		}
		return node;
	}

	public static HierarchicalNode replaceNameNodes(HierarchicalNode node, Map<String, VariableNode> variableToNodes)
	{
		if (node.isName())
		{
			VariableNode varNode = (VariableNode) node;
			if (variableToNodes.containsKey(varNode.getName()))
			{
				return new ValueNode(varNode.getValue());
			}
			else
			{
				return varNode;
			}
		}
		else
		{

			HierarchicalNode newNode = new HierarchicalNode(node.getType());
			for (int i = 0; i < node.getNumOfChild(); i++)
			{
				newNode.addChild(replaceNameNodes(node.getChild(i), variableToNodes));
			}
			return newNode;
		}

	}
}
