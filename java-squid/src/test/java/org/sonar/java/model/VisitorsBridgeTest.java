package org.sonar.java.model;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.junit.Test;
import org.sonar.api.rule.RuleKey;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.ast.visitors.SubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.api.SourceFile;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class VisitorsBridgeTest {

  @Test
  public void ignoreLinesForCertainsRules() throws Exception {
    SourceFile file = JavaAstScanner.scanSingleFile(new File("src/test/files/SuppressWarnings.java"), new VisitorsBridge(new DummyRule()));
    assertThat(file.getCheckMessages()).hasSize(3).onProperty("line").contains(5,9,9);
  }

  private static class DummyRule extends SubscriptionVisitor {

    @Override
    public void scanFile(JavaFileScannerContext context) {
      //Creation of this multimap by reading the value is unit tested on its own.
      Multimap<String, Integer> ignored = HashMultimap.create();
      ignored.putAll("foo:bar", Lists.newArrayList(3,4,5,6));
      context.ignoredLinesByRule(ignored);
      super.scanFile(context);
    }

    @Override
    public List<Tree.Kind> nodesToVisit() {
      return ImmutableList.of(Tree.Kind.IF_STATEMENT);
    }

    @Override
    public void visitNode(Tree tree) {
      context.addIssue(tree, RuleKey.of("foo", "bar"), "This issue should not be reported on source file");
      context.addIssue(tree, RuleKey.of("qix", "plop"), "This issue should be reported on source file");
    }
  }

}