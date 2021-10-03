proot-distro login --shared-tmp ubuntu -- env "DISPLAY=:1;XDG_RUNTIME_DIR=/data/data/com.termux/files/usr/tmp" Xwayland :1 >/dev/null &
proot-distro login --shared-tmp ubuntu -- env "DISPLAY=:1;XDG_RUNTIME_DIR=/data/data/com.termux/files/usr/tmp" xfce4-session
