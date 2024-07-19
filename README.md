# Circle Programmable Wallet SDK for Android - Sample

> Sample app for integrating Circle Programmable Wallet SDK.

- Bookmark
  - [Requirement](#requirement)
  - [Run the Sample App](#run-the-sample-app)
---


## Requirement

1. Java 17 is required for the sample app.

## Run the Sample App
You can install [the latest APK](https://github.com/circlefin/w3s-android-sample-app-wallets/blob/master/app/build/outputs/apk/debug/app-debug.apk) or follow the instructions below to run on a device / emulator directly.
1. Open the project by [Android Studio](https://developer.android.com/studio): File ➜ Open ➜ choose the project root folder.

<img src="readme_images/open_project.png" alt="drawing" width="400"/> 

2. Edit `values/config.xml` ➜ `pw_app_id` to fill in your `APP ID`
3. Add/Edit `local.properties` in the project's root with the following maven repository settings:
```properties
pwsdk.maven.url=https://maven.pkg.github.com/circlefin/w3s-android-sdk
pwsdk.maven.username=<GITHUB_USERNAME>
# Fine-grained personal access tokens or classic with package read permission.
pwsdk.maven.password=<GITHUB_PAT>  
```
> **Note**
> When pasting the values above for `<GITHUB_USERNAME>` and `<GITHUB_PAT>`, make sure to not surround the values with quotes.

- Check the following links for creating PAT.
  - [Creating a personal access token (classic)](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)
  - [Creating a fine-grained personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-fine-grained-personal-access-token)

 4. If there's no error after Gradle sync, select a device and click `Debug 'app'`.

<img src="readme_images/run_project.png" alt="drawing" width="600"/> 
 
5. There are three tabs corresponding to different login methods. Fill in the `App ID` and fill in the relevant fields in each tab according to the requirements of different login methods for following execution action.


<img src="readme_images/running_app_social.png" alt="drawing" width="200"/><img src="readme_images/running_app_email.png" alt="drawing" width="200"/><img src="readme_images/running_app_pin.png" alt="drawing" width="200"/>

6. (Optional) Auth configs setup. If you want to use social login for test , please follow below steps to and Social login infos.
  - [Google and Facebook] Add/Edit value with a specific key-name in `strings.xml` (please refer to the sample strings.xml below)
   
  ```properties
  <string name="google_web_client_id" translatable="false">YOUR_GOOGLE_WEB_CLIENT_ID</string>
  
  <string name="facebook_app_id">YOUR_FACEBOOK_APP_ID</string>
  <string name="fb_login_protocol_scheme">your_fb_protocol_scheme</string>
  <string name="facebook_client_token">YOUR_FACEBOOK_CLIENT_TOKEN</string>

  ```
  - [Apple] Add your Apple `service-id` as manifestPlaceholders to app’s `build.gradle`
  
  ```properties
  android {
   
  defaultConfig {
       …
       …
       manifestPlaceholders = [appAuthRedirectScheme: 'YOUR_APPLE_SERVICE_ID']
   }
   }


  ```
