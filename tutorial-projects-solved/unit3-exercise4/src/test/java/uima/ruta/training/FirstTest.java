/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package uima.ruta.training;

import java.io.IOException;
import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

public class FirstTest {

	@Test
	public void test() throws IOException, UIMAException {

		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText("some text mentioning Stephen.");
		AnalysisEngine ae = AnalysisEngineFactory.createEngine("uima.ruta.training.FirstRutaAE");
		ae.process(jcas);
		Type type = jcas.getTypeSystem().getType("uima.ruta.training.Second.MyType");
		Collection<AnnotationFS> select = CasUtil.select(jcas.getCas(), type);
		Assert.assertEquals(1, select.size());
		Assert.assertEquals("Stephen", select.iterator().next().getCoveredText());
	}

}
