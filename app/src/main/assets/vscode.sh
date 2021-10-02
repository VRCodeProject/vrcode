# TODO: remove hardcoded path

apt --yes --force-yes install wget tar
wget "https://code.visualstudio.com/sha/download?build=stable&os=linux-arm64" -O vscode.tar.gz

if [ ! -d "${PREFIX}"/tools ]; then
  mkdir "${PREFIX}"/tools
fi

tar -C "${PREFIX}"/tools -xzvf vscode.tar.gz
rm -rf vscode.tar.gz

mkdir "${PREFIX}"/tools/VSCode-linux-arm64/data

ln -f -s "${PREFIX}"/tools/VSCode-linux-arm64/code "${PREFIX}"/bin/code
