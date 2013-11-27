package com.langbricks.corpus.processor;

import com.langbricks.corpus.constants.Constants;
import com.langbricks.corpus.utilities.GateResources;
import com.langbricks.corpus.utilities.Utilities;
import gate.*;
import gate.creole.ResourceInstantiationException;
import gate.util.persistence.PersistenceManager;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.*;


/**
 * User: Andrei
 * Date: 14.5.11
 * Time: 18.27
 */
public class KeyphraseExtractor {
    /**
     * Local gate application
     */
    private CorpusController corpusController;

    private final char[] punctuations = new char[]{',', '.', '?', '!', ':', ';', '(', ')', '[', ']', '+', '/', '"', '>', '<', '=', '*', '%', '#'};

    private final String SPACE = " ".intern();

    /**
     * Default constructor
     */
    public KeyphraseExtractor() {
        // load NLP processing resources
        initPersistentGateResources();
    }

    /**
     * Method which init application from GATE application stored on the local drive
     */
    private void initPersistentGateResources() {
        try {
            Corpus corpus = Factory.newCorpus("corpus");
            corpusController = (CorpusController) PersistenceManager.loadObjectFromFile(new File(Utilities.getApplicationPath() + "/lib/plugins/sms-processing.gapp"));
            corpusController.setCorpus(corpus);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Corpus corpus = null;
        try {
            corpus = Factory.newCorpus("gate corpus");
        } catch (ResourceInstantiationException e) {
            e.printStackTrace();
        }
        corpusController.setCorpus(corpus);
    }

    /**
     * Method which run features extraction
     *
     * @param inputFolderPath
     * @param outputFolderPath
     */
    public void runKeyphrasesExtraction(String inputFolderPath, String outputFolderPath) {
        // run corpus cleanup
        CorpusUtilities corpusUtilities = new CorpusUtilities(inputFolderPath, outputFolderPath);
        // we need to create structure for output
        createFoldersStructure(outputFolderPath);
        // TODO: uncomment this  and finalize processing
        corpusUtilities.normalizeCorpusMessages(Constants.INPUT_PLAIN_TEXT_PER_LINE_FORMAT_FILE);
        corpusUtilities.calculteFrequences();
        // process files
        File inputFolder = new File(outputFolderPath + "/cleanedCorpus");
        File[] inputFiles = inputFolder.listFiles();
        Map<String, Integer> vpFrequencies = new TreeMap<String, Integer>();
        Map<String, Integer> npFrequencies = new TreeMap<String, Integer>();
        Map<String, Integer> frequencies = new TreeMap<String, Integer>();
        Map<String, Integer> allFrequencies = new TreeMap<String, Integer>();
        for (int i = 0; i < inputFiles.length; i++) {
            File currentFile = inputFiles[i];
            if (!currentFile.isFile()) {
                continue;
            }
            // probably not a good idea to load all in memory
            Collection<String> inputTexts = Utilities.readFile(currentFile.getPath(), "UTF-8");
            // process texts
            for (String text : inputTexts) {
                System.out.println("process: " + text);
                if (text.trim().length() == 0) {
                    continue;
                }
                Map<String, Collection<String>> extractedEntities = null;
                try {
                    extractedEntities = extractEntities(text);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                if (extractedEntities == null) {
                    continue;
                }
                Collection<String> vpList = extractedEntities.get(Constants.VP_ANNOTATIONS);
                Collection<String> npList = extractedEntities.get(Constants.NP_ANNOTATIONS);
                Collection<String> bnpList = extractedEntities.get(Constants.BNP_ANNOTATIONS);
                Collection<String> dbnpList = extractedEntities.get(Constants.DBNP_ANNOTATIONS);

                // verb phrases
                for (String vpPhrase : vpList) {
                    if (!vpFrequencies.containsKey(vpPhrase)) {
                        vpFrequencies.put(vpPhrase, 1);
                    } else {
                        vpFrequencies.put(vpPhrase, vpFrequencies.get(vpPhrase) + 1);
                    }
                }
                // noun phrases
                for (String npPhrase : npList) {
                    if (!npFrequencies.containsKey(npPhrase)) {
                        npFrequencies.put(npPhrase, 1);
                    } else {
                        npFrequencies.put(npPhrase, npFrequencies.get(npPhrase) + 1);
                    }
                }
                // merge noun phrases
                Collection<String> mergedCollection = new ArrayList<String>();
                mergedCollection.addAll(bnpList);
                mergedCollection.addAll(dbnpList);
                // noun phrases
                for (String phrase : mergedCollection) {
                    if (!frequencies.containsKey(phrase)) {
                        frequencies.put(phrase, 1);
                    } else {
                        frequencies.put(phrase, frequencies.get(phrase) + 1);
                    }
                }
            }
        }
        // calculate all frequencies
        updateCorpusFrequencies(vpFrequencies, allFrequencies);
        updateCorpusFrequencies(npFrequencies, allFrequencies);
        updateCorpusFrequencies(frequencies, allFrequencies);

        System.out.println();
        saveCollectedData(vpFrequencies, outputFolderPath, "verb-frequencies.txt");
        saveCollectedData(npFrequencies, outputFolderPath, "nouns-phrases-frequencies.txt");
        saveCollectedData(frequencies, outputFolderPath, "noun-frequencies.txt");
        saveCollectedData(allFrequencies, outputFolderPath, "corpus-frequencies.txt");
    }

    /**
     * Method for calculating all frequencies
     *
     * @param frequencies
     * @param allFrequencies
     */
    private void updateCorpusFrequencies(Map<String, Integer> frequencies, Map<String, Integer> allFrequencies) {
        for (String phrase : frequencies.keySet()) {
            if (!allFrequencies.containsKey(phrase)) {
                allFrequencies.put(phrase, frequencies.get(phrase));
            } else {
                allFrequencies.put(phrase, allFrequencies.get(phrase) + frequencies.get(phrase));
            }
        }
    }

    /**
     * Method to save frequencies to output folder
     *
     * @param frequencies
     * @param outputFolderPath
     * @param fileName
     */
    private void saveCollectedData(Map<String, Integer> frequencies, String outputFolderPath, String fileName) {
        Collection<String> collection = new ArrayList<String>();
        for (String phrase : frequencies.keySet()) {
            collection.add(phrase + "\t" + frequencies.get(phrase));
        }
        Utilities.write(collection, outputFolderPath + "/" + fileName);
    }


    /**
     * Method to extract entities defined from processing text. In this moment entities types are:
     * Noun Phrase, Hearst Pattern data, Verb phrase
     *
     * @param inputText
     * @return
     */
    private Map<String, Collection<String>> extractEntities(String inputText) {
        Map<String, Collection<String>> outputMap = new HashMap<String, Collection<String>>();
        Document gateDocument = null;
        try {
            gateDocument = Factory.newDocument(inputText);
            corpusController.getCorpus().add(gateDocument);
            corpusController.execute();
            AnnotationSet vpAnnotations = gateDocument.getAnnotations().get("VG");
            AnnotationSet npAnnotations = gateDocument.getAnnotations().get("NP");
            AnnotationSet bnpAnnotations = gateDocument.getAnnotations().get("BNP");
            AnnotationSet dbnpAnnotations = gateDocument.getAnnotations().get("dBNP");
            // populate response data
            Collection<String> vpData = getAnnotations(vpAnnotations, gateDocument);
            outputMap.put(Constants.VP_ANNOTATIONS, vpData);

            Collection<String> npData = getAnnotations(npAnnotations, gateDocument);
            outputMap.put(Constants.NP_ANNOTATIONS, npData);

            Collection<String> bnpData = getAnnotations(bnpAnnotations, gateDocument);
            outputMap.put(Constants.BNP_ANNOTATIONS, bnpData);

            Collection<String> dbnpData = getAnnotations(dbnpAnnotations, gateDocument);
            outputMap.put(Constants.DBNP_ANNOTATIONS, dbnpData);
        } catch (Throwable ex) {
            ex.printStackTrace();
            outputMap = null;
        } finally {
            if (corpusController.getCorpus() != null) {
                corpusController.getCorpus().clear();
            }
            if (gateDocument != null) {
                Factory.deleteResource(gateDocument);
            }
        }
        return outputMap;
    }

    private Collection<String> getAnnotations(AnnotationSet annotations, Document gateDocument) {
        Collection<String> collection = new ArrayList<String>();
        for (Annotation annotation : annotations) {
            String text = gate.Utils.stringFor(gateDocument, annotation);
            collection.add(text.trim().toLowerCase());
        }
        return collection;
    }

    /**
     * Method cleanup texts from punctuation
     *
     * @param entityText
     * @return
     */
    private String cleanupExtractedText(String entityText) {
        if (StringUtils.containsAny(entityText, punctuations)) {
            for (int i = 0; i < punctuations.length; i++) {
                if (StringUtils.contains(entityText, punctuations[i])) {
                    //String replacement = new StringBuilder().append(SPACE).append(punctuations[i]).append(SPACE).toString();
                    entityText = entityText.replace(String.valueOf(punctuations[i]), SPACE);
                }
            }
        }
        entityText = entityText.replace("  ", " ").trim();
        return entityText;
    }

    /**
     * @param outputFolderPath
     */
    private void createFoldersStructure(String outputFolderPath) {
        // create initial set
        String cleanedCorpusPath = outputFolderPath + "/cleanedCorpus";
        String corpusFrequencyPath = outputFolderPath + "/cleanedCorpus/frequency";
        // create cleaned corpus
        new File(cleanedCorpusPath).mkdirs();
        new File(corpusFrequencyPath).mkdirs();
    }


    /**
     * Main class for tests
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args != null && args.length == 3) {
            String pathToApplication = args[0];
            String pathToMessages = args[1];
            String pathToOutputFolder = args[2];
//            Utilities.setApplicationPath("D:\\projects\\phrases\\sources\\sms-processing\\build\\application");
            Utilities.setApplicationPath(pathToApplication);
            GateResources.initGateFrameWork(Utilities.getApplicationPath());
            KeyphraseExtractor keyphraseExtractor = new KeyphraseExtractor();
            //keyphraseExtractor.runKeyphrasesExtraction("D:\\projects\\phrases\\sources\\sms-processing\\tests", "D:\\projects\\phrases\\sources\\sms-processed");
            keyphraseExtractor.runKeyphrasesExtraction(pathToMessages, pathToOutputFolder);
        } else {
            System.out.println("Please specify command line parameters: PATH_TO_APPLICATION PATH_TO_FILE_WITH_MESSAGES PATH_TO_OUTPUT_FOLDER    ");
        }
    }

}
