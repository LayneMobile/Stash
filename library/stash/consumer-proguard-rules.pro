
# Keep classes/members we need for client functionality.
-keep @stash.annotations.Keep class *
-keepclassmembers class * {
    @stash.annotations.Keep *;
}
