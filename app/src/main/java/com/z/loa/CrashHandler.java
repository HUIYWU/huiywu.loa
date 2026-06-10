package com.z.loa;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler instance;
    private Context context;
    private Thread.UncaughtExceptionHandler defaultHandler;
    
    private CrashHandler() {}
    
    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }
    
    public void init(Context context) {
        this.context = context.getApplicationContext();
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        saveCrashLog(ex);
        // 显示转储目录提示
        showToast();
        restartApp();
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        }
    }
    
    private void saveCrashLog(Throwable ex) {
        try {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            StringBuilder sb = new StringBuilder();
            sb.append("=== 崩溃时间: ").append(time).append(" ===\n");
            sb.append("=== 设备信息 ===\n");
            sb.append("品牌: ").append(android.os.Build.BRAND).append("\n");
            sb.append("型号: ").append(android.os.Build.MODEL).append("\n");
            sb.append("Android版本: ").append(android.os.Build.VERSION.RELEASE).append("\n");
            sb.append("SDK版本: ").append(android.os.Build.VERSION.SDK_INT).append("\n");
            sb.append("应用版本: ").append(getAppVersion()).append("\n\n");
            sb.append("=== 异常堆栈 ===\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            sb.append(sw.toString());
            pw.close();
            sw.close();
                    
            saveToFile(sb.toString());
            saveToMemory(sb.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveToFile(String log) {
        FileOutputStream fos = null;
        try {
            // Android 10+ 使用应用专属目录
            File logDir;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                logDir = new File(context.getExternalFilesDir(null), "crash_logs");
            } else {
                logDir = new File(Environment.getExternalStorageDirectory(), 
                        "Android/data/" + context.getPackageName() + "/crash_logs");
            }
            
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // 文件名包含时间戳
            String fileName = "crash_" + System.currentTimeMillis() + ".log";
            File logFile = new File(logDir, fileName);
            
            fos = new FileOutputStream(logFile);
            fos.write(log.getBytes("UTF-8"));
            fos.flush();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void saveToMemory(String log) {
        // 保存到 SharedPreferences 或内存缓存
        android.content.SharedPreferences sp = context.getSharedPreferences("crash_info", 
                Context.MODE_PRIVATE);
        sp.edit()
          .putString("last_crash", log)
          .putLong("last_crash_time", System.currentTimeMillis())
          .apply();
    }
    
    private String getAppVersion() {
        try {
            android.content.pm.PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            // API 28+ 使用 longVersionCode
                return pi.getLongVersionCode() + "";
            } else {
                // 旧版本使用 versionCode，并转换为 long 类型
                @SuppressWarnings("deprecation")
                long versionCode = pi.versionCode;
                return pi.versionName + "(" + versionCode + ")";
            }        
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }
    
    private void showToast() {
        new Thread(() -> {
            Looper.prepare();
            Toast.makeText(context, "出现异常，异常日志已转储至Android/data/com.z.loa/files/crash_log", Toast.LENGTH_LONG).show();
            Looper.loop();
        }).start();
    }
    
    private void restartApp() {
        // 延迟2秒后重启
        new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
            android.content.Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(context.getPackageName());
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP |
                               android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }, 2000);
    }
    
    public String getLastCrashLog() {
        android.content.SharedPreferences sp = context.getSharedPreferences("crash_info", 
                Context.MODE_PRIVATE);
        return sp.getString("last_crash", "");
    }
    
    public List<File> getAllCrashLogs() {
        List<File> logs = new ArrayList<>();
        try {
            File logDir;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                logDir = new File(context.getExternalFilesDir(null), "crash_logs");
            } else {
                logDir = new File(Environment.getExternalStorageDirectory(), 
                        "Android/data/" + context.getPackageName() + "/files/crash_logs");
            }
            
            if (logDir.exists() && logDir.isDirectory()) {
                File[] files = logDir.listFiles((dir, name) -> name.endsWith(".log"));
                if (files != null) {
                    logs.addAll(Arrays.asList(files));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }
}
