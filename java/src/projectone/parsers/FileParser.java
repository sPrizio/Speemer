package projectone.parsers;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses an sgm file to obtain key information
 *
 * @author Stephen Prizio
 */
public class FileParser {
    private Logger logger = Logger.getLogger(FileParser.class);
    private File file;
    private StringBuilder text;
    private ArrayList<Integer> documentIds;

    /**
     * Regular constructor that creates a parser for a specific file
     * @param fileName - name of file to be parsed
     */
    public FileParser(String fileName) {
        BasicConfigurator.configure();  //  configures log4j logger

        this.file = new File(fileName);
        this.text = new StringBuilder();
        this.documentIds = new ArrayList<>();

        readFile();     //  reads file on construction to get file's contents
    }

    /**
     * Returns the contents of the file
     *
     * @return contents of the file as a string
     */
    public String getText() {
        return this.text.toString();
    }

    /**
     * Returns a list of all document ids in this file
     *
     * @return list of document ids
     */
    public List<Integer> getDocumentIds() {
        return this.documentIds;
    }


    //  HELPERS

    /**
     * Reads file and stores its contents
     */
    private void readFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            // read file contents
            while ((line = br.readLine()) != null) {
                this.text.append(line);
            }

            //  gets document ids
            findDocumentIds(this.text.toString());
        } catch (IOException e) {
            logger.info("File not found.");
        }
    }

    /**
     * Gets each id of the Reuter's articles
     *
     * @param text file's contents
     */
    private void findDocumentIds(String text) {
        Pattern pattern = Pattern.compile("NEWID=\"(\\d*)\"");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            this.documentIds.add(Integer.parseInt(matcher.group(1)));
        }
    }

    //private void
}