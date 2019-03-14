adb -d backup -noapk com.greenwald.aaron.ridetracker
( printf "\x1f\x8b\x08\x00\x00\x00\x00\x00" ; tail -c +25 backup.ab ) |  tar xfvz -
mv apps/com.greenwald.aaron.ridetracker/db ./db
rm -rf apps
rm backup.ab
