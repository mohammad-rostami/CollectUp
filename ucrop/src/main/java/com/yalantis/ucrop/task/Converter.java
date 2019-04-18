package com.yalantis.ucrop.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Converter extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "FFMpeg";

    private String cmd[] = null;
    private Process p = null;

    private Context context;
    private String path, output;

    public Converter(String path, String output, Context context) {
        this.path = path;
        this.output = output;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Context m_context = context;
        //First get the absolute path to the file
        File folder = m_context.getFilesDir();

        String fullpath = "";
        String filefolder = null;

        try {
            filefolder = folder.getCanonicalPath();
            if (!filefolder.endsWith("/"))
                filefolder += "/";

            fullpath = filefolder + "ffmpeg";

            String cmd[] = new String[]{fullpath, "-i", path, "-vcodec", "libx264", "-b:v", "1000k",
                    "-profile:v", "BaseLine", "-level", "3.0", "-acodec", "copy", "-f", "mp4",
                    "-preset", "ultrafast", "-tune", "film", "-r", "30", "-threads", "20", "-strict", "experimental",
                    "-vsync", "2", output};

            p = Runtime.getRuntime().exec(cmd);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Void... params) {


        String line;

        StringBuilder log = new StringBuilder();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(p.getErrorStream()) );
        try {
            p.waitFor();

            while ((line = in.readLine()) != null) {
                log.append(line).append("\n");
                Log.d(TAG, "onActivityResult: " + log.toString());
            }

            in.close();

            Log.d(TAG, "onActivityResult: " + log.toString());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

}
