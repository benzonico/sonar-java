package org.sonar.java.checks;

import com.google.common.collect.ImmutableList;
import org.sonar.java.resolve.Symbol;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.List;

public class AbstractMethodDetection extends SubscriptionBaseVisitor {
  @Override
  public List<Tree.Kind> nodesToVisit() {
    return ImmutableList.of(Tree.Kind.METHOD_INVOCATION);
  }


  private String definitionType;
  private String methodName;
  private List<String> parameterTypes;


  @Override
  public void visitNode(Tree tree) {
    MethodInvocationTree mit = (MethodInvocationTree) tree;
    if(hasSemantic()) {
      IdentifierTree id = null;
      if(mit.methodSelect().is(Tree.Kind.IDENTIFIER)) {
        id = (IdentifierTree) mit.methodSelect();
      } else if(mit.methodSelect().is(Tree.Kind.MEMBER_SELECT)) {
        id = ((MemberSelectExpressionTree) mit.methodSelect()).identifier();
      }
      if (id != null) {
        Symbol methodSymbol = getSemanticModel().getReference(id);
        if(methodSymbol.owner().getType().is(definitionType) && methodSymbol.getName().equals(methodName)) {

        }
      }
    }
  }
}
