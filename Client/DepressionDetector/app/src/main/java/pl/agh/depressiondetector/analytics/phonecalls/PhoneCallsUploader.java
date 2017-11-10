package pl.agh.depressiondetector.analytics.phonecalls;

import android.content.Context;

import java.io.File;

import pl.agh.depressiondetector.scheduler.Uploader;

import static pl.agh.depressiondetector.connection.API.PATH_SOUND_FILES;
import static pl.agh.depressiondetector.connection.HttpClient.AMR_TYPE;
import static pl.agh.depressiondetector.utils.FileUtils.getPhoneCallsDirectory;
import static pl.agh.depressiondetector.utils.NetworkUtils.postFile;


public class PhoneCallsUploader implements Uploader {

    @Override
    public boolean upload(Context appContext) {
        File directory = getPhoneCallsDirectory();
        boolean success = false;
        if (directory.exists()) {
            for (File file : directory.listFiles())
                if (postAMRFile(file, appContext))
                    success = file.delete();
                else
                    success = false;

        }
        return success;
    }

    private boolean postAMRFile(File file, Context context) {
        return postFile(file, context, PATH_SOUND_FILES, AMR_TYPE);
    }
}
