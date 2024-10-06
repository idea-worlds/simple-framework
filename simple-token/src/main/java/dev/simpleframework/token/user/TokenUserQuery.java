package dev.simpleframework.token.user;

/**
 * 用户信息查询器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface TokenUserQuery {

    /**
     * 获取用户信息
     *
     * @param loginId 登录 id
     * @return 用户信息
     */
    TokenUserInfo getInfoById(String loginId);

    /**
     * 根据账号名获取账号信息
     *
     * @param accountType 账号类型
     * @param accountName 账号名
     * @return 账号信息
     */
    TokenUserAccount getAccountByName(String accountType, String accountName);

    TokenUserQuery DEFAULT = new TokenUserQuery() {
        @Override
        public TokenUserInfo getInfoById(String loginId) {
            return null;
        }

        @Override
        public TokenUserAccount getAccountByName(String accountType, String accountName) {
            return null;
        }
    };

}
