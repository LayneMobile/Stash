### Roadmap ###
* v0.1.0
    * Testing!!!!!!!!
    * Rework/finish Gson stash collection/db implementation
        * Replace Jake Wharton DiskLruCache with custom implementation
    * Establish a way of cancelling aggregate requests
    * Complete all TODO's in code
    * Code cleanup
        * Use @Nullable and @NonNull
        * Remove anything that doesn't serve a real purpose
    * Javadoc
    * More work on samples
    * Add new out-of-the-box stash extensions as it makese since (like Gson, i.e. Ormlite, etc.)

* v0.2.0
    * Try to implement Retrofit/OkHttp http cache in a stash implementation
    * Params/Params.Builder auto-generation
    * Api/Params integration with builders
        * i.e.
```java
Request<User> request = Api.Login.buildRequest()
        .setUsername("homie")
        .setPassword("mysecrets")
        .build();
// And
Stash<User> stash = Api.Login.buildStash()
        .setUsername("homie")
        .build();
```
