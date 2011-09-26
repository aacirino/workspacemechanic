/*******************************************************************************
 * Copyright (C) 2010, Google Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.google.eclipse.mechanic.core.keybinding;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import com.google.eclipse.mechanic.core.keybinding.KeyBindingChangeSet;
import com.google.eclipse.mechanic.core.keybinding.KeyBindingSpec;
import com.google.eclipse.mechanic.core.keybinding.KeyBindingsModel;
import com.google.eclipse.mechanic.core.keybinding.KeyBindingsParser;
import com.google.eclipse.mechanic.core.keybinding.KeyBindingsModel.MetaData;
import com.google.eclipse.mechanic.internal.TaskType;
import com.google.eclipse.mechanic.tests.internal.RunAsJUnitTest;

@RunAsJUnitTest
public class KeyBindingsParserTest extends TestCase {

  private static final String TEST_NO_CHANGESETS =
    "{\n" +
    "  'metadata' : {\n" +
    "    'shortDescription' : 'Zorzella\\'s bindings',\n" +
    "    'description' : 'Zorzella\\'s bindings in the real world',\n" +
    "    'type' : 'LASTMOD'\n" + // Comma: *1
    "  }, \n" +
    "  'changeSets' : [\n" +
    "  ]\n" +
    "}\n";

  // *1 indicates that there used to be a comma at the end of the line, but
  // that's not supported in strict mode.
  private static final String TEST_JSON =
    "{\n" +
    "  'metadata' : {\n" +
    "    'shortDescription' : 'Zorzella\\'s bindings',\n" +
    "    'description' : 'Zorzella\\'s bindings in the real world',\n" +
    "    'type' : 'LASTMOD'\n" + // Comma: *1
    "  }, \n" +
    "  'changeSets' : [\n" +
    "    {\n" +
    "      'scheme' : 'org.eclipse.ui.emacsAcceleratorConfiguration',\n" +
    "      'platform' : 'Windows',\n" +
    "      'context' : 'org.eclipse.ui.contexts.window',\n" +
    "      'action' : 'add',\n" +
    "      'bindings' : [\n" +
    "        {'keys' : 'Shift+Alt+Q T', 'command' : {'id' : 'a.b.c.d.e'}}\n" + // Comma: *1
    "      ]\n" +
    "    },\n" +
//    "    {\n" +
//    "      'scheme' : 'org.eclipse.ui.emacsAcceleratorConfiguration',\n" +
//    "      'platform' : 'Windows',\n" +
//    "      'context' : 'org.eclipse.ui.contexts.window',\n" +
//    "      'action' : 'remove',\n" +
//    "      'bindings' : [\n" +
//    "        {'keys' : 'Shift+Alt+Q X'}\n" + // Comma: *1
//    "      ]\n" +
//    "    },\n" +
    "    {\n" +
    "      'scheme' : 'org.eclipse.ui.defaultAcceleratorConfiguration',\n" +
    "      'platform' : 'Windows',\n" +
    "      'context' : 'org.eclipse.ui.contexts.window',\n" +
    "      'action' : 'add',\n" +
    "      'bindings' : [\n" +
    "        {'keys' : 'Shift+Alt+Q T', 'command' : {'id' : 'a.b.c.d.e'}}\n" + // Comma: *1
    "      ]\n" +
    "    },\n" + // Comma *1
    "  ]\n" +
    "}\n";


  // *1 indicates that there used to be a comma at the end of the line, but
  // that's not supported in strict mode.
  private static final String TEST_FULL =
    "{\n" +
    "  'metadata' : {\n" +
    "    'shortDescription' : 'Zorzella\\'s bindings',\n" +
    "    'description' : 'Zorzella\\'s bindings in the real world',\n" +
    "    'type' : 'LASTMOD'\n" +
    "  }, \n" +
    "  'changeSets' : [\n" +
    "    {\n" +
    "      'scheme' : 'org.eclipse.ui.emacsAcceleratorConfiguration',\n" +
    "      'platform' : 'Windows',\n" +
    "      'context' : 'org.eclipse.ui.contexts.window',\n" +
    "      'action' : 'add',\n" +
    "      'bindings' : [\n" +
    "        {'keys' : 'Shift+Alt+Q T',\n" +
    "          'command' : {'id' : 'a.b.c.d.e', 'parameters' : { }}}\n" +
    "      ]\n" +
    "    },\n" +
//    "    {\n" +
//    "      'scheme' : 'org.eclipse.ui.emacsAcceleratorConfiguration',\n" +
//    "      'platform' : 'Windows',\n" +
//    "      'context' : 'org.eclipse.ui.contexts.window',\n" +
//    "      'action' : 'remove',\n" +
//    "      'bindings' : [\n" +
//    "        {'keys' : 'Shift+Alt+Q X'}\n" + // Comma *1
//    "      ]\n" +
//    "    },\n" +
    "    {\n" +
    "      'scheme' : 'org.eclipse.ui.defaultAcceleratorConfiguration',\n" +
    "      'platform' : 'Windows',\n" +
    "      'context' : 'org.eclipse.ui.contexts.window',\n" +
    "      'action' : 'add',\n" +
    "      'bindings' : [\n" +
    "        {'keys' : 'Shift+Alt+Q T',\n" +
    "          'command' : {'id' : 'a.b.c.d.e', 'parameters' : { 'a' : '1', 'b' : '2' }}}\n" +
    "      ]\n" +
    "    }\n" + // Comma *1
    "  ]\n" +
    "}\n";

  private static final String TEST_NO_PLATFORM =
      "{\n" +
      "  'metadata' : {\n" +
      "    'shortDescription' : 'Zorzella\\'s bindings',\n" +
      "    'description' : 'Zorzella\\'s bindings in the real world',\n" +
      "    'type' : 'LASTMOD'\n" +
      "  }, \n" +
      "  'changeSets' : [\n" +
      "    {\n" +
      "      'scheme' : 'org.eclipse.ui.defaultAcceleratorConfiguration',\n" +
      //  No platform should work
      "      'context' : 'org.eclipse.ui.contexts.window',\n" +
      "      'action' : 'add',\n" +
      "      'bindings' : [\n" +
      "        {'keys' : 'Shift+Alt+Q T',\n" +
      "          'command' : {'id' : 'a.b.c.d.e', 'parameters' : { 'a' : '1', 'b' : '2' }}}\n" +
      "      ]\n" +
      "    }\n" + // Comma *1
      "  ]\n" +
      "}\n";

  
  public void testNoChangesets() {
    KeyBindingsModel actual = KeyBindingsParser.deSerialize(new StringReader(TEST_NO_CHANGESETS));
    KeyBindingsModel expected = buildExpected(false, false);

    assertEquals(expected, actual);
  }

  public void testDefault() {
    KeyBindingsModel actual = KeyBindingsParser.deSerialize(new StringReader(TEST_JSON));
    KeyBindingsModel expected = buildExpected(true, false);

    assertEquals(expected, actual);
  }

  public void testFull() {
    KeyBindingsModel actual = KeyBindingsParser.deSerialize(new StringReader(TEST_FULL));
    KeyBindingsModel expected = buildExpected(true, true);

    assertEquals(expected, actual);
  }

  public void testNoPlatform() {
    KeyBindingsModel actual = KeyBindingsParser.deSerialize(new StringReader(TEST_NO_PLATFORM));
    // Just want to know this did not throw
  }

  public void testRoundTrip_basic() {
    doTestRoundTrip_entry(buildExpected(false, false));
  }

  public void testRoundTrip_extended() {
    doTestRoundTrip_entry(buildExpected(true, false));
  }

  public void testRoundTrip_full() {
    doTestRoundTrip_entry(buildExpected(false, true));
  }

  private void doTestRoundTrip_entry(KeyBindingsModel task) {
    String json = KeyBindingsParser.serialize(task);
    KeyBindingsModel reconstituted = KeyBindingsParser.deSerialize(new StringReader(json));
    assertEquals(task, reconstituted);
  }

  private KeyBindingsModel buildExpected(boolean hasChangeSets, boolean hasParams) {
    MetaData metadata = new MetaData(
        "Zorzella's bindings",
        "Zorzella's bindings in the real world",
        TaskType.LASTMOD
        );
    List<KeyBindingChangeSet> changeSets = new ArrayList<KeyBindingChangeSet>();
    if (hasChangeSets) {
      changeSets.add(buildExpectedChangeSetZero());
      changeSets.add(buildExpectedChangeSetOne(hasParams));
    }
    KeyBindingsModel result = new KeyBindingsModel(changeSets, metadata);
    return result;
  }

  private KeyBindingChangeSet buildExpectedChangeSetZero() {
    Collection<KeyBindingSpec> toRemove = new ArrayList<KeyBindingSpec>();
//    toRemove.add(new KeyBindingSpec(null, "Shift+Alt+Q X"));

    Collection<KeyBindingSpec> toAdd = new ArrayList<KeyBindingSpec>();
    KeyBindingSpec spec = new KeyBindingSpec("a.b.c.d.e", "Shift+Alt+Q T");
    toAdd.add(spec);

    return new KeyBindingChangeSet(
        "org.eclipse.ui.emacsAcceleratorConfiguration",
        "Windows",
        "org.eclipse.ui.contexts.window",
        "add",
        toAdd, toRemove
        );
  }

  private KeyBindingChangeSet buildExpectedChangeSetOne(boolean hasParams) {
    Collection<KeyBindingSpec> toRemove = new ArrayList<KeyBindingSpec>();
//    toRemove.add(new KeyBindingSpec(null, "Shift+Alt+Q X"));

    Collection<KeyBindingSpec> toAdd = new ArrayList<KeyBindingSpec>();
    KeyBindingSpec spec = new KeyBindingSpec("a.b.c.d.e", "Shift+Alt+Q T");
    if (hasParams) {
      spec = spec.withParam("a", "1").withParam("b", "2");
    }
    toAdd.add(spec);

    return new KeyBindingChangeSet(
        "org.eclipse.ui.defaultAcceleratorConfiguration",
        "Windows",
        "org.eclipse.ui.contexts.window",
        "add",
        toAdd, toRemove
        );
  }
}
