package dev.simpleframework.token.permission;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenPermission {

    private List<String> permissions;
    private List<String> roles;
    private boolean foundPermissions = false;
    private boolean foundRoles = false;
    @Getter
    private String lastMatchArg = "";

    public List<String> getPermissions() {
        this.setPermissions();
        return this.permissions;
    }

    public List<String> getRoles() {
        this.setRoles();
        return this.roles;
    }

    public boolean hasPermission(String... permissions) {
        if (permissions == null) {
            return false;
        }
        this.setPermissions();
        for (String permission : permissions) {
            this.lastMatchArg = permission;
            if (!match(this.permissions, permission)) {
                return false;
            }
        }
        return true;
    }

    public boolean anyPermission(String... permissions) {
        if (permissions == null) {
            return false;
        }
        this.setPermissions();
        for (String permission : permissions) {
            this.lastMatchArg = permission;
            if (match(this.permissions, permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(String... roles) {
        if (roles == null) {
            return false;
        }
        this.setRoles();
        for (String role : roles) {
            this.lastMatchArg = role;
            if (!match(this.roles, role)) {
                return false;
            }
        }
        return true;
    }

    public boolean anyRole(String... roles) {
        if (roles == null) {
            return false;
        }
        this.setRoles();
        for (String role : roles) {
            this.lastMatchArg = role;
            if (match(this.roles, role)) {
                return true;
            }
        }
        return false;
    }

    private void setPermissions() {
        if (this.foundPermissions) {
            return;
        }
        this.permissions = PermissionManager.findPermissions();
        this.foundPermissions = true;
    }

    private void setRoles() {
        if (this.foundRoles) {
            return;
        }
        this.roles = PermissionManager.findRoles();
        this.foundRoles = true;
    }

    private static boolean match(List<String> list, String element) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        if (list.contains(element)) {
            return true;
        }
        // 模糊匹配，即 * 表示全部
        for (String pattern : list) {
            if (like(pattern, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * copy from simple-core - Strings.like()
     */
    private static boolean like(String pattern, String str) {
        String likeStr = "*";
        // 都为 null ：相等
        if (pattern == null && str == null) {
            return true;
        }
        // 其中之一为 null ：不匹配
        if (pattern == null || str == null) {
            return false;
        }
        // 其中之一为空字符串：不匹配
        if (pattern.isEmpty() && !str.isEmpty() || !pattern.isEmpty() && str.isEmpty()) {
            return false;
        }
        // 将表达式按模糊字符分割为多个关键字
        List<String> keywords = new ArrayList<>();
        String prefixStr = "";
        String pattChar;
        for (int i = 0, len = pattern.length(); i < len; i++) {
            pattChar = String.valueOf(pattern.charAt(i));
            if (likeStr.equals(pattChar)) {
                if (prefixStr.isEmpty()) {
                    prefixStr = pattChar;
                } else if (!likeStr.equals(prefixStr)) {
                    keywords.add(prefixStr);
                    prefixStr = pattChar;
                }
            } else {
                if (likeStr.equals(prefixStr)) {
                    keywords.add(prefixStr);
                    prefixStr = "";
                }
                prefixStr = prefixStr + pattChar;
            }
        }
        if (!prefixStr.isEmpty()) {
            keywords.add(prefixStr);
        }

        int keywordSize = keywords.size();
        // 只有一个关键字时：全模糊或者全匹配
        if (keywordSize == 1) {
            String keyword = keywords.get(0);
            return likeStr.equals(keyword) || keyword.equals(str);
        }
        String matchStr = str;
        String keyword;
        for (int i = 0; i < keywordSize; i++) {
            keyword = keywords.get(i);
            // 第一个关键字
            if (i == 0) {
                // 关键字是模糊字符：继续匹配
                if (likeStr.equals(keyword)) {
                    continue;
                }
                // 关键字不是模糊字符：不是以关键字起始则匹配失败，否则截取关键字后继续匹配
                if (matchStr.startsWith(keyword)) {
                    matchStr = matchStr.substring(keyword.length());
                } else {
                    return false;
                }
            } else {
                // 是否最后一个关键字
                boolean last = i == keywordSize - 1;
                // 关键字是模糊字符
                if (likeStr.equals(keyword)) {
                    // 已是最后一个关键字：直接匹配成功
                    if (last) {
                        return true;
                    }
                    // 不是最后一个关键字：继续下一匹配
                    continue;
                }

                // 关键字不是模糊字符，前关键字必是模糊字符
                // 不包含关键字：匹配失败
                int indexOfMatch = matchStr.indexOf(keyword);
                if (indexOfMatch < 0) {
                    return false;
                }
                // 包含关键字：截取关键字后继续匹配
                matchStr = matchStr.substring(indexOfMatch + keyword.length());
                while (matchStr.startsWith(keyword)) {
                    // 有回文则一直截取到最后一个回文
                    matchStr = matchStr.substring(keyword.length());
                }

                // 已是最后一个关键字：判断匹配字符串是否截取至空
                if (last) {
                    return matchStr.isEmpty();
                }
            }
        }
        return false;
    }

}
