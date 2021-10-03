export XDG_RUNTIME_DIR=${TMPDIR}
Xwayland :1 >/dev/null &
ls "$XDG_RUNTIME_DIR"

proot-distro login --shared-tmp ubuntu -- env "DISPLAY=:1;XDG_RUNTIME_DIR=/tmp" xfce4-session
proot-distro login --shared-tmp ubuntu -- env "DISPLAY=:1;XDG_RUNTIME_DIR=/tmp" ls /tmp
