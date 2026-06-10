package com.badminton.constant;

public final class AppConstants {

    public static final String API_V1 = "/api/v1";
    public static final String AUTH_PATH = API_V1 + "/auth/**";
    public static final String ADMIN_PATH = API_V1 + "/admin/**";
    public static final String MANAGER_PATH = API_V1 + "/manager/**";
    public static final String CUSTOMER_PATH = API_V1 + "/customer/**";

    public static final int BCRYPT_STRENGTH = 12;
    public static final String BEARER_PREFIX = "Bearer ";

    private AppConstants() {
    }
}
