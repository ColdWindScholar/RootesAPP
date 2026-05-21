#!/system/bin/sh

dir=${0%/*}

# 限制电流值 例如: 3000000 (μA)
limit=$1
up_only="$2"

if [[ "$limit" = "" ]]
then
  limit=3000000
fi

fcc_limit() {
  fcc_override=/sys/class/power_supply/battery/constant_charge_current
  if [[ -f "$fcc_override" ]]; then
    if [[ "$up_only" != "1" || $(cat $fcc_override) -lt "$limit" ]]; then
      chmod 777 $fcc_override
      echo "$limit" > $fcc_override
      chmod 444 $fcc_override
    fi
    cat ${fcc_override}
    return
  fi
}
paths=$(ls /sys/class/power_supply/*/constant_charge_current_max | grep -v 'mtk-')
if [[ "$paths" == "" ]]; then
  fcc_limit
fi

# 更改限制 change_limit
change_limit() {
  # echo "更改限制值为：${1}μA"

  for path in $paths
  do
    if [[ "$up_only" != "1" || $(cat "$path") -lt "$limit" ]]; then
      chmod 0664 "$path"
      echo "$limit" > "$path"
    fi
    cat "$path"
  done
}

if [[ $(getprop vtools.fastcharge) = "" ]]; then
  "$dir"/fast_charge_run_once.sh >/dev/null 2>&1
  setprop vtools.fastcharge 1
fi

change_limit
# echo `date "+%Y-%m-%d %H:%M:%S.%MS"` " -> $limit_value" >> /cache/scene_charge.log
