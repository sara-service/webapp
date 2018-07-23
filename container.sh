#!/bin/bash -e
GITDIR=$(pwd)

if [ -d "../eclipseconfig" ]; then
	# use persistent workspace in ../ if explicitly created there
	WORKSPACE=$(cd .. && pwd)
else
	WORKSPACE=/tmp/sara
fi
case "$1" in 
	-*|"") ;;
	*) WORKSPACE="$1"; shift ;; # always allow workspace to be overridden
esac

containerize() {
	mkdir -p $WORKSPACE
	udocker run --user=$(whoami) --hostauth --hostenv -w "$WORKSPACE" -v "$WORKSPACE" -v "$GITDIR" sara "$@"
	echo -e "\e[1;34mCONTAINER STOPPED\e[0m"
}

eclipse() {
	echo -e '\e[32m'
	echo "===================================================="
	echo -en '\e[1m'
	echo "IF THIS IS YOUR INITAL RUN, PLEASE DO THE FOLLOWING:"
	echo -e '\e[0;32m'
	echo " 1) Import the SARA code as 'Existing Maven Project'"
	echo " 2) Open a terminal (Ctrl-Shift-Alt-T) and run:"
	echo "      $GITDIR/saradb/initdb.sh";
	echo " 3) Open 'bwfdm.sara.Application' and right-click 'Run' -> 'Java Application'"
	echo " 4) Wait for Spring to start, connect to 'http://localhost:8080'"
	echo " 5) Congrats ... you're done!"
	echo -e '\e[0m'
	containerize /opt/eclipse/eclipse -configuration "$WORKSPACE/eclipseconfig" -data "$WORKSPACE" "$@"
}	

case "$1" in
	-*|"") eclipse "$@" ;; # no command, maybe optional args
	*) containerize "$@" ;; # explicit command, execute that instead
esac
