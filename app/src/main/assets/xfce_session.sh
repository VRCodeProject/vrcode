export XDG_RUNTIME_DIR=${TMPDIR}
Xwayland :1 >/dev/null &
env DISPLAY=:1 xfce4-session