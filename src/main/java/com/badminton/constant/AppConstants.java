package com.badminton.constant;

public final class AppConstants {

    public static final String API_V1 = "/api/v1";
    public static final String ADMIN_PATH = API_V1 + "/admin/**";
    public static final String MANAGER_PATH = API_V1 + "/manager/**";
    public static final String CUSTOMER_PATH = API_V1 + "/customer/**";
    public static final String FILES_PATH = API_V1 + "/files/**";

    public static final String[] PUBLIC_AUTH_ENDPOINTS = {
            API_V1 + "/auth/register",
            API_V1 + "/auth/login",
            API_V1 + "/auth/refresh",
            API_V1 + "/auth/forgot-password",
            API_V1 + "/auth/reset-password"
    };

    public static final int BCRYPT_STRENGTH = 12;
    public static final String BEARER_PREFIX = "Bearer ";

    private AppConstants() {
    }
}
