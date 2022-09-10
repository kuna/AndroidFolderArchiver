Folder Archiver for Android
===========================

# What is this?

오늘도 누워서 인터넷을 돌아다니는 당신,

재미있는 짤방이 보이면 어김없이 저장하기 바쁩니다.

앗, 그런데 마구마구 저장하다 보니 용량도 부족해졌고, 핸드폰에 원치 않는 사진이 잔뜩 뜹니다.

지우기는 아까워서 적당히 묶어서 저장하고 싶은데, 손으로 일일이 하려니 의욕이 확 떨어지는데, 좋은 방법이 없을까요?

그럴 때 필요한 어플리케이션이 여기 있습니다!

# ScreenShots

![TaskDefScreenShot](screenshots/1.jpeg?raw=true)

![ArchivingScreenShot](screenshots/2.jpeg?raw=true)

# Features

* Archive files in a folder to a single or multiple zip file
  * Hidden files / nested folders are not backed up.
* Various modes for creating archive
  * by monthly
  * by daily
  * by auto_monthly(monthly unless the count of files are over 500)
  * all
* Feature for adding `.nomedia` in a source folder.
* AD-free

# Cautions

* This application requires overwhelming storage permission, so it will require access when first run. Would not execute properly if denied.
* 이 어플리케이션은 저장소 전체 권한 획득을 필요로 하며, 첫 실행때 이를 요구하는 다이어로그가 뜹니다.
* This application targets for SDK version over 30, Android 11.

# TODOs and Limitation

* **Currently Only Absolute Path is supported**. Uri path, e.g. `content://`, would not be recognized.
  * 현재 이 어플리케이션은 절대 경로밖에 인식하지 않습니다. `content://` 로 시작하는 경로는 현재 작동하지 않습니다.
  * You may need some application to get to know this. e.g. `termux`
  * `termux`와 같은 어플리케이션으로 절대 경로를 알아내야 해당 어플리케이션 사용이 가능합니다.
  * Going to be fixed soon.
* Add unit/feature test. No unit test as this project is made in a hurry (and simple).
* Add detailed help dialogs in application
* Bug: folder select dialog won't show up when modifying.

# License

MIT License

Icon from [flaticon](https://www.flaticon.com/kr/free-icon/archive_875461?related_id=711129&origin=search)
