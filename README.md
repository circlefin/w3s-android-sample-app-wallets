# Circle Programmable Wallet SDK for Android - Sample

> Sample app for integrating Circle Programmable Wallet SDK.

- Bookmark
  - [Requirement](#prerequisite)
  - [Run the Sample App](#run-the-sample-app)
---


## Requirement

1. Java 17 is required for the sample app.

## Run the Sample App
1. Edit `values/config.xml` ➜ `pw_app_id` to fill in your `APP ID`
e36b455b-2fb2-541a-81b0-21d29150818e
2. Add `local.properties` with maven repository settings:
```properties
pwsdk.maven.url=https://maven.pkg.github.com/circlefin/w3s-android-sdk
pwsdk.maven.username=<GITHUB_USERNAME> rossadi.ardian@gmail.com
# Fine-grained personal access tokens or classic with package read permission.
pwsdk.maven.password=<GITHUB_PAT> Rossadi96@
``` 
