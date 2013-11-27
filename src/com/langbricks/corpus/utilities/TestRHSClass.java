package com.langbricks.corpus.utilities;

import gate.*;
import gate.creole.ontology.Ontology;
import gate.jape.ActionContext;
import gate.jape.JapeException;
import gate.jape.RhsAction;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: 25.09.2006
 * Time: 15:03:29
 */

public class TestRHSClass implements RhsAction {

    public void setActionContext(ActionContext actionContext) {

    }

    public ActionContext getActionContext() {
        return null;
    }


    public void doit(Document doc, Map bindings,
                     AnnotationSet annotations, AnnotationSet inputAS,
                     AnnotationSet outputAS, Ontology ontology) throws JapeException {

        gate.AnnotationSet sentencesSet = (gate.AnnotationSet) bindings.get("nGramBuilder");
        Annotation sentence = sentencesSet.iterator().next();
        Long sentenceStart = sentence.getStartNode().getOffset();
        Long sentenceStop = sentence.getEndNode().getOffset();
        FeatureMap features = Factory.newFeatureMap();
        Collection<String> ngrams = new ArrayList<String>();
        features.put("kind", "word");
        AnnotationSet tokens = annotations.get("Token", features);
        if (tokens != null) {
            tokens = tokens.get("Token", sentenceStart, sentenceStop);
            List sorted = gate.Utils.inDocumentOrder(tokens);
            for (int i = 0; i < sorted.size(); i++) {
                Annotation currentToken = (Annotation) sorted.get(i);
                if (i + 2 <= sorted.size()) {
                    Annotation secondToken = (Annotation) sorted.get(i + 1);
                    Annotation thirdToken = (Annotation) sorted.get(i + 2);
                    String result = currentToken.getFeatures().get("string") + " " + secondToken.getFeatures().get("string") + " " + thirdToken.getFeatures().get("string");
                    ngrams.add(result);
                }
            }
            sentence.getFeatures().put("ngrams", ngrams);
        }

    } // end of method
}// end of class

