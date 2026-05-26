abort() {
    echo "$@" 1>&2
    exec exit 1
}


Script_Dir=$TMPDIR/Script_Dir
jian=`cat ~/downloader/path/* `
jian2=$Script_Dir/META-INF/com/google/android/update-binary

[[ ! -f "$jian" ]] && abort "！未下载文件"
echo "文件路径：$jian"

if [[ $1 = -apk ]]; then
    echo "开始安装玩机百宝箱-CMD版.apk，如果未能重启玩机百宝箱请手动重启"
    pm uninstall $Package_name
    cp -f "$jian" "$TMP/$Package_name.apk"
    pm install -r "$TMP/$Package_name.apk"
    rm -f "$TMP/$Package_name.apk"
    $Have_ROOT && am start -S $Package_name/com.projectkr.shell.SplashActivity
    exit 0
fi


echo "- 开始安装curl"
rm -rf $Script_Dir
mkdir -p $Script_Dir
unzip -oq "$jian" 'META-INF/com/google/android/update-binary' -d $Script_Dir

if [[ -f "$jian2" ]]; then
    sh "$jian2" $1 1 "$jian"
        if $Have_ROOT; then
            echo "已安装完成开始重启玩机百宝箱"
            am start -S $Package_name/com.projectkr.shell.SplashActivity
        fi
else
    abort "解压文件失败"
fi