# TODO: implement this with https://github.com/termux/proot-distro#running-additional-installation-steps
package2() {
  echo -e "${R} [${W}-${R}]${C} Checking required packages...""${W}"
  apt-get update -y
  apt install udisks2 -y
  rm /var/lib/dpkg/info/udisks2.postinst
  echo "" >/var/lib/dpkg/info/udisks2.postinst
  dpkg --configure -a
  apt-mark hold udisks2
  apt-get --yes -o Dpkg::Options::="--force-confold" --allow-downgrades --allow-remove-essential --allow-change-held-packages install sudo wget curl nano git keyboard-configuration tzdata xfce4 xfce4-goodies xfce4-terminal firefox menu inetutils-tools dialog exo-utils dbus-x11 fonts-beng fonts-beng-extra vlc gtk2-engines-murrine gtk2-engines-pixbuf -y --no-install-recommends
  apt-get update -y
  apt-get --yes -o Dpkg::Options::="--force-confold" --allow-downgrades --allow-remove-essential --allow-change-held-packages upgrade -y
  apt-get clean
}

# shellcheck disable=SC2120
installCode() {
  apt-get update -y
  apt-get upgrade -y
  apt-get install ca-certificates -y
  apt autoremove -y
  wget "https://code.visualstudio.com/sha/download?build=stable&os=linux-deb-arm64" -O code.deb
  apt install "./code.deb" -y

  sed -i 's/BIG-REQUESTS/_IG-REQUESTS/' /usr/lib/aarch64-linux-gnu/libxcb.so.1 # tricky
  mv /usr/share/code/code /usr/share/code/code.real
  cat>/usr/share/code/code<<EOF
#!/usr/bin/env sh
/usr/share/code/code.real --no-sandbox "$@"
EOF
  #Another tricky
  chmod +x /usr/share/code/code
  apt autoremove -y

}

addUser() {
  echo "user ALL=(ALL:ALL) ALL" >> /etc/sudoers
  adduser --disabled-password --gecos "" user
}
package2
installCode
addUser
