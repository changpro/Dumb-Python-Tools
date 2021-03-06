package JDown;


/* Resuming from break point, to implement this function, I have looked up several blogs all of which record the thread's job range
 * in a specific log file. With other forms of record such as the progress, specifically in this program, "hasDown", which is treated as a collective
 * attribute shared by threads. Due to concurrent problematic accessing, such a sharing attr must be atomic by java.util.concurrent.AtomicLong. And
 * another attribute "isWrong" is also stored as atomic for same reason.
 * */

import java.io.*;
import java.util.Properties;

class JDownTracer {

    private Properties properties;
    private int ThreadNum;
    private final String logName;

    JDownTracer(String logFileName, String url, int threadNum) {
        this.ThreadNum = threadNum;
        this.logName = logFileName + ".properties";
        this.properties = new Properties();
        properties.put("url", url);
        properties.put("hasDown", "0");
        for (int i = 0; i < threadNum; i++) {
            properties.put("thread_" + i, "0-0");
        }
    }

    static void load(JDownTracer tracer) {
        tracer.properties = new Properties();
        try {
            FileInputStream fs = new FileInputStream(tracer.logName);
            tracer.properties.load(fs);
            // close stream in time in case holding the file source too long and thus affect other calls' request.
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    synchronized void update(int threadID, long length, long pos, long limit) {
        properties.put("thread_"+threadID, pos + "-" + limit);
        properties.put("hasDown", String.valueOf(length + Long.parseLong(properties.getProperty("hasDown"))));

        try {
            FileOutputStream file = new FileOutputStream(logName);
            properties.store(file, null);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 2d-array storing pos and limit for all threads.
     */
    long[][] getRange() {
        long[][] range = new long[getThreadNum()][2];
        int threadNum = getThreadNum();
        for(int i = 0; i < threadNum; i++){
            String[] val = properties.getProperty(String.format("thread_%d", i)).split("-");
            range[i][0] = Long.parseLong(val[0]);
            range[i][1] = Long.parseLong(val[1]);
        }
        return range;
    }
    long getHasDown() {
        return Long.parseLong(properties.getProperty("hasDown"));
    }
    int getThreadNum(){
        return ThreadNum;
    }
}