curl -L https://gitee.com/rootes/scene/releases/download/1/LSPosed-v1.9.2-7249-Irena-release.zip >/sdcard/s.txt
        echo 下载完成
       apd modules install /sdcard/s.txt
       
       magisk --install-module /sdcard/s.txt
       
       ksu install-module /sdcard/s.txt
       echo 安装完成
       rm -rf /sdcard/s.txt