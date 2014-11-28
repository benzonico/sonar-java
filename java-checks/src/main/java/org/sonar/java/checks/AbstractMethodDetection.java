package org.sonar.java.checks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.sonar.java.resolve.Symbol;
import org.sonar.java.resolve.Type;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.List;

public abstract class AbstractMethodDetection extends SubscriptionBaseVisitor {

  private MethodDefinition methodDefinition;

  protected static class MethodDefinition {

    private String fullyQualifiedTypeName;
    private String methodName;
    private List<String> parameterTypes;

    private MethodDefinition(){
      parameterTypes = Lists.newArrayList();
    }

    public static MethodDefinition create() {
      return new MethodDefinition();
    }

    public MethodDefinition name(String methodName) {
      this.methodName = methodName;
      return this;
    }

    public MethodDefinition type(String fullyQualifiedTypeName) {
      this.fullyQualifiedTypeName = fullyQualifiedTypeName;
      return this;
    }

    public MethodDefinition addParameter(String fullyQualifiedTypeParameterName) {
      parameterTypes.add(fullyQualifiedTypeParameterName);
      return this;
    }

    public String getFullyQualifiedTypeName() {
      return fullyQualifiedTypeName;
    }

    public String getMethodName() {
      return methodName;
    }

    public List<String> getParameterTypes() {
      return parameterTypes;
    }

  }

  protected AbstractMethodDetection(MethodDefinition methodDefinition) {
    this.methodDefinition = methodDefinition;
  }

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return ImmutableList.of(Tree.Kind.METHOD_INVOCATION);
  }

  @Override
  public void visitNode(Tree tree) {
    MethodInvocationTree mit = (MethodInvocationTree) tree;
    if (hasSemantic()) {
      IdentifierTree id = getIdentifier(mit);
      if (id != null) {
        Symbol symbol = getSemanticModel().getReference(id);
        if (symbol.isKind(Symbol.MTH)) {
          Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) symbol;
          if (isSearchedMethod(methodSymbol)) {
            onMethodFound(mit);
          }
        }
      }
    }
  }

  private boolean isSearchedMethod(Symbol.MethodSymbol symbol) {
    return symbol.owner().getType().is(methodDefinition.getFullyQualifiedTypeName()) && symbol.getName().equals(methodDefinition.getMethodName()) && parametersAcceptable(symbol);
  }

  private boolean parametersAcceptable(Symbol.MethodSymbol methodSymbol) {
    boolean isSeekCall = true;
    List<Type> parametersTypes = methodSymbol.getParametersTypes();
    List<String> arguments = methodDefinition.getParameterTypes();
    if (parametersTypes.size() == arguments.size()) {
      int i = 0;
      for (Type parameterType : parametersTypes) {
        if (!parameterType.is(arguments.get(i))) {
          isSeekCall = false;
          break;
        }
        i++;
      }
    }
    return isSeekCall;
  }

  protected abstract void onMethodFound(MethodInvocationTree mit);

  private IdentifierTree getIdentifier(MethodInvocationTree mit) {
    IdentifierTree id = null;
    if (mit.methodSelect().is(Tree.Kind.IDENTIFIER)) {
      id = (IdentifierTree) mit.methodSelect();
    } else if (mit.methodSelect().is(Tree.Kind.MEMBER_SELECT)) {
      id = ((MemberSelectExpressionTree) mit.methodSelect()).identifier();
    }
    return id;
  }
}
