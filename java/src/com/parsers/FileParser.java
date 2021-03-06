package com.parsers;


import org.apache.log4j.Logger;
import com.entity.Document;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * I certify that this submission is my original work and meets the Faculty’s Expectations of Originality - 16 October 2017
 *
 * Parses an sgm file to obtain key information
 *
 * @author Stephen Prizio - 40001739
 */
public class FileParser {
    private Logger logger = Logger.getLogger(FileParser.class);
    private File file;
    private StringBuilder text;
    private ArrayList<Document> documents;

    /**
     * Regular constructor that creates a parser for a specific file
     *
     * @param fileName - name of file to be parsed
     */
    public FileParser(String fileName) {
        PropertyConfigurator.configure("java/resources/lib/log4j.properties");

        this.file = new File(fileName);
        this.text = new StringBuilder();
        this.documents = new ArrayList<>();

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
     * Returns a list of documents in this file
     *
     * @return list of documents
     */
    public List<Document> getDocuments() {
        return this.documents;
    }


    //  HELPERS

    /**
     * Reads file and stores its contents
     */
    private void readFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {
            String line;

            // reads file contents
            while ((line = br.readLine()) != null) {
                this.text.append(line);
                this.text.append(' ');
            }

            findDocuments(this.text.toString());
        } catch (IOException e) {
            logger.info("File not found.");
        }
    }

    /**
     * Reads raw document and extracts id
     *
     * @param text - raw document
     * @return document id
     */
    private int matchID(String text) {
        Pattern pattern = Pattern.compile("NEWID=\"(\\d*)\"");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 0;
    }

    /**
     * Reads raw document and extracts title
     *
     * @param text - raw document
     * @return document title
     */
    private String matchTitle(String text) {
        StringBuilder titleBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile("<TITLE>([\\s\\S]*?)</TITLE>");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            titleBuilder.append(matcher.group(1));
        } else {
            return "";
        }

        String titleClean = titleBuilder.toString();

        //  removes all non word characters
        titleClean = titleClean.replaceAll("[^\\w*\\s]", "");
        titleClean = titleClean.replaceAll("lt\\w*", "");

        return titleClean;
    }

    /**
     * Reads raw document and extracts body
     *
     * @param text - raw document
     * @return document body
     */
    private String matchBody(String text) {
        StringBuilder bodyBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile("<BODY>([\\s\\S]*?)</BODY>");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            bodyBuilder.append(matcher.group(1));
        } else {
            return "";
        }

        String bodyClean = bodyBuilder.toString();

        //  removes all non word characters
        bodyClean = bodyClean.replaceAll("\\w*&lt;\\w*\\s*\\w* | &lt;\\w*\\s\\w*\\s*\\w*> | Reuter\\S*; | REUTER\\S* | Reuter\\s*\\S*;", " ");
        bodyClean = bodyClean.replaceAll("\\w*>", " ");
        bodyClean = bodyClean.replaceAll("&lt;", " ");

        return bodyClean;
    }

    /**
     * Reads raw document and extracts text content, typically used when there aren't any <BODY></BODY> and <TEXT></TEXT> tags
     *
     * @param text - raw document
     * @return document text
     */
    private String matchText(String text) {
        StringBuilder textBuilder = new StringBuilder();
        Pattern txt = Pattern.compile("<TEXT[\\s\\S]*?>([\\s\\S]*?)<\\/TEXT>");
        Matcher txtMatcher = txt.matcher(text);

        if (txtMatcher.find()) {
            textBuilder.append(txtMatcher.group(1));
        } else {
            return "";
        }

        String textClean = textBuilder.toString();

        //  removes all non word characters
        textClean = textClean.replaceAll("[^\\w*\\s]", " ");
        textClean = textClean.replaceAll("lt\\w*", " ");
        textClean = textClean.trim().replaceAll(" +", " ");
        textClean = textClean.replaceAll("Reuter\\s\\d*|REUTER\\s\\d*", "");

        return textClean;
    }

    /**
     * Parses the file for documents demarcated by <REUTERS></REUTERS> tags
     * @param text - raw file text
     */
    private void findDocuments(String text) {
        Pattern reuters = Pattern.compile("<REUTERS([\\s\\S]*?)<\\/REUTERS>");
        Matcher reutersMatcher = reuters.matcher(text);

        while (reutersMatcher.find()) {
            String body = matchBody(reutersMatcher.group(1));
            String title = matchTitle(reutersMatcher.group(1));

            //  finds articles without bodies and titles
            if (("").equalsIgnoreCase(body) && ("").equalsIgnoreCase(title)) {
                body = matchText(reutersMatcher.group(1));
            }

            this.documents.add(new Document(matchID(reutersMatcher.group(1)), title, body));
        }
    }
}
