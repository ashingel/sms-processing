The goal of this project is to extract the next entities:
a) Noun Phrases + frequencies
b) Hearst Based Patterns data + frequencies
c) Verb phrases + frequencies 

How to build this project:
1) Install Apache Ant if necessary and define paths to ant bin folder
2) Install Java JDK 1.6+
2) Run ant build-jar-file. In case of success a new built application will be placed to SOURCE/build folder

How to run application:
1) Edit run-jar.bat from from SOURCE/ folder and define 
  a) Path to the SOURCE/build/application folder on your hard drive
  b) Path to file with messages in plain text format (1 message per line)
  c) Path to output folder where some stat will be saved
2) Copy edited run-jar.bat file to SOURCE/build/application
3) Run run-jar.bar file. Numbner of records currently processed should be printed in command line

Results explanations:
Previously defined output folder(OUTPUT_FOLDER) should contain:
OUTPUT_FOLDER/cleanedCorpus/frequency - simple frequency of non-processed messages(format word tab_symbol frequency).
OUTPUT_FOLDER/cleanedCorpus/input_file_name - not really cleaned corpus. A stub for future corpus pre-processing with stopwords and dictionaries
OUTPUT_FOLDER/cleanedCorpus/verb-frequencies.txt - verbs & verb phrases detected from corpus + frequencies.
OUTPUT_FOLDER/cleanedCorpus/nouns-phrases-frequencies.txt - noun phrases detected from corpus + frequencies.
OUTPUT_FOLDER/cleanedCorpus/corpus-frequencies.txt - Hearst Patterns based output + frequencies.
OUTPUT_FOLDER/cleanedCorpus/3-grams-frequencies.txt- 3-grams generated from corpus + frequencies.

From the first corpus touch I think a very deep corpus texts "recovery" should be made to get nice results. Texts are really noisy.
