package org.sonar.java.checks;

import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.model.JavaTree;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class AbstractMethodDetectionTest {

  @Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @Test
  public void detected() {
    class Visitor extends AbstractMethodDetection {

      public List<Integer> lines = Lists.newArrayList();

      protected Visitor() {
        super(MethodDefinition.create().type("A").name("method").addParameter("int"));
      }

      @Override
      protected void onMethodFound(MethodInvocationTree tree) {
        lines.add(((JavaTree) tree).getLine());
      }

    }
    Visitor visitor = new Visitor();
    JavaAstScanner.scanSingleFile(new File("src/test/files/checks/AbstractMethodDetection.java"), new VisitorsBridge(visitor));

    assertThat(visitor.lines).hasSize(1);
    assertThat(visitor.lines).containsExactly(10);
  }

}