@echo off
mkdir empty_dir
robocopy empty_dir target\dist /MIR /R:0 /W:0 > nul
rmdir empty_dir
rmdir /s /q target\dist
echo Cleanup done.
