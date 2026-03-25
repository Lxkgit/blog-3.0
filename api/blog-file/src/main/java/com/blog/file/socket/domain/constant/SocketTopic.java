package com.blog.file.socket.domain.constant;

/**
 * @Description 本机设备socket topic
 * @Author lxk
 * @CreateTime 2025-04-09
 */

public class SocketTopic {

    public static final String SOCKET_REGISTER = "register";
    public static final String SOCKET_HEART = "heart";
    public static final String SOCKET_SYSTEM = "system";

    /**
     * 文件移动
     */
    public static final String SOCKET_MOVE_FILE = "move_file";

    /**
     * 导出博客数据
     */
    public static final String SOCKET_EXPORT_BLOG_FILE = "export_blog_file";

    /**
     * 删除指定目录或文件
     */
    public static final String SOCKET_DELETE_FILE_OR_DIR = "delete_file_or_dir";

    /**
     * 系统消息
     */
    public static final String SYSTEM_INFO = "system_info";

}
