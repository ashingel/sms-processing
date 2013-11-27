package com.langbricks.corpus.utilities;

import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.POSTagger;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.morph.Morph;
import gate.util.Strings;

import java.io.File;
import java.net.MalformedURLException;

/**
 * User: Andrei
 * Date: 14.5.11
 * Time: 18.30
 */
public class GateResources {

    /**
     * Method which init GATE application
     *
     * @param applicationPath
     */
    public static synchronized void initGateFrameWork(String applicationPath) {
        System.out.println("Application path: " + applicationPath);
        try {
            if (Gate.getGateHome() == null) {
                // get GATE home directory
                File gateHome = new File(applicationPath + "/lib");
                // set parameters
                Gate.setGateHome(gateHome);
                System.setProperty("gate.user.session", gateHome.getPath());
                // set plugins home
                Gate.setPluginsHome(new File(applicationPath + "/lib/plugins"));
                // set configuration file
                Gate.setUserConfigFile(new File(gateHome, "user-gate.xml"));
                Gate.setSiteConfigFile(new File(applicationPath + "/lib/gate.xml"));
                Gate.init();
                Gate.getCreoleRegister().registerDirectories(new File(applicationPath + "/lib/plugins/ANNIE").toURL());
                // custom plugins can be added here
                //Gate.getCreoleRegister().registerDirectories(new File(applicationPath + "/lib/plugins/Stemmer_Snowball").toURL());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method create empty gate application
     *
     * @return
     * @throws gate.creole.ResourceInstantiationException
     *
     */
    public static synchronized SerialAnalyserController createEmptyGateApplication() {
        SerialAnalyserController gateApplication = null;
        try {
            gateApplication = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController");
        } catch (ResourceInstantiationException e) {
            e.printStackTrace();
        }
        return gateApplication;
    }

    /**
     * Method create GATE document with defined encoding value
     *
     * @param message
     * @param encoding
     * @return
     */
    public static synchronized gate.Document createGateDocument(String message, String encoding, Strings mimeType) {
        FeatureMap docParams = Factory.newFeatureMap();
        gate.Document doc = null;
        try {
            docParams.put(gate.Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME, message);
            //docParams.put(gate.Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, "text/plain");
            docParams.put(gate.Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, mimeType);
            docParams.put(gate.Document.DOCUMENT_ENCODING_PARAMETER_NAME, encoding);
            doc = (gate.Document) Factory.createResource("gate.corpora.DocumentImpl", docParams);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return doc;
    }
}
