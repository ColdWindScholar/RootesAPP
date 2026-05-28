#本脚本由　by Han | 情非得已c，编写
#应用于玩机百宝箱上


echo "- 出现OKAY=成功，FAILED=失败"
case $Scheme in
    1) $binariesPath/libfastboot.so flash recovery "$REC_File" ;;
    2) $binariesPath/libfastboot.so flash recovery_ramdisk "$REC_File" ;;
    3) $binariesPath/libfastboot.so boot "$REC_File"; ChongQi2=0 ;;
esac

[[ $ChongQi2 -eq 1 ]] && { $binariesPath/libfastboot.so flash misc $PeiZhi_File/misc.bin ; $binariesPath/libfastboot.so reboot; }
