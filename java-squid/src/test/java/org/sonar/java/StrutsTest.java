/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java;

import com.google.common.base.Charsets;
import org.fest.assertions.Delta;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.java.ast.api.JavaMetric;
import org.sonar.squidbridge.api.CodeVisitor;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceCodeSearchEngine;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceProject;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.squidbridge.measures.Metric;

import javax.lang.model.util.Types;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StrutsTest {

  private static JavaSquid squid;
  private static SourceProject project;
  private static Measurer measurer;
  private static SensorContext context;
  private static Collection<SourceCode> files;

  @BeforeClass
  public static void init() {
    File prjDir = new File("target/test-projects/struts-core-1.3.9");
    File srcDir = new File(prjDir, "src");
    File binDir = new File(prjDir, "bin");

    JavaConfiguration conf = new JavaConfiguration(Charsets.UTF_8);
    context = mock(SensorContext.class);
    Project sonarProject = mock(Project.class);
    ProjectFileSystem pfs = mock(ProjectFileSystem.class);
    when(pfs.getBasedir()).thenReturn(prjDir);
    when(sonarProject.getFileSystem()).thenReturn(pfs);
    measurer = new Measurer(sonarProject, context);
    squid = new JavaSquid(conf, null, measurer, new CodeVisitor[0]);
    squid.scanDirectories(Collections.singleton(srcDir), Collections.singleton(binDir));

    SourceCodeSearchEngine index = squid.getIndex();
    project = (SourceProject) index.search(new QueryByType(SourceProject.class)).iterator().next();
    files = index.search(new QueryByType(SourceFile.class));
  }

  @Test
  public void measures_on_project() throws Exception {
    ArgumentCaptor<Measure> captor = ArgumentCaptor.forClass(Measure.class);
    ArgumentCaptor<org.sonar.api.resources.File> sonarFilescaptor = ArgumentCaptor.forClass(org.sonar.api.resources.File.class);
    verify(context, atLeastOnce()).saveMeasure(sonarFilescaptor.capture(), captor.capture());
    List<Measure> measures = captor.getAllValues();
    int functions = 0;
    for (Measure measure : measures) {
      if(measure.getMetricKey().equals("functions")) {
        functions += measure.getIntValue();
      }
    }
    assertThat(project.getInt(JavaMetric.CLASSES)).isEqualTo(146);
    assertThat(project.getInt(JavaMetric.METHODS) + project.getInt(Metric.ACCESSORS)).isEqualTo(1437 + 48);
    assertThat(project.getInt(Metric.ACCESSORS)).isEqualTo(48);
    assertThat(project.getInt(JavaMetric.METHODS)).isEqualTo(1437);
    //56 methods in anonymous classes: not part of metric but part of number of methods in project.
    assertThat(functions).isEqualTo(1437-56);
    assertThat(project.getInt(JavaMetric.LINES)).isEqualTo(32878);
    assertThat(project.getInt(JavaMetric.LINES_OF_CODE)).isEqualTo(14007);
    assertThat(project.getInt(JavaMetric.STATEMENTS)).isEqualTo(6403);
    assertThat(project.getInt(JavaMetric.COMPLEXITY)).isEqualTo(3957 - 145 /* SONAR-3793 */- 1 /* SONAR-3794 */);
    assertThat(project.getInt(JavaMetric.COMMENT_LINES_WITHOUT_HEADER)).isEqualTo(7605);
    assertThat(project.getInt(Metric.PUBLIC_API)).isEqualTo(1348);
    assertThat(project.getInt(Metric.PUBLIC_DOC_API)).isEqualTo(842);
    assertThat(project.getDouble(Metric.PUBLIC_DOCUMENTED_API_DENSITY)).isEqualTo(0.62, Delta.delta(0.01));
  }


}
