package dev.simpleframework.token.user;

/**
 * 用户信息查询器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface UserQuery {

    /**
     * 获取用户信息
     *
     * @param loginId 登录 id
     * @return 用户信息
     */
    UserInfo getInfoById(String loginId);

    /**
     * 根据账号名获取账号信息
     *
     * @param accountType 账号类型
     * @param accountName 账号名
     * @return 账号信息
     */
    UserAccount getAccountByName(String accountType, String accountName);

    UserQuery DEFAULT = new UserQuery() {
        @Override
        public UserInfo getInfoById(String loginId) {
            return null;
        }

        @Override
        public UserAccount getAccountByName(String accountType, String accountName) {
            return null;
        }
    };

}
