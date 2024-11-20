package org.xuzhaorui.utils;

import java.io.File;

public class DirectoryExplorer {

    public static void main(String[] args) {
        String rootPath = "D:\\GitHubAndGitee\\spring-boot-socket-server-starter\\src\\main\\java\\org\\xuzhaorui";
        listDirectoriesAndFiles(new File(rootPath));
    }

    /**
     * 递归列出目录和文件
     *
     * @param rootDir 根目录文件对象
     */
    public static void listDirectoriesAndFiles(File rootDir) {
        if (rootDir == null || !rootDir.exists()) {
            System.out.println("路径不存在或为空：" + (rootDir != null ? rootDir.getPath() : "null"));
            return;
        }
        // 如果是目录
        String absolutePath = rootDir.getAbsolutePath();
        absolutePath = absolutePath.substring(79,absolutePath.length() );
        if (rootDir.isDirectory()) {
            System.out.println("目录为：" + absolutePath);

            // 列出当前目录下的所有文件和子目录
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    listDirectoriesAndFiles(file); // 递归处理
                }
            }
        } else {
            // 如果是文件
            System.out.println("文件为：" + absolutePath);
        }
    }
}
