package com.langbricks.corpus.processor;


import java.io.File;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.langbricks.corpus.constants.Constants;
import com.langbricks.corpus.utilities.Utilities;
import org.apache.commons.lang.StringUtils;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;

/**
 * User: andrei
 * Date: 3.2.2012
 * Time: 9.41.15
 */
public class CorpusUtilities {

    private final char[] punctuations = new char[]{',', '.', '?', '!', ':', ';', '(', ')', '[', ']', '+', '-', '/', '"', '&', '%', '>', '<', '=', '@', '~', '{', '}', '\\', '?'};

    private Pattern spacesPattern = Pattern.compile("\\s{1,}?");

    Map<String, Integer> wronglySpelledWords = new HashMap<String, Integer>();
    Map negationReplaceMap = null;
    Map<String, String> commonErrorReplaceMap = null;
    Map shortFormReplaceMap = null;
    Map customReplaceMap = null;
    private String inputFolderPath = null;
    private String outputFolderPath = null;
    private AhoCorasick searcher = null;


    public CorpusUtilities(String inputFolderPath, String outputFolderPath) {
        this.inputFolderPath = inputFolderPath;
        this.outputFolderPath = outputFolderPath;
        String resourcesFolder = Utilities.getApplicationPath();
        // here issue with loading TODO: fix it. Properties trimmed spaces in files
        this.negationReplaceMap = loadCustomData(resourcesFolder + "/resources/preprocessor/negation-forms/negation-forms.txt");
        //this.negationReplaceMap.load(new FileInputStream(new File(resourcesFolder + "/resources/preprocessor/negation-forms/negation-forms.txt")));
        this.commonErrorReplaceMap = loadCustomData(resourcesFolder + "/resources/preprocessor/common-error/common-errors.txt");
        this.shortFormReplaceMap = loadCustomData(resourcesFolder + "/resources/preprocessor/short-forms/short-form.txt");
        //this.shortFormReplaceMap.load(new FileInputStream(new File(resourcesFolder + "/resources/preprocessor/short-forms/short-form.txt")));
//        loadCustomReplacements(resourcesFolder);
        // init error correction
        searcher = new AhoCorasick();
        for (String error : commonErrorReplaceMap.keySet()) {
            searcher.add(error, this.commonErrorReplaceMap.get(error));
        }
        searcher.prepare();
    }

    /**
     * Method load custom properties from path
     *
     * @param pathToFile
     * @return
     */
    private Map loadCustomData(String pathToFile) {
        Map resultingMap = new HashMap();
        Collection data = Utilities.readFile(pathToFile, "UTF-8");
        Iterator<String> iterator = data.iterator();
        while (iterator.hasNext()) {
            String[] split = iterator.next().split("=");
            resultingMap.put(split[0], split[1]);
        }
        return resultingMap;
    }


//    private void loadCustomReplacements(String resourcesFolder) {
//        this.customReplaceMap = new Properties();
//        Set replaces = Utilities.readFileToSet(resourcesFolder + "/resources/preprocessor/custom-replaces/custom-replaces.txt", "UTF-8");
//        Iterator<String> iterator = replaces.iterator();
//        while (iterator.hasNext()) {
//            String[] split = iterator.next().split("=");
//            if (split.length == 1) {
//                this.customReplaceMap.put(split[0], " ");
//            } else {
//                this.customReplaceMap.put(split[0], split[1]);
//            }
//        }
//    }

    /**
     * Standard word frequencies calculation
     * Data saved in \nlp-next\src\main\resources\doc\corpus\CorpusFrequencies.xls
     */
    public void calculteFrequences() {
        File inputFolder = new File(this.outputFolderPath + "/cleanedCorpus");
        File[] files = inputFolder.listFiles();
        Map<String, Integer> keywordsMap = new HashMap<String, Integer>();
        for (int i = 0; i < files.length; i++) {
            File inputFile = files[i];
            if (!inputFile.isFile()) {
                continue;
            }
            Set corpusStringList = Utilities.readFileToSet(inputFile.getPath(), "UTF-8");
            Iterator<String> iterator = corpusStringList.iterator();
            while (iterator.hasNext()) {
                String[] text = iterator.next().toLowerCase().trim().split("\t");
                String string = text[0];
                string = preprocessMessage(string);
                // split by spaces\
                String[] split = string.split(" ");
                for (int j = 0; j < split.length; j++) {
                    String word = split[j];
                    if (StringUtils.isNumeric(word)) {
                        continue;
                    }
                    // process every keyword
                    if (keywordsMap.containsKey(word)) {
                        // we need to update frequency value
                        Integer frequency = keywordsMap.get(word);
                        frequency++;
                        keywordsMap.put(word, frequency);
                    } else {
                        // we need to create a new value
                        keywordsMap.put(word, 1);
                    }
                }
            }
        }
        Set treeSet = new TreeSet();
        Iterator iterator = keywordsMap.keySet().iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            treeSet.add(key + "\t" + keywordsMap.get(key));
        }
        Utilities.write(treeSet, this.outputFolderPath + "/cleanedCorpus/frequency/corpus-frequency.txt");
    }

    /**
     * Method to normalize text message with extended form of words
     *
     * @param message
     * @return
     */
    public synchronized String normalizeShortFormsMessage(String message) {

        List<String> errors = new ArrayList<String>();
        Iterator searcher = this.searcher.search(message.toCharArray());
        while (searcher.hasNext()) {
            SearchResult result = (SearchResult) searcher.next();
            errors.add((result.getOutputs().toArray()[0].toString()));
        }

        String[] splitSentence = message.split(" ");
        Set<String> words = new HashSet<String>(Arrays.asList(splitSentence));
        for (int j = 0; j < errors.size(); j++) {
            String word = errors.get(j);
            if (words.contains(word)) {
                for (int k = 0; k < splitSentence.length; k++) {
                    if (splitSentence[k].equalsIgnoreCase(word)) {
                        String replacement = this.commonErrorReplaceMap.get(word);
                        if (replacement != null) {
                            splitSentence[k] = replacement;
                        }
                        break;
                    }
                }
            }
        }
        String result = StringUtils.join(splitSentence, ' ');
        return result;
    }


//        for (Object key : this.shortFormReplaceMap.keySet()) {
//            message = message.replaceAll(key + "\\b", " " + String.valueOf(this.shortFormReplaceMap.get(key)) + " ");
//        }
//        return message;


    /**
     * Method to normalize text message with extended form of words
     *
     * @param message
     * @return
     */

    public synchronized String makeCustomReplacementsMessage(String message) {
        for (Object key : this.customReplaceMap.keySet()) {
            String key1 = (String) key;
            message = message.replaceAll(key1, String.valueOf(this.customReplaceMap.get(key)));
        }
        return message;
    }

    /**
     * Method to normalize text message with extended form of words
     *
     * @param message
     * @return
     */
    public synchronized String normalizeNegationsInMessage(String message) {
        for (Object key : this.negationReplaceMap.keySet()) {
            message = message.replaceAll(key + "\\b", " " + String.valueOf(this.negationReplaceMap.get(key)) + " ");
        }
        return message;
    }

    /**
     * Method to replace long sequences of punctuation characters: !! ==== and etc. may be an issue with sentiments results
     *
     * @param message
     * @return
     */
    public synchronized String normalizePunctuationInMessage(String message) {
        message = message.replaceAll("(\\p{Punct})(\\1+)", "$1 ");
        return message;
    }

    /**
     * Method remove all punctuations from the message
     *
     * @param message
     * @return
     */
    public synchronized String preprocessMessage(String message) {
        if (StringUtils.containsAny(message, punctuations)) {
            for (int i = 0; i < punctuations.length; i++) {
                if (StringUtils.contains(message, punctuations[i])) {
                    //String replacement = new StringBuilder().append(" ").append(punctuations[i]).append(" ").toString();
                    message = message.replace(String.valueOf(punctuations[i]), " ");
                }
            }
        }
        return message;
    }

    /**
     * Method which normalize messages corpus.
     *
     * @param fileFormatType
     */
    public void normalizeCorpusMessages(Integer fileFormatType) {
        File inputFolder = new File(inputFolderPath);
        System.out.println("Input folder path: " + this.inputFolderPath);
        File[] files = inputFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File inputFile = files[i];
            System.out.println("Process " + inputFile.getName());
            if (inputFile.isDirectory()) {
                // we should go deeper then
                scanFolder(inputFile, fileFormatType);
            } else {
                normalizeFile(inputFile, fileFormatType);
            }
            System.gc();
        }
    }

    private void scanFolder(File inputFolder, Integer fileFormatType) {
        System.out.println("Input folder path: " + inputFolder.getPath());
        File[] files = inputFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File inputFile = files[i];
            if (inputFile.isDirectory()) {
                // we should go deeper then
                scanFolder(inputFile, fileFormatType);
            } else {
                normalizeFile(inputFile, fileFormatType);
            }
        }
    }

    /**
     * Method to normalize file from input corpus
     *
     * @param inputFile
     * @param fileFormatType
     */
    private void normalizeFile(File inputFile, Integer fileFormatType) {
        Collection outputSet = new LinkedList();
        if (fileFormatType.equals(Constants.INPUT_PLAIN_TEXT_FORMAT_FILE)) {
            // just a text per file should be processed
            String message = Utilities.readTxtFile(inputFile.getPath(), "UTF-8");
            String newMessage = performNormalization(message);
            outputSet.add(newMessage);
        } else if (fileFormatType.equals(Constants.INPUT_PLAIN_TEXT_PER_LINE_FORMAT_FILE)) {
            Collection<String> corpusStringList = Utilities.readFileToListInOrder(inputFile.getPath(), "UTF-8");
            for (String message : corpusStringList) {
                // TODO: do not forget to uncomment  when it will be finally tested
                //String newMessage = performNormalization(message);
                String newMessage = message;
                outputSet.add(newMessage);
            }
            // save normalized corpus
            //String outputPath = inputFile.getPath().substring(this.inputFolderPath.length(), inputFile.getPath().length() - inputFile.getName().length());
            String outputPath = inputFile.getPath().substring(this.inputFolderPath.length(), inputFile.getPath().length() - inputFile.getName().length()) + "cleanedCorpus";
            new File(outputFolderPath + outputPath).mkdirs();
            Utilities.write(outputSet, outputFolderPath + outputPath + "/" + inputFile.getName());
        }
    }

    /**
     * Method with main normalization process
     *
     * @param message
     * @return
     */
    public String performNormalization(String message) {
        String newMessage = makeCustomReplacementsMessage(message);
        newMessage = normalizeNegationsInMessage(newMessage);
        newMessage = normalizeShortFormsMessage(newMessage);
        newMessage = normalizePunctuationInMessage(newMessage);
        newMessage = replaceSpaces(newMessage);
        // verify texts with spellchecker
//        checkSpellWithWordnet(newMessage);
        return newMessage;
    }

    public void testNormalization(String message) {
        String newMessage = performNormalization(message);
        System.out.println(newMessage);
    }

    /**
     * Method remove extra spaces in texts
     *
     * @param message
     * @return
     */
    private String replaceSpaces(String message) {
        Matcher matcher = spacesPattern.matcher(message);
        if (matcher.find()) {
            //System.out.println(message);
            message = message.replaceAll("\\s{1,}+", " ");
            //System.out.println(message);
        }
        return message;
    }

    private void scanFolder(File inputFile, Set outputSet) {
        File[] files = inputFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            File currentFile = files[i];
            if (currentFile.isDirectory()) {
                // we should go deeper then
                scanFolder(currentFile, outputSet);
            } else {
                Set fileSet = Utilities.readFileToSet(currentFile.getPath(), "UTF-8");
                outputSet.addAll(fileSet);
            }
        }
    }

    public void splitCorpusToFiles(String inputFile, String outputFolder) {
        Collection messages = Utilities.readFile(inputFile, "UTF-8");
        Iterator<String> iterator = messages.iterator();
        Integer fileNameIndex = 0;
        int index = 0;
        Collection stringsCollection = new ArrayList();
        while (iterator.hasNext()) {
            String txt = iterator.next();
            if (!(txt.endsWith(".") || txt.endsWith("?") || txt.endsWith("!"))) {
                txt += ".";
            }
            stringsCollection.add(txt);
            index++;
            if (index >= 100) {
                fileNameIndex++;
                Utilities.write(stringsCollection, outputFolder + "/" + fileNameIndex + ".txt");
                stringsCollection.clear();
                index = 0;
            }
        }

    }

    public static void main(String[] args) {
        Utilities.setApplicationPath("D:\\projects\\phrases\\sources\\sms-processing\\build\\application");
        String inputPath = "D:\\projects\\phrases\\sources\\sms-processing\\tests\\messages1.txt";
        String outputFolderPath = "D:\\projects\\phrases\\sources\\sms-processed";
        CorpusUtilities corpusUtilities = new CorpusUtilities("D:\\projects\\phrases\\sources\\sms-processing\\build\\application", outputFolderPath);;
        //corpusUtilities.normalizeFile(new File(inputPath), Constants.INPUT_PLAIN_TEXT_PER_LINE_FORMAT_FILE);
        corpusUtilities.normalizeShortFormsMessage("I didnt say u to nt to talk");
    }
}
