package pl.agh.depressiondetector.analytics.text;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import pl.agh.depressiondetector.utils.DateUtils;
import pl.agh.depressiondetector.utils.FileUtils;

public class TextFileWriter {
    private String text;
    private Date date;

    TextFileWriter(String text, Date date) {
        this.text = text;
        this.date = date;
    }

    public void saveText() throws IOException, JSONException {
        File outputDir = FileUtils.getTextDirectory();
        FileUtils.createDirectory(outputDir);

        String fileName = FileUtils.getTextFileName() + ".txt";
        File outputFile = new File(outputDir, fileName);
        boolean fileExists = outputFile.exists();

        FileOutputStream fileOutputStream = new FileOutputStream(outputFile, true);
        if (fileExists)
            fileOutputStream.write(",".getBytes());
        fileOutputStream.write(formatText().getBytes());
        fileOutputStream.close();
    }

    private String formatText() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("message", text);
        json.put("datetime", DateUtils.convertToServerDateTimeFormat(date));

        return json.toString();
    }
}
