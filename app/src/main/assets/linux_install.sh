# This script has been written with reference to modded-ubuntu
# https://github.com/modded-ubuntu/modded-ubuntu/
#
# Copyright 2021 modded-ubuntu. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

exit() {
  echo "Fake exit in debug"
}

R="$(printf '\033[1;31m')"
G="$(printf '\033[1;32m')"
Y="$(printf '\033[1;33m')"
# shellcheck disable=SC2034
B="$(printf '\033[1;34m')"
C="$(printf '\033[1;36m')"
W="$(printf '\033[1;37m')"

banner() {
  #  clear
  printf "\033[32mA modded gui version of ubuntu for Termux\033[0m\n"
}

package() {
  apt --yes update
  apt --yes -o Dpkg::Options::="--force-confold" --allow-downgrades --allow-remove-essential --allow-change-held-packages upgrade
  apt install git wget -y

  echo -e "${R} [${W}-${R}]${C} Checking required packages...""${W}"
  termux-setup-storage
  # shellcheck disable=SC2006
  if [[ $(command -v pulseaudio) && $(command -v proot-distro) && $(command -v wget) ]]; then
    echo -e "\n${R} [${W}-${R}]${G} Packages already installed.""${W}"
  else
    packs=(pulseaudio proot-distro wget)
    for hulu in "${packs[@]}"; do
      type -p "$hulu" &>/dev/null || {
        echo -e "\n${R} [${W}-${R}]${G} Installing package : ${Y}$hulu${C}""${W}"
        apt update -y
        apt upgrade -y
        apt install "$hulu" -y
      }
    done
  fi
}

distro() {
  echo -e "\n${R} [${W}-${R}]${C} Checking for Distro...""${W}"
  termux-reload-settings

  if [[ -d "$PREFIX/var/lib/proot-distro/installed-rootfs/ubuntu" ]]; then
    echo -e "\n${R} [${W}-${R}]${G} Distro already installed.""${W}"
    exit 0
  else
    proot-distro install ubuntu
    termux-reload-settings
  fi

  if [[ -d "$PREFIX/var/lib/proot-distro/installed-rootfs/ubuntu" ]]; then
    echo -e "\n${R} [${W}-${R}]${G} Installed Successfully !!""${W}"
  else
    echo -e "\n${R} [${W}-${R}]${G} Error Installing Distro !\n""${W}"
    exit 0
  fi
}

permission() {
  banner
  echo -e "${R} [${W}-${R}]${C} Setting up Environment...""${W}"

  echo "proot-distro login ubuntu" >"$PREFIX"/bin/ubuntu

  if [[ -e "$PREFIX/bin/ubuntu" ]]; then
    chmod +x "$PREFIX"/bin/ubuntu
    termux-reload-settings
    echo -e "\n"
  else
    echo -e "\n${R} [${W}-${R}]${G} Error Installing Distro !""${W}"
    exit 0
  fi
}

login() {
  banner
  echo -e "${W}"
  echo "proot-distro login --shared-tmp --fix-low-ports ubuntu" >"$PREFIX"/bin/ubuntu
  chmod +x "$PREFIX"/bin/ubuntu
}
fixSound() {
  echo -e "\n${R} [${W}-${R}]${C} Fixing Sound Problem...""${W}"
  if [[ ! -e "$HOME/.bashrc" ]]; then
    touch "$HOME"/.bashrc
  fi

  if grep -Fxq "pulseaudio --start --exit-idle-time=-1" "$HOME"/.bashrc; then
    return
  fi

  echo "pulseaudio --start --exit-idle-time=-1" >>"$HOME"/.bashrc
  echo "pacmd load-module module-native-protocol-tcp auth-ip-acl=127.0.0.1 auth-anonymous=1" >>"$HOME"/.bashrc
}

banner
package
distro
permission
login
fixSound
uname -a
proot-distro login --shared-tmp ubuntu -- bash /tmp/SCRIPT_PLACEHOLDER
# This works as a trick, SCRIPT_PLACEHOLEDER will be replaced at runtime.
# You can find reference to this at Constants.kt
