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

installCode() {
  apt-get update -y
  apt-get upgrade -y
  apt-get install ca-certificates -y
  apt autoremove -y
  echo -e "${R} [${W}-${R}]${C} Installing Visual Studio Code...""${W}"

  sh -c 'echo "deb [arch=amd64,arm64,armhf trusted=yes] https://packages.microsoft.com/repos/code stable main" > /etc/apt/sources.list.d/vscode.list'

  apt install apt-transport-https -y
  apt update -y
  apt install code -y

  sed -i 's/BIG-REQUESTS/_IG-REQUESTS/' /usr/lib/aarch64-linux-gnu/libxcb.so.1 # trickky
  sed -i 's/"$@"/--no-sandbox "$@"' # trickky FIXME: later fix this

  apt autoremove -y

}

addUser() {
  echo "user ALL=(ALL:ALL) ALL" >> /etc/sudoers
  adduser --disabled-password --gecos "" user
}
# TODO: Support sound transfer
#sound() {
#  echo -e "\n${R} [${W}-${R}]${C} Fixing Sound Problem...""${W}"
#  if [[ ! -e "$HOME/.bashrc" ]]; then
#    touch "$HOME"/.bashrc
#  fi
#
#  echo "pulseaudio --start --exit-idle-time=-1" >>"$HOME"/.bashrc
#  echo "pacmd load-module module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1" >>"$HOME"/.bashrc
#}
package2
installCode
addUser
