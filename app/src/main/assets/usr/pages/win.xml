<?xml version="1.0" encoding="utf-8"?>
<!-- START -->
<group>
    <text>
        <slices>
            <slice size="18" color="#FFFF0000">目前实验测试，如果不可用请反馈作者！</slice>
        </slices>
    </text>
    </group>
<!-- END -->
<!-- START -->
<group>
   <action confirm="true">
        <title>刷入到Windows并重启手机</title>
        <desc>重启到Windows？注意uefi.img大小写</desc>
           <param name="test">
                <option value="reboot">重启</option>
                <option value="exit">不重启</option>
            </param>
        <set>
        ab_slot=$(getprop ro.boot.slot_suffix)

        img21=$(blktool -n -N boot$ab_slot --print-device)
        img211=$(blktool -n -N boot --print-device)


if [ "$(getprop ro.product.device)" = "duo" ]; then
  echo "不支持"
  exit
else

dd if=$img of=$img21

dd if=$img of=$img211
 fi
        $test
fi</set>
    <param name="img" type="file" suffix="img" editable="true" required="true" title="选择您的Mindows引导镜像（boot）" desc="" />
    </action>
   </group>
   <!-- END -->
<!-- START -->
   <group>
    <action>
        <title>挂载共享空间</title>
        <desc>功能实验，如果不可用请联系开发者</desc>
        <set>mkdir /data/mindows
mount -t ntfs /dev/block/bootdevice/by-name/$cd $fq</set>
            <param name="cd" desc="系统盘" label="" type="enum" required="required">
                <option value="mindowswin">C盘</option>
                <option value="mindowsdat">D盘</option>
            </param>
            
            <param name="fq" desc="挂载位置" label="" type="enum" required="required">
                <option value="/data/mindows">/data/mindows</option>
                <option value="/data/media/$ANDROID_UID/">/sdcard</option>
                <option value="/mnt/">/mnt/</option>
            </param>
    </action>
    </group>
<!-- END -->