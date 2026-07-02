-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class net.burningtnt.terracotta.TerracottaAndroidAPI {
    native <methods>;
    private static int onVpnServiceStateChanged(...);
}

-keep class net.burningtnt.terracotta.TerracottaAndroidAPI$Metadata {
    *;
}
-keep interface net.burningtnt.terracotta.TerracottaAndroidAPI$VpnServiceCallback {
    *;
}
-keep interface net.burningtnt.terracotta.TerracottaAndroidAPI$VpnServiceRequest {
    *;
}
