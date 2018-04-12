
The idea of this app is to run popular Linux-like shell tools (i.e. binary files like busybox, curl, etc) on an unrooted phone. Basically, the same principle is used in Busybox install (no root) from here: https://forum.xda-developers.com/showthread.php?t=2195692

This app copies all binary files from /assets dir inside apk into /data/user/0/com.andycar.binarytest/files

After that, it makes them executable.

So, if you issue a command export PATH=$PATH:/data/user/0/com.andycar.binarytest/files; export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/data/user/0/com.andycar.binarytest/files , you will be able to run 'busybox' or 'curl' in terminal emulator without any prefix. (valid throigh the single terminal session)
