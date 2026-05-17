#本脚本由　by Han | 情非得已c，编写
#应用于玩机百宝箱上


Initialization() {
cat <<Han >"$xml"
<?xml version="1.0" encoding="utf-8"?>
<group title="搜索">
    <action reload="true" interruptible="false">
        <title>重新搜索</title>
        <params>
            <param name="Content" title="输入你要搜索的功能标题，不区分大小写" required="true" />
        </params>
        <set>. ./Search_Content.sh $1</set>
    </action>
</group>

<group>

     <action>
        <title>清除搞机搜索结果</title>
        <set>rm -f $Pages/Search_Results.xml $Pages/ADB/Search_Results.xml
        am start -S $Package_name/com.root.system.SplashActivity; echo 将在下次打开玩机百宝箱时生效</set>
    </action>
</group>

Han
}


xml="$1/Search_Results.xml"
echo "- 正在搜索$Content，需要一段时间"
Initialization "$1"
while read File; do
    [[ -z "$File" ]] || [[ "$File" = $xml ]] && continue
    unset START END d
    START=($(sed -n '/<!-- START -->/=' "$File"))
    GJC=($(sed -n -e '/<get>/d' -e '/<set>/d' -re "s/>.*$Content.*<\//<!-- Content -->/i" -e '/<!-- Content -->/=' "$File"))
    END=($(sed -n '/<!-- END -->/=' "$File"))
    num=0
        if [[ ${#START[@]} != ${#END[@]} ]]; then
            error "- $File文件索引异常，请私信我修复"
            continue
            # echo "查看数组START，数组个数${#START[@]}"
            # echo ${START[@]}
            # echo "查看数组END，数组个数${#END[@]}"
            # echo ${END[@]}
        fi
            echo "- 搜索$File中……"
            for c in ${GJC[@]}; do
                # echo "关键词所在行$c"
                [[ -z "${END[$num]}" ]] && break
                while [[ "${END[$num]}" -lt "$c" ]]; do
               # echo ""${END[$num]}" -lt "$c""
                    num=$((num+1))
                done
                    [[ $d = $num ]] && continue
                    line="${START[$num]},${END[$num]}"
                    sed -n "${line}p" "$File" 1>>"$xml"
                    d=$num
            done
done <<Han
$(grep -E -il ">.*$Content.*</" "$1"/*)
Han
wait
echo '</group>' >>"$xml"
result=$(grep -F -c '<!-- START -->' "$xml")
echo "- 已搜索到$result个结果"
sed -i "11a <group title=\"搜索&#34;$Content&#34;结果($result)\">" "$xml"
