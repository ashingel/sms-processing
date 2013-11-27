package com.langbricks.corpus.constants;

/**
 * Class with project constants
 * User: Andrei
 * Date: 14.5.11
 * Time: 18.35
 */
public interface Constants {
    /**
     * Standard text file like review
     */
    Integer INPUT_PLAIN_TEXT_FORMAT_FILE = 1;
    /**
     * Standard text with text per line format.For example review per line and etc
     */
    Integer INPUT_PLAIN_TEXT_PER_LINE_FORMAT_FILE = 2;

    String NP_ANNOTATIONS = "np";
    String VP_ANNOTATIONS = "vp";
    String BNP_ANNOTATIONS = "bnp";
    String DBNP_ANNOTATIONS = "bnp";
}
