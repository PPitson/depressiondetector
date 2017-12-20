package pl.agh.depressiondetector.scheduler;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.text.format.DateUtils;


public class UploadScheduler extends JobService implements UploadSchedulerThread.StateListener {

    private static final int UPLOAD_JOB_ID = 42;

    private UploadSchedulerThread uploadThread;
    private JobParameters params;

    @Override
    public boolean onStartJob(JobParameters params) {
        this.params = params;
        uploadThread = new UploadSchedulerThread(this, this);
        uploadThread.start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        uploadThread.cancel();
        return true;
    }

    @Override
    public void onFinished() {
        jobFinished(params, false);
    }

    public static boolean schedule(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) {
            JobInfo.Builder builder = new JobInfo.Builder(UPLOAD_JOB_ID, new ComponentName(context, UploadScheduler.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(DateUtils.HOUR_IN_MILLIS * 12);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                builder.setRequiresBatteryNotLow(true);

            return scheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS;
        }

        return false;
    }
}
