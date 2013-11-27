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

        gate.AnnotationSet set = (gate.AnnotationSet) bindings.get("featurePattern1");
        gate.AnnotationSet intensifierSet = (gate.AnnotationSet) bindings.get("intensifier");
        FeatureMap features = Factory.newFeatureMap();
        String intensifier = gate.Utils.stringFor(doc, intensifierSet);
        features.put("intensifier", intensifier);
        outputAS.add(set.firstNode(), set.lastNode(), "FeaturePattern1", features);
    } // end of method
}// end of class

