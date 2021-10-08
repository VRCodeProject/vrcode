env "XDG_RUNTIME_DIR=/data/data/com.termux/files/usr/tmp" Xwayland :0 >/dev/null &
sleep 2s # Wait for wayland client service
proot-distro login --user user --shared-tmp ubuntu -- env "DISPLAY=:0" dbus-launch --exit-with-session startxfce4