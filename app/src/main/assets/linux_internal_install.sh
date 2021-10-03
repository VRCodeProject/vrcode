package2() {
  banner
  echo -e "${R} [${W}-${R}]${C} Checking required packages...""${W}"
  sudo apt-get update -y
  sudo apt install udisks2 -y
  sudo rm /var/lib/dpkg/info/udisks2.postinst
  echo "" >/var/lib/dpkg/info/udisks2.postinst
  sudo dpkg --configure -a
  sudo apt-mark hold udisks2
  sudo apt-get install sudo wget curl nano git keyboard-configuration tzdata xfce4 xfce4-goodies xfce4-terminal firefox menu inetutils-tools dialog exo-utils dbus-x11 fonts-beng fonts-beng-extra vlc gtk2-engines-murrine gtk2-engines-pixbuf -y --no-install-recommends
  sudo apt-get update -y
  sudo apt-get upgrade -y
  sudo apt-get clean
}

refs() {
  sudo apt-get update -y
  sudo apt-get upgrade -y
  sudo apt autoremove -y
  banner
  echo -e "${R} [${W}-${R}]${C} Installing Visual Studio Code...""${W}"

  sudo sh -c 'echo "deb [arch=amd64,arm64,armhf trusted=yes] https://packages.microsoft.com/repos/code stable main" > /etc/apt/sources.list.d/vscode.list'


  sudo apt install apt-transport-https -y
  sudo apt update -y
  sudo apt install code -y

  sudo apt autoremove -y

}

sound() {
  echo -e "\n${R} [${W}-${R}]${C} Fixing Sound Problem...""${W}"
  if [[ ! -e "$HOME/.bashrc" ]]; then
    touch "$HOME"/.bashrc
  fi

  echo "pulseaudio --start --exit-idle-time=-1" >>"$HOME"/.bashrc
  echo "pacmd load-module module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1" >>"$HOME"/.bashrc
}

sound
package2
refs
