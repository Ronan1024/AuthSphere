package com.authsphere.server.account.security;

/**
 * 密码哈希结果。
 *
 * @param hash 哈希值
 * @param salt Base64 编码盐值
 * @param algorithm 算法标识
 */
public record PasswordHash(String hash, String salt, String algorithm) {
}
