/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.namefind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectStreamException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

import org.junit.Test;

/**
 * This is the test class for {@link NameSampleDataStream}..
 */
public class NameSampleDataStreamTest {

  /**
   * Create a string from a array section.
   * 
   * @param tokens the tokens
   * @param nameSpan the section
   * @return the string
   */
  private static String sublistToString(String[] tokens, Span nameSpan) {
    StringBuilder sb = new StringBuilder();
    for (int i = nameSpan.getStart(); i < nameSpan.getEnd(); i++) {
      sb.append(tokens[i] + " ");
    }

    return sb.toString().trim();
  }
  
  /**
   * Create a NameSampleDataStream from a corpus with entities annotated but
   * without nameType and validate it.
   * 
   * @throws Exception
   */
  @Test
  public void testWithoutNameTypes() throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream(
        "opennlp/tools/namefind/AnnotatedSentences.txt");

    String encoding = "ISO-8859-1";

    NameSampleDataStream ds = new NameSampleDataStream(
        new PlainTextByLineStream(new InputStreamReader(in, encoding)));

    NameSample ns = ds.read();

    String[] expectedNames = { "Alan McKennedy", "Julie", "Marie Clara",
        "Stefanie Schmidt", "Mike", "Stefanie Schmidt", "George", "Luise",
        "Alisa Fernandes", "Alisa", "Mike Sander", "Stefan Miller",
        "Stefan Miller", "Stefan Miller", "Elenor Meier", "Gina Schneider",
        "Bruno Schulz", "Michel Seile", "George Miller", "Miller",
        "Peter Schubert", "Natalie" };

    List<String> names = new ArrayList<String>();
    List<Span> spans = new ArrayList<Span>();

    while (ns != null) {
      for (Span nameSpan : ns.getNames()) {
        names.add(sublistToString(ns.getSentence(), nameSpan));
        spans.add(nameSpan);
      }
      ns = ds.read();
    }

    assertEquals(expectedNames.length, names.size());
    assertEquals(new Span(6,8), spans.get(0));
    assertEquals(new Span(3,4), spans.get(1));
    assertEquals(new Span(1,3), spans.get(2));
    assertEquals(new Span(4,6), spans.get(3));
    assertEquals(new Span(1,2), spans.get(4));
    assertEquals(new Span(4,6), spans.get(5));
    assertEquals(new Span(2,3), spans.get(6));
    assertEquals(new Span(16,17), spans.get(7));
    assertEquals(new Span(0,2), spans.get(8));
    assertEquals(new Span(0,1), spans.get(9));
    assertEquals(new Span(3,5), spans.get(10));
    assertEquals(new Span(3,5), spans.get(11));
    assertEquals(new Span(10,12), spans.get(12));
    assertEquals(new Span(1,3), spans.get(13));
    assertEquals(new Span(6,8), spans.get(14));
    assertEquals(new Span(6,8), spans.get(15));
    assertEquals(new Span(8,10), spans.get(16));
    assertEquals(new Span(12,14), spans.get(17));
    assertEquals(new Span(1,3), spans.get(18));
    assertEquals(new Span(0,1), spans.get(19));
    assertEquals(new Span(2,4), spans.get(20));
    assertEquals(new Span(5,6), spans.get(21));
  }

  /**
   * Checks that invalid spans cause an {@link ObjectStreamException} to be thrown.
   */
  @Test
  public void testWithoutNameTypeAndInvalidData() {
    NameSampleDataStream smapleStream = new NameSampleDataStream(
        ObjectStreamUtils.createObjectStream("<START> <START> Name <END>"));
    
    try {
      smapleStream.read();
      fail();
    } catch (IOException e) {
    }
    
    smapleStream = new NameSampleDataStream(
        ObjectStreamUtils.createObjectStream("<START> Name <END> <END>"));
    
    try {
      smapleStream.read();
      fail();
    } catch (IOException e) {
    }
    
    smapleStream = new NameSampleDataStream(
        ObjectStreamUtils.createObjectStream("<START> <START> Person <END> Street <END>"));
    
    try {
      smapleStream.read();
      fail();
    } catch (IOException e) {
    }
  }
  
  /**
   * Create a NameSampleDataStream from a corpus with entities annotated
   * with multiple nameTypes, like person, date, location and organization, and validate it.
   * 
   * @throws Exception
   */
  @Test
  public void testWithNameTypes() throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream(
        "opennlp/tools/namefind/voa1.train");

    NameSampleDataStream ds = new NameSampleDataStream(
        new PlainTextByLineStream(new InputStreamReader(in)));

    Map<String, List<String>> names = new HashMap<String, List<String>>();
    Map<String, List<Span>> spans = new HashMap<String, List<Span>>();
    
    final String person = "person";
    final String date = "date";
    final String location = "location";
    final String organization = "organization";

    NameSample ns;
    while ((ns = ds.read()) != null) {
      Span[] nameSpans = ns.getNames();

      for (int i = 0; i < nameSpans.length; i++) {
        if (!names.containsKey(nameSpans[i].getType())) {
          names.put(nameSpans[i].getType(), new ArrayList<String>());
          spans.put(nameSpans[i].getType(), new ArrayList<Span>());
        }
        names.get(nameSpans[i].getType())
            .add(sublistToString(ns.getSentence(), nameSpans[i]));
        spans.get(nameSpans[i].getType())
            .add(nameSpans[i]);
      }
    }
    
    String[] expectedPerson = { "Barack Obama", "Obama", "Obama",
        "Lee Myung - bak", "Obama", "Obama", "Scott Snyder", "Snyder", "Obama",
        "Obama", "Obama", "Tim Peters", "Obama", "Peters" };

    String[] expectedDate = { "Wednesday", "Thursday", "Wednesday" };

    String[] expectedLocation = { "U . S .", "South Korea", "North Korea",
        "China", "South Korea", "North Korea", "North Korea", "U . S .",
        "South Korea", "United States", "Pyongyang", "North Korea",
        "South Korea", "Afghanistan", "Seoul", "U . S .", "China" };
    
    String[] expectedOrganization = {"Center for U . S . Korea Policy"};
    
    assertEquals(expectedPerson.length, names.get(person).size());
    assertEquals(expectedDate.length, names.get(date).size());
    assertEquals(expectedLocation.length, names.get(location).size());
    assertEquals(expectedOrganization.length, names.get(organization).size());
    
    assertEquals(new Span(5,7), spans.get(person).get(0));
    assertEquals(expectedPerson[0], names.get(person).get(0));
    assertEquals(new Span(10,11 ), spans.get(person).get(1));
    assertEquals(expectedPerson[1], names.get(person).get(1));
    assertEquals(new Span(29,30), spans.get(person).get(2));
    assertEquals(expectedPerson[2], names.get(person).get(2));
    assertEquals(new Span(23,27 ), spans.get(person).get(3));
    assertEquals(expectedPerson[3], names.get(person).get(3));
    assertEquals(new Span(1,2 ), spans.get(person).get(4));
    assertEquals(expectedPerson[4], names.get(person).get(4));
    assertEquals(new Span(8,9), spans.get(person).get(5));
    assertEquals(expectedPerson[5], names.get(person).get(5));
    assertEquals(new Span(0,2), spans.get(person).get(6));
    assertEquals(expectedPerson[6], names.get(person).get(6));
    assertEquals(new Span(25,26), spans.get(person).get(7));
    assertEquals(expectedPerson[7], names.get(person).get(7));
    assertEquals(new Span(1,2), spans.get(person).get(8));
    assertEquals(expectedPerson[8], names.get(person).get(8));
    assertEquals(new Span(6,7), spans.get(person).get(9));
    assertEquals(expectedPerson[9], names.get(person).get(9));
    assertEquals(new Span(14,15), spans.get(person).get(10));
    assertEquals(expectedPerson[10], names.get(person).get(10));
    assertEquals(new Span(0,2), spans.get(person).get(11));
    assertEquals(expectedPerson[11], names.get(person).get(11));
    assertEquals(new Span(12,13), spans.get(person).get(12));
    assertEquals(expectedPerson[12], names.get(person).get(12));
    assertEquals(new Span(12,13), spans.get(person).get(13));
    assertEquals(expectedPerson[13], names.get(person).get(13));

    assertEquals(new Span(7,8), spans.get(date).get(0));
    assertEquals(expectedDate[0], names.get(date).get(0));
    assertEquals(new Span(27,28), spans.get(date).get(1));
    assertEquals(expectedDate[1], names.get(date).get(1));
    assertEquals(new Span(15,16), spans.get(date).get(2));
    assertEquals(expectedDate[2], names.get(date).get(2));
    
    assertEquals(new Span(0, 4), spans.get(location).get(0));
    assertEquals(expectedLocation[0], names.get(location).get(0));
    assertEquals(new Span(10,12), spans.get(location).get(1));
    assertEquals(expectedLocation[1], names.get(location).get(1));
    assertEquals(new Span(28,30), spans.get(location).get(2));
    assertEquals(expectedLocation[2], names.get(location).get(2));
    assertEquals(new Span(3,4), spans.get(location).get(3));
    assertEquals(expectedLocation[3], names.get(location).get(3));
    assertEquals(new Span(5,7), spans.get(location).get(4));
    assertEquals(expectedLocation[4], names.get(location).get(4));
    assertEquals(new Span(16,18), spans.get(location).get(5));
    assertEquals(expectedLocation[5], names.get(location).get(5));
    assertEquals(new Span(1,3), spans.get(location).get(6));
    assertEquals(expectedLocation[6], names.get(location).get(6));
    assertEquals(new Span(5,9), spans.get(location).get(7));
    assertEquals(expectedLocation[7], names.get(location).get(7));
    assertEquals(new Span(0,2), spans.get(location).get(8));
    assertEquals(expectedLocation[8], names.get(location).get(8));
    assertEquals(new Span(4,6), spans.get(location).get(9));
    assertEquals(expectedLocation[9], names.get(location).get(9));
    assertEquals(new Span(10,11), spans.get(location).get(10));
    assertEquals(expectedLocation[10], names.get(location).get(10));
    assertEquals(new Span(6,8), spans.get(location).get(11));
    assertEquals(expectedLocation[11], names.get(location).get(11));
    assertEquals(new Span(4,6), spans.get(location).get(12));
    assertEquals(expectedLocation[12], names.get(location).get(12));
    assertEquals(new Span(10,11), spans.get(location).get(13));
    assertEquals(expectedLocation[13], names.get(location).get(13));
    assertEquals(new Span(12,13), spans.get(location).get(14));
    assertEquals(expectedLocation[14], names.get(location).get(14));
    assertEquals(new Span(5,9), spans.get(location).get(15));
    assertEquals(expectedLocation[15], names.get(location).get(15));
    assertEquals(new Span(11,12), spans.get(location).get(16));
    assertEquals(expectedLocation[16], names.get(location).get(16));
    
    assertEquals(new Span(7,15), spans.get(organization).get(0));
    assertEquals(expectedOrganization[0], names.get(organization).get(0));
    
  }
  
  @Test
  public void testWithNameTypeAndInvalidData() {
    
    NameSampleDataStream smapleStream = new NameSampleDataStream(
        ObjectStreamUtils.createObjectStream("<START:> Name <END>"));
    
    try {
      smapleStream.read();
      fail();
    } catch (IOException e) {
    }
    
    smapleStream = new NameSampleDataStream(
        ObjectStreamUtils.createObjectStream("<START:street> <START:person> Name <END> <END>"));
    
    try {
      smapleStream.read();
      fail();
    } catch (IOException e) {
    }
  }
  
  @Test
  public void testClearAdaptiveData() throws IOException {
    StringBuilder trainingData = new StringBuilder();
    trainingData.append("a\n");
    trainingData.append("b\n");
    trainingData.append("c\n");
    trainingData.append("\n");
    trainingData.append("d\n");
    
    ObjectStream<String> untokenizedLineStream =
      new PlainTextByLineStream(new StringReader(trainingData.toString()));
    
    ObjectStream<NameSample> trainingStream = new NameSampleDataStream(untokenizedLineStream);
    
    assertFalse(trainingStream.read().isClearAdaptiveDataSet());
    assertFalse(trainingStream.read().isClearAdaptiveDataSet());
    assertFalse(trainingStream.read().isClearAdaptiveDataSet());
    assertTrue(trainingStream.read().isClearAdaptiveDataSet());
    assertNull(trainingStream.read());
  }
  
}