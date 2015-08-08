# swiggityspeare
[![forthebadge](http://forthebadge.com/images/badges/powered-by-case-western-reserve.svg)](http://forthebadge.com)

Character-based recurrent neural networks from colloquial English with a touch of Shakespeare, wrapped into an IRC chatbot

### dependencies

It's bring-your-own neural network for privacy reasons.  There's a sample network in dependencies/irc_network.t7: it's a quick shakespeare-trained network.
There are several dependencies.  My code's dependencies are all taken care of at compile time with the .jar files in the
`dependencies` directory.

The neural network is expected to be [Andrej Karpathy's char-rnn](https://github.com/karpathy/char-rnn),
cloned directly in the `dependencies` directory, so that the structure is `swiggityspeare/dependencies/char-rnn`.
This program has several dependencies of its own, and I would highly recommend using the GPU accelerated CUDA code.
To get that working you need `Torch` `luarocks` `cunn` `cutorch` `nngraph` `optim`, and the nvidia CUDA runtime.
Test the environment with a quick `th train.lua`, whose default settings should begin training from a shakespeare
 dataset included in the char-rnn repository.  

At this point usage is pretty simple.  The project is based on IntelliJ, so just `build`->`build artifacts`->`swiggityspeare.jar`, or use the prebuilt `swiggityspeare.jar` provided.

### usage

Execution is a simple `java -jar swiggityspeare.jar`.  However, the jar expects the `dependencies` directory to be
alongside it (or the `-d` switch to be set), so that it knows where the `char-rnn` code is, and can execute it. 
The prepackaged .t7 neural network needs to be moved into the cloned `char-rnn` directory.

when in doubt, `java -jar swiggityspeare.jar --IHaveNoIdeaWhatImDoing` (or any invalid switch, like --help) will print the usage text.  For the lazy:
usage: 

    swiggityspeare
    -c <arg>   channels to join, including quotes, in the format "#chan1 #chan2" [#cwru]
    -d <arg>   relative path of the char-rnn directory ["dependencies/char-rnn"]
    -n <arg>   nick for the bot to take [swiggityspeare]
    -p <arg>   IRC server port number (SSL is assumed) [6697]
    -s <arg>   IRC server hostname [irc.case.edu]
    -t <arg>   filename of the .t7 holding the neural network within
               char-rnn's root directory [irc_network.t7]

### tl;dr

- `git clone https://github.com/raidancampbell/swiggityspeare.git`
- `cd swiggityspeare/dependencies`
- `git clone https://github.com/karpathy/char-rnn.git`
- `mv irc_network.t7 char-rnn`
- [install `Torch`](http://torch.ch/)
- `luarocks install nngraph`
- `luarocks install optim`
- `luarocks install cutorch`
- `luarocks install cunn`
