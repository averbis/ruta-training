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
import org.junit.Test;

import junit.framework.Assert;

public class FirstTest {
	
	@Test
	public void test() throws IOException, UIMAException {
		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText("some text mentioning Stephen.");
		AnalysisEngine ae = AnalysisEngineFactory.createEngine("uima.ruta.training.FirstRutaAE");
		ae.process(jcas);
		Type type =  jcas.getTypeSystem().getType("uima.ruta.training.Second.MyType");
		Collection<AnnotationFS> select = CasUtil.select(jcas.getCas(), type);
		Assert.assertEquals(1, select.size());
		Assert.assertEquals("Stephen", select.iterator().next().getCoveredText());
	}
	
}
