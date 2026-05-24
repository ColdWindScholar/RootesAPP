#本脚本由　by Han | 情非得已c，编写
#应用于玩机百宝箱上


case $Option in
    01)
        $binariesPath/libfastboot.so --disable-verity flash vbmeta "$img_File"
        echo "- 已禁用AVB2.0的DM校验"
    ;;
    02)
        $binariesPath/libfastboot.so --disable-verification flash vbmeta "$img_File"
        echo "- 已禁用AVB2.0的启动校验"
    ;;
    03)
        $binariesPath/libfastboot.so --disable-verity --disable-verification flash vbmeta "$img_File"
        echo "- 已禁用AVB2.0的启动校验/DM校验"
    ;;
esac
