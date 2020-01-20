package org.apache.uima.ruta.trials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.trials.type.TrialsEntity;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

public class Evaluate {

	public static void main(String[] args) throws UIMAException, FileNotFoundException, SAXException, IOException {

		// requires typesystems mentioned in types.txt
		JCas goldJCas = JCasFactory.createJCas();
		JCas processJCas = JCasFactory.createJCas();

		File aeFile = new File("target/generated-sources/ruta/descriptor/uima/ruta/training/ClinicalTrialsRutaAE.xml");

		AnalysisEngineDescription aed = UIMAFramework.getXMLParser()
				.parseAnalysisEngineDescription(new XMLInputSource(aeFile));
		AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(aed);

		Collection<File> files = FileUtils.listFiles(new File("src/main/resources/data"), new String[] { "xmi" },
				false);

		List<TrialsEntity> tps = new ArrayList<>();
		List<TrialsEntity> fps = new ArrayList<>();
		List<TrialsEntity> fns = new ArrayList<>();

		for (File file : files) {
			goldJCas.reset();
			processJCas.reset();
			XmiCasDeserializer.deserialize(new FileInputStream(file), goldJCas.getCas());
			processJCas.setDocumentText(goldJCas.getDocumentText());

			// apply additional components

			ae.process(processJCas);

			compare(file.getName(), goldJCas, processJCas, tps, fps, fns);

		}

		printResult("OVERALL MICRO AVERAGE:", tps, fps, fns);
	}

	private static void compare(String doc, JCas goldJCas, JCas processJCas, List<TrialsEntity> tps,
			List<TrialsEntity> fps, List<TrialsEntity> fns) {
		Collection<TrialsEntity> allGold = JCasUtil.select(goldJCas, TrialsEntity.class);
		Collection<TrialsEntity> allProcess = JCasUtil.select(processJCas, TrialsEntity.class);

		Collection<TrialsEntity> tp = new ArrayList<>();
		Collection<TrialsEntity> fp = new ArrayList<>();
		Collection<TrialsEntity> fn = new ArrayList<>();

		for (TrialsEntity goldAnnotation : allGold) {
			boolean found = false;
			for (TrialsEntity processAnnotation : allProcess) {
				if (equals(goldAnnotation, processAnnotation)) {
					tp.add(processAnnotation);
					found = true;
					break;
				}
			}
			if (!found) {
				fn.add(goldAnnotation);
			}
		}

		for (TrialsEntity processAnnotation : allProcess) {
			boolean found = false;
			for (TrialsEntity goldAnnotation : allGold) {
				if (equals(goldAnnotation, processAnnotation)) {
					found = true;
					break;
				}
			}
			if (!found) {
				fp.add(processAnnotation);
			}
		}

		printResult(doc, tp, fp, fn);

		tps.addAll(tp);
		fps.addAll(fp);
		fns.addAll(fn);

	}

	private static boolean equals(TrialsEntity goldAnnotation, TrialsEntity processAnnotation) {
		boolean sameType = goldAnnotation.getType().getName().equals(processAnnotation.getType().getName());
		boolean sameBegin = goldAnnotation.getBegin() == processAnnotation.getBegin();
		boolean sameEnd = goldAnnotation.getEnd() == processAnnotation.getEnd();
		return sameType && sameBegin && sameEnd;
	}

	private static void printResult(String doc, Collection<TrialsEntity> tps, Collection<TrialsEntity> fps,
			Collection<TrialsEntity> fns) {
		double tpCount = tps.size();
		double fpCount = fps.size();
		double fnCount = fns.size();
		double precision = 1;
		if (tpCount + fpCount != 0) {
			precision = tpCount / (tpCount + fpCount);
		}
		double recall = 1;
		if (tpCount + fnCount != 0) {
			recall = tpCount / (tpCount + fnCount);
		}
		double f1 = 2 * (precision * recall) / (precision + recall);

		System.out.printf(Locale.ENGLISH, doc + "\tp: %.2f\tr: %.2f\tf1: %.2f", precision, recall, f1);
		System.out.println();
	}

}
