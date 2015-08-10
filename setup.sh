#!/usr/bin/env bash

function no_torch {
  echo 'You need to have torch installed.'
  echo 'Would you like us to install it for you?[yes/no]'
  read install
  if [ "yes" = "$install" ]; then
    install_torch
  else
    echo 'Torch must be installed to continue. Please install torch and try again.'
    echo 'http://torch.ch/docs/getting-started.html#installing-torch'
    exit
  fi
}

function install_torch {
  curl -s https://raw.githubusercontent.com/torch/ezinstall/master/install-deps | bash
  git clone https://github.com/torch/distro.git ~/torch --recursive
  wd=$(pwd)
  cd ~/torch; ./install.sh
  cd $wd
  echo 'Installed torch to ~/torch'
  source ~/.bashrc
}

if [ -z $(which th) ]; then
  no_torch
fi

git submodule init
git submodule update

cp dependencies/irc_network.t7 dependencies/char-rnn/

LUA_PACKAGES=nngraph optim cutorch cunn

for package in LUA_PACKAGES; do
  luarocks install $package
done
