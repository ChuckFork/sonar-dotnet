/*
 * SonarSource :: .NET :: Shared library
 * Copyright (C) 2014-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.dotnet.shared.plugins;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonarsource.dotnet.shared.sarif.SarifParserCallback;
import org.sonarsource.dotnet.shared.sarif.SarifParserFactory;

@ScannerSide
public class RoslynDataImporter {
  public void importRoslynReport(List<Path> reportPaths, final SensorContext context, Map<String, List<RuleKey>> activeRoslynRulesByPartialRepoKey) {
    Map<String, String> repositoryKeyByRoslynRuleKey = getRepoKeyByRoslynRuleKey(activeRoslynRulesByPartialRepoKey);
    SarifParserCallback callback = new SarifParserCallbackImpl(context, repositoryKeyByRoslynRuleKey);

    for (Path reportPath : reportPaths) {
      // TODO handle possible collisions
      SarifParserFactory.create(reportPath).accept(callback);
    }
  }

  private static Map<String, String> getRepoKeyByRoslynRuleKey(Map<String, List<RuleKey>> activeRoslynRulesByPartialRepoKey) {
    Map<String, String> repositoryKeyByRoslynRuleKey = new HashMap<>();
    for (List<RuleKey> rules : activeRoslynRulesByPartialRepoKey.values()) {
      for (RuleKey activeRoslynRuleKey : rules) {
        String previousRepositoryKey = repositoryKeyByRoslynRuleKey.put(activeRoslynRuleKey.rule(), activeRoslynRuleKey.repository());
        if (previousRepositoryKey != null) {
          throw new IllegalArgumentException("Rule keys must be unique, but \"" + activeRoslynRuleKey.rule() +
            "\" is defined in both the \"" + previousRepositoryKey + "\" and \"" + activeRoslynRuleKey.repository() +
            "\" rule repositories.");
        }
      }
    }
    return repositoryKeyByRoslynRuleKey;
  }
}