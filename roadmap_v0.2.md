### Roadmap ###
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
