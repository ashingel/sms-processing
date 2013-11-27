package com.langbricks.corpus.utilities;


import java.io.*;
import java.util.*;

/**
 * User: Andrei
 * Date: 30.4.11
 * Time: 20.57
 */
public class Utilities {

    private static String applicationPath = null;

    public static void setApplicationPath(String applicationPath) {
        Utilities.applicationPath = applicationPath;
    }

    public static String getApplicationPath() {
        return applicationPath;
    }

    public static synchronized Collection readFile(String path, String encoding) {
        List strings = new ArrayList();
        File file = new File(path);
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            Reader reader = new InputStreamReader(is, encoding);
            BufferedReader in = new BufferedReader(reader);
            String str;
            while ((str = in.readLine()) != null) {
                strings.add(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    /**
     * Method will write lines of texts to the path
     *
     * @param text
     * @param filePath
     */
    public static synchronized void write(Collection text, String filePath) {
        try {
            FileWriter fstream = new FileWriter(filePath);
            BufferedWriter out = new BufferedWriter(fstream);
            Iterator iterator = text.iterator();
            String message;
            while (iterator.hasNext()) {
                message = iterator.next().toString();
                out.write(message);
                out.newLine();
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            // Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Method read file to text
     *
     * @param requestFile
     * @return
     */
    public static synchronized String readTxtFile(String inputFilePath,String encoding) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            BufferedReader in = new BufferedReader(new FileReader(new File(inputFilePath)));
            String str;
            while ((str = in.readLine()) != null) {
                stringBuffer.append(str);
                stringBuffer.append("\n");
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString().trim();
    }

    public static synchronized Set readFileToSet(String path, String encoding) {
        Set strings = new TreeSet();
        File file = new File(path);
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            Reader reader = new InputStreamReader(is, encoding);
            BufferedReader in = new BufferedReader(reader);
            String str;
            while ((str = in.readLine()) != null) {
                strings.add(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    public static synchronized Collection readFileToListInOrder(String path, String encoding) {
        Collection  strings = new LinkedList();
        File file = new File(path);
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            Reader reader = new InputStreamReader(is, encoding);
            BufferedReader in = new BufferedReader(reader);
            String str;
            while ((str = in.readLine()) != null) {
                strings.add(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

}
