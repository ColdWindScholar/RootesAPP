if [[ "$1" == "" ]] || [[ "$2" == "" ]];then
  return
fi

mode="$1"

if [[ ! -f "$2" ]]; then
  return
fi

# freeze_apps=""
source $2

for app in $freeze_apps; do
  if [[ "$app" == "com.android.vending" ]]; then
    pm disable com.google.android.gsf
    pm disable com.google.android.gsf.login
    pm disable com.google.android.gms
    pm disable com.android.vending
    pm disable com.google.android.play.games
    pm disable com.google.android.syncadapters.contacts
  elif [[ "$mode" == "suspend" ]]; then
    pm suspend ${app}
    am force-stop ${app}
    am kill current ${app}
  else
    pm disable ${app}
  fi
done
