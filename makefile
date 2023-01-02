default: clean build wrapper # cleans then builds java program and c wrappers

clean: # clean files using gradle wrapper
	./gradlew clean -q
	rm -f server
	rm -f client

build: # build into an uberjar that includes deps
	./gradlew shadowJar

wrapper: # build wrapper files into binaries
	gcc server_wrapper.c -o server
	gcc client_wrapper.c -o client

tape: # build the tape file using vhs
	vhs < socks.tape

deps: # list all dependencies that my project uses
	./gradlew dependencies

props: # list properties of the project
	./gradlew properties
