# swiggityspeare
[![forthebadge](http://forthebadge.com/images/badges/powered-by-case-western-reserve.svg)](http://forthebadge.com)

Character-based recurrent neural networks from colloquial English with a touch of Shakespeare, wrapped into an IRC chatbot

It's bring-your-own neural network for privacy reasons.  I'll try to include a semi-decent sample.
There are several dependencies.  My code's dependencies are all taken care of with the .jar files in the
`dependencies` directory.

The neural network is expected to be [Andrej Karpathy's char-rnn](https://github.com/karpathy/char-rnn),
cloned directly in the `dependencies` directory, so that the structure is `swiggityspeare/dependencies/char-rnn`.
This program has several dependencies of its own, and I would highly recommend using the GPU accelerated CUDA code.
To get that working you need `Torch` `luarocks` `cunn` `cutorch`, and the nvidia CUDA runtime

At this point usage is pretty simple.  The project is based on IntelliJ, so just `build`->`build artifacts`->`swiggityspeare.jar`
execution is an equally simple `java -jar swiggityspeare.jar`.  However, the jar expects the `dependencies` directory to be
alongside it, so that it knows where the `char-rnn` code is, and can execute it.
