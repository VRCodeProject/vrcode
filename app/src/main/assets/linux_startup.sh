env "XDG_RUNTIME_DIR=/data/data/com.termux/files/usr/tmp" Xwayland :0 >/dev/null &
#ls -a /data/data/com.termux/files/usr/tmp/.X11-unix/
sleep 2s # Wait for wayland client service
#proot-distro login --shared-tmp ubuntu -- ls -a /data/data/com.termux/files/usr/tmp/.X11-unix/
proot-distro login --user user --shared-tmp ubuntu -- env "DISPLAY=:0" dbus-launch --exit-with-session startxfce4
#proot-distro login --user user --shared-tmp ubuntu -- env "DISPLAY=:0" dbus-launch --exit-with-session code --no-sandbox --verbose