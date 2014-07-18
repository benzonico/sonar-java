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
package org.sonar.plugins.java;

import com.google.common.collect.Lists;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.ProjectClasspath;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.checks.NoSonarFilter;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.java.JavaConfiguration;
import org.sonar.java.JavaSquid;
import org.sonar.java.SonarComponents;
import org.sonar.java.api.JavaUtils;
import org.sonar.java.checks.CheckList;
import org.sonar.squidbridge.api.CodeVisitor;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Phase(name = Phase.Name.PRE)
@DependsUpon(JavaUtils.BARRIER_BEFORE_SQUID)
@DependedUpon(value = JavaUtils.BARRIER_AFTER_SQUID)
public class JavaSquidSensor implements Sensor {

  private final AnnotationCheckFactory annotationCheckFactory;
  private final NoSonarFilter noSonarFilter;
  private final ProjectClasspath projectClasspath;
  private final SonarComponents sonarComponents;
  private final DefaultJavaResourceLocator javaResourceLocator;
  private final RulesProfile profile;
  private final FileSystem fs;
  private Settings settings;

  public JavaSquidSensor(RulesProfile profile, NoSonarFilter noSonarFilter, ProjectClasspath projectClasspath, SonarComponents sonarComponents, FileSystem fs,
                         DefaultJavaResourceLocator javaResourceLocator, Settings settings) {
    this.profile = profile;
    this.annotationCheckFactory = AnnotationCheckFactory.create(profile, CheckList.REPOSITORY_KEY, CheckList.getChecks());
    this.noSonarFilter = noSonarFilter;
    this.projectClasspath = projectClasspath;
    this.sonarComponents = sonarComponents;
    this.fs = fs;
    this.javaResourceLocator = javaResourceLocator;
    this.settings = settings;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return fs.hasFiles(fs.predicates().hasLanguage(Java.KEY));
  }

  public void analyse(Project project, SensorContext context) {
    Collection<CodeVisitor> checks = annotationCheckFactory.getChecks();

    JavaSquid squid = new JavaSquid(createConfiguration(project), sonarComponents, checks.toArray(new CodeVisitor[checks.size()]));
    squid.scan(getSourceFiles(), getTestFiles(project), getBytecodeFiles(project));

    javaResourceLocator.setSquidIndex(squid.getIndex());

    new Bridges(squid, settings).save(context, project, annotationCheckFactory, noSonarFilter, profile);
  }

  private Collection<InputFile> getSourceFiles() {
    return Lists.newArrayList(fs.inputFiles(fs.predicates().hasType(InputFile.Type.MAIN)));
  }

  private Collection<InputFile> getTestFiles(Project project) {
    return Lists.newArrayList(fs.inputFiles(fs.predicates().hasType(InputFile.Type.TEST)));
  }

  private List<File> getBytecodeFiles(Project project) {
    if (settings.getBoolean(CoreProperties.DESIGN_SKIP_DESIGN_PROPERTY)) {
      return Collections.emptyList();
    }
    return projectClasspath.getElements();
  }

  private JavaConfiguration createConfiguration(Project project) {
    boolean analyzePropertyAccessors = settings.getBoolean(JavaPlugin.SQUID_ANALYSE_ACCESSORS_PROPERTY);
    JavaConfiguration conf = new JavaConfiguration(fs.encoding());
    conf.setAnalyzePropertyAccessors(analyzePropertyAccessors);
    return conf;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
