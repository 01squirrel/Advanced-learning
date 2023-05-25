package com.example.learnningproject.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ApkUtils {
    /**
     * 安装一个apk文件
     **/
    public static void installApk(Context context, File uriFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(uriFile), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 卸载一个app
     **/
    public static void uninstallApp(Context context, String packageName) {
        //通过程序的包名创建uri
        Uri packageUri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
        context.startActivity(intent);
    }

    /** 重启APP**/
    public static void restartApp(Context context) {
        PackageManager manager = context.getPackageManager();
        if(manager == null) {
            return;
        }
        Intent intent = manager.getLaunchIntentForPackage(context.getPackageName());
        if(intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @return boolean true存在 false不存在
     **/
    public static boolean isSoftInstalled(Context context, String packageName) {
        //获取包管理器packageManager
        PackageManager manager = context.getPackageManager();
        //获取所有已安装程序的包信息,限Android 11以下
        List<PackageInfo> packageInfos = manager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<>();
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String name = packageInfos.get(i).packageName;
                packageNames.add(name);
            }
        }
        return packageNames.contains(packageName);
    }

    /**
     * 检测是否存在对应的文件
     **/
    public static boolean isSoftInstalled(Context context, File file) {
        return isSoftInstalled(context, file.getAbsolutePath());
    }

    /**
     * 根据文件路径获取包名
     **/
    public static String getPackageNameByPath(Context context, String filePath) {
        PackageManager manager = context.getPackageManager();
        PackageInfo packageInfo = manager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo info = packageInfo.applicationInfo;
            return info.packageName;
        }
        return null;
    }

    /**
     * 从apk中获取版本信息
     **/
    public static String getChannelFromApk(Context context, String channelPrefix) {
        //从apk包中获取
        ApplicationInfo info = context.getApplicationInfo();
        String sourceDir = info.sourceDir;
        //默认放在meta-inf/里， 所以需要再拼接一下
        String key = "META-INF/" + channelPrefix;
        String ret = "";
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                String name = zipEntry.getName();
                if (name.startsWith(key)) {
                    ret = name;
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String[] splits = ret.split(channelPrefix);
        String channel = "";
        if (splits.length >= 2) {
            channel = ret.substring(key.length());
        }
        return channel;
    }

    //执行命令 pm install -r filepath 安装apk
    public static CommandResult installApk(String filepath) {
        String cmd = "pm install -r " + filepath;
        return executeCmd(new String[]{cmd},null,true,true);
    }

    public static CommandResult executeCmd(String[] commands, String[] envp, boolean isRooted, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, "", "");
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream outputStream = null;
        String LINE_SEPARATOR = System.getProperty("line.separator");
        try {
            process = Runtime.getRuntime().exec(isRooted ? "su" : "sh", envp, null);
            outputStream = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) continue;
                outputStream.write(command.getBytes());
                outputStream.writeBytes(LINE_SEPARATOR);
                outputStream.flush();
            }
            outputStream.writeBytes("exit " + LINE_SEPARATOR);
            outputStream.flush();
            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                errorResult = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
                String line;
                if ((line = successResult.readLine()) != null) {
                    successMsg.append(line);
                    while ((line = successResult.readLine()) != null) {
                        successMsg.append(LINE_SEPARATOR).append(line);
                    }
                }
                if ((line = errorResult.readLine()) != null) {
                    errorMsg.append(line);
                    while ((line = errorResult.readLine()) != null) {
                        errorMsg.append(LINE_SEPARATOR).append(line);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(successResult != null) {
                    successResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new CommandResult(
                result,
                successMsg == null ? "" : successMsg.toString(),
                errorMsg == null ? "" : errorMsg.toString()
        );
    }

    public static class CommandResult {
        public int result;
        public String successMsg;
        public String errorMsg;

        public CommandResult(final int result, final String successMsg, final String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "result: " + result + "\n" +
                    "successMsg: " + successMsg + "\n" +
                    "errorMsg: " + errorMsg;
        }
    }
}
