#本脚本由　by Han | 情非得已c，编写
#应用于玩机百宝箱上


$binariesPath/libadb.so root
echo 已禁止给对方设备供电
$binariesPath/libadb.so shell su -c "
Charging_control=$Charging_control
Charging_control2=$Charging_control2
echo 1 >$Charging_control
echo 0 >$Charging_control2"
